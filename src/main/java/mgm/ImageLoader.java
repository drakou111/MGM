package mgm;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.jar.JarFile;

public class ImageLoader {

    private static List<ImageInfo> getImagesFromFiles(List<String> imageFiles, int startId, boolean allow, String path) {
        int id = startId;
        List<ImageInfo> images = new ArrayList<>();
        for (String imageFileName : imageFiles) {
            // Ensure the path starts with a slash
            String newPath = "/" + imageFileName.replace("src\\main\\resources\\", "").replace("\\", "/");

            try (InputStream inputStream = ImageLoader.class.getResourceAsStream(newPath)) {
                if (inputStream != null) {
                    BufferedImage image = ImageIO.read(inputStream);
                    if (image != null) {
                        // Check if the image is grayscale
                        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY || image.getType() == BufferedImage.TYPE_USHORT_GRAY) {
                            image = convertToRGB(image); // Convert grayscale to RGB
                        }
                        String blockName = newPath.replace("/" + path + "/","").replace(".png","").replace("_", " ");
                        blockName = capitalizeFirstLetterOfEachWord(blockName);

                        images.add(new ImageInfo(image, id, blockName, allow));
                        id++;
                    } else {
                        System.err.println("Failed to read image from " + newPath);
                    }
                } else {
                    System.err.println("Resource not found: " + newPath);
                }
            } catch (IOException e) {
                System.err.println("Error loading image from resource: " + newPath);
                e.printStackTrace();
            }
        }
        return images;
    }
    public static List<ImageInfo> loadImages() {
        List<ImageInfo> images = new ArrayList<>();

        int id = 0;

        try {
            // Get all image files in the "blocks" folder (Works for JAR or file system)
            List<String> imageFiles = getAllFilesInResourceFolder("blocks");
            List<String> badImageFiles = getAllFilesInResourceFolder("odds");

            images.addAll(getImagesFromFiles(imageFiles, 0, true, "blocks"));
            int idOffset = images.size();
            images.addAll(getImagesFromFiles(badImageFiles, idOffset, false, "odds"));

        } catch (IOException e) {
            System.err.println("Error loading image files list.");
            e.printStackTrace();
        }

        return images;
    }

    public static String capitalizeFirstLetterOfEachWord(String input) {
        // Split the input string into words
        String[] words = input.split("\\s+");

        // Capitalize the first letter of each word
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                // Capitalize the first letter and append the rest of the word
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1)).append(" ");
            }
        }

        // Remove the trailing space and return the result
        return result.toString().trim();
    }

    private static BufferedImage convertToRGB(BufferedImage grayscaleImage) {
        int width = grayscaleImage.getWidth();
        int height = grayscaleImage.getHeight();

        // Create a new RGB image
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Loop through each pixel in the grayscale image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the grayscale value (0-255)
                int gray = grayscaleImage.getRaster().getSample(x, y, 0);

                // Set the same value for R, G, and B channels
                int rgb = (gray << 16) | (gray << 8) | gray;

                // Set the pixel in the new image
                rgbImage.setRGB(x, y, rgb);
            }
        }

        return rgbImage;
    }

    public static List<String> getAllFilesInResourceFolder(String folderName) throws IOException {
        List<String> filePaths = new ArrayList<>();
        String path = "src/main/resources/" + folderName;

        // Check if running from a JAR or file system
        if (isRunningFromJar()) {
            // If it's running from a JAR, use JarFile to access resources
            try (JarFile jarFile = getJarFile()) {
                jarFile.stream()
                        .filter(entry -> entry.getName().startsWith(folderName))
                        .forEach(entry -> filePaths.add(entry.getName()));
            }
        } else {
            // If it's running from the file system, use Files.list() to get resources
            if (Files.exists(Paths.get(path))) {
                filePaths.addAll(Files.list(Paths.get(path))
                        .map(pathItem -> pathItem.toString().replace("file:", ""))
                        .collect(Collectors.toList()));
            }
        }

        return filePaths;
    }

    // Check if the application is running from a JAR file
    private static boolean isRunningFromJar() {
        return ImageLoader.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith(".jar");
    }

    // Get the current JAR file dynamically (if running from a JAR)
    private static JarFile getJarFile() throws IOException {
        String jarPath = ImageLoader.class.getProtectionDomain().getCodeSource().getLocation().toString();
        // Remove "file:" prefix
        jarPath = jarPath.substring(5);
        return new JarFile(jarPath);
    }
}
