package poppyfanboy.pseudo3dgame.graphics;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;

public class Texture {
    private BufferedImage[] texturesPyramid;

    public Texture(BufferedImage originalTexture) {
        int width = originalTexture.getWidth();
        int height = originalTexture.getHeight();
        List<BufferedImage> texturesPyramid = new ArrayList<>();
        BufferedImage currentTexture = originalTexture;
        texturesPyramid.add(currentTexture);
        width /= 2; height /= 2;
        while (width != 1 || height != 1) {
            currentTexture = resample(currentTexture, width, height);
            texturesPyramid.add(currentTexture);
            if (width > 1) width >>= 1;
            if (height > 1) height >>= 1;
        }
        this.texturesPyramid = texturesPyramid.toArray(new BufferedImage[0]);
    }

    public BufferedImage getTexture(int mipLevel) {
        mipLevel = Math.max(0, Math.min(texturesPyramid.length - 1, mipLevel));
        return texturesPyramid[mipLevel];
    }

    private static BufferedImage resample(BufferedImage texture,
            int outWidth, int outHeight) {
        double xSampleStep = 2;
        double xStart = 0.5;
        double ySampleStep = 2;
        double yStart = 0.5;

        BufferedImage resampled = new BufferedImage(
                outWidth, outHeight, BufferedImage.TYPE_INT_RGB);
        int[] data = ((DataBufferInt) resampled.getRaster()
                .getDataBuffer()).getData();
        for (int x = 0; x < outWidth; x++)
            for (int y = 0; y < outHeight; y++) {
                double sampleX = xStart + x * xSampleStep;
                double sampleY = yStart + y * ySampleStep;
                data[y * outWidth + x] = sample(texture, sampleX, sampleY);
            }
        return resampled;
    }

    private static int pixel(BufferedImage texture, int x, int y) {
        int width = texture.getWidth();
        int height = texture.getHeight();
        return texture.getRGB(
                (x % width + width) % width,
                (y % height + height) % height);
    }

    private static int sample(BufferedImage texture, double x, double y) {
        int y0 = lerp(
                pixel(texture, (int) x, (int) y),
                pixel(texture, (int) x + 1, (int) y),
                x % 1);
        int y1 = lerp(
                pixel(texture, (int) x, (int) y + 1),
                pixel(texture, (int) x + 1, (int) y + 1),
                x % 1);
        return lerp(y0, y1, y % 1);
    }

    private static int lerp(int colorA, int colorB, double t) {
        int red = (int) (
                (colorA >> 16 & 0xFF) * (1 - t) + (colorB >> 16 & 0xFF) * t);
        int green = (int) (
                (colorA >> 8 & 0xFF) * (1 - t) + (colorB >> 16 & 0xFF) * t);
        int blue = (int) (
                (colorA & 0xFF) * (1 - t) + (colorB >> 16 & 0xFF) * t);
        return red << 16 | green << 8 | blue;
    }
}
