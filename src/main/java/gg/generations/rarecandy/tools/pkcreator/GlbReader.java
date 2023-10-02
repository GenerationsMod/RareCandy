package gg.generations.rarecandy.tools.pkcreator;

//import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
//import com.google.gson.*;
//import de.javagl.jgltf.model.GltfConstants;
//import de.javagl.jgltf.model.GltfModel;
//import de.javagl.jgltf.model.MeshModel;
//import de.javagl.jgltf.model.NodeModel;
//import de.javagl.jgltf.model.creation.AccessorModels;
//import de.javagl.jgltf.model.creation.GltfModelBuilder;
//import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
//import de.javagl.jgltf.model.impl.*;
//import de.javagl.jgltf.model.io.Buffers;
//import de.javagl.jgltf.model.io.GltfModelReader;
//import de.javagl.jgltf.model.io.v2.GltfModelWriterV2;
//import de.javagl.jgltf.model.v2.MaterialModelV2;
//import gg.generations.rarecandy.tools.gui.DialogueUtils;
//import org.joml.Matrix4f;
//import org.lwjgl.util.nfd.NativeFileDialog;
//
//import javax.imageio.ImageIO;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.FloatBuffer;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.*;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//import java.util.stream.Collectors;

public class GlbReader {
//    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
//    public static void main(String[] args) {
//        NativeFileDialog.NFD_Init();
//        var chosenFile = DialogueUtils.chooseFile("GLB;glb");
//
//        while (chosenFile != null) {
//            read(chosenFile);
//            chosenFile = DialogueUtils.chooseFile("GLB;glb");
//        }
//    }
//
//    public static record Material(String name, String image) {}
//
//    public static void read(Path path) {
//        var gltfReader = new GltfModelReader();
//
//        try {
//            var gltf = (DefaultGltfModel) gltfReader.readWithoutReferences(Files.newInputStream(path));
//
//
//            var images = gltf.getTextureModels().stream().collect(Collectors.toMap(a -> a, a -> {
//                var node = a.getImageModel();
//                return new Texture(node.getName(), node.getImageData());
//            }));
//
//            var materials = gltf.getMaterialModels().stream().map(a -> (MaterialModelV2) a).map(a -> new Material(a.getName(), a.getBaseColorTexture().getImageModel().getName().replace(".tga", "") + ".png")).toList();
//
//            var variants =(((List<Map<String, String>>)((Map<String, Object>) gltf.getExtensions().get("KHR_materials_variants")).get("variants"))).stream().map(a -> a.get("name")).toList();
//
//            var defaultVariant = new HashMap<String, String>();
//
//            var variantMap = variants.stream().collect(Collectors.toMap(a -> a, a -> new HashMap<String, String>()));
//
//            for(var mesh : gltf.getMeshModels()) {
//                if(mesh.getName().endsWith("Iris")) continue;
//
//                defaultVariant.put(mesh.getName(), mesh.getMeshPrimitiveModels().get(0).getMaterialModel().getName());
//
//                ((List<Map<String, Object>>) ((Map<String, Object>) mesh.getMeshPrimitiveModels().get(0).getExtensions().get("KHR_materials_variants")).get("mappings")).forEach(new Consumer<Map<String, Object>>() {
//                    @Override
//                    public void accept(Map<String, Object> map) {
//                        var material = materials.get((int) map.get("material")).name();
//                        var variants1 = ((List<Integer>) map.get("variants")).stream().map(variants::get).toList();
//
//                        variants1.forEach(new Consumer<String>() {
//                            @Override
//                            public void accept(String s) {
//                                variantMap.get(s).put(mesh.getName(), material);
//                            }
//                        });
//                    }
//                });
//            }
//
//            var p = Path.of("convert", "input").resolve(path.getFileName().toString().replace(".glb", ""));
//
//            Files.createDirectories(p);
//
//            images.values().forEach(texture -> {
//                try {
//                    var image = ImageIO.read(new ByteBufferBackedInputStream(texture.buffer()));
//                    ImageIO.write(image, "PNG", Files.newOutputStream(p.resolve(texture.name.replace(".tga", "") + ".png")));
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//
//            var gltfModel = strip(gltf);
//
//            JsonObject config = new JsonObject();
//            config.addProperty("scale", gltfModel.right());
//            var jsonMaterials = new JsonObject();
//            materials.forEach(material -> {
//                var json = new JsonObject();
//                json.addProperty("type", "solid");
//                json.addProperty("texture", material.image);
//                jsonMaterials.add(material.name(), json);
//            });
//            config.add("materials", jsonMaterials);
//
//            config.add("defaultVariant", defaultVarint(defaultVariant));
//
//
//            var variantJson = new JsonObject();
//            variantMap.forEach((s, stringStringHashMap) -> variantJson.add(s, defaultVarint(stringStringHashMap)));
//            config.add("variants", variantJson);
//
//            var string = gson.toJson(config);
//
//            var p1 = p.resolve("config.json");
//
//            var p2 = path.toAbsolutePath().getParent().resolve("anims");
//
//            if(Files.exists(p2)) {
//                Files.newDirectoryStream(p2).forEach(new Consumer<Path>() {
//                    @Override
//                    public void accept(Path path1) {
//                        try {
//                            Files.copy(path1, p.resolve(path1.getFileName()), StandardCopyOption.REPLACE_EXISTING);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                });
//
////                Files.copy(p2, p, StandardCopyOption.REPLACE_EXISTING);
//            }
//
//            Files.writeString(p1, string);
//
//
//            var gltfWriter = new GltfModelWriterV2();
//            Files.createDirectories(p);
//            gltfWriter.writeBinary(gltfModel.left(), Files.newOutputStream(p.resolve(path.getFileName())));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//
//    private static JsonElement defaultVarint(HashMap<String, String> defaultVariant) {
//        var defaultVariantJson = new JsonObject();
//        defaultVariant.forEach((s, s2) -> {
//            var json = new JsonObject();
//            json.addProperty("material", s2);
//            json.addProperty("hide", false);
//            defaultVariantJson.add(s, json);
//        });
//
//        return defaultVariantJson;
//    }
//
//    private static record Texture(String name, ByteBuffer buffer) {}
//
//    public static Pair<GltfModel, Double> strip(DefaultGltfModel gltf) {
//        var sceneModel = new DefaultSceneModel();
//
//        var pair = calculateSkin(gltf.getSkinModel(0), sceneModel);
//
//        var max = 0d;
//        var min = 0d;
//
//        List<MeshModel> meshModels = gltf.getMeshModels();
//        for (MeshModel mesh : meshModels) {
//            if(mesh.getName().endsWith("Iris")) {
//                continue;
//            }
//            var primitive = (DefaultMeshPrimitiveModel) mesh.getMeshPrimitiveModels().get(0);
//
//            var meshModel = new DefaultMeshModel();
//            var meshPrimitiveModel = MeshPrimitiveBuilder.create();
//
//            primitive.getAttributes().forEach((s, a) -> meshPrimitiveModel.addAttribute(s, AccessorModels.create(a.getComponentType(), a.getElementType().name(), a.isNormalized(), a.getAccessorData().createByteBuffer())));
//            meshPrimitiveModel.setTriangles();
//            var indices = primitive.getIndices();
//            meshPrimitiveModel.setIndices(AccessorModels.create(indices.getComponentType(), indices.getElementType().name(), indices.isNormalized(), indices.getAccessorData().createByteBuffer()));
//
//
//            var accessor = mesh.getMeshPrimitiveModels().get(0).getAttributes().get("POSITION");
//            max = Math.max(max, accessor.getMin()[1].doubleValue());
//            min = Math.min(min, accessor.getMax()[1].doubleValue());
//
//            var m = meshPrimitiveModel.build();
//            meshModel.addMeshPrimitiveModel(m);
//            meshModel.setName(mesh.getName());
//            // Create a node with the mesh
//
//            var nodeModel = new DefaultNodeModel();
//            nodeModel.setName(mesh.getName());
//            nodeModel.addMeshModel(meshModel);
//            nodeModel.setSkinModel(pair.right());
//            pair.left().addChild(nodeModel);
//        }
//
//        var gltfModelBuilder = GltfModelBuilder.create();
//        gltfModelBuilder.addSkinModel(pair.right());
//        gltfModelBuilder.addSceneModel(sceneModel);
//
//        return new Pair<>(gltfModelBuilder.build(), (1f/ (max - min)));
//    }
//
//    private static Pair<DefaultNodeModel, DefaultSkinModel> calculateSkin(DefaultSkinModel skin, DefaultSceneModel sceneModel) {
//
//        var rootNode = skin.getJoints().get(0);
//
//        while(rootNode.getParent() != null)
//            rootNode = rootNode.getParent();
//
//        var root = (DefaultNodeModel) rootNode;
//
//        var newSkin = new DefaultSkinModel();
//        var newRoot = new DefaultNodeModel();
//
//        populate(root, newRoot, newSkin, true);
//
//        var ibmBuffer = FloatBuffer.allocate(16 * (newSkin.getJoints().size()));
//
//        var arr = new float[16];
//
//        var matrix = new Matrix4f();
//
//        List<NodeModel> joints = newSkin.getJoints();
//        for (NodeModel jointNode : joints) {
//            matrix.set(jointNode.computeGlobalTransform(null)).invert();
//            matrix.get(arr);
//            ibmBuffer.put(arr);
//        }
//
//        ibmBuffer.rewind();
//
//        newSkin.setInverseBindMatrices(AccessorModels.create(GltfConstants.GL_FLOAT, "MAT4", false, Buffers.createByteBufferFrom(ibmBuffer)));
//
//        sceneModel.addNode(newRoot);
//
//        return new Pair<DefaultNodeModel, DefaultSkinModel>(newRoot, newSkin);
//    }
//
//    private static void populate(DefaultNodeModel root, DefaultNodeModel newRoot, DefaultSkinModel newSkin, boolean isRoot) {
//        newRoot.setTranslation(root.getTranslation());
//        newRoot.setRotation(root.getRotation());
//        newRoot.setScale(root.getScale());
//        newRoot.setName(root.getName());
//        if(!isRoot) newSkin.addJoint(newRoot);
//
//        if(root.getChildren() != null) {
//            for(var child : root.getChildren()) {
//                if(!child.getMeshModels().isEmpty()) continue;
//                var newChild = new DefaultNodeModel();
//                newRoot.addChild(newChild);
//                populate((DefaultNodeModel) child, newChild, newSkin, false);
//            }
//        }
//    }
//
//    public static record Pair<L, R>(L left, R right) {}
}
