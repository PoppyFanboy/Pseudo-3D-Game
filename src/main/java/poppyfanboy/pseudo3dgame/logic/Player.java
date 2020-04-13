package poppyfanboy.pseudo3dgame.logic;

import java.util.function.IntFunction;
import poppyfanboy.pseudo3dgame.logic.TileField.TileType;
import poppyfanboy.pseudo3dgame.util.Double2;

public class Player implements TileField.TileFieldObject {
    public static final double FORWARD_VELOCITY = 0.05;
    public static final double ANGLE_VELOCITY = 0.05;
    public static final double RADIUS = 0.5;

    private Double2 coords;
    private double angle;
    private double forwardVelocity, angleVelocity;

    public Player(Double2 coords) {
        this.coords = coords;
    }

    public void tick() {
        angle += angleVelocity;
        coords = coords.add(Math.cos(angle) * forwardVelocity,
                Math.sin(angle) * forwardVelocity);
    }

    public void setForwardVelocity(double velocity) {
        forwardVelocity = velocity;
    }

    public void setAngleVelocity(double velocity) {
        angleVelocity = velocity;
    }

    public double getAngle() {
        return angle;
    }

    public Double2 getCoords() {
        return coords;
    }

    @Override
    public void put(TileField tileField) {
        TileType.PLAYER.put(tileField, coords.toInt());
    }

    @Override
    public void remove(TileField tileField) {
        TileType.PLAYER.remove(tileField, coords.toInt());
    }

    @Override
    public IntFunction<TileField.Tile> tiles() {
        return i -> i == 0
                ? new TileField.Tile(TileType.PLAYER, coords.toInt()) : null;
    }

    @Override
    public int tilesCount() {
        return 1;
    }

    @Override
    public boolean collides(TileField.Tile tile) {
        if (tile.tileType != TileType.WALL && tile.tileType != TileType.PLAYER)
            return false;
        return tile.coords.equals(coords.toInt());
    }
}