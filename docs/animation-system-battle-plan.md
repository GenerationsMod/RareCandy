# Animation System Battle Plan

## Scope

Write a pure animation system for RareCandy as a library component.

This pass is not the Cobblemon integration. It should not know about Minecraft
entities, render context, MoLang, renderer upload, skinning buffers, GUI tools, or
RareCandy instance storage.

The goal is a clean animation core that later systems can feed.

## Package

Use:

```text
gg.generations.rarecandy.renderer.animation.mixer
```

## Core Concepts

### Bone

A bone is the animation target.

It needs:

- stable id
- name
- parent id
- rest translation
- rest rotation
- rest scale
- locator flag

It should not expose renderer data.

### Bone Rig

A rig owns the bone hierarchy and lookup.

It needs:

- bone count
- bone by id
- bone id by name
- root bone id
- children by bone id
- locator bone ids

The root bone and locator bones are part of the core data model, but the core does
not expose game-specific locator behavior.

### Local Pose

A local pose stores per-bone channels:

- translation
- rotation
- scale
- visibility

Each transform channel has a written flag, so sampled clips and overlays can write
only the bones/channels they actually affect.

### Bone Mask

Masks define what a layer can affect.

Needed masks:

- all
- none
- explicit bone ids
- subtree
- excluding another mask

## Animation Source

An animation source is anything that can sample local bone channels.

Shape:

```java
public interface AnimationSource {
    void sample(float timeSeconds, LocalPose out, BoneMask mask);
    boolean loops();
    float durationSeconds();
}
```

No render context.
No entity context.
No Minecraft movement values.
No Cobblemon types.

Future integration can implement sources that know about those values. The core does
not.

## Animation Layer

A layer is one active contribution to the final pose.

Fields:

- id
- label
- mode
- source
- mask
- weight
- local time
- blend curve
- blend mode
- remove when complete

Layer modes:

- base
- pose
- overlay
- primary
- additive
- override

Blend modes:

- replace
- add
- multiply scale

## Mixer

The mixer owns:

- one `BoneRig`
- active layers
- final `LocalPose`
- reusable sample `LocalPose`

Public API:

```java
public final class AnimationMixer {
    public void addLayer(AnimationLayer layer);
    public void clearLayers();
    public void update(float deltaSeconds);
    public LocalPose pose();
}
```

The mixer does not output matrices.
The mixer does not output skinning transforms.
The mixer does not expose locator world transforms.

Those are later adapter/renderer responsibilities.

## Evaluation

On update:

1. Advance layer local time.
2. Reset final pose to rig rest pose.
3. Evaluate base layers as a weighted blend.
4. Evaluate non-base layers in mode order.
5. Apply masks and blend modes.
6. Remove completed one-shot layers if requested.
7. Expose final `LocalPose`.

## Blend Rules

For weighted base layers:

- sample each source into a reusable pose
- blend only written channels
- normalize weights per channel
- translation blends linearly
- scale blends linearly
- rotation uses repeated normalized slerp
- visibility uses a simple strongest/threshold rule for now

For later layers:

- replace: lerp/slerp current final pose toward sampled channels by weight
- add: add translation delta, apply rotation delta, add scale delta from 1
- multiply scale: normal replace for translation/rotation, multiplicative scale

## Not In This Pass

Do not add:

- Cobblemon imports
- Minecraft imports
- render context
- entity state context
- movement/look procedural inputs
- MoLang
- `Skeleton` adapter
- `Animation` changes
- `AnimationInstance` changes
- renderer changes
- skinning matrix output
- locator world transform query API
- GUI/tool changes

## Completion

For implementation later, completion means:

- isolated core classes exist
- they compile in the RareCandy library source set
- `.\gradlew.bat rare_candy` succeeds
