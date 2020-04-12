package poppyfanboy.pseudo3dgame.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import poppyfanboy.pseudo3dgame.logic.*;
import poppyfanboy.pseudo3dgame.logic.TileField.TileType;
import poppyfanboy.pseudo3dgame.util.Int2;

public class GameMap {
    private WalkingGameplay gameplay;

    public GameMap(WalkingGameplay gameplay) {
        this.gameplay = gameplay;
    }

    public void render(Graphics2D g) {
        if (gameplay == null) {
            return;
        }
        TileField tileField = gameplay.getTileField();
        for (int x = 0; x < tileField.getSize().x; x++)
            for (int y = 0; y < tileField.getSize().y; y++) {
                Int2 coords = new Int2(x, y);
                if (!tileField.isEmpty(coords)) {
                    if (tileField.getTile(coords).tileType == TileType.PLAYER) {
                        g.setColor(Color.RED);
                    } else {
                        g.setColor(Color.GRAY);
                    }
                    g.fillRect(10 * coords.x, 10 * coords.y, 10, 10);
                }
            }
    }
}
