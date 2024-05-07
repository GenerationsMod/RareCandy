import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UniqueColorCounter {

        // Count the number of unique colors in the BufferedImage
        public static int countUniqueColors(BufferedImage image) {
            Set<Integer> uniqueColors = new HashSet<>();

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);
                    uniqueColors.add(rgb);
                }
            }

            return uniqueColors.size();
        }

        public static void main(String[] args) throws IOException {
            // Example usage
            BufferedImage image = ImageIO.read(new File("D:\\Git Repos\\RareCandy\\src\\main\\resources\\textures\\burnt_concrete.png"));
            // Populate image with data

            int uniqueColorCount = countUniqueColors(image);
            System.out.println("Number of unique colors: " + uniqueColorCount);
        }
    }