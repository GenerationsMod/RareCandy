//package gg.generations.rarecandy.tools;
//
//import gg.generations.rarecandy.pokeutils.ModelConfig;
//import gg.generations.rarecandy.pokeutils.PixelAsset;
//import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
//import gg.generations.rarecandy.renderer.components.MeshObject;
//import gg.generations.rarecandy.renderer.components.MultiRenderObject;
//import gg.generations.rarecandy.renderer.components.RenderObject;
//import gg.generations.rarecandy.renderer.loading.AnimResource;
//import gg.generations.rarecandy.renderer.loading.ModelLoader;
//import gg.generations.rarecandy.renderer.model.GLModel;
//import gg.generations.rarecandy.renderer.model.GlCallSupplier;
//import gg.generations.rarecandy.renderer.textures.TextureArray;
//import gg.generations.rarecandy.tools.gui.DialogueUtils;
//import org.lwjgl.util.nfd.NativeFileDialog;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class ModelStats {
//    private final static ModelLoader loader = new ModelLoader();
//    private static float total = 0;
//
//    public static void main(String[] args) {
//        NativeFileDialog.NFD_Init();
//        ITextureLoader.setInstance(new TextureLoader());
//
//        var chosenFiles = DialogueUtils.chooseMultipleFiles("PK;pk");
//
//        while (chosenFiles != null) {
//
//            for (var file : chosenFiles) {
//                loader.<MeshObject>createObject(
//                        () -> PixelAsset.of(file, null),
//                        (GlCallSupplier<MultiRenderObject>) (model, animResources, imageFiles, variants, names, config, mro) -> {
//                            var glCalls = new ArrayList<Runnable>();
//                            try {
////                                String log = "Name: " + file.getFileName();
//
////                                ModelLoader.byteAmount = 0;
//
//                                System.out.println(PixelAsset.GSON.toJson(config));
//
//                            ModelLoader.processModel(object, gltfModel, animResources, images, config, glCalls, MeshObject::new, GLModel::new);
//
////                                var bytes = (ModelLoader.byteAmount / 1048576f);
////                                total += bytes;
////                            log += " " + (ModelLoader.byteAmount / 1048576f) + "MB";
////                                System.out.println(log + " " + total);
//                            } catch (Exception e) {
//                                throw new RuntimeException("Failed to interpret data", e);
//                            }
//                            return glCalls;
//                        },
//                        object -> {
//                        }
//                );
//            }
//
//            chosenFiles = DialogueUtils.chooseMultipleFiles("PK;pk");
//        }
//
//        System.out.println("Total Buffer Size: " + total);
//    }
//}
