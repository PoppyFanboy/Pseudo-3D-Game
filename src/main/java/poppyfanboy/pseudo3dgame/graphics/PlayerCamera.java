package poppyfanboy.pseudo3dgame.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import poppyfanboy.pseudo3dgame.logic.WalkingGameplay;
import poppyfanboy.pseudo3dgame.util.Int2;

public class PlayerCamera {
    public static final double FOV = Math.PI / 3;
    public static final int STRIP_WIDTH = 1;

    private Int2 size;
    private WalkingGameplay gameplay;

    public PlayerCamera(WalkingGameplay gameplay, Int2 size) {
        this.size = size;
        this.gameplay = gameplay;
    }

    public void render(Graphics2D g) {
        if (gameplay == null) {
            return;
        }
        int stripsCount = size.x / STRIP_WIDTH;
        double angle = -FOV / 2;

        for (int i = 0; i < stripsCount; i++) {
            double d = gameplay.playerRayCast(angle, 10);
            int grayShade = (int) (255 / (d + 1));
            g.setColor(new Color(grayShade, grayShade, grayShade));
            g.fillRect(i * STRIP_WIDTH, (int) (0.5 * size.y * (1 - 1 / d)),
                    STRIP_WIDTH, (int) (size.y / d));
            angle += FOV / stripsCount;
        }
    }
}
