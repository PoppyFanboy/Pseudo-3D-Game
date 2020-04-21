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

    private Game.Resolution resolution;
    private EnumMap<SpriteType, BufferedImage> sprites;
    private EnumMap<SpriteType, int[][]> spritesVerticalStrips;

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

    public int[] verticalSample(SpriteType spriteType, double x) {
        x = Math.min(1, Math.max(0, x));
        int i = Math.min((int) (x * TILE_SPRITE_SIZE), TILE_SPRITE_SIZE - 1);
        return spritesVerticalStrips.get(spriteType)[i];
    }

    public int[] lerpHorizontalSample(SpriteType spriteType,
            Double2 a, Double2 b, int width) {
        BufferedImage sprite = sprites.get(spriteType);
        // writing directly into the array is significantly faster
        int[] data = new int[width];
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
        return data;
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


        int[][] spriteStrips = new int[TILE_SPRITE_SIZE][TILE_SPRITE_SIZE];
        for (int x = 0; x < TILE_SPRITE_SIZE; x++) {
            for (int y = 0; y < TILE_SPRITE_SIZE; y++) {
                spriteStrips[x][y] = sprite.getRGB(x, y);
            }
        }
        spritesVerticalStrips.put(spriteType, spriteStrips);
        sprites.put(spriteType, sprite);
    }
}
