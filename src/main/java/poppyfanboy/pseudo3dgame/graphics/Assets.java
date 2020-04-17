package poppyfanboy.pseudo3dgame.graphics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import javax.imageio.ImageIO;

public class Assets {
    private static final String TEXTURES_PATH = "/textures/";
    private static final int TILE_SPRITE_SIZE = 64;

    private EnumMap<SpriteType, BufferedImage> sprites;

    public enum SpriteType {
        BRICK_WALL
    }

    public Assets() throws IOException {
        sprites = new EnumMap<>(SpriteType.class);
        loadSprite(SpriteType.BRICK_WALL, "wall.png",
                TILE_SPRITE_SIZE, TILE_SPRITE_SIZE);
    }

    public BufferedImage verticalSample(SpriteType spriteType, double x) {
        x = Math.max(0, Math.min(1, x));
        BufferedImage sprite = sprites.get(spriteType);
        return sprite.getSubimage((int) (x * sprite.getWidth())
                , 0, 1, sprite.getHeight());
    }

    private void loadSprite(SpriteType spriteType, String path,
            int width, int height) throws IOException {
        BufferedImage sprite = new BufferedImage(TILE_SPRITE_SIZE,
                TILE_SPRITE_SIZE, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = (Graphics2D) sprite.getGraphics();
        g.setTransform(AffineTransform.getScaleInstance(
                (float) width / TILE_SPRITE_SIZE,
                (float) height / TILE_SPRITE_SIZE));
        g.drawImage(ImageIO.read(
                Assets.class.getResource(TEXTURES_PATH + path)),
                0, 0, null);
        g.dispose();
        sprites.put(spriteType, sprite);
    }
}
