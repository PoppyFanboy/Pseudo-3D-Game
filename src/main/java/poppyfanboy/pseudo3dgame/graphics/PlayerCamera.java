package poppyfanboy.pseudo3dgame.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.CountDownLatch;
import poppyfanboy.pseudo3dgame.Game;
import poppyfanboy.pseudo3dgame.logic.WalkingGameplay;
import poppyfanboy.pseudo3dgame.util.*;

public class PlayerCamera {
    public static final int RENDER_DISTANCE = 9;
    public static final double FOV = Math.PI / 3;
    public static final int STRIP_WIDTH = 1;
    public static final int WALL_HEIGHT = 1;

    private DrawThread[] threads;
    private final BufferedImage buffer;
    private int fromX, toX;

    private Rotation delta;
    private double ppDistance;

    private Double2[][] floorCoords;
    private int[] floorAlpha8;

    private Game.Resolution resolution;
    private Assets assets;
    private WalkingGameplay gameplay;

    public PlayerCamera(WalkingGameplay gameplay, Game.Resolution resolution,
            Assets assets) {
        this.resolution = resolution;
        this.assets = assets;
        this.gameplay = gameplay;

        final int width = resolution.getSize().x;
        final int height = resolution.getSize().y;

        delta = new Rotation(FOV / width * STRIP_WIDTH);
        ppDistance = width * 0.5 / Math.tan(FOV / 2);
        Rotation leftmostAngle = new Rotation(-FOV / 2);
        Rotation rightmostAngle = new Rotation(FOV / 2);

        floorAlpha8 = new int[height];
        floorCoords = new Double2[height][width / STRIP_WIDTH];

        final int threadRenderWidth = 256;
        int threadsCount = Math.max(1, Math.min(16,
            Math.min((width + threadRenderWidth - 1) / threadRenderWidth,
            Runtime.getRuntime().availableProcessors())));

        threads = new DrawThread[threadsCount - 1];

        this.fromX = 0;
        this.toX = width / threadsCount;
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 1; i < threadsCount; i++) {
            threads[i - 1] =
             new DrawThread(
                    i * width / threadsCount / STRIP_WIDTH * STRIP_WIDTH,
                    (i + 1) * width / threadsCount / STRIP_WIDTH * STRIP_WIDTH);
            threads[i - 1].start();
        }

        int yStart = (int) ((height + ppDistance / RENDER_DISTANCE) * 0.5);
        for (int y = 0; y < height / 2; y += STRIP_WIDTH) {
            double dFloorForward
                    = 0.5 / (y + yStart - height / 2.0) * ppDistance;
            Double2 floorCoordsLeft = leftmostAngle
                    .applyX(dFloorForward / leftmostAngle.cos);
            Double2 floorCoordsRight = rightmostAngle
                    .applyX(dFloorForward / rightmostAngle.cos);

            Double2 floorCoordsDelta
                    = floorCoordsRight.sub(floorCoordsLeft)
                    .times((double) STRIP_WIDTH / width);
            Double2 currFloorCoords
                    = floorCoordsLeft.add(floorCoordsDelta
                    .times((double) this.fromX / STRIP_WIDTH));
            for (int j = 0; j < width / STRIP_WIDTH; j++) {
                floorCoords[y][j] = currFloorCoords;
                currFloorCoords = currFloorCoords.add(floorCoordsDelta);
            }
            floorAlpha8[y] = (int) (0xFF
                    * (1 - Math.min(1, dFloorForward / RENDER_DISTANCE)));
        }
    }

    private class DrawThread extends Thread {
        private boolean renderTask = false;
        private CountDownLatch latch;
        private int fromX, toX;

        public DrawThread(int fromX, int toX) {
            this.fromX = fromX;
            this.toX = toX;
        }

        public synchronized void startRenderTask(CountDownLatch latch) {
            this.latch = latch;
            renderTask = true;
            this.notify();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    synchronized (this) {
                        while (!renderTask) this.wait();
                    }
                    try {
                        render(fromX, toX);
                        renderTask = false;
                    } finally {
                        latch.countDown();
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void render(Graphics2D g, double interpolation) {
        CountDownLatch latch = new CountDownLatch(threads.length);
        for (DrawThread thread : threads) thread.startRenderTask(latch);

        render(fromX, toX);
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        g.drawImage(buffer, 0, 0, null);
    }

    public void render(int fromX, int toX) {
        if (gameplay == null) return;

        int[] bufferData = ((DataBufferInt)
                buffer.getRaster().getDataBuffer()).getData();
        // projection plane (PP)
        Int2 ppSize = resolution.getSize();
        Double2 coords = gameplay.getPlayerCoords();
        Rotation playerAngle = gameplay.getPlayerRotation();

        int yStart = (int) ((ppSize.y + ppDistance / RENDER_DISTANCE) * 0.5);
        Rotation angle
                = new Rotation(FOV * (fromX - ppSize.x / 2.0) / ppSize.x);

        for (int i = 0; i < (toX - fromX) / STRIP_WIDTH; i++) {
            // multiplying by cosine scales the distances an removes the fish
            // eye effect
            WalkingGameplay.RayCollision rayCollision = gameplay.playerRayCast(
                    coords, playerAngle.combine(angle), RENDER_DISTANCE);
            double d = rayCollision == null
                    ? Double.POSITIVE_INFINITY
                    : rayCollision.d * angle.cos;
            double dProj = (double) WALL_HEIGHT / d * ppDistance;

            // ceiling (placeholder)
            int wallStartY = (int) ((ppSize.y - dProj) * 0.5);
            int floorStartY = (int) ((ppSize.y + dProj) / 2 - yStart);
            floorStartY = Math.max(0, floorStartY);
            floorStartY = floorStartY / STRIP_WIDTH * STRIP_WIDTH;

            if (rayCollision == null) wallStartY = yStart + floorStartY;
            for (int y = 0; y < wallStartY; y++) {
                int index = y * buffer.getWidth()
                        + (i + fromX / STRIP_WIDTH) * STRIP_WIDTH;
                fillRect(bufferData, buffer.getWidth(), index, 0, 0);
            }
            // wall
            if (rayCollision != null) {
                int alpha8 = (int) (0xFF
                        * (1 - Math.min(1, rayCollision.d / RENDER_DISTANCE)));
                for (int y = 0; y < dProj; y += STRIP_WIDTH) {
                    int index = (wallStartY + y) * buffer.getWidth()
                            + (i + fromX / STRIP_WIDTH) * STRIP_WIDTH;
                    if (index < 0)
                        continue;
                    if (index >= bufferData.length)
                        break;

                    Double2 textureCoords = new Double2(
                            rayCollision.hitPoint, y / dProj);

                    // used in mipmapping as an heuristic coefficient
                    double angleCoeff;
                    if (rayCollision.normalCos > 0.2)
                        angleCoeff = 4 * (1 - rayCollision.normalCos);
                    else if (rayCollision.normalCos > 0.1)
                        angleCoeff = 6 * (1 - rayCollision.normalCos);
                    else
                        angleCoeff = 10 * (1 - rayCollision.normalCos);
                    int mipLevel = (int) Math.max(
                            STRIP_WIDTH * 1.5 * 64 / dProj,
                            STRIP_WIDTH * 64 / dProj * angleCoeff);

                    int value = assets.sample(Assets.SpriteType.BRICK_WALL,
                            textureCoords, mipLevel);
                    fillRect(bufferData, buffer.getWidth(), index, value,
                            alpha8);
                }
            }

            for (int y = floorStartY; y < ppSize.y - yStart; y += STRIP_WIDTH) {
                double dFloorForward
                        = 0.5 / (y + yStart - ppSize.y / 2.0) * ppDistance;

                int index = (y + yStart) * buffer.getWidth()
                        + (i + fromX / STRIP_WIDTH) * STRIP_WIDTH;
                int value = assets.sample(Assets.SpriteType.BRICK_MOSSY_FLOOR,
                        coords.add(playerAngle.apply(
                            floorCoords[y][i + fromX / STRIP_WIDTH])),
                        (int) Math.max(0, dFloorForward - 1));
                fillRect(bufferData, buffer.getWidth(), index, value,
                        floorAlpha8[y]);
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
                    if (index < bufferData.length)
                        bufferData[index] = darken(value, alpha8);
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
