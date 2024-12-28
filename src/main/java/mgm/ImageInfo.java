package mgm;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageInfo {
    public int id;
    public BufferedImage image;
    public Color averageColor;
    public Color maxColor;
    public Color minColor;
    public boolean allowed;
    public String name;

    public ImageInfo(BufferedImage image, int id, String name, boolean allowed) {
        this.image = image;
        this.id = id;
        this.allowed = allowed;
        this.name = name;
        calculateColors();
    }

    private void calculateColors() {
        int totalPixels = image.getWidth() * image.getHeight();
        long sumR = 0, sumG = 0, sumB = 0;
        int maxR = 0, maxG = 0, maxB = 0;
        int minR = 255, minG = 255, minB = 255;

        // Iterate through each pixel in the image
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y));

                // Accumulate the RGB values for average calculation
                sumR += pixelColor.getRed();
                sumG += pixelColor.getGreen();
                sumB += pixelColor.getBlue();

                // Calculate the max RGB values
                maxR = Math.max(maxR, pixelColor.getRed());
                maxG = Math.max(maxG, pixelColor.getGreen());
                maxB = Math.max(maxB, pixelColor.getBlue());

                // Calculate the min RGB values
                minR = Math.min(minR, pixelColor.getRed());
                minG = Math.min(minG, pixelColor.getGreen());
                minB = Math.min(minB, pixelColor.getBlue());
            }
        }

        // Calculate the average color
        averageColor = new Color((int)(sumR / totalPixels), (int)(sumG / totalPixels), (int)(sumB / totalPixels));

        // Set the max and min colors
        maxColor = new Color(maxR, maxG, maxB);
        minColor = new Color(minR, minG, minB);
    }
}
