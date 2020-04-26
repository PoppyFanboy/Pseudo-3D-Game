package poppyfanboy.pseudo3dgame;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;
import poppyfanboy.pseudo3dgame.util.Int2;

public class Display {
    private final BufferStrategy bufferStrategy;
    private Canvas canvas;

    public Display(Int2 size, String title, KeyListener keyListener) {
        // window
        JFrame frame = new JFrame();
        frame.setTitle(title);
        frame.setSize(size.x, size.y);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.addKeyListener(keyListener);
        frame.setVisible(true);
        // canvas
        canvas = new Canvas();
        canvas.setMaximumSize(new Dimension(size.x, size.y));
        canvas.setMinimumSize(new Dimension(size.x, size.y));
        canvas.setPreferredSize(new Dimension(size.x, size.y));
        canvas.setFocusable(false);
        frame.add(canvas);
        frame.pack();
        canvas.createBufferStrategy(3);
        bufferStrategy = canvas.getBufferStrategy();
    }

    public Graphics2D getGraphics() {
        Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        return g;
    }

    public void render() {
        bufferStrategy.show();
    }
}
