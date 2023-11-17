package gg.generations.rarecandy.renderer.animation;

import de.javagl.jgltf.model.AnimationModel;

import java.util.List;

public class GlbAnimation extends Animation<AnimationModel> {
    public GlbAnimation(AnimationModel rawAnimation, Skeleton skeleton, int speed) {
        super(rawAnimation.getName(), speed, skeleton, rawAnimation);

        animationModifier.accept(this, "glb");
    }

    @Override
    AnimationNode[] fillAnimationNodes(AnimationModel item) {
        return fillAnimationNodesGlb(item.getChannels());
    }

    private AnimationNode[] fillAnimationNodesGlb(List<AnimationModel.Channel> channels) {
        var animationNodes = new AnimationNode[channels.size()];

        for (var channel : channels) {
            var node = channel.getNodeModel();
            animationNodes[nodeIdMap.computeIfAbsent(node.getName(), this::newNode)] = new AnimationNode(channels.stream().filter(c -> c.getNodeModel().equals(node)).toList(), node);
        }

        return animationNodes;
    }
}
