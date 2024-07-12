package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.renderer.LoggerUtil;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SubfolderContentsComparator {

    public static void compareSubfolderContents(String mainFolderPath) {
        Map<String, Set<String>> subfolderContentsMap = new HashMap<>();

        File mainFolder = new File(mainFolderPath);
        if (!mainFolder.exists() || !mainFolder.isDirectory()) {
            System.err.println("Invalid directory path: " + mainFolderPath);
            return;
        }

        File[] subFolders = mainFolder.listFiles(File::isDirectory);
        if (subFolders != null) {
            for (File subFolder : subFolders) {
                Set<String> filesSet = new HashSet<>();
                File[] files = subFolder.listFiles(File::isFile);
                if (files != null) {
                    for (File file : files) {
                        filesSet.add(file.getName());
                    }
                }
                subfolderContentsMap.put(subFolder.getName(), filesSet);
            }
        }

        // Find common files among subfolders
        Set<String> commonFiles = new HashSet<>(subfolderContentsMap.get(subfolderContentsMap.keySet().iterator().next()));
        for (Set<String> filesSet : subfolderContentsMap.values()) {
            commonFiles.retainAll(filesSet);
        }

        // Find unique files for each subfolder
        Map<String, Set<String>> uniqueFilesMap = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : subfolderContentsMap.entrySet()) {
            Set<String> uniqueFiles = new HashSet<>(entry.getValue());
            uniqueFiles.removeAll(commonFiles);
            uniqueFilesMap.put(entry.getKey(), uniqueFiles);
        }

        LoggerUtil.print("Common Files: " + commonFiles);
        LoggerUtil.print("Unique Files per Subfolder:");
        for (Map.Entry<String, Set<String>> entry : uniqueFilesMap.entrySet()) {
            LoggerUtil.print("Subfolder: " + entry.getKey() + ", Unique Files: " + entry.getValue());
        }
    }

    public static void main(String[] args) {
        String mainFolderPath = "C:\\Users\\water\\Downloads\\vivillon";
        compareSubfolderContents(mainFolderPath);
    }
}
