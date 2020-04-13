package poppyfanboy.pseudo3dgame.graphics;

import java.awt.*;
import poppyfanboy.pseudo3dgame.logic.*;
import poppyfanboy.pseudo3dgame.logic.TileField.TileType;
import poppyfanboy.pseudo3dgame.util.Int2;

public class GameMap {
    private final static int TILE_SIZE = 40;

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
                if (!tileField.isEmpty(coords) && tileField.getTile(coords)
                        .tileType == TileType.WALL) {
                    g.setColor(Color.GRAY);
                    g.fillRect(TILE_SIZE * coords.x, TILE_SIZE * coords.y,
                            TILE_SIZE, TILE_SIZE);
                }
            }
        g.setColor(Color.RED);
        Player player = gameplay.player;
        double x = player.getCoords().x * TILE_SIZE;
        double y = player.getCoords().y * TILE_SIZE;
        double r = Player.RADIUS * TILE_SIZE, alpha = player.getAngle();

        g.fillOval((int) (x - r / 2.0), (int) (y - r / 2.0), (int) r, (int) r);
        g.setStroke(new BasicStroke(5));
        g.drawLine((int) x, (int) y, (int) (x + Math.cos(alpha) * TILE_SIZE),
                (int) (y + Math.sin(alpha) * TILE_SIZE));
    }
}
