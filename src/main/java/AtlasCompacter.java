import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.IModelConfig;
import gg.generations.rarecandy.pokeutils.resource.ResourceLocator;
import gg.generations.rarecandy.tools.gui.DialogueUtils;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AtlasCompacter {
    private static final int ATLAS_SIZE = 1024;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
            "0.000000",
            DecimalFormatSymbols.getInstance(Locale.ROOT)
    );

    public static void main(String[] args) {
        DialogueUtils.init();
        DialogueUtils.chooseFolder("", AtlasCompacter::runThingy);

    }

    private static void runThingy(Path path) {
        if(path == null) {
            System.exit(0);
        }

        ResourceLocator locator = null;
        try {
            locator = ResourceLocator.of(path);
            var config = IModelConfig.GSON.fromJson(new InputStreamReader(locator.getInputStream("config.json")), JsonObject.class);


            var atlasBuild = AtlasCompacter.run(locator);
            var materialTransforms = MaterialCompressor.applyAtlasTextures(config, atlasBuild);
            MaterialCompressor.applyAtlasTransformsToDefaultVariant(config, materialTransforms);
            MaterialCompressor.applyAtlasTransformsToAllVariants(config, materialTransforms);
            MaterialCompressor.removeDuplicateMaterialsAfterAtlas(config, atlasBuild);
            locator.putFile("config.json", IModelConfig.GSON.toJson(config).getBytes(StandardCharsets.UTF_8));
            locator.save();
            MaterialCompressor.deleteCompactedImages(locator, atlasBuild);
        } catch (IOException e) {

        }

        DialogueUtils.chooseFolder("", AtlasCompacter::runThingy);
    }

    public static AtlasBuild run(ResourceLocator locator) throws IOException {

        var loadResult = loadImages(locator);
        var atlases = packImages(loadResult.packableImages());
        writeAtlasImages(locator, atlases);
        return new AtlasBuild(List.copyOf(atlases), buildPackedTextures(atlases));
    }

    private static LoadResult loadImages(ResourceLocator locator) {
        var packableImages = new ArrayList<ImageEntry>();
        var skippedImages = new ArrayList<SkippedImage>();

        try {
            for (String path : locator.getFileNames()) {
                BufferedImage image;

                image = ImageIO.read(locator.getInputStream(path));
                if (image == null) {
//                    System.err.println("Skipping non-image file: " + path);
                    continue;
                }

                int width = image.getWidth();
                int height = image.getHeight();
                if (width == ATLAS_SIZE && height == ATLAS_SIZE) {
                    skippedImages.add(new SkippedImage(path, width, height, "already_1024x1024"));
                    continue;
                }

                if (width > ATLAS_SIZE || height > ATLAS_SIZE) {

                    if(width == 2048 && height == 1024) {
                        packableImages.add(new ImageEntry(path, image, 1024, 512));
                    } else if(width == 2048 && height == 2048) {
                        packableImages.add(new ImageEntry(path, image, 1024, 1024));
                    } else {
                            throw new RuntimeException(
                                    "Image is larger than the atlas size and cannot be packed: "
                                            + path
                                            + " ("
                                            + width
                                            + "x"
                                            + height
                                            + ")"
                            );
                        }
                } else {
                    packableImages.add(new ImageEntry(path, image, width, height));
                }
            }

            packableImages.sort(
                    Comparator.comparingInt(ImageEntry::area).reversed()
                            .thenComparing(Comparator.comparingInt(ImageEntry::maxDimension).reversed())
                            .thenComparing(ImageEntry::name)
            );


        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read image: "/* + path*/, exception);
        }

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
                    throw new IllegalStateException("Failed to place image in a new atlas: " + image.name());
                }
                atlases.add(atlas);

                continue;
            }

            Placement placement = atlases.get(bestCandidate.atlasIndex()).insert(image);
            if (placement == null) {
                throw new IllegalStateException("Packing state changed unexpectedly for image: " + image.name());
            }
        }

        return atlases;
    }

    private static void  writeAtlasImages(ResourceLocator locator, List<Atlas> atlases) throws IOException {
        for (Atlas atlas : atlases) {
            BufferedImage atlasImage = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = atlasImage.createGraphics();
            graphics.setComposite(AlphaComposite.Src);

            for (Placement placement : atlas.placements()) {
                graphics.drawImage(placement.image().image(), placement.x(), placement.y(), placement.width(), placement.height(), null);
            }

            graphics.dispose();

            var outputStream = new ByteArrayOutputStream();

            ImageIO.write(atlasImage, "PNG", outputStream);

            locator.putFile(atlas.fileName(), outputStream.toByteArray());
        }
    }

    private static Map<String, PackedTexture> buildPackedTextures(List<Atlas> atlases) {
        var packedTextures = new LinkedHashMap<String, PackedTexture>();

        for (Atlas atlas : atlases) {
            for (Placement placement : atlas.placements()) {
                packedTextures.put(
                        placement.image().name(),
                        new PackedTexture(
                                placement.image().name(),
                                atlas.fileName(),
                                placement.x(),
                                placement.y(),
                                placement.width(),
                                placement.height()
                        )
                );
            }
        }

        return Map.copyOf(packedTextures);
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

    private record DialogSelection(List<String> inputFiles, ResourceLocator outputDirectory) {
    }

    public record AtlasBuild(List<Atlas> atlases, Map<String, PackedTexture> packedTextures) {
    }

    public record PackedTexture(String sourceName, String atlasFileName, int x, int y, int width, int height) {
        public float scaleX() {
            return width / (float) ATLAS_SIZE;
        }

        public float scaleY() {
            return height / (float) ATLAS_SIZE;
        }

        public float offsetX() {
            return x / (float) ATLAS_SIZE;
        }

        public float offsetY() {
            return y / (float) ATLAS_SIZE;
        }
    }

    private record LoadResult(List<ImageEntry> packableImages, List<SkippedImage> skippedImages) {
    }

    private record ImageEntry(String name, BufferedImage image, int width, int height) {
        private int area() {
            return width * height;
        }

        private int maxDimension() {
            return Math.max(width, height);
        }
    }

    private record SkippedImage(String path, int width, int height, String reason) {
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

    public static final class Atlas {
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
