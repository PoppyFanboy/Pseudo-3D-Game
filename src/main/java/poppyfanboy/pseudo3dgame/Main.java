package poppyfanboy.pseudo3dgame;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.setProperty("sun.java2d.opengl", "true");
        Game game = new Game(Game.Resolution._640x480);
        game.start();
    }
}
