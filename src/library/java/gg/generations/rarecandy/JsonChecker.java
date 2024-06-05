package gg.generations.rarecandy;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonChecker {

    public static void main(String[] args) {
        String directoryPath = "D:\\Git Repos\\Generations-Core\\common\\src\\main\\resources\\data\\cobblemon\\spawn_pool_world";
        checkJsonFiles(directoryPath);
    }

    private static void checkJsonFiles(String directoryPath) {
        try {
            Files.walk(Paths.get(directoryPath), FileVisitOption.FOLLOW_LINKS)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(JsonChecker::processFile);
        } catch (IOException e) {
            System.out.println("Error accessing the directory: " + e.getMessage());
        }
    }

    private static void processFile(Path filePath) {
        try {
            JsonElement element = JsonParser.parseReader(Files.newBufferedReader(filePath));
            System.out.println(filePath.toAbsolutePath() + " - Succeed");
        } catch (JsonParseException | IOException e) {
            System.out.println(filePath.toAbsolutePath() + " - Fail");
        }
    }
}