package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileParser {

    public static String[] parse() throws IOException {
        Path filePath = Path.of(Main.FILE_PATH);
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + Main.FILE_PATH);
        }

        List<String> lines = Files.readAllLines(filePath);
        return lines.toArray(new String[0]);
    }
}
