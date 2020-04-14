package poppyfanboy.pseudo3dgame.logic;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import poppyfanboy.pseudo3dgame.util.*;

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

    public TileType getTile(Int2 coords) {
        if (!rangeCheck(coords)) {
            return TileType.EMPTY;
        }
        int tileValue = map[mapIndex(coords)];
        if (tileValue == 0) {
            return TileType.EMPTY;
        }
        TileType tileType = TileType
                .values()[Integer.numberOfTrailingZeros(tileValue) + 1];
        return tileType;
    }

    public Iterator<TileType> getTiles(Int2 coords) {
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
            public TileType next() {
                if (tileIndex == 0) {
                    tileIndex = -1;
                    return TileType.EMPTY;
                }
                int shift = Integer.numberOfTrailingZeros(tileValue);
                TileType tileType = TileType.values()[tileIndex + shift];
                tileIndex += shift + 1;
                tileValue >>= shift + 1;
                if (tileValue == 0) {
                    tileIndex = -1;
                }
                return tileType;
            }
        };
    }

    public boolean conflicts(Int2 coords, TileType tileType) {
        if (isEmpty(coords)) {
            return false;
        }
        Iterator<TileType> iterator = getTiles(coords);
        while (iterator.hasNext()) {
            if (tileType.conflicts(iterator.next()))
                return true;
        }
        return false;
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

        private static final Double2[][] CONVEX_HULLS = {
            // EMPTY
            {  },
            // WALL
            {   new Double2(0, 0), new Double2(0, 1),
                new Double2(1, 1), new Double2(1, 0) },
            // PLAYER
            {   new Double2(0.5 - Player.DIAMETER / 2,
                        0.5 - Player.DIAMETER / 2),
                new Double2(0.5 - Player.DIAMETER / 2,
                        0.5 + Player.DIAMETER / 2),
                new Double2(0.5 + Player.DIAMETER / 2,
                        0.5 + Player.DIAMETER / 2),
                new Double2(0.5 + Player.DIAMETER / 2,
                        0.5 - Player.DIAMETER / 2) }
        };

        public boolean conflicts(TileType other) {
            if (this == other) {
                return false;
            }
            if (this.ordinal() > other.ordinal()) {
                return other.conflicts(this);
            }
            return this == WALL && other == PLAYER;
        }

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

        public List<Double2> getConvexHull() {
            return Arrays.asList(CONVEX_HULLS[ordinal()]);
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

    public abstract static class TileFieldObject {
        private Double2 coords;

        public TileFieldObject(Double2 coords) {
            this.coords = coords;
        }

        public abstract void put(TileField tileField);
        public abstract void remove(TileField tileField);
        public abstract boolean collides(Tile tile);
        public abstract List<Tile> tiles();

        public Double2 getCoords() {
            return coords;
        }

        public void setCoords(Double2 coords) {
            this.coords = coords;
        }

        public boolean collides(TileFieldObject other) {
            for (Tile tile : other.tiles()) {
                if (this.collides(tile))
                    return true;
            }
            return false;
        }

        /**
         * The {@code other} object is shifted in the specified direction and
         * then the collision is tested.
         */
        public boolean collides(TileFieldObject other, Double2 shift) {
            shift = shift.add(coords);
            for (Tile tile : other.tiles()) {
                if (this.collides(tile.shift(shift.toInt())))
                    return true;
            }
            return false;
        }
    }
}
