# Animation Runtime Replacement

This is the target shape for replacing the old animation runtime.

No hidden wrapper around the old `Animation`.
No broad rename pass.
No duplicated keyframes per rendered instance.

## Data Ownership

### Shared Per Loaded Model

One loaded model owns animation data that every instance can reuse:

```text
ModelAnimations
  Rig rig
  AnimationData[] animations
  Map<String, Integer> animationNameToId
```

This data is immutable after load.

### Per Rendered Instance

Each rendered model instance owns only runtime state:

```text
AnimationState
  current animation id
  current time
  layer weights/times if blending is active
  BonePose working buffers
  Matrix4f[] skinning matrices
```

Many instances can point at the same `ModelAnimations`.
They must not copy the animation keyframes.

## Names

Use plain names:

- `AnimationData`: shared loaded animation key data
- `AnimationState`: per-instance runtime state
- `BonePose`: per-bone local transform result
- `Rig`: bone hierarchy and rest pose

Avoid vague names like `Clip`.

## Loading

The loader should build the new animation data directly.

Correct path:

```text
SMD/GFBANM/TRANM/TRACM
  -> AnimationData
```

Wrong path:

```text
SMD/GFBANM/TRANM/TRACM
  -> old Animation.AnimationNode
  -> old Animation
  -> new animation system
```

`ModelObjectCompiler.rebuildAnimations(...)` is the load boundary. It should
produce `ModelAnimations` or fill animation fields that use the new data types.

## AnimationData

`AnimationData` should contain shared data only:

```text
id
name
duration seconds
loops
bone tracks
material offset tracks, if still supported
```

Bone tracks contain keyframes for:

```text
translation
rotation
scale
visibility
```

Times should be stored in seconds after loading. Do not keep format-specific
frame units in runtime data.

## AnimationState

`AnimationState` belongs to one `AnimatedObjectInstance`.

It answers:

```text
what animation is playing?
what time is it at?
what is the current BonePose?
what matrices should be uploaded?
```

It does not own keyframes.

## Frame Evaluation

Per instance, per frame:

```text
1. Advance AnimationState time.
2. Sample shared AnimationData into BonePose.
3. Traverse Rig hierarchy.
4. Write Matrix4f[] skinning matrices.
5. Renderer uploads matrices as it already does.
```

The matrix array is still required by the current renderer. It is output, not
the animation data model.

## BonePose

`BonePose` is the sampled local transform for each bone:

```text
translation
rotation
scale
visibility
```

Local means parent-relative. The hierarchy traversal converts it into skinning
matrices.

## Scaling To Instances

For 500 instances of the same model:

```text
1 shared ModelAnimations
500 AnimationState objects
500 matrix buffers
0 duplicated keyframe arrays
```

That is the point of separating loaded animation data from per-instance state.

## Old Runtime Removal

Do not put the new system inside the old `Animation` class.

The old classes should stop being the owner of the live runtime path:

```text
Animation
AnimationInstance
Animation.AnimationNode
```

They can remain temporarily only if unused by the new path.

## Replacement Order

1. Add the shared data types.
2. Add the per-instance state type.
3. Change the loader to build shared animation data directly.
4. Change `MultiRenderObject` to store the shared animation data.
5. Change `AnimatedObjectInstance` to store per-instance animation state.
6. Change the PK viewer to select animations by id/name on that state.
7. Keep renderer matrix upload unchanged until the new runtime is working.
8. Remove old runtime references only after they are dead.
