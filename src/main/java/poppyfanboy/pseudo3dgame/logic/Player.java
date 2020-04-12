package poppyfanboy.pseudo3dgame.logic;

import java.util.function.IntFunction;
import poppyfanboy.pseudo3dgame.logic.TileField.TileType;
import poppyfanboy.pseudo3dgame.util.Double2;

public class Player implements TileField.TileFieldObject {
    private Double2 coords;

    public Player(Double2 coords) {
        this.coords = coords;
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