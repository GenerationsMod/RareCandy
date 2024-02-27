package gg.generations.rarecandy.tools;

import dev.thecodewarrior.binarysmd.BinarySMD;
import dev.thecodewarrior.binarysmd.formats.SMDBinaryWriter;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import dev.thecodewarrior.binarysmd.studiomdl.NodesBlock;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import gg.generations.rarecandy.pokeutils.smdi.BoneStateT;
import gg.generations.rarecandy.pokeutils.smdi.BoneT;
import gg.generations.rarecandy.pokeutils.smdi.KeyframeT;
import gg.generations.rarecandy.pokeutils.smdi.SMDIT;
import org.jetbrains.annotations.NotNull;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SmdConvert {
    public static void main(String[] args) throws IOException {
        var path = "D:\\Git Repos\\RareCandy\\run\\converter\\in\\pm0245_00_00_00280_sleep01_start.gfbanm.smd";
        var smdFile = new SMDTextReader().read(Files.readString(Path.of(path)));

        SkeletonBlock skeleton;
        NodesBlock nodes;

        var smdi = new SMDIT();

        for(var block : smdFile.blocks) {
            if(block instanceof SkeletonBlock skeletonBlock) {
                @NotNull List<SkeletonBlock.@NotNull Keyframe> keyframeList = skeletonBlock.keyframes;
                var keyframes = new KeyframeT[keyframeList.size()];
                for (int i = 0; i < keyframeList.size(); i++) {
                    var keyframe = keyframeList.get(i);
                    var kf = keyframes[i] = new KeyframeT();
                    kf.setTime(keyframe.time);

                    var states = new BoneStateT[keyframe.states.size()];

                    for (int j = 0; j < keyframe.states.size(); j++) {
                        var state = keyframe.states.get(j);
                        var s = states[j] = new BoneStateT();
                        s.setBone(state.bone);
                        s.setPosX(state.posX);
                        s.setPosY(state.posY);
                        s.setPosZ(state.posZ);
                        s.setRotX(state.rotX);
                        s.setRotY(state.rotY);
                        s.setRotZ(state.rotZ);
                    }

                    kf.setStates(states);
                }

                smdi.setKeyframes(keyframes);
            } else if(block instanceof NodesBlock skeletonBlock) {
                var bones = new BoneT[skeletonBlock.bones.size()];
                for (int i = 0; i < bones.length; i++) {
                    var bone = skeletonBlock.bones.get(i);
                    var b = bones[i] = new BoneT();
                    b.setId(bone.id);
                    b.setName(bone.name);
                    b.setParent(bone.parent);
                }

                smdi.setBones(bones);
            }
        }

        Files.write(Path.of(path.replace(".smd", ".smdi")), smdi.serializeToBinary());

        new SMDBinaryWriter().write(smdFile, MessagePack.newDefaultPacker(Files.newOutputStream(Path.of(path.replace(".smd", ".smdx")))));
    }
}
