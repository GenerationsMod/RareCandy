//package gg.generations.rarecandy.renderer.animation;
//
//import de.javagl.jgltf.model.AnimationModel;
//import de.javagl.jgltf.model.NodeModel;
//import org.joml.Quaternionf;
//import org.joml.Vector3f;
//
//import java.util.List;
//
//public class GlbAnimation extends Animation<AnimationModel> {
//    public GlbAnimation(AnimationModel rawAnimation, Skeleton skeleton, int speed) {
//        super(rawAnimation.getName(), speed, skeleton, rawAnimation, GlbAnimation::fillAnimationNodes, Animation::fillOffsets);
//
//        animationModifier.accept(this, "glb");
//    }
//
//    public static AnimationNode[] fillAnimationNodes(Animation<AnimationModel> animation, AnimationModel item) {
//        return fillAnimationNodesGlb(animation, item.getChannels());
//    }
//
//    private static AnimationNode[] fillAnimationNodesGlb(Animation<AnimationModel> animation, List<AnimationModel.Channel> channels) {
//        var animationNodes = new AnimationNode[channels.size()];
//
//        for (var channel : channels) {
//            var node = channel.getNodeModel();
//            animationNodes[animation.nodeIdMap.computeIfAbsent(node.getName(), animation::newNode)] = createNode(channels.stream().filter(c -> c.getNodeModel().equals(node)).toList(), node);
//        }
//
//        return animationNodes;
//    }
//
//    private static AnimationNode createNode(List<AnimationModel.Channel> nodeChannels, NodeModel node) {
//        if (nodeChannels.size() > 3) throw new RuntimeException("More channels than we can handle");
//
//        var animationNode = new AnimationNode();
//
//        for (var channel : nodeChannels) {
//            switch (channel.getPath()) {
//                case "translation" -> {
//                    var timeBuffer = channel.getSampler().getInput().getBufferViewModel().getBufferViewData().asFloatBuffer();
//                    var translationBuffer = channel.getSampler().getOutput().getBufferViewModel().getBufferViewData().asFloatBuffer();
//
//                    for (var i = 0; i < timeBuffer.capacity(); i++) {
//                        animationNode.positionKeys.add(timeBuffer.get(), new Vector3f(translationBuffer.get(), translationBuffer.get(), translationBuffer.get()));
//                    }
//                }
//
//                case "rotation" -> {
//                    var timeBuffer = channel.getSampler().getInput().getBufferViewModel().getBufferViewData().asFloatBuffer();
//                    var rotationBuffer = channel.getSampler().getOutput().getBufferViewModel().getBufferViewData().asFloatBuffer();
//
//                    for (var i = 0; i < timeBuffer.capacity(); i++) {
//                        animationNode.rotationKeys.add(timeBuffer.get(), new Quaternionf(rotationBuffer.get(), rotationBuffer.get(), rotationBuffer.get(), rotationBuffer.get()));
//                    }
//                }
//
//                case "scale" -> {
//                    var timeBuffer = channel.getSampler().getInput().getBufferViewModel().getBufferViewData().asFloatBuffer();
//                    var scaleBuffer = channel.getSampler().getOutput().getBufferViewModel().getBufferViewData().asFloatBuffer();
//
//                    for (var i = 0; i < timeBuffer.capacity(); i++) {
//                        animationNode.scaleKeys.add(timeBuffer.get(), new Vector3f(scaleBuffer.get(), scaleBuffer.get(), scaleBuffer.get()));
//                    }
//                }
//
//                default -> throw new RuntimeException("Unknown Channel Type \"" + channel.getPath() + "\"");
//            }
//        }
//
//        if (animationNode.positionKeys.size() == 0)
//            animationNode.positionKeys.add(0, node.getTranslation() != null ? convertArrayToVector3f(node.getTranslation()) : new Vector3f());
//        if (animationNode.rotationKeys.size() == 0)
//            animationNode.rotationKeys.add(0, node.getRotation() != null ? convertArrayToQuaterionf(node.getRotation()) : new Quaternionf());
//        if (animationNode.scaleKeys.size() == 0)
//            animationNode.scaleKeys.add(0, node.getScale() != null ? convertArrayToVector3f(node.getScale()) : new Vector3f(1, 1, 1));
//
//        return animationNode;
//    }
//
//    private static Vector3f convertArrayToVector3f(float[] array) {
//        return new Vector3f().set(array);
//    }
//
//    private static Quaternionf convertArrayToQuaterionf(float[] array) {
//        return new Quaternionf().set(array[0], array[1], array[2], array[3]);
//    }
//}
