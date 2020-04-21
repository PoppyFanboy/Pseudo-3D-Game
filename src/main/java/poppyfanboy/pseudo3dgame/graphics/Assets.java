package poppyfanboy.pseudo3dgame.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.EnumMap;
import javax.imageio.ImageIO;
import poppyfanboy.pseudo3dgame.Game;
import poppyfanboy.pseudo3dgame.util.Double2;

public class Assets {
    private static final String TEXTURES_PATH = "/textures/";
    private static final int TILE_SPRITE_SIZE = 64;
    private static final int STRIPS_COUNT
            = TILE_SPRITE_SIZE / PlayerCamera.STRIP_WIDTH;

    private Game.Resolution resolution;
    private EnumMap<SpriteType, BufferedImage> sprites;
    private EnumMap<SpriteType, BufferedImage[]> spritesVerticalStrips;

    private BufferedImage hBuffer;

    public enum SpriteType {
        BRICK_WALL, BRICK_MOSSY_FLOOR;
    }

    public Assets(Game.Resolution resolution) throws IOException {
        this.resolution = resolution;
        sprites = new EnumMap<>(SpriteType.class);
        spritesVerticalStrips = new EnumMap<>(SpriteType.class);
        loadSprite(SpriteType.BRICK_WALL, "wall.png");
        loadSprite(SpriteType.BRICK_MOSSY_FLOOR, "floor.png");

        hBuffer = new BufferedImage(resolution.getSize().x, 1,
                BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage verticalSample(SpriteType spriteType, double x) {
        if (!spritesVerticalStrips.containsKey(spriteType)) {
            return null;
        }
        x = Math.min(1, Math.max(0, x));
        int i = Math.min((int) (x * TILE_SPRITE_SIZE), TILE_SPRITE_SIZE - 1);
        return spritesVerticalStrips.get(spriteType)[i];
    }

    public Color sample(SpriteType spriteType, Double2 coords) {
        BufferedImage sprite = sprites.get(spriteType);
        coords
            = new Double2(((coords.x % 1) + 1) % 1, ((coords.y % 1) + 1) % 1);
        return new Color(sprite.getRGB(
                (int) (coords.x * sprite.getWidth()),
                (int) (coords.y * sprite.getWidth())));
    }

    public BufferedImage lerpHorizontalSample(SpriteType spriteType,
            Double2 a, Double2 b, int width) {
        BufferedImage sprite = sprites.get(spriteType);
        // writing directly into the array is significantly faster
        int[] data = ((DataBufferInt)
                hBuffer.getRaster().getDataBuffer()).getData();
        double dx = (b.x - a.x) / width * PlayerCamera.STRIP_WIDTH;
        double dy = (b.y - a.y) / width * PlayerCamera.STRIP_WIDTH;
        double tx = ((a.x % 1) + 1) % 1, ty = ((a.y % 1) + 1) % 1;
        for (int x = 0; x < width; x += PlayerCamera.STRIP_WIDTH) {
            int value = sprite.getRGB(
                    (int) (tx * sprite.getWidth()),
                    (int) (ty * sprite.getHeight()));
            for (int i = 0; i < PlayerCamera.STRIP_WIDTH; i++)
                data[x + i] = value;
            tx += dx;
            ty += dy;
            if (tx < 0) tx++; else if (tx >= 1) tx--;
            if (ty < 0) ty++; else if (ty >= 1) ty--;
        }
        return hBuffer;
    }

    private void loadSprite(SpriteType spriteType, String path)
            throws IOException {
        BufferedImage sprite = new BufferedImage(TILE_SPRITE_SIZE,
                TILE_SPRITE_SIZE, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = (Graphics2D) sprite.getGraphics();
        BufferedImage notScaled
                = ImageIO.read(Assets.class.getResource(TEXTURES_PATH + path));
        g.setTransform(AffineTransform.getScaleInstance(
                (float) TILE_SPRITE_SIZE / notScaled.getWidth(),
                (float) TILE_SPRITE_SIZE / notScaled.getHeight()));
        g.drawImage(notScaled, 0, 0, null);
        g.dispose();


        BufferedImage[] spriteStrips = new BufferedImage[TILE_SPRITE_SIZE];
        for (int i = 0; i < spriteStrips.length; i++) {
            spriteStrips[i] = sprite.getSubimage(i, 0, 1, sprite.getHeight());
        }
        spritesVerticalStrips.put(spriteType, spriteStrips);
        sprites.put(spriteType, sprite);
    }
}
