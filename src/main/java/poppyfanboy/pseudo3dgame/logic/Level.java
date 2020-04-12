package poppyfanboy.pseudo3dgame.logic;

import java.util.function.IntFunction;
import poppyfanboy.pseudo3dgame.logic.TileField.TileType;
import poppyfanboy.pseudo3dgame.util.Int2;

public class Level implements TileField.TileFieldObject {
    private static final String map
        = "##########"
        + "#........#"
        + "#..##....#"
        + "#...#....#"
        + "#........#"
        + "#..##....#"
        + "#.#####.##"
        + "#....#...#"
        + "#....#...#"
        + "##########";
    // only solid tiles are listed here
    private static final TileField.Tile[] TILES;
    private static final Int2 size = new Int2(10, 10);

    static {
        int tilesCount = 0;
        for (int i = 0; i < map.length(); i++) {
            if (map.charAt(i) == '#')
                tilesCount++;
        }
        TILES = new TileField.Tile[tilesCount];

        for (int i = 0, tileIndex = 0; i < map.length(); i++)
            if (map.charAt(i) == '#') {
                TILES[tileIndex] = new TileField.Tile(TileType.WALL,
                        new Int2(i % size.x, i / size.y));
                tileIndex++;
            }
    }

    @Override
    public void put(TileField tileField) {
        for (TileField.Tile tile : TILES)
            TileType.WALL.put(tileField, tile.coords);
    }

    @Override
    public void remove(TileField tileField) {
        for (TileField.Tile tile : TILES)
            TileType.WALL.remove(tileField, tile.coords);
    }

    @Override
    public IntFunction<TileField.Tile> tiles() {
        return i -> i >= 0 && i < TILES.length ? TILES[i] : null;
    }

    @Override
    public int tilesCount() {
        return TILES.length;
    }

    @Override
    public boolean collides(TileField.Tile tile) {
        if (tile.tileType != TileType.WALL && tile.tileType != TileType.PLAYER)
            return false;
        return tile.coords.x >= 0 && tile.coords.x < size.x
                && tile.coords.y >= 0 && tile.coords.y < size.y
                && map.charAt(tile.coords.y * size.x + tile.coords.x) == '#';
    }
}