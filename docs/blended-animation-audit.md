# Bone Animation Mixer Architecture

## Scope

This document is about the pure animation system RareCandy should expose.

The Minecraft/Cobblemon integration layer can decide which poses, actions, quirks,
conditions, and entity values are active. RareCandy should provide the runtime that
evaluates those requested animation contributions into one final bone pose.

The runtime concept is `Bone`. Minecraft's `ModelPart` is one possible backing
implementation used by Cobblemon. RareCandy's current `Skeleton`, `ModelNode`, and
renderer `Bone` types should adapt to the same animation-facing interface.

## Cobblemon Source Shape

The Cobblemon source jar is in:

`reference/cobblemon-1.7.4-sources/`

Key source files read:

- `pose/Bone.java`
- `pose/Pose.kt`
- `pose/ModelPartTransformation.kt`
- `PosableModel.kt`
- `PosableState.kt`
- `LocatorAccess.kt`
- `JsonPose.kt`
- `ModelPartExtensions.kt`
- `animation/ActiveAnimation.kt`
- `animation/PoseAnimation.kt`
- `animation/PrimaryAnimation.kt`
- `animation/PoseTransitionAnimation.kt`
- `bedrock/animation/BedrockAnimation.kt`
- `bedrock/animation/BedrockActiveAnimation.kt`
- `bedrock/animation/BedrockPoseAnimation.kt`
- `bedrock/animation/BedrockAnimationAdapter.kt`

What those files imply:

- Cobblemon already has a `Bone` interface as the model hierarchy boundary.
- `ModelFrame.rootPart` is a `Bone`.
- Coded models commonly set `rootPart` from a named top-level child, such as
  `root.registerChildWithAllChildren("meowth")`.
- JSON posers can specify `rootBone`; otherwise loading falls back to a named model
  root.
- Locators are encoded as empty bones named with `internal_locator__`.
- Locator matrices are updated after all pose and animation mutations are applied.
- Pose state is not a single animation. It includes current pose, primary animation,
  active animations, quirks, pose animations, pose transforms, named animations, and
  locator state.
- Bedrock/Blockbench animation data is keyed by bone name, with position, rotation,
  scale timelines, effects, and timeline instructions.

MoLang matters where it resolves conditions, values, animation references, keyframe
expressions, and effects. It is not the animation system itself.

## Current Cobblemon Evaluation Model

Cobblemon's runtime is mutation based:

1. Reset model bones to default transforms.
2. Run queued state actions.
3. Validate or transition the current pose.
4. Apply global transformed parts.
5. Apply pose-specific transformed parts.
6. Run quirks when no primary animation blocks them.
7. Run primary animation if one exists.
8. Run active animations.
9. Run current pose animations.
10. Update locator matrices.

The important split:

- `PoseAnimation`: shared model-level animations used by poses/idles/procedural
  helpers. They cannot hold per-entity state.
- `ActiveAnimation`: per-state action animations with start time and completion.
- `PrimaryAnimation`: wraps an `ActiveAnimation`, fades base pose intensity down,
  and can exclude pose-animation labels such as `look`.
- `PoseTransitionAnimation`: interpolates between two poses. This is transition
  support, not the full blending system.

For RareCandy, the same shape should be expressed as sampled layers into pose buffers
instead of direct field mutation on Minecraft `ModelPart`.

## External Shape To Support

The system needs to support more than one current animation:

- base pose animations: idle, walk, swim, fly, sleep, portrait, profile
- named action animations: cry, recoil, physical, special, status, faint
- stateful overlays that affect only some bones while the base pose continues
- primary actions that can temporarily control most or all of the model
- look animation applied to one or more bones
- pose-specific transformed bone edits
- quirks that play over the current pose
- procedural placeholder animations for named bones
- a named root bone
- locator bones used as attachment/reference transforms
- Bedrock/Blockbench clips keyed by bone name

The poser/mod side can own the rules. RareCandy needs a mixer that accepts the
resulting active animation requests and produces:

```text
bones + clips + procedural layers + weights + masks -> final pose + matrices + locator transforms
```

## Vocabulary

Use animation-facing names:

- `Bone`: hierarchical transform target.
- `BoneId`: stable integer id for a loaded rig.
- `BoneName`: author-facing bone reference.
- `RootBone`: configured root of the animated model hierarchy.
- `LocatorBone`: named bone evaluated for attachment/effect/camera references.
- `AnimationClip`: authored keyframe data.
- `LocalPose`: local TRS and visibility for bones.
- `AnimationLayer`: one active contribution to the final pose.
- `AnimationMixer`: evaluates layers into one final pose.

Avoid making the mixer API depend on Minecraft `ModelPart`. That class belongs in an
adapter.

## Bone Interface

The mixer needs a small abstraction over a model hierarchy.

```java
public interface Bone {
    int id();
    String name();
    int parentId();

    Vector3f restTranslation();
    Quaternionf restRotation();
    Vector3f restScale();

    Matrix4f inverseBindMatrix();

    boolean isLocator();
    boolean isRenderable();
}
```

Adapter targets:

- Cobblemon/Minecraft: adapt `ModelPart` plus Cobblemon's `Bone` tree.
- RareCandy: adapt `Skeleton`, `ModelNode`, and renderer `Bone`.
- Future importers: adapt any Blockbench/Bedrock hierarchy without changing the
  mixer.

If an existing renderer class already uses the same simple name, keep the package
boundary clear. The runtime concept is still a bone interface, not a concrete
Minecraft part.

## Bone Rig

Add a rig object that owns indexing and lookup.

```java
public interface BoneRig {
    int boneCount();
    Bone bone(int id);
    int rootBoneId();
    int boneId(String name);
    List<Integer> childrenOf(int id);
    List<Integer> locatorBoneIds();
}
```

Requirements:

- Bone ids are stable for the loaded model.
- Parent/child order is deterministic.
- `rootBoneId()` comes from model config, poser metadata, or importer metadata.
- Missing named bones are validation errors when the authoring data requires them.
- Locator bones are addressable by name and id.
- The rig preserves non-renderable bones because they can be animation targets.

## Root Bone

The root bone is a named bone used for whole-model animation inside the rig.

This is separate from the renderer object's world transform.

Responsibilities:

- receive generic whole-model animation channels
- support Bedrock's `root_part` target
- anchor locator traversal
- carry authored root motion when a clip targets it
- keep world placement on the instance transform

Rules:

- There is exactly one configured root bone for a rig.
- If the configured root bone is missing, loading should report a hard validation
  error.
- Root-bone motion can be included in the final local pose.
- `ObjectInstance.modelMatrix` still owns world placement.

## Locator Bones

Locator bones are named transform targets that may or may not render geometry.

Cobblemon creates them from Blockbench locators as empty bones with the prefix
`internal_locator__`. It also exposes fallback locator states such as `root`,
`target`, `middle`, `top`, and `special_attack`.

RareCandy should store locators as real bones in the rig:

```java
public Matrix4f locatorWorldTransform(String locatorName);
public Matrix4f boneWorldTransform(int boneId);
```

Use cases:

- particles
- sound/effect origins
- held item transforms
- camera/profile/portrait anchors
- move hit/impact references
- debug overlays

Locator bones must be evaluated through the same final pose path as renderable bones.
The renderer can skip locator geometry; it cannot skip locator transforms.

## Clip Sampling

An animation clip is authored keyframe data mapped to bone names.

```java
public interface AnimationClip {
    String name();
    float durationSeconds();
    boolean loops();

    void sample(float timeSeconds, LocalPose out, BoneMask mask, AnimationContext context);
    boolean targetsBone(int boneId);
}
```

Sampling writes local TRS into `LocalPose`.

Missing channels:

- no bone track: leave that bone unwritten
- missing translation: leave translation unwritten
- missing rotation: leave rotation unwritten
- missing scale: leave scale unwritten

This makes overlays possible without forcing every clip to key every bone.

## Local Pose

`LocalPose` stores local transform data for every bone.

```java
public final class LocalPose {
    public final Vector3f[] translations;
    public final Quaternionf[] rotations;
    public final Vector3f[] scales;
    public final boolean[] translationWritten;
    public final boolean[] rotationWritten;
    public final boolean[] scaleWritten;
    public final boolean[] visible;
    public final boolean[] visibilityWritten;
}
```

Rules:

- The frame starts from the rig rest pose.
- A clip or procedural source writes only the bones/channels it targets.
- Written flags are per channel, not just per bone.
- Visibility is part of pose evaluation because Cobblemon transformed parts can
  change visibility.
- Final matrices are built once after all layers are mixed.

## Layer Types

The mixer should distinguish how a layer contributes.

```java
public enum LayerMode {
    BASE,
    POSE_TRANSFORM,
    STATEFUL,
    QUIRK,
    PROCEDURAL,
    PRIMARY,
    OVERRIDE
}
```

### Base

Base layers are current-pose animations:

- ground idle
- ground walk
- air idle
- air fly
- water idle
- sleep
- portrait/profile

Usually there is one selected pose, but the mixer should allow multiple base clips
with weights for locomotion blends.

### Pose Transform

Pose transforms are static local edits and visibility values.

Cobblemon represents these as `ModelPartTransformation`, applied after reset and
before most animations.

RareCandy should represent them as pose layers that can write local translation,
rotation, scale, and visibility.

### Stateful

Stateful layers are active animations that play over the base pose.

Examples:

- cry
- recoil
- blink
- mouth/head movement
- one-shot expression changes

They only affect bones/channels they write.

### Quirk

Quirks are scheduled active animations attached to the current pose. Cobblemon runs
them when no primary animation is active and quirks are enabled for the render
context.

In RareCandy they are just layers supplied by the caller, with timing owned by the
state/poser layer.

### Procedural

Procedural layers synthesize animation at runtime.

Examples:

- look
- biped walk helper
- quadruped walk helper
- wing flap helper
- wave/translation/rotation functions

They write into `LocalPose` like clips.

### Primary

Primary layers can take control for action duration.

Examples:

- physical attack
- special attack
- status move
- faint
- pose transition

Cobblemon allows one primary animation at a time. It fades pose intensity down using
a curve and can exclude labels. RareCandy should model that as a primary layer group
with masks and exclusion labels.

## Layer Request

The integration layer submits active layers to the mixer.

```java
public final class AnimationLayer {
    public String id;
    public String label;
    public LayerMode mode;
    public AnimationSource source;
    public BoneMask mask;
    public float weight;
    public float localTimeSeconds;
    public BlendCurve curve;
    public boolean removeWhenComplete;
}
```

`label` supports Cobblemon's primary-exclusion behavior. For example, a primary
animation can prevent most pose animations but leave the `look` label at full weight.

## Animation Source

A source can be authored or procedural.

```java
public interface AnimationSource {
    void sample(
        BoneRig rig,
        float timeSeconds,
        LocalPose out,
        BoneMask mask,
        AnimationContext context
    );

    boolean loops();
    float durationSeconds();
}
```

Implementations:

- `ClipAnimationSource`
- `BedrockClipAnimationSource`
- `LookAnimationSource`
- `PoseTransformSource`
- `PlaceholderWalkSource`
- `PlaceholderWingFlapSource`

The same mixer should handle imported Pokémon animations and generated helper motion.

## Animation Context

The mixer needs runtime values but not game logic.

```java
public interface AnimationContext {
    float ageSeconds();
    float limbSwing();
    float limbSwingAmount();
    float lookYawDegrees();
    float lookPitchDegrees();
    float movementSpeed();
    float number(String key, float defaultValue);
}
```

Cobblemon/Minecraft can populate this from entity state, partial ticks, riding data,
MoLang runtime values, or GUI state. RareCandy only consumes values needed by sources.

## Weighted Clip Mixing

The core requirement is continuously active weighted clips.

Example:

```text
base = 0.25 ground_idle + 0.75 ground_walk
stateful = cry mouth/head layer
look = procedural yaw/pitch on head subtree
```

This is different from a transition-only system. A blend can remain active for many
frames with weights changing every frame.

## Blend Rules

For each layer group and each bone/channel:

1. Sample each source into a temporary pose.
2. Ignore bones/channels outside the layer mask.
3. Ignore unwritten channels.
4. Normalize weights within the group when the group is blended.
5. Blend translations linearly.
6. Blend scales linearly.
7. Blend rotations using normalized weighted quaternion accumulation or repeated
   normalized slerp.
8. Apply the result to the accumulated final pose according to the group mode.

Repeated slerp is acceptable for the first implementation:

```java
rotation.set(first);
accumulatedWeight = firstWeight;

for each next sample:
    float t = nextWeight / (accumulatedWeight + nextWeight);
    rotation.slerp(nextRotation, t).normalize();
    accumulatedWeight += nextWeight;
```

Later, replace that with quaternion weighted sum plus hemisphere correction.

## Additive And Override Channels

Cobblemon's current mutation model often adds rotation/position deltas onto the
current bone state and multiplies scale toward a target.

RareCandy should make that explicit per source or layer:

```java
public enum ChannelBlendMode {
    REPLACE,
    ADD,
    MULTIPLY_SCALE
}
```

Suggested defaults:

- authored full-body clips: `REPLACE` within their layer group
- pose transformed parts: `ADD` for translation/rotation, `MULTIPLY_SCALE` for scale
- procedural look/walk helpers: `ADD`
- primary authored action: `REPLACE` or `ADD` depending on imported clip semantics

The importer should preserve whether a source represents absolute local TRS or a
delta from rest/current pose.

## Masks

Layers need masks.

```java
public interface BoneMask {
    boolean includes(int boneId);
}
```

Mask types:

- full rig
- explicit bone list
- subtree from bone
- inverse/exclusion list
- label-derived mask

Needed operations:

```java
BoneMask all();
BoneMask only(Collection<Integer> boneIds);
BoneMask subtree(BoneRig rig, int rootBoneId);
BoneMask excluding(BoneMask base, BoneMask excluded);
```

Use cases:

- look on head/neck bones
- mouth-only cry
- wing-only flap
- primary action excluding look
- locator-only debug layers

## Labels

Labels are runtime grouping names.

Examples:

- `base`
- `look`
- `quirk`
- `primary`
- `mouth`
- `wings`

Cobblemon's `PrimaryAnimation.prevents` checks pose-animation labels against
excluded labels. RareCandy should support that without depending on Cobblemon types.

## Primary Evaluation

Cobblemon primary behavior:

- one primary animation at a time
- active non-primary animations can be removed unless they endure primary animations
- quirks are cleared
- pose intensity becomes `1 - primaryCurve(t)`
- blocked pose animations continue at reduced intensity
- excluded labels continue at full intensity

RareCandy mixer behavior:

```text
baseWeight = 1 - primaryWeight
primaryWeight = primaryCurve(primaryNormalizedTime)
```

Then:

- base/pose animations blocked by the primary use `baseWeight`
- excluded labels use their normal weight
- primary layer uses `primaryWeight`
- completed primary layers are removed by the caller or reported as complete

This covers transition support without reducing the whole system to transitions.

## Recommended Frame Evaluation

```text
advance layer times outside or inside mixer
clear final pose to rest pose
evaluate selected base group
apply pose-transform layers
evaluate stateful active layers
evaluate quirk layers
evaluate procedural layers
evaluate primary layer with exclusions
resolve visibility
build local and global matrices
publish skinning matrices
publish bone and locator world transforms
emit completed-layer ids
```

Order can become configurable later. The first implementation should match the
Cobblemon behavior closely enough for integration.

## Matrix Build

After the final local pose is produced:

1. Walk the bone hierarchy from `rootBoneId()`.
2. Build each local matrix from local TRS.
3. Accumulate parent global matrix.
4. Store global bone transforms for queries.
5. For skinned bones, multiply by inverse bind matrix.
6. Upload final skinning matrices to the existing instance buffer.

The shader contract can remain unchanged.

## Visibility

Visibility should be a bone-level result.

```java
boolean[] boneVisible;
```

Renderer behavior:

- hide meshes attached to invisible bones
- skip draw groups when all relevant bones are invisible
- keep locator transforms available even when locator bones are invisible
- keep legacy mesh `hideDuringAnimation` as an additional renderer rule for now

Default visibility is true. Layers only change visibility when they explicitly write
it.

## Locator Output

Expose evaluated transforms:

```java
Matrix4f boneWorldTransform(int boneId);
Matrix4f locatorWorldTransform(String name);
```

These transforms should include:

- object world transform
- evaluated animation pose
- parent hierarchy

They should not require the renderer to redraw or re-sample animation.

## Current RareCandy Gaps

### Animation Emits Matrices Too Early

`renderer/animation/Animation.getFrameTransform` samples one clip and immediately
walks the hierarchy to produce skinning matrices.

Needed:

- split clip sampling from matrix building
- sample into `LocalPose`
- blend poses before hierarchy evaluation
- keep final matrices instance-owned

### Animation Stores Shared Output

`Animation` has cached matrix output. Multiple instances using the same clip can
compete over mutable output.

Needed:

- clips are immutable data
- output buffers live on `AnimationMixer` or the animated instance
- temporary sample poses are owned by the mixer/frame

### Instance Has One Current Animation

`AnimatedObjectInstance` stores `currentAnimation`.

Needed:

- one `AnimationMixer` per animated instance
- mixer owns active layers
- existing `changeAnimation(AnimationInstance)` can become a compatibility wrapper
  that clears layers and adds one base/override layer

### Skeleton Has Useful Data But No Rig API

RareCandy already has:

- `Skeleton.rootNode`
- `Skeleton.jointMap`
- `Skeleton.boneIdMap`
- `Skeleton.bones`
- `ModelNode.parent`
- `ModelNode.children`
- renderer `Bone.inverseBindMatrix`

Needed:

- adapter from `Skeleton` to `BoneRig`
- stable root bone id
- locator bone detection/config
- bone-name validation

### Visibility Is Mesh And Animation Id Based

`MultiRenderObject.shouldRender` checks `hideDuringAnimation[mesh][animId]`.

Needed:

- final `boneVisible` from the mixer
- mesh visibility derived from attached bones where possible
- legacy mesh hide rules remain as a second filter until replaced

### Root Bone Is Not Explicit Enough

RareCandy imports a root node from the scene, but Cobblemon has a model root bone
concept that can be chosen by poser metadata.

Needed:

- config/import path for root bone name
- validation when clips target `root_part`
- clear separation between root bone local motion and object world matrix

### Locator Bones Are Missing

Current source search did not show a RareCandy locator concept.

Needed:

- locator names in model config or importer metadata
- locator bones included in rig traversal
- evaluated locator matrices exposed after mixer update

## Proposed Runtime Objects

### `AnimationMixer`

```java
public final class AnimationMixer {
    private final BoneRig rig;
    private final List<AnimationLayer> layers;
    private final LocalPose restPose;
    private final LocalPose finalPose;
    private final Matrix4f[] skinningMatrices;
    private final Matrix4f[] boneWorldMatrices;
    private final boolean[] boneVisible;

    public void update(float deltaSeconds, AnimationContext context);
    public Matrix4f[] skinningMatrices();
    public Matrix4f boneWorldTransform(int boneId);
    public Matrix4f locatorWorldTransform(String locatorName);
}
```

### `ClipSampler`

```java
public interface ClipSampler {
    void sample(
        AnimationClip clip,
        float timeSeconds,
        LocalPose out,
        BoneMask mask,
        AnimationContext context
    );
}
```

Existing RareCandy `AnimationNode` data can be sampled through this without doing the
hierarchy walk.

### `SkeletonBoneRig`

```java
public final class SkeletonBoneRig implements BoneRig {
    public SkeletonBoneRig(Skeleton skeleton, String rootBoneName, Set<String> locatorNames);
}
```

Responsibilities:

- map `ModelNode` tree to bone ids
- expose rest local TRS
- expose inverse bind matrices
- mark locator bones
- report missing roots/locators/clip target names

## Example: Standing With Blink And Look

Active layers:

- base `ground_idle`, `BASE`, mask all, weight 1
- blink clip, `STATEFUL`, mask eye/head bones, curve weight
- look source, `PROCEDURAL`, mask head subtree, label `look`, weight 1

Final result:

- body follows idle
- blink affects only keyed channels
- look adds yaw/pitch to selected bones
- one final bone pose is emitted

## Example: Moving Blend

Active base layers:

- `ground_idle`, weight `1 - speedWeight`
- `ground_walk`, weight `speedWeight`

If run exists:

- `ground_idle`, idle range
- `ground_walk`, walk range
- `ground_run`, run range

The blend can stay active indefinitely. It is not an entry/exit transition.

## Example: Primary Attack With Look Excluded

Active layers:

- base `battle_idle`
- look procedural layer labeled `look`
- primary `physical`, mask all, excluded label `look`

Final result:

- attack controls the body at primary weight
- look remains active on excluded bones
- base pose fades according to primary curve where blocked
- caller removes or replaces the primary when complete

## Example: Locator Bone

Model has locator bone `mouth_fx`.

Active layers:

- base idle
- cry stateful mouth animation

After evaluation:

- `locatorWorldTransform("mouth_fx")` reflects cry mouth movement
- particles/effects can spawn at the animated mouth locator
- no geometry is required on `mouth_fx`

## Implementation Milestones

### Phase 1: Rig Abstraction

- Add bone-facing interfaces.
- Add `SkeletonBoneRig`.
- Add root bone name/config path.
- Add locator bone metadata.
- Validate clip bone names against rig names.

### Phase 2: Pose Buffers

- Add `LocalPose`.
- Add rest-pose initialization from `ModelNode`.
- Add per-channel written flags.
- Add visibility flags.

### Phase 3: Clip Sampling

- Extract RareCandy `AnimationNode` sampling from `Animation.getFrameTransform`.
- Sample local TRS into `LocalPose`.
- Keep old matrix path as a wrapper during migration.

### Phase 4: Mixer

- Add `AnimationSource`.
- Add `AnimationLayer`.
- Add `AnimationMixer`.
- Support base weighted blends.
- Support stateful overlays.
- Support primary layer weight/exclusions.

### Phase 5: Procedural Sources

- Add look source.
- Add pose-transform source.
- Add helper walk/wing/rotation/translation sources as needed.

### Phase 6: Renderer Hook

- `AnimatedObjectInstance` owns an `AnimationMixer`.
- Instance upload uses mixer skinning matrices.
- Expose bone/locator world transforms.
- Keep single-animation playback API as compatibility.

## Testing

Minimum tests:

- one clip at weight 1 matches current playback matrices
- two base clips blend continuously without restarting either clip
- stateful overlay only changes masked bones/channels
- primary layer fades base intensity down where labels are blocked
- primary excluded label remains active
- pose transform writes translation/rotation/scale/visibility correctly
- root bone animation affects children
- `root_part` target resolves to the configured root bone
- locator transform follows animated parent bones
- multiple instances using the same clip do not share output matrices
- legacy single-animation API still renders through wrapper

## Design Rule

RareCandy should not decide which Cobblemon pose is correct.

RareCandy should evaluate the active animation layers it is given:

```text
rig + active layers + masks + weights + context -> final pose + skinning matrices + locator transforms
```

That is the blender.
