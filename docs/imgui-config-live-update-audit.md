# ImGui Model Config Live Update Audit

## Goal

Editing the ImGui-backed `IModelConfig` should update the loaded model immediately.

The update path should do the smallest useful refresh, but it must freely rebuild the loaded model buffer/object when a change affects layout, counts, mesh order, or data that is not cached in the current runtime object.

This is not a performance feature. The point is that the editor result is visible immediately.

## Current Load Path

The viewer currently loads through `RareCandyCanvas.openFile`, stores `canvas.config`, and calls `ModelLoader.createObject`.

`ModelLoader.createObject` does the high-level object build:

- Resolves names from config: meshes, variants, materials, images.
- Creates the `MultiRenderObject` from those names.
- Builds the texture array with `readImages`.
- Completes material inheritance and creates runtime `Material[]`.
- Builds runtime `Variant[]`, `variantRelationships`, and `stageRelationships`.
- Reads animation resources.
- Calls `processModel` to build mesh buffers, animation arrays, and the packed model SSBO.

`ModelLoader.processModel` then:

- Reads `model.glb`.
- Builds the skeleton using `excludeMeshNamesFromSkeleton`.
- Builds `Animation[]` and `hideDuringAnimation`.
- Builds vertex, index, mesh offset, material, and variant byte buffers.
- Packs all of those into one GL `modelBuffer`.
- Stores range offsets for `VertexBuffer`, `IndexBuffer`, `MeshOffsetBuffer`, `MaterialBuffer`, and `VariantBuffer`.

The shader reads `VariantBuffer` and `MaterialBuffer` directly. Updating the Java config object alone does not update rendered pixels.

## Runtime State That Matters

Loaded state lives mainly in `MultiRenderObject`:

- `meshNameToId`
- `materialNameToId`
- `variantNameToId`
- `imageNameToId`
- `meshes`
- `materials`
- `variants`
- `images`
- `variantRelationships`
- `stageRelationships`
- `animations`
- `animationNameToId`
- `animationNames`
- `hideDuringAnimation`
- `modelBuffer`
- `vertex`, `index`, `meshOffsets`, `material`, `variant` SSBO ranges
- `drawBuffer` per `RenderStage`
- `scale`

The render loop also depends on:

- `RareCandyCanvas.scaleModifier`
- `RareCandyCanvas.loadedModelInstance.variant()`
- `Selected` state for current mesh/variant
- `PixelAssetTree` entries for variants, animations, images, and meshes

## Required Dirty Information

The config editor should not only return `boolean dirty`.

It needs to report at least:

- Section changed.
- Entry key changed.
- Field changed.
- Whether an entry was added, removed, or renamed.
- Old value and new value when needed for dependency checks.

Suggested dirty categories:

- `SCALE`
- `MATERIAL_FIELD`
- `MATERIAL_ADD_REMOVE`
- `VARIANT_DETAIL_FIELD`
- `VARIANT_ADD_REMOVE`
- `VARIANT_PARENT_FIELD`
- `DEFAULT_VARIANT_FIELD`
- `RELATIONSHIP_TABLE`
- `TEXTURE_SET`
- `ANIMATION_SETTINGS`
- `MESH_OPTIONS`
- `NAMES_OR_LAYOUT`
- `FULL_REBUILD`

The dispatcher can start specific and escalate when the change invalidates more state.

## Update Levels

Use these levels in order. Escalate when the current loaded object cannot safely represent the edited config.

### Level 0: Config/UI Only

Use when a field is saved but currently has no runtime consumer.

Known current example:

- `IMeshOptions.aliases()` appears to be serialized but is not used by `ModelLoader.processPrimitiveModel`; only `invert()` is currently used there.

### Level 1: Canvas State Only

Use when no renderer buffer data changes.

`scale` can update:

- `canvas.scaleModifier`
- `loadedModel.scale`

Caveat: `processAnimations` also bakes `config.scale()` into skeletal offsets through `ISkeletalTransform.scale(config.scale())`. If scale edits are expected to affect those root animation offsets, animation rebuild is also needed.

### Level 2: Fixed Slot Buffer Upload

Use when the edited record already exists and no name list/count changes.

Material field edits:

- Re-complete the edited material and any children that inherit from it.
- Rebuild affected `Material` records.
- Update `loadedModel.materials[materialId]`.
- Upload affected slots into `MaterialBuffer`.
- If cull/blend/disableDepth changed, recompute render stages for all mesh/variant relationships using affected materials.

Variant detail edits:

- Re-resolve the affected variant and descendants if parent inheritance is involved.
- Rebuild affected `Variant` records.
- Update `loadedModel.variants`.
- Upload affected slots into `VariantBuffer`.
- Recompute `variantRelationships` and `stageRelationships` for affected mesh/variant pairs.

This only works while affected material, variant, mesh, and image names already exist in the current runtime maps, and while the unique `Variant[]` count still fits the existing `VariantBuffer` range.

### Level 3: Relationship/Table Recompute

Use when CPU-side routing changes but buffer record sizes do not necessarily grow.

Examples:

- Existing variant changes material to another existing material.
- Existing default variant changes material/effect/hide/transform for an existing mesh.
- Existing variant parent `inherits` changes to another existing variant.
- Existing aliases affect only relationship resolution and all target meshes already exist in `meshNameToId`.

Actions:

- Recompute resolved variants.
- Recompute `variantRelationships`.
- Recompute `stageRelationships`.
- Ensure `drawBuffer` contains entries for any newly used `RenderStage`.
- Re-upload `VariantBuffer` if variant records changed.

If a new variant name, mesh name, material name, or larger unique variant count appears, escalate.

### Level 4: Texture Array Rebuild

Use when image names or resolution change but mesh/material/variant counts can remain valid.

Examples:

- Material image field changes to a texture name not currently in `imageNameToId`.
- `resolution` changes.

Actions:

- Regenerate image names from current config.
- Rebuild `TextureArray`.
- Replace `loadedModel.images`.
- Replace `imageNameToId`.
- Rebuild affected material records with new image indices.
- Upload affected/all material slots.
- Reinitialize the image tree UI.

Escalate if the material set also changes in a way that changes `MaterialBuffer` size.

### Level 5: Packed Model Buffer Rebuild

Use when the packed `modelBuffer` ranges need new sizes or offsets.

Examples:

- Material count changes.
- Unique variant record count outgrows the old `VariantBuffer` range.
- Mesh count/order changes.
- Vertex/index/mesh offset data must be regenerated.

Actions:

- Rebuild `modelBuffer` from current config.
- Refresh `SbboOffset` ranges.
- Refresh CPU arrays/maps tied to that buffer.
- Keep current camera, selected variant/mesh if names still exist, and current animation choice if still valid.

This is allowed and should be the normal fallback.

### Level 6: Full Loaded Object Rebuild

Use when the current `MultiRenderObject` shape no longer matches config enough that replacing it is simpler/safer.

Examples:

- `excludeMeshNamesFromSkeleton` changes.
- `modelOptions.invert` changes with current data, because the source mesh/index data is not retained in `MultiRenderObject`.
- `meshesToRenderFirst` changes, because it changes mesh ordering.
- `aliases` changes in a way that changes loaded mesh names.
- A default variant adds a mesh not currently loaded in `meshNameToId`.
- Variant map adds/removes variant names, changing `variantNameToId` and relationship dimensions.

Actions:

- Re-run current model load against the current temp asset and current config.
- Preserve camera transform, scale, selected names, and current animation by name when possible.
- Rebuild `PixelAssetTree`.

## Config Section Audit

### `scale`

Primary live update:

- `canvas.scaleModifier`
- `loadedModel.scale`

Escalate to animation rebuild if root animation offsets must reflect the new scale.

Risk: current `ISkeletalTransform.scale(float)` mutates the transform position. Reusing the same mutable config object across animation rebuilds needs care.

### `materials`

Field-level update is usually possible.

Direct fields:

- `parent`
- `shader`
- `cull`
- `blend`
- `images`
- `values`

Runtime effects:

- `shader` maps to `Material.colorMethod`.
- `images` map texture names to `imageNameToId` indices.
- color/emission/useLight/disableDepth are in `Material.values`.
- `cull`, `blend`, and `disableDepth` affect `RenderStage`.

Update rules:

- Existing material, existing image names: rebuild/upload material slot.
- Parent edit: rebuild/upload that material and all descendants using it.
- Cull/blend/disableDepth edit: rebuild stages for relationships using affected materials.
- New image name or resolution: texture array rebuild.
- New material referenced by variants: packed buffer or object rebuild.
- Removed material currently used by variants: rebuild or fail visibly; do not leave stale material ids.

### `defaultVariant`

Controls mesh default material/effect/hide/transform.

Update rules:

- Existing mesh and existing material: recompute default relationship and affected variant fallback results.
- Changed transform/effect/paradox/hide: rebuild affected `Variant` record and upload variant slot.
- Added mesh not already loaded: full object rebuild.
- Removed mesh: relationship recompute may hide it only if current object can still represent it; otherwise rebuild is cleaner.

### `variants`

Controls variant names, inheritance, and overrides.

Update rules:

- Editing an existing override for existing mesh/material: recompute resolved variant, relationship, stage, and variant buffer.
- Editing `inherits`: recompute that variant and descendants.
- Adding/removing variant names: relationship dimension and `variantNameToId` change, so full object rebuild is the safe path.
- Adding/removing override meshes may require full object rebuild if those meshes were not loaded.

### `hideDuringAnimation`

Only fills `loadedModel.hideDuringAnimation[mesh][animation]`.

Update rules:

- Existing mesh and animation names: recompute hide table only.
- Missing mesh name has no live effect unless that mesh is loaded elsewhere.
- Adding/removing animations is not controlled here; that comes from asset animation files.

### `animationFpsOverride`

Used while constructing `Animation[]`.

Update rules:

- Existing animation name: rebuild that `Animation` or the full animation array.
- Preserve current animation by name after rebuild.

### `animationLoopsOverride`

Used while constructing `Animation[]`.

Update rules:

- Existing animation name: update/rebuild that `Animation`.
- If current animation is active, preserve or restart predictably by name.

### `offsets`

Used as root skeletal transform when constructing `Animation`.

Update rules:

- Existing animation name: rebuild that `Animation` or full animation array.
- Watch mutation from `ISkeletalTransform.scale(config.scale())`.

### `materialsWithSameMaterialAnimation`

Used while building material animation offset arrays.

Current material animation application appears mostly disabled/commented in runtime, but the loader still builds `Animation.Offset[]`.

Update rules:

- Rebuild animation offset arrays if this should be reflected.
- If material animation remains disabled, this can be treated as config-only until that code path is restored.

### `ignoreScaleInAnimation`

Used while constructing each `Animation`.

Update rules:

- Rebuild affected animations or full animation array.

### `modelOptions`

Currently `invert()` changes triangle index order in `processPrimitiveModel`.

Update rules:

- `invert` needs index data regeneration. Since source mesh data is not retained in `MultiRenderObject`, use full loaded object rebuild with current code.
- `aliases()` currently appears unused by loading; treat as config-only unless loader support is added.

### `meshesToRenderFirst`

Changes mesh ordering in generated names.

Update rules:

- Full object rebuild. Mesh order affects mesh ids, relationships, draw data, and selected mesh ids.

### `aliases`

Affects generated mesh names and how default/variant entries map onto actual meshes.

Update rules:

- Full object rebuild is safest.
- A narrower relationship-only update is possible only if every old/new alias target is already loaded and mesh order does not change.

### `excludeMeshNamesFromSkeleton`

Used when constructing `Skeleton`.

Update rules:

- Full object rebuild.

### `resolution`

Used by `readImages`.

Update rules:

- Rebuild texture array.
- Rebuild/reupload material records if image layer ids change.

## Needed Loader/Runtime Seams

The current useful logic is mostly private inside `ModelLoader`. To implement live updates without duplicating logic, extract or expose these operations:

- Generate names from config.
- Complete material inheritance.
- Build one runtime `Material`.
- Resolve variants with inheritance.
- Build one runtime `Variant`.
- Recompute variant/stage relationships.
- Build/rebuild animation arrays.
- Rebuild hide table.
- Rebuild texture array.
- Rebuild packed `modelBuffer` from current config.

The updater should live outside the ImGui config classes. The config classes should only report precise changes.

## Suggested Update Dispatcher

The dispatch shape should be:

1. ImGui edit mutates the live `IModelConfig` instance.
2. ImGui edit emits a dirty event with section/key/field/action.
3. `PokeUtilsGui` marks the file dirty.
4. A runtime updater receives `canvas`, current asset reader, loaded model, and dirty event.
5. Updater applies the smallest valid refresh.
6. If validation fails at any level, escalate to the next rebuild level.
7. Preserve selected variant, selected mesh, current animation, camera, and scale where names still exist.

## Implementation Warnings

- Do not make ImGui config classes own renderer reload logic.
- Do not use a plain boolean dirty flag for renderer updates.
- Do not assume changing config object fields changes GL buffers.
- Do not assume a material edit only affects one material; inheritance can make it affect children.
- Do not assume a variant edit only affects one variant; parent inheritance can make it affect descendants.
- Do not assume `Variant[]` size stays fixed; unique variant dedup can grow or shrink.
- Do not leave `stageRelationships` stale when material render state changes.
- Do not leave UI selection ids stale after rebuild; restore by name.
- Do not mutate source config transforms repeatedly during animation rebuilds.

## Minimum Safe First Pass

The first implementation can be deliberately conservative:

1. Add precise dirty events to ImGui config editors.
2. Direct-update `scale`.
3. Direct-update existing material value/image/shader/cull/blend fields when material/image names already exist.
4. Recompute all variant relationships and upload all variant records for existing variant/detail edits when counts fit.
5. Recompute hide table for hide-during-animation edits.
6. For everything else, rebuild the loaded object from the current config.

That gets immediate visual feedback without requiring every narrow update path on day one.
