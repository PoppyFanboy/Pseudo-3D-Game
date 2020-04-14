package poppyfanboy.pseudo3dgame.logic;

import java.util.Arrays;
import java.util.List;
import poppyfanboy.pseudo3dgame.logic.TileField.TileType;
import poppyfanboy.pseudo3dgame.util.*;

public class Level extends TileField.TileFieldObject {
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

    private static final Int2 size = new Int2(10, 10);
    // only solid tiles are listed here
    private static final TileField.Tile[] TILES = stringToTiles(map, size);

    public Level(Double2 coords) {
        super(coords);
    }

    @Override
    public void put(TileField tileField) {
        for (TileField.Tile tile : TILES)
            TileType.WALL.put(tileField, getCoords().add(tile.coords).toInt());
    }

    @Override
    public void remove(TileField tileField) {
        for (TileField.Tile tile : TILES) {
            TileType.WALL.remove(tileField,
                    getCoords().add(tile.coords).toInt());
        }
    }

    @Override
    public List<TileField.Tile> tiles() {
        return Arrays.asList(TILES);
    }

    @Override
    public boolean collides(TileField.Tile tile) {
        if (tile.tileType.conflicts(TileType.WALL))
            return false;
        Int2 tileLocalCoords = tile.coords.add(getCoords().times(-1).toInt());
        return tileLocalCoords.x >= 0 && tileLocalCoords.x < size.x
                && tileLocalCoords.y >= 0 && tileLocalCoords.y < size.y
                && map.charAt(tileLocalCoords.y * size.x
                        + tileLocalCoords.x) == '#';
    }

    private static TileField.Tile[] stringToTiles(String map, Int2 size) {
        TileField.Tile[] tiles = null;
        for (int pass = 0; pass < 2; pass++) {
            int tileIndex = 0;
            for (int i = 0; i < map.length(); i++)
                if (map.charAt(i) == '#') {
                    if (pass == 1) {
                        tiles[tileIndex] = new TileField.Tile(TileType.WALL,
                                new Int2(i % size.x, i / size.y));
                    }
                    tileIndex++;
                }
            if (pass == 0)
                tiles = new TileField.Tile[tileIndex];
        }
        return tiles;
    }
}