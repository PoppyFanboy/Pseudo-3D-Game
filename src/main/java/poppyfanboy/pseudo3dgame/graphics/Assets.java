package poppyfanboy.pseudo3dgame.graphics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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
    private EnumMap<SpriteType, Texture> sprites;

    public enum SpriteType {
        BRICK_WALL, BRICK_MOSSY_FLOOR, WOOD_CEILING;
    }

    public Assets(Game.Resolution resolution) throws IOException {
        sprites = new EnumMap<>(SpriteType.class);
        loadSprite(SpriteType.BRICK_WALL, "wall.png");
        loadSprite(SpriteType.BRICK_MOSSY_FLOOR, "floor.png");
        loadSprite(SpriteType.WOOD_CEILING, "ceiling.png");
    }

    public int sample(SpriteType spriteType, Double2 coords, int mipLevel) {
        BufferedImage sprite = sprites.get(spriteType).getTexture(mipLevel);
        int x = Math.min((int) ((coords.x % 1 + 1) % 1 * sprite.getWidth()),
                TILE_SPRITE_SIZE - 1);
        int y = Math.min((int) ((coords.y % 1 + 1) % 1 * sprite.getHeight()),
                TILE_SPRITE_SIZE - 1);
        return sprite.getRGB(x, y);
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
        sprites.put(spriteType, new Texture(sprite));
    }
}
