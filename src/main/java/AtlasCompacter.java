import gg.generations.rarecandy.tools.gui.DialogueUtils;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class AtlasCompacter {
    private static final int ATLAS_SIZE = 1024;
    private static final String IMAGE_FILTER = "Images;png,jpg,jpeg,bmp,gif,tif,tiff,webp";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
            "0.000000",
            DecimalFormatSymbols.getInstance(Locale.ROOT)
    );

    public static void main(String[] args) {
        try {
            run();
        } catch (Exception exception) {
            System.err.println("Atlas compaction failed: " + exception.getMessage());
            exception.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void run() throws IOException {
        DialogSelection selection = chooseSelectionWithDialog();
        if (selection == null) {
            System.out.println("No images were selected.");
            return;
        }

        Files.createDirectories(selection.outputDirectory());

        var loadResult = loadImages(selection.inputFiles());
        var atlases = packImages(loadResult.packableImages());
        writeAtlasImages(selection.outputDirectory(), atlases);
        writeManifest(selection.outputDirectory(), atlases, loadResult.skippedImages());
        printReport(selection.outputDirectory(), atlases, loadResult.skippedImages());
    }

    private static DialogSelection chooseSelectionWithDialog() {
        DialogueUtils.init();
        try {
            var selectedFilesFuture = new CompletableFuture<List<Path>>();
            DialogueUtils.chooseMultipleFiles(
                    "Choose images",
                    Path.of("").toAbsolutePath().normalize().toString(),
                    IMAGE_FILTER,
                    paths -> selectedFilesFuture.complete(paths == null ? List.of() : List.copyOf(paths))
            );

            List<Path> selectedFiles = selectedFilesFuture.join();
            if (selectedFiles.isEmpty()) {
                return null;
            }

            var outputDirectoryFuture = new CompletableFuture<Path>();
            DialogueUtils.chooseFolder(Path.of("").toAbsolutePath().normalize().toString(), outputDirectoryFuture::complete);

            Path outputDirectory = outputDirectoryFuture.join();
            if (outputDirectory == null) {
                return null;
            }

            return new DialogSelection(
                    selectedFiles.stream()
                            .map(path -> path.toAbsolutePath().normalize())
                            .distinct()
                            .toList(),
                    outputDirectory.toAbsolutePath().normalize()
            );
        } finally {
            DialogueUtils.quit();
        }
    }

    private static LoadResult loadImages(List<Path> paths) {
        var packableImages = new ArrayList<ImageEntry>();
        var skippedImages = new ArrayList<SkippedImage>();

        for (Path path : paths) {
            BufferedImage image;
            try {
                image = ImageIO.read(path.toFile());
            } catch (IOException exception) {
                throw new UncheckedIOException("Failed to read image: " + path, exception);
            }

            if (image == null) {
                System.err.println("Skipping non-image file: " + path);
                continue;
            }

            int width = image.getWidth();
            int height = image.getHeight();

            if (width == ATLAS_SIZE && height == ATLAS_SIZE) {
                skippedImages.add(new SkippedImage(path, width, height, "already_1024x1024"));
                continue;
            }

            if (width > ATLAS_SIZE || height > ATLAS_SIZE) {
                throw new IllegalArgumentException(
                        "Image is larger than the atlas size and cannot be packed: "
                                + path
                                + " ("
                                + width
                                + "x"
                                + height
                                + ")"
                );
            }

            packableImages.add(new ImageEntry(path, image, width, height));
        }

        packableImages.sort(
                Comparator.comparingInt(ImageEntry::area).reversed()
                        .thenComparing(Comparator.comparingInt(ImageEntry::maxDimension).reversed())
                        .thenComparing(image -> image.path().toString())
        );

        return new LoadResult(packableImages, skippedImages);
    }

    private static List<Atlas> packImages(List<ImageEntry> images) {
        var atlases = new ArrayList<Atlas>();

        for (ImageEntry image : images) {
            AtlasCandidate bestCandidate = null;

            for (Atlas atlas : atlases) {
                AtlasCandidate candidate = atlas.preview(image);
                if (candidate != null && (bestCandidate == null || candidate.isBetterThan(bestCandidate))) {
                    bestCandidate = candidate;
                }
            }

            if (bestCandidate == null) {
                Atlas atlas = new Atlas(atlases.size());
                Placement placement = atlas.insert(image);
                if (placement == null) {
                    throw new IllegalStateException("Failed to place image in a new atlas: " + image.path());
                }
                atlases.add(atlas);
                continue;
            }

            Placement placement = atlases.get(bestCandidate.atlasIndex()).insert(image);
            if (placement == null) {
                throw new IllegalStateException("Packing state changed unexpectedly for image: " + image.path());
            }
        }

        return atlases;
    }

    private static void writeAtlasImages(Path outputDirectory, List<Atlas> atlases) throws IOException {
        for (Atlas atlas : atlases) {
            BufferedImage atlasImage = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = atlasImage.createGraphics();
            graphics.setComposite(AlphaComposite.Src);

            for (Placement placement : atlas.placements()) {
                graphics.drawImage(placement.image().image(), placement.x(), placement.y(), null);
            }

            graphics.dispose();

            Path outputPath = outputDirectory.resolve(atlas.fileName());
            ImageIO.write(atlasImage, "PNG", outputPath.toFile());
        }
    }

    private static void writeManifest(Path outputDirectory, List<Atlas> atlases, List<SkippedImage> skippedImages) throws IOException {
        Path manifestPath = outputDirectory.resolve("atlas_manifest.csv");
        var lines = new ArrayList<String>();
        lines.add("source,status,atlas,x,y,width,height,scale_x,scale_y,offset_x,offset_y");

        for (Atlas atlas : atlases) {
            List<Placement> placements = new ArrayList<>(atlas.placements());
            placements.sort(Comparator.comparingInt(Placement::y).thenComparingInt(Placement::x));

            for (Placement placement : placements) {
                lines.add(String.join(",",
                        escapeCsv(displayPath(placement.image().path())),
                        "packed",
                        atlas.fileName(),
                        Integer.toString(placement.x()),
                        Integer.toString(placement.y()),
                        Integer.toString(placement.width()),
                        Integer.toString(placement.height()),
                        formatDecimal(placement.scaleX()),
                        formatDecimal(placement.scaleY()),
                        formatDecimal(placement.offsetX()),
                        formatDecimal(placement.offsetY())
                ));
            }
        }

        for (SkippedImage skippedImage : skippedImages) {
            lines.add(String.join(",",
                    escapeCsv(displayPath(skippedImage.path())),
                    skippedImage.reason(),
                    "",
                    "",
                    "",
                    Integer.toString(skippedImage.width()),
                    Integer.toString(skippedImage.height()),
                    "",
                    "",
                    "",
                    ""
            ));
        }

        Files.write(manifestPath, lines, StandardCharsets.UTF_8);
    }

    private static void printReport(Path outputDirectory, List<Atlas> atlases, List<SkippedImage> skippedImages) {
        int packedCount = atlases.stream().mapToInt(atlas -> atlas.placements().size()).sum();
        System.out.println("Packed " + packedCount + " image(s) into " + atlases.size() + " atlas(es).");
        System.out.println("Output directory: " + outputDirectory.toAbsolutePath().normalize());

        for (Atlas atlas : atlases) {
            double usagePercent = (atlas.usedArea() * 100.0) / (ATLAS_SIZE * ATLAS_SIZE);
            System.out.println();
            System.out.println(atlas.fileName() + " - " + atlas.placements().size() + " image(s), "
                    + formatDecimal(usagePercent) + "% filled");

            List<Placement> placements = new ArrayList<>(atlas.placements());
            placements.sort(Comparator.comparingInt(Placement::y).thenComparingInt(Placement::x));

            for (Placement placement : placements) {
                System.out.println("  " + displayPath(placement.image().path())
                        + " -> pos=(" + placement.x() + "," + placement.y() + ")"
                        + " size=(" + placement.width() + "x" + placement.height() + ")"
                        + " scale=(" + formatDecimal(placement.scaleX()) + "," + formatDecimal(placement.scaleY()) + ")"
                        + " offset=(" + formatDecimal(placement.offsetX()) + "," + formatDecimal(placement.offsetY()) + ")");
            }
        }

        if (!skippedImages.isEmpty()) {
            System.out.println();
            System.out.println("Skipped " + skippedImages.size() + " image(s) that were already 1024x1024:");
            for (SkippedImage skippedImage : skippedImages) {
                System.out.println("  " + displayPath(skippedImage.path()));
            }
        }

        System.out.println();
        System.out.println("Manifest written to " + outputDirectory.resolve("atlas_manifest.csv").toAbsolutePath().normalize());
    }

    private static String displayPath(Path path) {
        Path absolutePath = path.toAbsolutePath().normalize();
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();

        if (absolutePath.startsWith(workingDirectory)) {
            return workingDirectory.relativize(absolutePath).toString();
        }

        return absolutePath.toString();
    }

    private static String formatDecimal(double value) {
        return DECIMAL_FORMAT.format(value);
    }

    private static String escapeCsv(String value) {
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private record DialogSelection(List<Path> inputFiles, Path outputDirectory) {
    }

    private record LoadResult(List<ImageEntry> packableImages, List<SkippedImage> skippedImages) {
    }

    private record ImageEntry(Path path, BufferedImage image, int width, int height) {
        private int area() {
            return width * height;
        }

        private int maxDimension() {
            return Math.max(width, height);
        }
    }

    private record SkippedImage(Path path, int width, int height, String reason) {
    }

    private record Placement(ImageEntry image, int atlasIndex, int x, int y, int width, int height) {
        private double scaleX() {
            return width / (double) ATLAS_SIZE;
        }

        private double scaleY() {
            return height / (double) ATLAS_SIZE;
        }

        private double offsetX() {
            return x / (double) ATLAS_SIZE;
        }

        private double offsetY() {
            return y / (double) ATLAS_SIZE;
        }
    }

    private record AtlasCandidate(int atlasIndex, Rect rect, int shortSideFit, int longSideFit) {
        private boolean isBetterThan(AtlasCandidate other) {
            if (shortSideFit != other.shortSideFit) {
                return shortSideFit < other.shortSideFit;
            }
            if (longSideFit != other.longSideFit) {
                return longSideFit < other.longSideFit;
            }
            if (rect.y() != other.rect.y()) {
                return rect.y() < other.rect.y();
            }
            if (rect.x() != other.rect.x()) {
                return rect.x() < other.rect.x();
            }
            return atlasIndex < other.atlasIndex;
        }
    }

    private record Rect(int x, int y, int width, int height) {
        private boolean contains(Rect other) {
            return other.x >= x
                    && other.y >= y
                    && other.x + other.width <= x + width
                    && other.y + other.height <= y + height;
        }
    }

    private static final class Atlas {
        private final int index;
        private final List<Rect> freeRectangles = new ArrayList<>();
        private final List<Placement> placements = new ArrayList<>();

        private Atlas(int index) {
            this.index = index;
            freeRectangles.add(new Rect(0, 0, ATLAS_SIZE, ATLAS_SIZE));
        }

        private AtlasCandidate preview(ImageEntry image) {
            return scoreRect(image.width(), image.height());
        }

        private Placement insert(ImageEntry image) {
            AtlasCandidate candidate = scoreRect(image.width(), image.height());
            if (candidate == null) {
                return null;
            }

            placeRect(candidate.rect());
            Placement placement = new Placement(
                    image,
                    index,
                    candidate.rect().x(),
                    candidate.rect().y(),
                    image.width(),
                    image.height()
            );
            placements.add(placement);
            return placement;
        }

        private AtlasCandidate scoreRect(int width, int height) {
            AtlasCandidate bestCandidate = null;

            for (Rect freeRectangle : freeRectangles) {
                if (freeRectangle.width() < width || freeRectangle.height() < height) {
                    continue;
                }

                int leftoverHoriz = freeRectangle.width() - width;
                int leftoverVert = freeRectangle.height() - height;
                int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
                int longSideFit = Math.max(leftoverHoriz, leftoverVert);

                Rect rect = new Rect(freeRectangle.x(), freeRectangle.y(), width, height);
                AtlasCandidate candidate = new AtlasCandidate(index, rect, shortSideFit, longSideFit);
                if (bestCandidate == null || candidate.isBetterThan(bestCandidate)) {
                    bestCandidate = candidate;
                }
            }

            return bestCandidate;
        }

        private void placeRect(Rect usedRect) {
            for (int i = 0; i < freeRectangles.size(); i++) {
                Rect freeRect = freeRectangles.get(i);
                if (!splitFreeRect(freeRect, usedRect)) {
                    continue;
                }

                freeRectangles.remove(i);
                i--;
            }

            pruneFreeList();
        }

        private boolean splitFreeRect(Rect freeRect, Rect usedRect) {
            if (!intersects(freeRect, usedRect)) {
                return false;
            }

            if (usedRect.x() < freeRect.x() + freeRect.width()
                    && usedRect.x() + usedRect.width() > freeRect.x()) {
                if (usedRect.y() > freeRect.y() && usedRect.y() < freeRect.y() + freeRect.height()) {
                    freeRectangles.add(new Rect(
                            freeRect.x(),
                            freeRect.y(),
                            freeRect.width(),
                            usedRect.y() - freeRect.y()
                    ));
                }

                int usedBottom = usedRect.y() + usedRect.height();
                int freeBottom = freeRect.y() + freeRect.height();
                if (usedBottom < freeBottom) {
                    freeRectangles.add(new Rect(
                            freeRect.x(),
                            usedBottom,
                            freeRect.width(),
                            freeBottom - usedBottom
                    ));
                }
            }

            if (usedRect.y() < freeRect.y() + freeRect.height()
                    && usedRect.y() + usedRect.height() > freeRect.y()) {
                if (usedRect.x() > freeRect.x() && usedRect.x() < freeRect.x() + freeRect.width()) {
                    freeRectangles.add(new Rect(
                            freeRect.x(),
                            freeRect.y(),
                            usedRect.x() - freeRect.x(),
                            freeRect.height()
                    ));
                }

                int usedRight = usedRect.x() + usedRect.width();
                int freeRight = freeRect.x() + freeRect.width();
                if (usedRight < freeRight) {
                    freeRectangles.add(new Rect(
                            usedRight,
                            freeRect.y(),
                            freeRight - usedRight,
                            freeRect.height()
                    ));
                }
            }

            return true;
        }

        private void pruneFreeList() {
            for (int i = 0; i < freeRectangles.size(); i++) {
                Rect current = freeRectangles.get(i);
                boolean removedCurrent = false;

                for (int j = i + 1; j < freeRectangles.size(); j++) {
                    Rect other = freeRectangles.get(j);

                    if (current.contains(other)) {
                        freeRectangles.remove(j);
                        j--;
                        continue;
                    }

                    if (other.contains(current)) {
                        freeRectangles.remove(i);
                        i--;
                        removedCurrent = true;
                        break;
                    }
                }

                if (removedCurrent) {
                    continue;
                }
            }
        }

        private boolean intersects(Rect first, Rect second) {
            return first.x() < second.x() + second.width()
                    && first.x() + first.width() > second.x()
                    && first.y() < second.y() + second.height()
                    && first.y() + first.height() > second.y();
        }

        private String fileName() {
            return "atlas_" + String.format(Locale.ROOT, "%02d", index) + ".png";
        }

        private List<Placement> placements() {
            return List.copyOf(placements);
        }

        private int usedArea() {
            return placements.stream().mapToInt(placement -> placement.width() * placement.height()).sum();
        }
    }
}
