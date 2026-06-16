# Material Inspector Proposal

## Goal

Add a model-viewer tool for debugging material, mesh, variant, and atlas-transform issues without guessing from `config.json`.

The immediate use case is checking cases like an eye material sampling the wrong atlas region. The tool should show what the selected mesh/variant/material actually renders as, expose the existing runtime buffer values for tweaking, and make the selected mesh obvious in the main viewport.

## Core Workflow

1. Load a `.pk` or model folder normally.
2. Select a variant.
3. Select a mesh.
4. Resolve the material used by that mesh for the selected variant.
5. Show an isolated material preview for that exact mesh/variant/material combination.
6. Overlay the mesh UV coordinates over the preview.
7. Allow tweaking existing material and variant buffer values.
8. Highlight the selected mesh in the main model viewport.

## Scope

The tool edits and previews existing runtime data only.

Allowed:

- Select current variant.
- Select current mesh.
- Inspect the material assigned to that mesh/variant.
- Inspect and tweak material buffer fields.
- Inspect and tweak variant buffer fields.
- Switch existing texture indices.
- Tweak existing UV transforms.
- Preview the result in an FBO-backed ImGui widget.
- Draw the selected mesh UVs over the preview.
- Highlight selected mesh in the main model viewport.

Not allowed:

- No texture resizing.
- No atlas repacking.
- No creating new textures.
- No creating new materials as part of this first tool.
- No broad renderer restructure just to make the inspector.

## UI Proposal

Add a `Material Inspector` window.

Controls:

- Variant selector.
- Mesh selector.
- Read-only resolved material name for the selected mesh/variant.
- Optional material selector for direct inspection, but mesh+variant remains the main path.
- Texture index controls for the selected material:
  - diffuse
  - layer
  - mask
  - emission
- Material controls:
  - color method / shader mode
  - cull
  - blend
  - use light
  - disable depth
  - base colors
  - emission colors
  - emission intensities
- Variant controls:
  - material index
  - effect
  - paradox
  - hide
  - diffuse/layer/mask/emission UV transform

Preview:

- FBO image shown in ImGui.
- Rendered material result for the selected mesh/variant/material.
- UV wire overlay drawn on top.
- No automatic atlas edits. The preview shows the current sampled result.

## Main Viewport Highlight

The selected mesh should be visible in the normal model viewport.

Preferred behavior:

- Keep the normal model render unchanged.
- Add a lightweight highlight overlay for the selected mesh.
- Highlight should not mutate `config.json` or variant state.

Possible implementation:

- Track `selectedMeshId` in `RareCandyCanvas` or a small viewer state object.
- Add a second draw pass for only that mesh.
- Use a simple highlight pipeline or state override:
  - solid emissive tint
  - optional wireframe/edge overlay
  - depth-aware enough to identify the selected mesh

Existing `overrides` only force visibility for hidden meshes. They are not a highlight system.

## Render/Preview Design

The material preview should be a separate FBO pass.

Inputs:

- Current model.
- Selected mesh id.
- Selected variant id.
- Resolved material/variant buffer entries.
- Existing texture array.

Pass behavior:

- Render only the selected mesh/variant/material set.
- Use current runtime material/variant data.
- Render to an offscreen framebuffer.
- Display FBO color attachment in ImGui.
- Overlay UV coordinates after the material preview.

UV overlay:

- Use the selected mesh's UVs.
- Apply the selected variant's UV transform for the chosen texture layer where useful.
- Draw lines over the preview so atlas-transform problems are visible.

## Editing Behavior

The first version can edit runtime buffers only.

That means edits are useful for debugging immediately, but do not have to save back to `config.json` yet.

Later save-back can map edits to:

- `materials.<name>.images`
- `materials.<name>.values`
- `defaultVariant` / `variants`
- `VariantDetails.transform`

Keep save-back separate from the preview/debugging tool so the first implementation stays controlled.

## Crash/Bad Asset Recovery

The viewer should report asset/model issues instead of crashing.

For this inspector, useful diagnostics include:

- Missing `config.json`.
- Missing `model.glb`.
- Missing material referenced by selected variant.
- Missing texture referenced by selected material.
- Texture dimensions not matching `config.resolution`.
- Mesh selection missing from runtime mesh map.
- Variant relationship missing for selected mesh/variant.
- Skeleton/bone count beyond renderer support.

Errors should appear in a viewer panel and should not kill the application.

## Open Questions

- Should runtime tweaks immediately affect the main model, or only the preview FBO?
- Should preview edits be resettable per material/variant?
- Should selected mesh highlight use wireframe, tint, outline, or all three as modes?
- Should direct material selection be allowed when no mesh uses that material in the current variant?
- Should save-back be added after preview/editing is stable?

## Minimal First Milestone

1. Add selected variant and selected mesh state.
2. Add mesh list selection in the viewer.
3. Resolve selected mesh + selected variant to material and variant buffer entries.
4. Add main viewport selected-mesh highlight.
5. Add Material Inspector window with read-only resolved data.
6. Add FBO preview for selected mesh/material.
7. Add UV overlay.
8. Add editable runtime material/variant controls.

