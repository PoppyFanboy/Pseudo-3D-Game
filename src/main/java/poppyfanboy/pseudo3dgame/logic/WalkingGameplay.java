package poppyfanboy.pseudo3dgame.logic;

import poppyfanboy.pseudo3dgame.util.*;

public class WalkingGameplay {
    private TileField tileField = new TileField(new Int2(10, 10));
    private Player player;
    private Level level;

    public WalkingGameplay() {
        // test code
        player = new Player(new Double2(1.5, 1.5));
        level = new Level(new Double2(0, 0));
        player.put(tileField);
        level.put(tileField);
    }

    public void tick() {
        player.tick();
    }

    public TileField getTileField() {
        return tileField;
    }

    public void setPlayerVelocity(double vForward, double vAngle) {
        player.setVelocity(vForward, vAngle);
    }

    public Rotation getPlayerRotation() {
        return player.getRotation();
    }

    public Double2 getPlayerCoords() {
        return player.getCoords();
    }
}
