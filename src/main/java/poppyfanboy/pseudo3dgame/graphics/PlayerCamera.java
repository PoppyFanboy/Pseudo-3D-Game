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
    public static final int WALL_HEIGHT = 1;

    private Rotation delta, leftmostAngle, rightmostAngle;
    double ppDistance;

    Double2[] floorCoordsA, floorCoordsDelta;

    private Game.Resolution resolution;
    private Assets assets;
    private WalkingGameplay gameplay;

    // java graphics does not handle drawing pixel strips well
    private BufferedImage buffer;

    public PlayerCamera(WalkingGameplay gameplay, Game.Resolution resolution,
            Assets assets) {
        this.resolution = resolution;
        this.assets = assets;
        this.gameplay = gameplay;

        delta = new Rotation(FOV / resolution.getSize().x * STRIP_WIDTH);
        leftmostAngle = new Rotation(-FOV / 2);
        rightmostAngle = new Rotation(FOV / 2);
        ppDistance = resolution.getSize().x * 0.5 / Math.tan(FOV / 2);

        buffer = new BufferedImage(resolution.getSize().x,
                resolution.getSize().y, BufferedImage.TYPE_INT_RGB);

        floorCoordsA = new Double2[resolution.getSize().y];
        floorCoordsDelta = new Double2[resolution.getSize().y];
    }

    public void render(Graphics2D g, double interpolation) {
        if (gameplay == null) {
            return;
        }
        int[] bufferData = ((DataBufferInt)
                buffer.getRaster().getDataBuffer()).getData();

        // fill the upper part of the screen with black
        bufferData[0] = Color.BLACK.getRGB();
        int len = buffer.getHeight() * buffer.getWidth();
        for (int i = 1; i < len; i += i) {
            System.arraycopy(bufferData, 0,
                    bufferData, i, Math.min((len - i), i));
        }
        // projection plane (PP)
        Int2 ppSize = resolution.getSize();
        int stripsCount = ppSize.x / STRIP_WIDTH;
        Double2 coords = gameplay.getPlayerCoords();
        Rotation playerAngle = gameplay.getPlayerRotation();

        Rotation playerLeftmostAngle = playerAngle.combine(leftmostAngle);
        Rotation playerRightmostAngle = playerAngle.combine(rightmostAngle);
        int yStart = (int) ((ppSize.y + ppDistance / RENDER_DISTANCE) * 0.5);

        int firstFloorY = ppSize.y;
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

            int alpha8 = (int) (0xFF / Math.max(1, d));
            for (int y = 0; y < dProj; y += STRIP_WIDTH) {
                int index = (startY + y) * buffer.getWidth() + i * STRIP_WIDTH;
                if (index < 0) continue;
                if (index >= bufferData.length) break;

                int value = strip[(int) (y / dProj * strip.length)];
                fillRect(bufferData, index, value, alpha8);
            }

            // floor
            int floorStartY = (int) ((ppSize.y + dProj) / 2) - yStart;
            floorStartY = floorStartY / STRIP_WIDTH * STRIP_WIDTH;

            // lazy floor computation
            if (floorStartY < firstFloorY) {
                for (int y = floorStartY; y < firstFloorY; y += STRIP_WIDTH) {
                    double dFloorForward
                            = 0.5 / (y + yStart - ppSize.y * 0.5) * ppDistance;

                    Double2 floorCoordsLeft = coords.add(playerLeftmostAngle
                            .applyX(dFloorForward / leftmostAngle.cos));
                    Double2 floorCoordsRight = coords.add(playerRightmostAngle
                            .applyX(dFloorForward / rightmostAngle.cos));

                    floorCoordsA[y] = floorCoordsLeft;
                    floorCoordsDelta[y]
                            = floorCoordsRight.sub(floorCoordsLeft)
                            .times((double) STRIP_WIDTH / ppSize.x);
                }
                firstFloorY = floorStartY;
            }

            for (int y = floorStartY; y < ppSize.y - yStart; y += STRIP_WIDTH) {
                alpha8 = (int) (0xFF * (y / 1.1) / ppSize.y);
                Double2 floorCoords = floorCoordsA[y]
                        .add(floorCoordsDelta[y].times(i));

                int index
                        = (y + yStart) * buffer.getWidth() + i * STRIP_WIDTH;
                int value = assets.sample(
                        Assets.SpriteType.BRICK_MOSSY_FLOOR, floorCoords);
                fillRect(bufferData, index, value, alpha8);
            }

            angle = angle.combine(delta);
        }
        g.drawImage(buffer, 0, 0, null);
    }

    private void fillRect(int[] bufferData, int stripIndex, int value,
            int alpha8) {
        if (STRIP_WIDTH == 1) {
            bufferData[stripIndex] = darken(value, alpha8);
        } else {
            int index = stripIndex;
            for (int j = 0; j < STRIP_WIDTH; j++) {
                for (int k = 0; k < STRIP_WIDTH; k++) {
                    if (index < bufferData.length) {
                        bufferData[index] = darken(value, alpha8);
                    }
                    index++;
                }
                index += buffer.getWidth() - STRIP_WIDTH;
            }
        }
    }

    private static int darken(int value, int alpha8) {
        int red = (value >> 16 & 0xFF) * alpha8 >> 8;
        int green = (value >> 8 & 0xFF) * alpha8 >> 8;
        int blue = (value & 0xFF) * alpha8 >> 8;
        return red << 16 | green << 8 | blue;
    }
}
