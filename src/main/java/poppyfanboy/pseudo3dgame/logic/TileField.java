package poppyfanboy.pseudo3dgame.logic;

import java.util.Iterator;
import java.util.function.IntFunction;
import poppyfanboy.pseudo3dgame.util.Int2;

public class TileField {
    // how many objects a single tile can fit
    private static final int TILE_CAPACITY = Integer.SIZE;

    // Placing an object on the map corresponds to setting an appropriate bit
    // of the integer. This allows you to have several objects on the same tile.
    private int[] map;
    private final Int2 size;

    public TileField(Int2 size) {
        this.size = size;
        map = new int[size.x * size.y];
    }

    public boolean isEmpty(Int2 coords) {
        return !rangeCheck(coords) || map[mapIndex(coords)] == 0;
    }

    public Tile getTile(Int2 coords) {
        if (!rangeCheck(coords)) {
            return new Tile(TileType.EMPTY, coords);
        }
        int tileValue = map[mapIndex(coords)];
        if (tileValue == 0) {
            return new Tile(TileType.EMPTY, coords);
        }
        TileType tileType = TileType
                .values()[Integer.numberOfTrailingZeros(tileValue) + 1];
        return new Tile(tileType, coords);
    }

    public Iterator<Tile> getTiles(Int2 coords) {
        return new Iterator<>() {
            int tileValue = rangeCheck(coords) ? map[mapIndex(coords)] : 0;
            int tileIndex = tileValue == 0 ? 0 : 1;

            @Override
            public boolean hasNext() {
                int shift = Integer.numberOfTrailingZeros(tileValue);
                return tileIndex != -1 && (tileIndex == 0
                        || tileIndex + shift < TileType.values().length);
            }

            @Override
            public Tile next() {
                if (tileIndex == 0) {
                    tileIndex = -1;
                    return new Tile(TileType.EMPTY, coords);
                }
                int shift = Integer.numberOfTrailingZeros(tileValue);
                TileType tileType = TileType.values()[tileIndex + shift];
                tileIndex += shift + 1;
                tileValue >>= shift + 1;
                if (tileValue == 0) {
                    tileIndex = -1;
                }
                return new Tile(tileType, coords);
            }
        };
    }

    public Int2 getSize() {
        return size;
    }

    private boolean rangeCheck(Int2 coords) {
        return coords.x >= 0 && coords.y >= 0
                && coords.x < size.x && coords.y < size.y;
    }

    private int mapIndex(Int2 coords) {
        return coords.y * size.x + coords.x;
    }

    public enum TileType {
        EMPTY, WALL, PLAYER;

        public int getBitMask() {
            return 1 << (ordinal() - 1) % TILE_CAPACITY;
        }

        public void put(TileField tileField, Int2 coords) {
            int i = tileField.mapIndex(coords);
            if (i < tileField.map.length)
                tileField.map[i] = tileField.map[i] | getBitMask();
        }

        public void remove(TileField tileField, Int2 coords) {
            int i = tileField.mapIndex(coords);
            if (i < tileField.map.length)
                tileField.map[i] = tileField.map[i] & ~getBitMask();
        }
    }

    public static class Tile {
        public final TileField.TileType tileType;
        public final Int2 coords;

        public Tile(TileField.TileType tileType, Int2 coords) {
            this.tileType = tileType;
            this.coords = coords;
        }

        public Tile shift(Int2 v) {
            return new Tile(tileType, coords.add(v));
        }
    }

    public interface TileFieldObject {
        void put(TileField tileField);
        void remove(TileField tileField);
        IntFunction<Tile> tiles();
        int tilesCount();
        boolean collides(Tile tile);

        default boolean collides(TileFieldObject other) {
            for (int i = 0; i < other.tilesCount(); i++) {
                if (this.collides(other.tiles().apply(i)))
                    return true;
            }
            return false;
        }

        /**
         * The {@code other} object is shifted in the specified direction and
         * then the collision is tested.
         */
        default boolean collides(TileFieldObject other, Int2 shift) {
            for (int i = 0; i < other.tilesCount(); i++) {
                if (this.collides(other.tiles().apply(i).shift(shift))) {
                    return true;
                }
            }
            return false;
        }
    }
}
