package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.animation.Animation;

import java.nio.file.Path;
import java.util.stream.Stream;

public class AnimationExport {
    public static void export(Animation animation, Path path) {
        if(Stream.of(animation.getAnimationNodes()).anyMatch(a -> a.scaleKeys.values().length != 1)) {
            System.out.println("Warning animation has scaling. SMD doesn't work.");
            return;
        }

//        animation.nodeIdMap.
//
//        SMDFile smdFile = new SMDFile();
//
//        var nodes = new NodesBlock();
//
//
//        skeleton.keyframes.add();


    }
}
