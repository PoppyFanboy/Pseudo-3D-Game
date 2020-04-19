package poppyfanboy.pseudo3dgame.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import poppyfanboy.pseudo3dgame.Game;
import poppyfanboy.pseudo3dgame.logic.WalkingGameplay;
import poppyfanboy.pseudo3dgame.util.*;

public class PlayerCamera {
    public static final double FOV = Math.PI / 3;
    public static final int STRIP_WIDTH = 4;
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

            // floor
            for (int y = (int) (ppSize.y + dProj) / 2 - 1; y < ppSize.y;
                    y += STRIP_WIDTH) {
                // length of the projection of the vector from the spectator
                // to the intersection with the floor
                double dFloor
                        = 0.5 / (y - ppSize.y / 2.0) * ppDistance / angle.cos;
                Double2 floorCoords = coords.add(playerAngle.combine(angle)
                        .apply(new Double2(dFloor, 0)));
                g.setColor(assets.sample(Assets.SpriteType.BRICK_MOSSY_FLOOR,
                        floorCoords.x % 1, floorCoords.y % 1));
                g.fillRect(i * STRIP_WIDTH, y, STRIP_WIDTH, STRIP_WIDTH);
            }
            angle = angle.combine(delta);
        }

        // floor shading
        Composite old = g.getComposite();
        g.setColor(Color.BLACK);
        final int hStripSize = 1;
        for (int y = ppSize.y / 2; y < ppSize.y; y += hStripSize) {
            float alpha = 1.5F * (1 - (float)  y / ppSize.y);
            g.setComposite(
                    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.drawRect(0, y, ppSize.x, hStripSize);
        }
        g.setComposite(old);

        angle = initialAngle;
        for (int i = 0; i < stripsCount; i++) {
            // multiplying by cosine scales the distances an removes the fish
            // eye effect
            WalkingGameplay.RayCollision rayCollision = gameplay.playerRayCast(
                    coords, playerAngle.combine(angle), 10);
            double d = rayCollision.d * angle.cos;
            double dProj = (double) WALL_HEIGHT / d * ppDistance;

            // wall
            BufferedImage strip = assets.verticalSample(
                    Assets.SpriteType.BRICK_WALL, rayCollision.hitPoint);
            g.drawImage(strip, i * STRIP_WIDTH,
                    (int) ((ppSize.y - dProj) * 0.5), STRIP_WIDTH,
                    (int) dProj, null);

            // wall shading
            old = g.getComposite();
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
