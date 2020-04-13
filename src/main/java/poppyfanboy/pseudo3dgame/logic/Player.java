package poppyfanboy.pseudo3dgame.logic;

import poppyfanboy.pseudo3dgame.logic.TileField.TileType;
import poppyfanboy.pseudo3dgame.util.*;

public class Player extends TileField.TileFieldObject {
    public static final double FORWARD_VELOCITY = 0.05;
    public static final double ANGLE_VELOCITY = 0.05;
    public static final double RADIUS = 0.5;

    private Rotation rotation = new Rotation(0);
    private double vAngle, vForward;

    public Player(Double2 coords) {
        super(coords);
    }

    public void tick() {
        rotation = rotation.combine(new Rotation(vAngle));
        Double2 velocity = rotation.apply(new Double2(vForward, 0));
        setCoords(getCoords().add(velocity));
    }

    public void setVelocity(double vForward, double vAngle) {
        this.vAngle = vAngle;
        this.vForward = vForward;
    }

    public Rotation getRotation() {
        return rotation;
    }

    @Override
    public void put(TileField tileField) {
        TileType.PLAYER.put(tileField, getCoords().toInt());
    }

    @Override
    public void remove(TileField tileField) {
        TileType.PLAYER.remove(tileField, getCoords().toInt());
    }

    @Override
    public boolean collides(TileField.Tile tile) {
        if (tile.tileType != TileType.WALL && tile.tileType != TileType.PLAYER)
            return false;
        return tile.coords.equals(getCoords().toInt());
    }

    @Override
    public ArrayWrapper<TileField.Tile> tiles() {
        return new ArrayWrapper<>(new TileField.Tile[] {
                new TileField.Tile(TileType.PLAYER, getCoords().toInt()) });
    }
}