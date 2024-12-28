package mgm;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BlockGradient {
    public List<ImageInfo> allBlocks;

    public BlockGradient() {
        allBlocks = ImageLoader.loadImages();
    }

    public ArrayList<ImageInfo> getGradient(int noiseTolerance, int gradientLength, boolean removeDuplicates, List<Color> colors) {
        ArrayList<ImageInfo> gradient = new ArrayList<>();
        for (int i = 0; i < gradientLength; i++) {
            float percentage = (float) i / (gradientLength - 1);
            Color color = getColorAt(percentage, colors);

            ImageInfo closest = getClosestImageToColor(color, noiseTolerance);
            if (removeDuplicates && gradient.size() > 0 && gradient.get(gradient.size() - 1).id == closest.id)
                continue;

            gradient.add(closest);
        }
        return gradient;
    }

    public ImageInfo getClosestImageToColor(Color color, int noiseTolerance) {
        // Linear search for closest color match
        ImageInfo closestImage = null;
        float minDistance = Float.MAX_VALUE;

        for (ImageInfo imageInfo : allBlocks) {

            if (!imageInfo.allowed)
                continue;

            float noiseDistance = colorDistance(imageInfo.maxColor, imageInfo.minColor);
            if (noiseDistance > 3 * noiseTolerance * noiseTolerance)
                continue;

            float distance = colorDistance(color, imageInfo.averageColor);

            if (distance < minDistance) {
                minDistance = distance;
                closestImage = imageInfo;
            }
        }
        return closestImage;
    }

    private float colorDistance(Color c1, Color c2) {
        int dr = c1.getRed() - c2.getRed();
        int dg = c1.getGreen() - c2.getGreen();
        int db = c1.getBlue() - c2.getBlue();
        return dr * dr + dg * dg + db * db;  // Avoid square roots for faster comparison
    }

    public Color getColorAt(float percentage, List<Color> colors) {
        // Calculate the position in the color list
        int totalColors = colors.size();
        float index = percentage * (totalColors - 1);

        // Get the two colors we are interpolating between
        int startIndex = (int) index;
        int endIndex = Math.min(startIndex + 1, totalColors - 1);

        // Interpolation amount
        float fraction = index - startIndex;

        Color startColor = colors.get(startIndex);
        Color endColor = colors.get(endIndex);

        // Interpolate each color component (R, G, B)
        int r = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * fraction);
        int g = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * fraction);
        int b = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * fraction);

        // Return the interpolated color
        return new Color(r, g, b);
    }
}
