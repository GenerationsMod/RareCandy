package gg.generations;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.components.AnimatedMeshObject;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import gg.generations.rarecandy.tools.TextureLoader;
import gg.generations.rarecandy.tools.gui.DialogueUtils;
import gg.generations.rarecandy.tools.pkcreator.PixelConverter;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.lwjgl.util.nfd.NativeFileDialog;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static gg.generations.rarecandy.tools.gui.GuiHandler.OPTIONS;

public class Testing {
    public static void main(String[] args) throws IOException {
        NativeFileDialog.NFD_Init();
        var list = DialogueUtils.chooseMultipleFiles("PK;pk");

        if(list == null) return;

        var base =  Path.of("D:\\models");

        var result = Path.of("main.fsrepo");

        if(Files.notExists(base))Files.createDirectory(base);

        try {
            for(var path : list) {
                PixelConverter.unpackPk(path, base.resolve(path.getFileName().toString().replace(".pk", "")));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        try (var stream = Files.walk(base).filter(Files::isRegularFile)) {
            List<Path> files = stream.toList();
            writeRepository(files, base, result);
        }
    }


    public static void writeRepository(List<Path> files, Path relativePath, Path output) throws IOException {
        if(Files.notExists(output.toAbsolutePath().getParent())) Files.createDirectories(output.toAbsolutePath().getParent());

        try (var xzWriter = new XZOutputStream(Files.newOutputStream(output), OPTIONS)) {
            try (var tarWriter = new TarArchiveOutputStream(xzWriter)) {
                for (var file : files) {
                    var entry = new TarArchiveEntry(file, relativePath.relativize(file).toString());
                    tarWriter.putArchiveEntry(entry);
                    if (Files.isRegularFile(file)) try (var is = new BufferedInputStream(Files.newInputStream(file))) {
                        IOUtils.copy(is, tarWriter);
                    }
                    tarWriter.closeArchiveEntry();

                    System.out.println("Packed: " + file);
                }
            }
        }
    }
}
