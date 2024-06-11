package gg.generations.rarecandy.tools;

import java.io.File;
import java.util.*;

public class FolderNameOrganizer {

    public static Map<String, Set<String>> organizeFolderNames(String directoryPath) {
        Map<String, Set<String>> folderMap = new HashMap<>();

        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Invalid directory path");
            return folderMap;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String[] parts = file.getName().split("_", 2);
                    if (parts.length == 2) {
                        String key = parts[0];
                        String value = parts[1];

                        Set<String> valuesSet = folderMap.getOrDefault(key, new HashSet<>());
                        valuesSet.add(value);
                        folderMap.put(key, valuesSet);
                    }
                }
            }
        }

        return folderMap;
    }

    public static void main(String[] args) {
        String directoryPath = "C:\\Users\\water\\Downloads\\vivillon";
        Map<String, Set<String>> folderMap = organizeFolderNames(directoryPath);

        // Print the organized folder names
        for (Map.Entry<String, Set<String>> entry : folderMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Values: " + entry.getValue());
        }
    }
}