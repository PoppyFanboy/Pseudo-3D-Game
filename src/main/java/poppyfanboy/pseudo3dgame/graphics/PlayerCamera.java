package poppyfanboy.pseudo3dgame.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import poppyfanboy.pseudo3dgame.Game;
import poppyfanboy.pseudo3dgame.logic.WalkingGameplay;
import poppyfanboy.pseudo3dgame.util.*;

public class PlayerCamera {
    public static final int RENDER_DISTANCE = 10;
    public static final double FOV = Math.PI / 3;
    public static final int STRIP_WIDTH = 1;
    public static final int FLOOR_SHADE_WIDTH = 5;
    public static final int WALL_HEIGHT = 1;

    private Rotation delta, leftmostAngle, rightmostAngle;

    private Game.Resolution resolution;
    private Assets assets;
    private WalkingGameplay gameplay;

    // java graphics does not handle drawing pixel strips well
    private BufferedImage buffer;
    private Graphics2D gBuffer;

    public PlayerCamera(WalkingGameplay gameplay, Game.Resolution resolution,
            Assets assets) {
        this.resolution = resolution;
        this.assets = assets;
        this.gameplay = gameplay;

        delta = new Rotation(FOV / resolution.getSize().x * STRIP_WIDTH);
        leftmostAngle = new Rotation(-FOV / 2);
        rightmostAngle = new Rotation(FOV / 2);
        buffer = new BufferedImage(resolution.getSize().x,
                resolution.getSize().y, BufferedImage.TYPE_INT_RGB);
        gBuffer = buffer.createGraphics();
    }

    public void render(Graphics2D g, double interpolation) {
        if (gameplay == null) {
            return;
        }
        float[] hsb = new float[3];
        int[] bufferData = ((DataBufferInt)
                buffer.getRaster().getDataBuffer()).getData();

        // fill the upper part of the screen with black
        bufferData[0] = Color.BLACK.getRGB();
        int len = buffer.getHeight() * buffer.getWidth();
        for (int i = 1; i < len; i += i) {
            System.arraycopy(bufferData, 0,
                    bufferData, i, Math.min((len - i), i));
        }
        // projection plane (PP) size and distance from the player to the PP
        Int2 ppSize = resolution.getSize();
        double ppDistance = ppSize.x * 0.5 / Math.tan(FOV / 2);

        int stripsCount = ppSize.x / STRIP_WIDTH;
        Double2 coords = gameplay.getPlayerCoords();
        Rotation playerAngle = gameplay.getPlayerRotation();

        // drawing floor line by line
        Rotation playerLeftmostAngle = playerAngle.combine(leftmostAngle);
        Rotation playerRightmostAngle = playerAngle.combine(rightmostAngle);
        int yStart = (int) (ppSize.y / 2 + ppDistance / 2 / RENDER_DISTANCE);

        for (int y = yStart; y < ppSize.y; y += STRIP_WIDTH) {
            double dFloorForward = 0.5 / (y - ppSize.y / 2.0) * ppDistance;

            Double2 floorCoordsLeft = coords.add(playerLeftmostAngle
                    .applyX(dFloorForward / leftmostAngle.cos));
            Double2 floorCoordsRight = coords.add(playerRightmostAngle
                    .applyX(dFloorForward / rightmostAngle.cos));

            int[] strip = assets.lerpHorizontalSample(
                    Assets.SpriteType.BRICK_MOSSY_FLOOR,
                    floorCoordsLeft, floorCoordsRight,
                    ppSize.x);
            float alpha = ((float)  (y - yStart / 1.1) / ppSize.y);
            for (int i = 0; i < strip.length; i++) {
                int value = strip[i];
                Color.RGBtoHSB((value >> 16) & 0xFF, (value >> 8) & 0xFF,
                        value & 0xFF, hsb);
                strip[i] = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] * alpha);
            }

            for (int i = 0; i < STRIP_WIDTH; i++)
                if (y + i < ppSize.y) {
                    System.arraycopy(strip, 0, bufferData, (y + i) * ppSize.x,
                            ppSize.x);
                }
        }

        Rotation angle = leftmostAngle;
        for (int i = 0; i < stripsCount; i++) {
            // multiplying by cosine scales the distances an removes the fish
            // eye effect
            WalkingGameplay.RayCollision rayCollision = gameplay.playerRayCast(
                    coords, playerAngle.combine(angle), RENDER_DISTANCE);
            if (rayCollision == null) {
                angle = angle.combine(delta);
                continue;
            }
            double d = rayCollision.d * angle.cos;
            double dProj = (double) WALL_HEIGHT / d * ppDistance;

            // wall
            int[] strip = assets.verticalSample(
                    Assets.SpriteType.BRICK_WALL, rayCollision.hitPoint);
            int startY = (int) ((ppSize.y - dProj) * 0.5);

            float alpha = (float) (1 / Math.max(1, d));
            for (int y = 0; y < dProj; y++) {
                int index = (startY + y) * buffer.getWidth() + i * STRIP_WIDTH;
                if (index < 0 || index >= bufferData.length) continue;
                for (int j = 0; j < STRIP_WIDTH; j++) {
                    int value = strip[(int) ((float) y / dProj * strip.length)];
                    value = 0xff000000 | value;
                    Color.RGBtoHSB((value >> 16) & 0xFF, (value >> 8) & 0xFF,
                            value & 0xFF, hsb);
                    value = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] * alpha);
                    bufferData[index + j] = value;
                }
            }

            angle = angle.combine(delta);
        }
        g.drawImage(buffer, 0, 0, null);
    }
}
