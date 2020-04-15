package poppyfanboy.pseudo3dgame.logic;

import java.util.Collections;
import java.util.List;
import poppyfanboy.pseudo3dgame.logic.TileField.TileType;
import poppyfanboy.pseudo3dgame.util.*;

public class Player extends TileField.TileFieldObject {
    public static final double FORWARD_VELOCITY = 0.025;
    public static final double ANGLE_VELOCITY = 0.025;
    public static final double DIAMETER = 0.5;

    private Rotation rotation = new Rotation(0);
    private double vAngle, vForward;
    private TileField tileField;

    public Player(Double2 coords, TileField tileField) {
        super(coords);
        this.tileField = tileField;
    }

    public void tick() {
        remove(tileField);
        rotation = rotation.combine(new Rotation(vAngle));
        Double2 velocity = rotation.apply(new Double2(vForward, 0));
        setCoords(getCoords().add(velocity));
        // collide with walls around the player
        int i = 0;
        while (i < 9) {
            // skip the center
            if (i == 4) {
                i++;
                continue;
            }
            Double2 otherTileCoords = new Int2(i % 3 - 1, i / 3 - 1)
                    .add(getCoords().floor());
            AABB aabb = new AABB(otherTileCoords.floor(),
                    otherTileCoords.floor().add(1, 1));
            Double2 offset
                    = aabb.circleCollision(getCoords(), Player.DIAMETER / 2);

            if (offset.nonZero(1e-10)
                    && !tileField.isEmpty(otherTileCoords.toInt())
                    && tileField.conflicts(
                            otherTileCoords.toInt(), TileType.PLAYER)) {
                setCoords(getCoords().add(offset));
                i = 0;
            }
            i++;
        }
        put(tileField);
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
        if (tile.tileType.conflicts(TileType.PLAYER))
            return false;
        return tile.coords.equals(getCoords().toInt());
    }

    @Override
    public List<TileField.Tile> tiles() {
        return Collections.singletonList(
                new TileField.Tile(TileType.PLAYER, getCoords().toInt()));
    }
}