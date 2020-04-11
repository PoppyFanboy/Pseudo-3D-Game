package poppyfanboy.pseudo3dgame;

import java.awt.Graphics2D;

/**
 * A separate instance of the game including the game window.
 */
public class Game {
    private static final int MAX_FRAMESKIP = 5;
    public static final int TICK_RATE = 50;
    private static final long TICK_DURATION = 1_000_000_000 / TICK_RATE;

    private Resolution resolution = Resolution._640x480;
    private Display display;
    private Thread thread;

    public Game(Resolution resolution) {
        display = new Display(resolution.getWidth(), resolution.getHeight(),
                "test");
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
                render();
            }
        });
        thread.start();
    }

    private void tick() {
    }

    private void render() {
        Graphics2D g = display.getGraphics();
        g.fillRect(40, 40, 40, 40);
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
