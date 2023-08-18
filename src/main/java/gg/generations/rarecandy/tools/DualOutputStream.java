package gg.generations.rarecandy.tools;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DualOutputStream extends PrintStream {
    private final PrintStream consolePrintStream;

    public DualOutputStream(Path logFolderPath) throws IOException {
        super(new BufferedOutputStream(Files.newOutputStream(createLogFile(logFolderPath))));
        consolePrintStream = System.out;
    }

    private static Path createLogFile(Path logFolderPath) throws IOException {
        Files.createDirectories(logFolderPath);
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String filename = "data-" + currentDateTime + ".log";
        return logFolderPath.resolve(filename);
    }

    @Override
    public void flush() {
        super.flush();
        consolePrintStream.flush();
    }

    @Override
    public void close() {
        super.close();
        consolePrintStream.close();
    }

    @Override
    public void write(int b) {
        super.write(b);
        consolePrintStream.write(b);
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) {
        super.write(b, off, len);
        consolePrintStream.write(b, off, len);
    }

    @Override
    public void write(@NotNull byte[] b) throws IOException {
        super.write(b);
        consolePrintStream.write(b);
    }
}