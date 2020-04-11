package poppyfanboy.pseudo3dgame;

public class Main {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        Game game = new Game(Game.Resolution._640x480);
        game.start();
    }
}
