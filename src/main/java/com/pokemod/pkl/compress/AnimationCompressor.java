package com.pokemod.pkl.compress;

import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AnimationCompressor {

    public static void main(String[] args) throws IOException {
        Files.list(Paths.get("C:\\Users\\hydos\\Desktop\\AllPokemonSwSh\\pm0951_00_00\\Animations")).forEach(originalAnimation -> {
            if(!Files.isRegularFile(originalAnimation)) return;
            if(!originalAnimation.toFile().toString().endsWith(".smd")) return;

            try {
                var output = originalAnimation.getParent().resolve(originalAnimation.getFileName().toString().replace(".smd", "pkanm"));
                byte[] originalBytes = Files.readAllBytes(originalAnimation);

                var smd = new String(originalBytes);
                var lineSplit = smd.replace("\r", "").split("\n");
                var nodes = Arrays.copyOfRange(lineSplit, indexOf(lineSplit, "nodes", 0) + 1, indexOf(lineSplit, "end", 0));
                var skeletonInfo = Arrays.copyOfRange(lineSplit, indexOf(lineSplit, "skeleton", 0) + 1, indexOf(lineSplit, "end", 1));
                var endPosition = -1;

                var buf = ByteBuffer.wrap(new byte[originalBytes.length]);
                for (var node : nodes) {
                    var id = Integer.parseInt(node.split(" ")[0]);
                    var name = node.split(" ")[1];
                    var parentId = Integer.parseInt(node.split(" ")[2]);
                    if (id > 255 || parentId > 255) throw new RuntimeException("Cannot write more than 256 nodes.");

                    buf.put((byte) id);
                    writeString(buf, name);
                    buf.put((byte) parentId);
                }

                short[] largestValue = {0};
                var posNumberMap = new HashMap<Vector3f, Short>();
                var usePosMap = true;

                for (var s : skeletonInfo) {
                    var split = s.split(" ");
                    if (split.length == 2) continue;

                    var posX = Float.parseFloat(split[1]);
                    var posY = Float.parseFloat(split[2]);
                    var posZ = Float.parseFloat(split[3]);

                    if (largestValue[0] > Short.MAX_VALUE - 4) {
                        usePosMap = false;
                        break;
                    }

                    posNumberMap.computeIfAbsent(new Vector3f(posX, posY, posZ), aFloat -> largestValue[0]++);
                }

                var rotNumberMap = new HashMap<Vector3f, Short>();
                var useRotMap = true;
                largestValue[0] = 0;

                for (var s : skeletonInfo) {
                    var split = s.split(" ");
                    if (split.length == 2) continue;

                    var rotX = Float.parseFloat(split[4]);
                    var rotY = Float.parseFloat(split[5]);
                    var rotZ = Float.parseFloat(split[6]);

                    if (largestValue[0] > Short.MAX_VALUE - 4) {
                        useRotMap = false;
                        break;
                    }

                    rotNumberMap.computeIfAbsent(new Vector3f(rotX, rotY, rotZ), aFloat -> largestValue[0]++);
                }

                buf.put((byte) (usePosMap ? 1 : 0));
                buf.putShort((short) posNumberMap.size());
                for (var entry : posNumberMap.entrySet()) {
                    buf.putShort(entry.getValue());
                    buf.putFloat(entry.getKey().x());
                    buf.putFloat(entry.getKey().y());
                    buf.putFloat(entry.getKey().z());
                }

                buf.put((byte) (useRotMap ? 1 : 0));
                buf.putShort((short) rotNumberMap.size());
                for (var entry : rotNumberMap.entrySet()) {
                    buf.putShort(entry.getValue());
                    buf.putFloat(entry.getKey().x());
                    buf.putFloat(entry.getKey().y());
                    buf.putFloat(entry.getKey().z());
                }

                var currentTime = -1;
                for (var skeletonLine : skeletonInfo) {
                    var split = skeletonLine.split(" ");
                    if (split[0].equals("time")) {
                        currentTime = Integer.parseInt(split[1]);
                        buf.put((byte) 0); // 0 = time change
                        buf.putInt(currentTime);
                    } else {
                        var boneId = Integer.parseInt(split[0]);
                        buf.put((byte) 1); // 1 = bone change
                        buf.put((byte) boneId);

                        var posX = Float.parseFloat(split[1]);
                        var posY = Float.parseFloat(split[2]);
                        var posZ = Float.parseFloat(split[3]);
                        var pos = new Vector3f(posX, posY, posZ);

                        var rotX = Float.parseFloat(split[4]);
                        var rotY = Float.parseFloat(split[5]);
                        var rotZ = Float.parseFloat(split[6]);
                        var rot = new Vector3f(rotX, rotY, rotZ);

                        if (usePosMap) {
                            var cachedValue = posNumberMap.get(pos);
                            buf.putShort(cachedValue);
                        } else {
                            buf.putFloat(posX);
                            buf.putFloat(posY);
                            buf.putFloat(posZ);
                        }

                        if (useRotMap) {
                            var cachedValue = rotNumberMap.get(rot);
                            buf.putShort(cachedValue);
                        } else {
                            buf.putFloat(rotX);
                            buf.putFloat(rotY);
                            buf.putFloat(rotZ);
                        }

                        buf.putFloat(rotX);
                        buf.putFloat(rotY);
                        buf.putFloat(rotZ);
                    }
                }

                endPosition = buf.position();
                Files.deleteIfExists(output);
                Files.write(output, Arrays.copyOfRange(buf.array(), 0, endPosition));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static int similarity(Map<Vector3f, Short> posMap, Map<Vector3f, Short> rotMap) {
        var similarity = 0;

        for (var posEntry : posMap.entrySet()) {
            for (var rotEntry : rotMap.entrySet()) {
                if(posEntry.getKey().x == (rotEntry.getKey().x)) similarity++;
                if(posEntry.getKey().y == (rotEntry.getKey().y)) similarity++;
                if(posEntry.getKey().z == (rotEntry.getKey().z)) similarity++;
            }
        }

        return similarity;
    }

    private static void writeString(ByteBuffer newBytes, String str) {
        newBytes.putInt(str.length());
        for (var c : str.toCharArray()) newBytes.putChar(c);
    }

    private static int indexOf(String[] arr, String line, int skipCount) {
        int skipped = 0;

        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(line)) {
                if (skipped != skipCount) skipped++;
                else return i;
            }
        }

        return -1;
    }
}
