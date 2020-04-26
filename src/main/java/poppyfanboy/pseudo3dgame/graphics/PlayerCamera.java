package poppyfanboy.pseudo3dgame.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import poppyfanboy.pseudo3dgame.Game;
import poppyfanboy.pseudo3dgame.logic.WalkingGameplay;
import poppyfanboy.pseudo3dgame.util.*;

public class PlayerCamera {
    public static final int RENDER_DISTANCE = 10;
    public static final double FOV = Math.PI / 3;
    public static final int STRIP_WIDTH = 1;
    public static final int WALL_HEIGHT = 1;

    private DrawThread[] threads;
    private BufferedImage buffer;

    private Rotation leftmostAngle, rightmostAngle, delta;
    double ppDistance;

    Double2[] floorCoordsA, floorCoordsDelta;

    private Game.Resolution resolution;
    private Assets assets;
    private WalkingGameplay gameplay;

    public PlayerCamera(WalkingGameplay gameplay, Game.Resolution resolution,
            Assets assets) {
        this.resolution = resolution;
        this.assets = assets;
        this.gameplay = gameplay;

        delta = new Rotation(FOV / resolution.getSize().x * STRIP_WIDTH);
        ppDistance = resolution.getSize().x * 0.5 / Math.tan(FOV / 2);
        leftmostAngle = new Rotation(-FOV / 2);
        rightmostAngle = new Rotation(FOV / 2);

        floorCoordsA = new Double2[resolution.getSize().y];
        floorCoordsDelta = new Double2[resolution.getSize().y];

        buffer = new BufferedImage(resolution.getSize().x,
                resolution.getSize().y, BufferedImage.TYPE_INT_RGB);

        int threadsCount = Math.max(1,
                Math.min(16, Math.min((resolution.getSize().x + 127) / 128,
                        Runtime.getRuntime().availableProcessors())));
        threadsCount = 3;
        threads = new DrawThread[threadsCount];
        int threadPaintWidth = resolution.getSize().x / threadsCount;
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new DrawThread(
                    i * threadPaintWidth, Math.min(resolution.getSize().x,
                    (i + 1) * threadPaintWidth));
            threads[i].start();
        }
    }

    private class DrawThread extends Thread {
        private int fromX, toX;
        private final BufferedImage buffer;
        private volatile boolean buffering = false, rendering = false;

        public DrawThread(int fromX, int toX) {
            this.fromX = fromX;
            this.toX = toX;
            buffer = new BufferedImage(
                    toX - fromX, resolution.getSize().y,
                    BufferedImage.TYPE_INT_RGB);
        }

        public synchronized void setGraphics(Graphics2D g) {
            rendering = true;
            while (buffering)
                Thread.onSpinWait();
            g.drawImage(buffer, fromX, 0, null);
            rendering = false;
        }

        @Override
        public void run() {
            render(buffer, fromX, toX);
            while (true) {
                buffering = true;
                render(buffer, fromX, toX);
                buffering = false;
                while (rendering)
                    Thread.onSpinWait();
            }
        }
    }

    public void render(Graphics2D g, double interpolation) {
        for (DrawThread thread : threads) {
            thread.setGraphics((Graphics2D) g.create());
        }
    }

    public void render(BufferedImage buffer, int fromX, int toX) {
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
        Double2 coords = gameplay.getPlayerCoords();
        Rotation playerAngle = gameplay.getPlayerRotation();

        Rotation playerLeftmostAngle = playerAngle.combine(leftmostAngle);
        Rotation playerRightmostAngle = playerAngle.combine(rightmostAngle);
        int yStart = (int) ((ppSize.y + ppDistance / RENDER_DISTANCE) * 0.5);

        int firstFloorY = ppSize.y;
        Rotation angle = new Rotation(FOV * (fromX - ppSize.x / 2.0) / ppSize.x);
        for (int i = fromX; i < toX; i++) {
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
                fillRect(bufferData, buffer.getWidth(), index, value, alpha8);
            }

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
                fillRect(bufferData, buffer.getWidth(), index, value, alpha8);
            }

            angle = angle.combine(delta);
        }
    }

    private void fillRect(int[] bufferData, int bufferWidth,
            int stripIndex, int value, int alpha8) {
        if (STRIP_WIDTH == 1) {
            if (stripIndex < bufferData.length)
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
                index += bufferWidth - STRIP_WIDTH;
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
