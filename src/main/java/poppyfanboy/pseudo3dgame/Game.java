package poppyfanboy.pseudo3dgame;

import java.awt.Graphics2D;
import poppyfanboy.pseudo3dgame.graphics.GameMap;
import poppyfanboy.pseudo3dgame.logic.WalkingGameplay;

public class Game {
    private static final int MAX_FRAMESKIP = 5;
    public static final int TICK_RATE = 100;
    private static final long TICK_DURATION = 1_000_000_000 / TICK_RATE;

    private Resolution resolution = Resolution._640x480;
    private Display display;
    private Thread thread;

    private WalkingGameplay gameplay = new WalkingGameplay();
    private InputManager inputManager = new InputManager(gameplay);
    private GameMap gameMap = new GameMap(gameplay);

    public Game(Resolution resolution) {
        display = new Display(resolution.getWidth(), resolution.getHeight(),
                "test", inputManager);
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

        private static final int[][] WIDTH_HEIGHT = {
            {640, 480}, {800, 600}, {1024, 768}, {1280, 960}
        };

        public int getWidth() {
            return WIDTH_HEIGHT[ordinal()][0];
        }

        public int getHeight() {
            return WIDTH_HEIGHT[ordinal()][1];
        }
    }
}
