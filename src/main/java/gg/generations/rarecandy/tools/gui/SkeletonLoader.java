package gg.generations.rarecandy.tools.gui;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.NodeModel;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import gg.generations.rarecandy.pokeutils.*;
import gg.generations.rarecandy.renderer.components.BoneMesh;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import org.joml.Matrix4f;

import java.util.*;

public class SkeletonLoader {
    public static void createSkeleton(MultiRenderObject<BoneMesh> objects, GltfModel gltfModel, Map<String, SMDFile> smdFileMap, Map<String, byte[]> gfbFileMap, Map<String, Pair<byte[], byte[]>> tranmFilesMap, Map<String, String> images, ModelConfig config, List<Runnable> glCalls) {
//        checkForRootTransformation(objects, gltfModel);
//        if (gltfModel.getSceneModels().size() > 1) throw new RuntimeException("Cannot handle more than one scene");
//
//        Map<String, Animation> animations = null;
//
//        Skeleton skeleton;
//
//        if (!gltfModel.getSkinModels().isEmpty()) {
//            skeleton = new Skeleton(gltfModel.getSkinModels().get(0));
//            animations = gltfModel.getAnimationModels().stream().map(animationModel -> new GlbAnimation(animationModel, skeleton, Animation.GLB_SPEED)).collect(Collectors.toMap(animation -> animation.name, animation -> animation));
//
//            for (var entry : tranmFilesMap.entrySet()) {
//                var name = entry.getKey();
//                var buffer = ByteBuffer.wrap(entry.getValue());
//
//                var gfbAnim = gg.generations.rarecandy.pokeutils.tranm.Animation.getRootAsAnimation(buffer);
//
//                if(gfbAnim.anim() == null) continue;
//
//                animations.put(name, new TranmAnimation(name, gfbAnim, new Skeleton(skeleton)));
//            }
//
//            var set = new HashSet<String>();
//
//            for (var entry : gfbFileMap.entrySet()) {
//                var name = entry.getKey();
//
//                var gfbAnim = AnimationT.deserializeFromBinary(entry.getValue());
//
//                if(gfbAnim.getSkeleton() == null) continue;
//
//                set.addAll(Stream.of(gfbAnim.getSkeleton().getTracks()).map(a -> a.getName()).toList());
//
//                animations.put(name, new GfbAnimation(name, gfbAnim, new Skeleton(skeleton)));
//            }
//
//            for (var entry : smdFileMap.entrySet()) {
//                var key = entry.getKey();
//                var value = entry.getValue();
//
//                for (var block : value.blocks) {
//                    if (block instanceof SkeletonBlock skeletonBlock) {
//                        animations.put(key, new SmdAnimation(key, skeletonBlock, new Skeleton(skeleton), Animation.FPS_24));
//                        break;
//                    }
//                }
//            }
//        } else {
//            skeleton = null;
//        }
//
//        if(!gltfModel.getSkinModels().isEmpty()) {
//            var skin = gltfModel.getSkinModels().get(0);
//            var indices = new ArrayList<Integer>();
//            var vertices = IntStream.range(0, skin.getJoints().size()).toArray();
//
//            var model = new GLModel();
//            var root = skin.getSkeleton();
//
//            if(root == null) {
//                root = gltfModel.getSceneModels().get(0).getNodeModels().get(0);
//            }
//
//            traverse(root, skin.getJoints().indexOf(root), skin.getJoints(), indices);
//
//            glCalls.add(() -> {
//                model.vao = GL30.glGenVertexArrays();
//
//                GL30.glBindVertexArray(model.vao);
//
//                int vbo = GL15.glGenBuffers();
//                var buffervertex = BufferUtils.createIntBuffer(vertices.length);
//                buffervertex.put(vertices).flip();
//
//                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
//                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffervertex, GL15.GL_STATIC_DRAW);
//
//                GL30.glVertexAttribIPointer(0, 1, GL15.GL_INT, 2, 0);
//                GL30.glEnableVertexAttribArray(0);
//
//                model.ebo = GL15.glGenBuffers();
//                GL30.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, model.ebo);
//
//                IntBuffer inddexBuffer = BufferUtils.createIntBuffer(indices.size());
//                inddexBuffer.put(indices.stream().mapToInt(Integer::intValue).toArray()).flip();
//                GL30.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, inddexBuffer, GL15.GL_STATIC_DRAW);
//                GL30.glBindVertexArray(0);
//
//                model.meshDrawCommands.add(new MeshDrawCommand(model.vao, GL11.GL_LINES, GL11.GL_INT, model.ebo, indices.size()));
//            });
//
//            var animatedMeshObject = new BoneMesh(animations, model, skeleton);
//
//
//            objects.add(animatedMeshObject);
//        }
//
//        objects.dimensions.set(calculateDimensions(gltfModel));
    }

    private static void traverse(NodeModel root, int index, List<NodeModel> joints, ArrayList<Integer> indices) {
        for (var bone : root.getChildren()) {
            var childIndex = joints.indexOf(bone);

            if (index > -1 && childIndex > -1) {
                indices.add(index);
                indices.add(childIndex);
            }

            traverse(bone, childIndex, joints, indices);
        }
    }

    public static void checkForRootTransformation(MultiRenderObject<BoneMesh> objects, GltfModel gltfModel) {
        if (gltfModel.getSkinModels().isEmpty()) {
            var node = gltfModel.getNodeModels().get(0);
            while (node.getParent() != null) node = node.getParent();
            var rootTransformation = new Matrix4f().set(node.createGlobalTransformSupplier().get());
            objects.setRootTransformation(rootTransformation);
        }
    }

}
