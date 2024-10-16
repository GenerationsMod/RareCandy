//package gg.generations.rarecandy.tools;
//
//import de.javagl.jgltf.model.AccessorModel;
//import de.javagl.jgltf.model.ImageModel;
//import de.javagl.jgltf.model.creation.AccessorModels;
//import de.javagl.jgltf.model.creation.GltfModelBuilder;
//import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
//import de.javagl.jgltf.model.impl.DefaultAccessorModel;
//import de.javagl.jgltf.model.impl.DefaultMeshModel;
//import de.javagl.jgltf.model.impl.DefaultNodeModel;
//import de.javagl.jgltf.model.impl.DefaultSceneModel;
//import de.javagl.jgltf.model.io.GltfModelReader;
//import de.javagl.jgltf.model.io.v2.GltfModelWriterV2;
//import de.javagl.jgltf.model.v2.MaterialModelV2;
//import gg.generations.rarecandy.pokeutils.*;
//import gg.generations.rarecandy.pokeutils.util.ImageUtils;
//import gg.generations.rarecandy.renderer.loading.ModelLoader;
//import gg.generations.rarecandy.tools.gui.DialogueUtils;
//import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
//import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
//import org.lwjgl.util.nfd.NativeFileDialog;
//
//import javax.imageio.ImageIO;
//import java.io.BufferedInputStream;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardOpenOption;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class GlbReader {
//    private static final GltfModelReader reader = new GltfModelReader();
//    public static void main(String[] args) throws IOException {
//        NativeFileDialog.NFD_Init();
//
//        var path = DialogueUtils.chooseFile("GLB;glb");
//
//        while(path != null) {
//            if(path.toString().endsWith("pk")) {
//                PixelAsset asset = new PixelAsset(Files.newInputStream(path), null);
//
//                if(asset.files.size() == 1 && asset.getModelFile() != null) {
//                    var path1 = Path.of(path.getFileName().toString().replace(".pk", ""));
//                    if(Files.notExists(path1)) Files.createDirectory(path1);
//                    var glb = reader.readWithoutReferences(new ByteArrayInputStream(asset.getModelFile()));
//                    var images = glb.getImageModels();
//
//                    var imageMap = new HashMap<ImageModel, String>();
//
//                        for (var image : images) {
//                            imageMap.put(image, image.getName().toLowerCase().replace(" ", "_") + ".png");
//                            var buffer = image.getImageData();
//
//                            var bufferedImage = ImageUtils.readAsBufferedImage(buffer);
//
//
//                            ImageIO.write(bufferedImage, "PNG", path1.resolve(image.getName().toLowerCase().replace(" ", "_") + ".png").toFile() );
//                        }
//
//                        var materials = new HashMap<String, MaterialReference>();
//                        var materialsList = new ArrayList<String>();
//
//                        for (int i = 0; i < glb.getMaterialModels().size(); i++) {
//                            var material = (MaterialModelV2) glb.getMaterialModels().get(i);
//                            var image = imageMap.get(material.getBaseColorTexture().getImageModel());
//                            MaterialReference reference = new MaterialReference(null, "solid", null, null, Map.of("diffuse", image), null);
//
//                            materials.put(material.getName(), reference);
//                            materialsList.add(material.getName());
//                        }
//
//                        var defaultVariant = new HashMap<String, VariantDetails>();
//
//                        var variants1 = ModelLoader.getVariants(glb);
//
//                        var variantMap = variants1 == null ? Map.of("regular", new VariantParent(null, new HashMap<>())) : variants1.stream().collect(Collectors.toMap(a -> a, a -> new VariantParent(null, new HashMap<>())));
//
//                        glb.getMeshModels().forEach(meshModel -> {
//                            var mesh = meshModel.getMeshPrimitiveModels().get(0);
//                            defaultVariant.put(meshModel.getName(), new VariantDetails(mesh.getMaterialModel().getName(), false, null));
//
//                            var variantMap1 = ModelLoader.createMeshVariantMap(mesh, materialsList, variants1);
//
//                            for (Map.Entry<String, String> entry : variantMap1.entrySet()) {
//                                var key = entry.getKey();
//                                var value = entry.getValue();
//
//                                if(!defaultVariant.get(meshModel.getName()).material().equals(value)) variantMap.get(key).details().put(meshModel.getName(), new VariantDetails(value, false, null));
//                            }
//                        });
//
//
//                        var config = new ModelConfig();
//                        config.materials = materials;
//                        config.defaultVariant = defaultVariant;
//
//
//
//                        config.variants = variantMap;
//
//                        Files.writeString(path1.resolve("config.json"), PixelAsset.GSON.toJson(config));
//
//                        var sceneModel = new DefaultSceneModel();
//
//                        var root = new DefaultNodeModel();
//                        sceneModel.addNode(root);
//
//                        for (var mesh : glb.getMeshModels()) {
//                            var meshModel = new DefaultMeshModel();
//                            meshModel.setName(mesh.getName());
//
//                            for (var primitiveMesh : mesh.getMeshPrimitiveModels()) {
//                                var primitiveMeshModel = MeshPrimitiveBuilder.create();
//
//                                primitiveMesh.getAttributes().forEach((k, v) -> primitiveMeshModel.addAttribute(k, createAccessor(v)));
//                                primitiveMeshModel.setTriangles();
//                                primitiveMeshModel.setIndices(createAccessor(primitiveMesh.getIndices()));
//                                meshModel.addMeshPrimitiveModel(primitiveMeshModel.build());
//                            }
//
//                            var nodeModel = new DefaultNodeModel();
//                            nodeModel.setName(meshModel.getName());
//                            nodeModel.addMeshModel(meshModel);
//                            root.addChild(nodeModel);
//                        }
//
//                        var gltfModelBuilder = GltfModelBuilder.create();
//                        gltfModelBuilder.addSceneModel(sceneModel);
//                        var gltfModel = gltfModelBuilder.build();
//
//                        var gltfWriter = new GltfModelWriterV2();
//                        var glbPath = path1.resolve(path.getFileName().toString().replace(".pk", ".glb"));
//                        if(Files.notExists(glbPath)) Files.createFile(glbPath);
//                        gltfWriter.writeBinary(gltfModel, Files.newOutputStream(glbPath, StandardOpenOption.WRITE));
//
//                        asset = new PixelAsset(path1);
//
//                        try (var sevenZOutput = new SevenZOutputFile(path1.toAbsolutePath().getParent().resolve(asset.name).toFile())) {
//                            for (var file : asset.files.entrySet()) {
//                                var entry = new SevenZArchiveEntry();
//                                sevenZOutput.putArchiveEntry(entry);
//                                entry.setName(file.getKey());
//                                entry.setSize(file.getValue().length);
//                                try (var is = new BufferedInputStream(new ByteArrayInputStream(file.getValue()))) {
//                                    byte[] buffer = new byte[1024];
//                                    int length;
//                                    while ((length = is.read(buffer)) > 0) {
//                                        sevenZOutput.write(buffer, 0, length);
//                                    }
//                                }
//                                sevenZOutput.closeArchiveEntry();
//                            }
//
//                            sevenZOutput.finish();
//                        }
//                    }
//                }
//
//                path = DialogueUtils.chooseFile("PK;pk");
//            }
//    }
//
//    private static DefaultAccessorModel createAccessor(AccessorModel defaultAccessorModel) {
//        return AccessorModels.create(defaultAccessorModel.getComponentType(), defaultAccessorModel.getElementType().name(), defaultAccessorModel.isNormalized(), defaultAccessorModel.getAccessorData().createByteBuffer());
//    }
//}
