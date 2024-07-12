package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.renderer.LoggerUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModelGlbComparator {

    public static Map<String, Set<String>> compareModelGlbFiles(String mainFolderPath) {
        Map<String, Set<String>> identicalModelsMap = new HashMap<>();

        File mainFolder = new File(mainFolderPath);
        if (!mainFolder.exists() || !mainFolder.isDirectory()) {
            System.err.println("Invalid directory path: " + mainFolderPath);
            return identicalModelsMap;
        }

        File[] subFolders = mainFolder.listFiles(File::isDirectory);
        if (subFolders != null) {
            for (int i = 0; i < subFolders.length; i++) {
                File folder1 = subFolders[i];
                File glbFile1 = new File(folder1, "model.glb");
                if (glbFile1.exists()) {
                    String hash1 = hashFile(glbFile1);

                    for (int j = i + 1; j < subFolders.length; j++) {
                        File folder2 = subFolders[j];
                        File glbFile2 = new File(folder2, "model.glb");
                        if (glbFile2.exists()) {
                            String hash2 = hashFile(glbFile2);

                            if (hash1.equals(hash2)) {
                                identicalModelsMap.computeIfAbsent(hash1, k -> new HashSet<>()).add(folder1.getName());
                                identicalModelsMap.get(hash1).add(folder2.getName());
                            }
                        }
                    }
                }
            }
        }

        return identicalModelsMap;
    }

    private static String hashFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String mainFolderPath = "C:\\Users\\water\\Downloads\\vivillon";
        Map<String, Set<String>> identicalModelsMap = compareModelGlbFiles(mainFolderPath);

        // Organize folder names based on identical model.glb files
        LoggerUtil.print("Folders with Identical model.glb Files:");
        for (Set<String> folders : identicalModelsMap.values()) {
            LoggerUtil.print("Folders: " + folders);
        }
    }
}