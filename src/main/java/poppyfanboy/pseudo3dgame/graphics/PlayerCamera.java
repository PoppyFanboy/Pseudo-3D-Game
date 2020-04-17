package poppyfanboy.pseudo3dgame.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import poppyfanboy.pseudo3dgame.Game;
import poppyfanboy.pseudo3dgame.logic.WalkingGameplay;
import poppyfanboy.pseudo3dgame.util.Int2;

public class PlayerCamera {
    public static final double FOV = Math.PI / 3;
    public static final int STRIP_WIDTH = 5;
    public static final int WALL_HEIGHT = 1;

    private Game.Resolution resolution;
    private Assets assets;
    private WalkingGameplay gameplay;

    public PlayerCamera(WalkingGameplay gameplay, Game.Resolution resolution,
            Assets assets) {
        this.resolution = resolution;
        this.assets = assets;
        this.gameplay = gameplay;
    }

    public void render(Graphics2D g) {
        if (gameplay == null) {
            return;
        }
        // projection plane (PP) size and distance from the player to the PP
        Int2 ppSize = resolution.getSize();
        double ppDistance = ppSize.x * 0.5 / Math.tan(FOV / 2);

        int stripsCount = ppSize.x / STRIP_WIDTH;
        double angle = -FOV / 2;

        for (int i = 0; i < stripsCount; i++) {
            // multiplying by cosine scales the distances an removes the fish
            // eye effect
            WalkingGameplay.RayCollision rayCollision
                    = gameplay.playerRayCast(angle, 10);
            double d = rayCollision.d * Math.cos(angle);
            double dProj = (double) WALL_HEIGHT / d * ppDistance;

            BufferedImage strip = assets.verticalSample(
                    Assets.SpriteType.BRICK_WALL,
                    rayCollision.hitPoint);

            for (int j = 0; j < STRIP_WIDTH; j++) {
                g.drawImage(strip, i * STRIP_WIDTH + j,
                        (int) ((ppSize.y - dProj) * 0.5), STRIP_WIDTH,
                        (int) dProj, null);
            }

            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, (float) (1 - 1 / Math.max(1, d))));
            g.setColor(Color.BLACK);
            g.fillRect(i * STRIP_WIDTH, (int) ((ppSize.y - dProj) * 0.5),
                    STRIP_WIDTH, (int) dProj);
            g.setComposite(old);

            angle += FOV / stripsCount;
        }
    }
}
