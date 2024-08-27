package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Skeleton;

import java.util.Map;

public interface AnimResource {
    Animation.AnimationNode[] getNodes(Skeleton skeleton);

    Map<String, Animation.Offset> getOffsets();

    public long fps();

    public static String cleanAnimName(Map.Entry<String, byte[]> entry) {
        var str = entry.getKey();
        return cleanAnimName(str);
    }

    public static String cleanAnimName(String str) {
        var substringEnd = str.lastIndexOf(".") == -1 ? str.length() : str.lastIndexOf(".");
        var substringStart = str.lastIndexOf("/") == -1 ? 0 : str.lastIndexOf("/");
        return str.substring(substringStart, substringEnd);
    }
}