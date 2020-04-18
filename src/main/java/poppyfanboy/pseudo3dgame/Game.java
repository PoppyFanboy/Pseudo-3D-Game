package poppyfanboy.pseudo3dgame;

import java.awt.Graphics2D;
import java.io.IOException;
import poppyfanboy.pseudo3dgame.graphics.Assets;
import poppyfanboy.pseudo3dgame.graphics.GameMap;
import poppyfanboy.pseudo3dgame.graphics.PlayerCamera;
import poppyfanboy.pseudo3dgame.logic.WalkingGameplay;
import poppyfanboy.pseudo3dgame.util.Int2;

public class Game {
    private static final int MAX_FRAMESKIP = 5;
    public static final int TICK_RATE = 60;
    private static final long TICK_DURATION = 1_000_000_000 / TICK_RATE;

    private Resolution resolution;
    private Display display;
    private Thread thread;

    private WalkingGameplay gameplay = new WalkingGameplay();
    private InputManager inputManager = new InputManager(gameplay);
    private GameMap gameMap = new GameMap(gameplay);
    private PlayerCamera playerCamera;

    public Game(Resolution resolution) throws IOException {
        display = new Display(resolution.getSize(), "test", inputManager);
        playerCamera = new PlayerCamera(gameplay, resolution, new Assets());
    }

    public synchronized void start() {
        if (thread != null && !thread.isInterrupted())
            return;
        thread = new Thread(() -> {
            long nextTick = System.nanoTime();
            while (!Thread.currentThread().isInterrupted()) {
                int frameSkipCount = 0;
                while (System.nanoTime() > nextTick
                        && frameSkipCount < MAX_FRAMESKIP) {
                    tick();
                    nextTick += TICK_DURATION;
                    frameSkipCount++;
                }
                double interpolation = ((double) System.nanoTime()
                        - (nextTick - TICK_DURATION)) / TICK_DURATION;
                render(interpolation);
            }
        });
        thread.start();
    }

    private void tick() {
        inputManager.tick();
        gameplay.tick();
    }

    private void render(double interpolation) {
        Graphics2D g = display.getGraphics();
        playerCamera.render(g, interpolation);
        gameMap.render(g);
        g.dispose();
        display.render();
    }

    public synchronized void stop() {
        if (thread == null || thread.isInterrupted())
            return;
        thread.interrupt();
    }

    public enum Resolution {
        _640x480, _800x600, _1024x768, _1280x960;

        private static final Int2[] WIDTH_HEIGHT = {
            new Int2(640, 480), new Int2(800, 600), new Int2(1024, 768),
            new Int2(1280, 960)
        };

        private static final int[] BLOCK_SIZE = {
            160, 200, 256, 320
        };

        public Int2 getSize() {
            return WIDTH_HEIGHT[ordinal()];
        }

        public int getBlockSize() {
            return BLOCK_SIZE[ordinal()];
        }
    }
}
