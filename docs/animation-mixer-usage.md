# Animation Mixer Usage

This document explains how to use the pure animation mixer core in:

```text
gg.generations.rarecandy.renderer.animation.mixer
```

The mixer is intentionally only a local-pose evaluator. It does not know about
Minecraft, Cobblemon, renderer uploads, skinning matrices, model instances,
MoLang, or locator world transforms.

Use it when you have animation data that can sample local bone channels and you
want a blended final local pose.

## Main Types

- `Bone`: immutable rig bone data.
- `BoneRig`: immutable hierarchy and bone lookup.
- `LocalPose`: mutable per-bone local transform channels.
- `BoneMask`: decides which bones a layer may affect.
- `AnimationSource`: something that samples animation data into a `LocalPose`.
- `KeyframeAnimationSource`: built-in keyframed `AnimationSource`.
- `BoneAnimationTrack`: one bone's keyframed channels.
- `AnimationLayer`: one active contribution to the final pose.
- `AnimationMixer`: evaluates layers and exposes the final `LocalPose`.

## Basic Flow

1. Build a `BoneRig`.
2. Implement or create one or more `AnimationSource` instances.
3. Create an `AnimationMixer` for the rig.
4. Add `AnimationLayer` instances to the mixer.
5. Call `update(deltaSeconds)` each frame or tick.
6. Read the final `LocalPose` from `pose()`.

## Build a Rig

Bone ids must be dense and zero-based. Parent ids refer to other bone ids.
The root bone uses `Bone.NO_PARENT`.

```java
import gg.generations.rarecandy.renderer.animation.mixer.Bone;
import gg.generations.rarecandy.renderer.animation.mixer.BoneRig;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

var bones = List.of(
        new Bone(
                0,
                "root",
                Bone.NO_PARENT,
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Quaternionf(),
                new Vector3f(1.0f, 1.0f, 1.0f),
                false
        ),
        new Bone(
                1,
                "head",
                0,
                new Vector3f(0.0f, 1.0f, 0.0f),
                new Quaternionf(),
                new Vector3f(1.0f, 1.0f, 1.0f),
                false
        ),
        new Bone(
                2,
                "hat_locator",
                1,
                new Vector3f(0.0f, 0.25f, 0.0f),
                new Quaternionf(),
                new Vector3f(1.0f, 1.0f, 1.0f),
                true
        )
);

var rig = new BoneRig(bones, 0);
```

`BoneRig` validates:

- ids are dense and unique
- names are unique
- the root uses `Bone.NO_PARENT`
- all non-root bones have a valid parent
- the hierarchy is connected

## Write an Animation Source

An `AnimationSource` writes sampled local channels into the `out` pose.
Only write channels that the source actually animates. This keeps clips sparse
and lets blending leave untouched channels alone.

Always respect the `mask` argument before writing a bone.

```java
import gg.generations.rarecandy.renderer.animation.mixer.AnimationSource;
import gg.generations.rarecandy.renderer.animation.mixer.BoneMask;
import gg.generations.rarecandy.renderer.animation.mixer.LocalPose;
import org.joml.Quaternionf;

public final class HeadNodSource implements AnimationSource {
    private final int headBoneId;

    public HeadNodSource(int headBoneId) {
        this.headBoneId = headBoneId;
    }

    @Override
    public void sample(float timeSeconds, LocalPose out, BoneMask mask) {
        if (!mask.affects(headBoneId)) {
            return;
        }

        float angle = (float) Math.sin(timeSeconds * Math.PI * 2.0f) * 0.25f;
        out.setRotation(headBoneId, new Quaternionf().rotationX(angle));
    }

    @Override
    public boolean loops() {
        return true;
    }

    @Override
    public float durationSeconds() {
        return 1.0f;
    }
}
```

The core does not prescribe where animation data comes from. A source can sample
keyframes, procedural math, converted GLB data, or a future adapter.

## Use the Built-In Keyframe Source

For standalone use, create a `KeyframeAnimationSource` from one or more
`BoneAnimationTrack` instances. Each track targets one bone and can contain
translation, rotation, scale, and visibility keys.

```java
import gg.generations.rarecandy.renderer.animation.mixer.BoneAnimationTrack;
import gg.generations.rarecandy.renderer.animation.mixer.KeyframeAnimationSource;
import org.joml.Quaternionf;

var headTrack = BoneAnimationTrack.builder(headBoneId)
        .addRotation(0.0f, new Quaternionf().rotationX(0.0f))
        .addRotation(0.5f, new Quaternionf().rotationX(0.25f))
        .addRotation(1.0f, new Quaternionf().rotationX(0.0f))
        .build();

var source = KeyframeAnimationSource.builder(1.0f, true)
        .addTrack(headTrack)
        .build();
```

Sampling behavior:

- translation keys interpolate linearly
- scale keys interpolate linearly
- rotation keys use normalized slerp
- visibility keys are stepped
- empty tracks are ignored
- duplicate tracks for the same bone are rejected
- duplicate key times in the same channel are rejected

The source still writes sparse channels. If a track only has rotation keys, it
only writes rotation.

## Use It From Existing Animation Data

Existing animation data should be converted into an `AnimationSource`, then fed
to the mixer as a layer. Keep that conversion outside the mixer core. For
example, an adapter near the existing renderer animation package can convert
`Animation.AnimationNode` key storage into `KeyframeAnimationSource`.

The current `Animation` class stores key times in animation-time units, not
seconds. Convert them before building keyframes:

```java
float seconds = (float) key.time() / animation.ticksPerSecond;
float durationSeconds = (float) animation.animationDuration / animation.ticksPerSecond;
```

The adapter shape should look like this:

```java
static KeyframeAnimationSource toMixerSource(Animation animation, BoneRig rig) {
    var builder = KeyframeAnimationSource.builder(
            (float) animation.animationDuration / animation.ticksPerSecond,
            animation.loops
    );

    for (var entry : animation.getAnimationNodes().entrySet()) {
        var boneId = rig.boneId(entry.getKey());
        if (boneId < 0) {
            continue;
        }

        var node = entry.getValue();
        var track = BoneAnimationTrack.builder(boneId);

        for (var key : node.positionKeys) {
            track.addTranslation(
                    (float) key.time() / animation.ticksPerSecond,
                    key.value()
            );
        }

        for (var key : node.rotationKeys) {
            track.addRotation(
                    (float) key.time() / animation.ticksPerSecond,
                    key.value()
            );
        }

        for (var key : node.scaleKeys) {
            track.addScale(
                    (float) key.time() / animation.ticksPerSecond,
                    key.value()
            );
        }

        builder.addTrack(track.build());
    }

    return builder.build();
}
```

Do not put this adapter in the mixer package if it needs to import
`gg.generations.rarecandy.renderer.animation.Animation` or `Skeleton`. The mixer
package should stay reusable and only deal with `BoneRig`, `LocalPose`, masks,
layers, and sources.

## Animation Runtime Pattern

At runtime, animation state code should own the mixer and layers. The existing
renderer can keep owning matrix upload.

```java
var mixer = new AnimationMixer(rig);

var idleSource = toMixerSource(idleAnimation, rig);
var walkSource = toMixerSource(walkAnimation, rig);

var idleLayer = new AnimationLayer(
        "idle",
        "Idle",
        AnimationLayerMode.BASE,
        idleSource
);
idleLayer.setWeight(1.0f);

var walkLayer = new AnimationLayer(
        "walk",
        "Walk",
        AnimationLayerMode.BASE,
        walkSource
);
walkLayer.setWeight(0.0f);

mixer.addLayer(idleLayer);
mixer.addLayer(walkLayer);
```

Then update weights from animation state:

```java
idleLayer.setWeight(1.0f - movementAmount);
walkLayer.setWeight(movementAmount);

mixer.update(deltaSeconds);
var localPose = mixer.pose();
```

The `localPose` is the animation result. A separate adapter should then traverse
the rig hierarchy and convert local translation, rotation, scale, and visibility
into whatever the renderer needs. That adapter is where matrix output belongs,
not in `AnimationMixer`.

## Create a Mixer and Layers

Base layers are evaluated first as a weighted blend. Other layers are evaluated
afterward in `AnimationLayerMode` order.

```java
import gg.generations.rarecandy.renderer.animation.mixer.AnimationLayer;
import gg.generations.rarecandy.renderer.animation.mixer.AnimationLayerMode;
import gg.generations.rarecandy.renderer.animation.mixer.AnimationMixer;
import gg.generations.rarecandy.renderer.animation.mixer.BoneMasks;

var mixer = new AnimationMixer(rig);

var headBoneId = rig.requireBoneId("head");
var source = new HeadNodSource(headBoneId);

var layer = new AnimationLayer(
        "head_nod",
        "Head Nod",
        AnimationLayerMode.OVERLAY,
        source
);
layer.setMask(BoneMasks.subtree(rig, headBoneId));
layer.setWeight(1.0f);

mixer.addLayer(layer);
```

For a full-body clip, use the default mask or `BoneMasks.all()`.

```java
layer.setMask(BoneMasks.all());
```

For a layer that affects only explicit bones:

```java
layer.setMask(BoneMasks.explicit(headBoneId));
```

To exclude a subtree:

```java
var bodyWithoutHead = BoneMasks.excluding(
        BoneMasks.all(),
        BoneMasks.subtree(rig, headBoneId)
);
layer.setMask(bodyWithoutHead);
```

## Update and Read the Pose

Call `update` with elapsed seconds. The mixer advances layer local time, resets
the final pose to the rig rest pose, samples active layers, blends them, removes
completed one-shot layers when configured, and exposes the final local pose.

```java
mixer.update(deltaSeconds);

var pose = mixer.pose();
var headTranslation = pose.translation(headBoneId);
var headRotation = pose.rotation(headBoneId);
var headScale = pose.scale(headBoneId);
var headVisible = pose.visible(headBoneId);
```

`pose()` returns local channels only. Converting that local pose into model-space
matrices, skinning transforms, or locator world transforms is a later adapter or
renderer responsibility.

## Layer Modes

`AnimationLayerMode.BASE`

Weighted base animation. Multiple base layers are blended together per channel
and become the starting pose for later layers.

`AnimationLayerMode.POSE`

Applied after base layers. Useful for pose corrections or partial-body pose
changes.

`AnimationLayerMode.OVERLAY`

Applied after pose layers. Useful for secondary motion or expression layers.

`AnimationLayerMode.PRIMARY`

Applied after overlays. Useful for high-priority action layers.

`AnimationLayerMode.ADDITIVE`

Applied after primary layers. Defaults to `BlendMode.ADD` in the simple
constructor.

`AnimationLayerMode.OVERRIDE`

Applied last. Useful for layers that should win over earlier contributions.

## Blend Modes

`BlendMode.REPLACE`

Interpolates the current final pose toward sampled channels by layer weight.
Translation and scale use linear interpolation. Rotation uses slerp.

`BlendMode.ADD`

Adds sampled translation, applies sampled rotation as a delta from identity, and
adds sampled scale delta from `1.0`.

`BlendMode.MULTIPLY_SCALE`

Translation and rotation behave like replace. Scale is applied
multiplicatively.

## One-Shot Layers

Non-looping sources can remove their layer after completion.

```java
var actionLayer = new AnimationLayer(
        "attack",
        "Attack",
        AnimationLayerMode.PRIMARY,
        attackSource
);
actionLayer.setRemoveWhenComplete(true);

mixer.addLayer(actionLayer);
```

A source is complete when:

```text
!source.loops() && layer.localTimeSeconds() >= source.durationSeconds()
```

The mixer clamps sample time to the duration for non-looping sources.

## Local Pose Written Flags

`LocalPose` tracks whether each channel has been written:

```java
pose.hasTranslation(boneId);
pose.hasRotation(boneId);
pose.hasScale(boneId);
pose.hasVisibility(boneId);
```

Animation sources should leave unwritten channels untouched. For example, a
rotation-only source should only call `setRotation`.

The final mixer pose is reset to rest pose every update, so final-pose channels
are complete. Sparse behavior matters most for sampled source poses and layer
blending.

## Integration Boundary

Keep the mixer core isolated. Do not put these concerns into
`gg.generations.rarecandy.renderer.animation.mixer`:

- Minecraft or Cobblemon entity state
- render context
- model instance storage
- MoLang
- renderer upload code
- skinning matrix output
- locator world transform queries
- GUI tools

Those should be handled by adapter layers that feed `AnimationSource` data into
the mixer and consume the final `LocalPose`.
