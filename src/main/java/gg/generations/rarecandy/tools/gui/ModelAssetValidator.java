//package gg.generations.rarecandy.tools.gui;
//
//import gg.generations.rarecandy.pokeutils.*;
//import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
//import gg.generations.rarecandy.renderer.animation.Skeleton;
//import gg.generations.rarecandy.renderer.loading.ModelLoader;
//import org.lwjgl.assimp.AIMesh;
//import org.lwjgl.assimp.Assimp;
//import org.lwjgl.stb.STBImage;
//import org.lwjgl.system.MemoryUtil;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.IntBuffer;
//import java.util.ArrayList;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.IntStream;
//
//final class ModelAssetValidator {
//    private static final int MAX_BONES = 220;
//
//    private ModelAssetValidator() {
//    }
//
//    static Report validate(ResourceReader asset) {
//        var messages = new ArrayList<String>();
//        var errors = new ArrayList<String>();
//
//        try {
//            if (!asset.hasFile("config.json")) {
//                errors.add("Missing config.json.");
//                return new Report(messages, errors);
//            }
//
//            if (!asset.hasFile("model.glb")) {
//                errors.add("Missing model.glb.");
//                return new Report(messages, errors);
//            }
//
//            var config = ModelConfig.read(asset);
//            validateConfigShape(config, errors);
//
//            if (errors.isEmpty()) {
//                validateTextures(asset, config, errors);
//                validateSkeleton(asset, config, errors);
//            }
//        } catch (Exception e) {
//            errors.add("Asset validation failed: " + e.getMessage());
//        }
//
//        return new Report(messages, errors);
//    }
//
//    private static void validateConfigShape(IModelConfig config, List<String> errors) {
//        if (config == null) {
//            errors.add("config.json could not be parsed.");
//            return;
//        }
//
//        if (config.materials() == null || config.materials().isEmpty()) {
//            errors.add("config.json has no materials.");
//        }
//        if (config.defaultVariant() == null || config.defaultVariant().isEmpty()) {
//            errors.add("config.json has no defaultVariant mesh mapping.");
//        }
//        if (config.variants() == null) {
//            errors.add("config.json has no variants map.");
//        }
//    }
//
//    private static void validateTextures(ResourceReader asset, ModelConfig config, List<String> errors) throws IOException {
//        int resolution = config.resolution != null ? config.resolution : 1024;
//        var usedMaterials = collectUsedMaterials(config);
//
//        for (String materialName : usedMaterials) {
//            var reference = config.materials().get(materialName);
//            if (reference == null) {
//                errors.add("Material '" + materialName + "' is referenced but not defined.");
//                continue;
//            }
//
//            reference.complete(config.materials());
//            collectImages(reference).forEach(imageName -> validateTexture(asset, imageName, resolution, materialName, errors));
//        }
//    }
//
//    private static Set<String> collectUsedMaterials(ModelConfig config) {
//        var used = new LinkedHashSet<String>();
//
//        collectVariantMaterials(config.defaultVariant, used);
//        if (config.variants != null) {
//            config.variants.values().forEach(variant -> {
//                if (variant != null) collectVariantMaterials(variant.details(), used);
//            });
//        }
//
//        return used;
//    }
//
//    private static void collectVariantMaterials(Map<String, VariantDetails> details, Set<String> used) {
//        if (details == null) return;
//        details.values().forEach(detail -> {
//            if (detail != null && detail.material() != null) {
//                used.add(detail.material());
//            }
//        });
//    }
//
//    private static Set<String> collectImages(IMaterialReference reference) {
//        var images = new LinkedHashSet<String>();
//        if (reference.images() == null) return images;
//
//        addImage(images, reference.images().diffuse());
//        addImage(images, reference.images().layer());
//        addImage(images, reference.images().mask());
//        addImage(images, reference.images().emission());
//        return images;
//    }
//
//    private static void addImage(Set<String> images, String image) {
//        if (image != null && !image.isBlank()) {
//            images.add(image);
//        }
//    }
//
//    private static void validateTexture(ResourceReader asset, String imageName, int resolution, String materialName, List<String> errors) {
//        try {
//            if (!asset.hasFile(imageName)) {
//                errors.add("Material '" + materialName + "' references missing texture '" + imageName + "'.");
//                return;
//            }
//
//            var info = readImageInfo(asset.getFile(imageName));
//            if (info == null) {
//                errors.add("Texture '" + imageName + "' could not be decoded.");
//                return;
//            }
//
//            if (info.width() != resolution || info.height() != resolution) {
//                errors.add("Texture '" + imageName + "' is " + info.width() + "x" + info.height() + "; expected " + resolution + "x" + resolution + ".");
//            }
//        } catch (Exception e) {
//            errors.add("Texture '" + imageName + "' failed validation: " + e.getMessage());
//        }
//    }
//
//    private static ImageInfo readImageInfo(byte[] bytes) {
//        ByteBuffer imageBuffer = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();
//        IntBuffer width = MemoryUtil.memAllocInt(1);
//        IntBuffer height = MemoryUtil.memAllocInt(1);
//        IntBuffer channels = MemoryUtil.memAllocInt(1);
//
//        try {
//            if (!STBImage.stbi_info_from_memory(imageBuffer, width, height, channels)) {
//                return null;
//            }
//
//            return new ImageInfo(width.get(0), height.get(0));
//        } finally {
//            MemoryUtil.memFree(width);
//            MemoryUtil.memFree(height);
//            MemoryUtil.memFree(channels);
//            MemoryUtil.memFree(imageBuffer);
//        }
//    }
//
//    private static void validateSkeleton(ResourceReader asset, IModelConfig config, List<String> errors) throws IOException {
//        var scene = ModelLoader.read(asset);
//        try {
//            var rootNode = ModelNode.create(scene.mRootNode());
//            var meshes = IntStream.range(0, scene.mNumMeshes())
//                    .mapToObj(i -> AIMesh.create(scene.mMeshes().get(i)))
//                    .toArray(AIMesh[]::new);
//            var skeleton = new Skeleton(rootNode, meshes, config.excludeMeshNamesFromSkeleton());
//            int bones = skeleton.jointMap.size();
//
//            if (bones > MAX_BONES) {
////                errors.add("Configured skeleton has " + bones + " joints; renderer supports " + MAX_BONES + ". Set excludeMeshNamesFromSkeleton=true or reduce the skeleton before loading.");
//            }
//        } finally {
//            Assimp.aiReleaseImport(scene);
//        }
//    }
//
//    private record ImageInfo(int width, int height) {
//    }
//
//    record Report(List<String> warnings, List<String> errors) {
//        boolean hasErrors() {
//            return !errors.isEmpty();
//        }
//
//        List<String> messages() {
//            var all = new ArrayList<String>(warnings.size() + errors.size());
//            warnings.forEach(warning -> all.add("Warning: " + warning));
//            errors.forEach(error -> all.add("Error: " + error));
//            return all;
//        }
//    }
//}
