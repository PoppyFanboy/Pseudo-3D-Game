package poppyfanboy.pseudo3dgame.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import poppyfanboy.pseudo3dgame.Game;
import poppyfanboy.pseudo3dgame.logic.WalkingGameplay;
import poppyfanboy.pseudo3dgame.util.*;

public class PlayerCamera {
    public static final double FOV = Math.PI / 3;
    public static final int STRIP_WIDTH = 1;
    public static final int WALL_HEIGHT = 1;

    private Rotation delta, initialAngle;

    private Game.Resolution resolution;
    private Assets assets;
    private WalkingGameplay gameplay;

    public PlayerCamera(WalkingGameplay gameplay, Game.Resolution resolution,
            Assets assets) {
        this.resolution = resolution;
        this.assets = assets;
        this.gameplay = gameplay;

        delta = new Rotation(FOV / resolution.getSize().x * STRIP_WIDTH);
        initialAngle = new Rotation(-FOV / 2);
    }

    public void render(Graphics2D g, double interpolation) {
        if (gameplay == null) {
            return;
        }
        // projection plane (PP) size and distance from the player to the PP
        Int2 ppSize = resolution.getSize();
        double ppDistance = ppSize.x * 0.5 / Math.tan(FOV / 2);

        int stripsCount = ppSize.x / STRIP_WIDTH;
        Double2 coords = gameplay.getPlayerCoords();
        Rotation playerAngle = gameplay.getPlayerRotation();
        Rotation angle = initialAngle;

        for (int i = 0; i < stripsCount; i++) {
            // multiplying by cosine scales the distances an removes the fish
            // eye effect
            WalkingGameplay.RayCollision rayCollision = gameplay.playerRayCast(
                    coords, playerAngle.combine(angle), 10);
            double d = rayCollision.d * angle.cos;
            double dProj = (double) WALL_HEIGHT / d * ppDistance;

            BufferedImage strip = assets.verticalSample(
                    Assets.SpriteType.BRICK_WALL,
                    rayCollision.hitPoint, STRIP_WIDTH);
            g.drawImage(strip, i * STRIP_WIDTH,
                    (int) ((ppSize.y - dProj) * 0.5), STRIP_WIDTH,
                    (int) dProj, null);

            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, (float) (1 - 1 / Math.max(1, d))));
            g.setColor(Color.BLACK);
            g.fillRect(i * STRIP_WIDTH, (int) ((ppSize.y - dProj) * 0.5),
                    STRIP_WIDTH, (int) dProj);
            g.setComposite(old);

            angle = angle.combine(delta);
        }
    }
}
