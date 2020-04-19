package poppyfanboy.pseudo3dgame.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import javax.imageio.ImageIO;

public class Assets {
    private static final String TEXTURES_PATH = "/textures/";
    private static final int TILE_SPRITE_SIZE = 64;
    private static final int STRIPS_COUNT
            = TILE_SPRITE_SIZE / PlayerCamera.STRIP_WIDTH;

    private EnumMap<SpriteType, BufferedImage> sprites;
    private EnumMap<SpriteType, BufferedImage[]> spritesVerticalStrips;

    public enum SpriteType {
        BRICK_WALL, BRICK_MOSSY_FLOOR;
    }

    public Assets() throws IOException {
        sprites = new EnumMap<>(SpriteType.class);
        spritesVerticalStrips = new EnumMap<>(SpriteType.class);
        loadSprite(SpriteType.BRICK_WALL, "wall.png");
        loadSprite(SpriteType.BRICK_MOSSY_FLOOR, "floor.png");
    }

    public BufferedImage verticalSample(SpriteType spriteType, double x) {
        if (!spritesVerticalStrips.containsKey(spriteType)) {
            return null;
        }
        x = Math.min(1, Math.max(0, x));
        int i = Math.min((int) (x * TILE_SPRITE_SIZE), TILE_SPRITE_SIZE - 1);
        return spritesVerticalStrips.get(spriteType)[i];
    }

    public Color sample(SpriteType spriteType, double x, double y) {
        BufferedImage sprite = sprites.get(spriteType);
        x = Math.min(1, Math.max(0, x));
        y = Math.min(1, Math.max(0, y));
        int xIndex = Math.min(
                (int) (x * sprite.getWidth()), sprite.getWidth() - 1);
        int yIndex = Math.min(
                (int) (y * sprite.getWidth()), sprite.getHeight() - 1);
        return new Color(sprite.getRGB(xIndex, yIndex));
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
