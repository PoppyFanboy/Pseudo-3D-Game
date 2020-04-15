package poppyfanboy.pseudo3dgame.graphics;

import java.awt.*;
import poppyfanboy.pseudo3dgame.logic.*;
import poppyfanboy.pseudo3dgame.util.Double2;
import poppyfanboy.pseudo3dgame.util.Int2;

public class GameMap {
    private final static int TILE_SIZE = 10;

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
                switch (tileField.getTile(coords)) {
                    case EMPTY:
                        g.setColor(Color.WHITE);
                        break;
                    case PLAYER:
                        g.setColor(Color.ORANGE);
                        break;
                    default:
                        g.setColor(Color.DARK_GRAY);
                        break;
                }
                g.fillRect(TILE_SIZE * coords.x, TILE_SIZE * coords.y,
                        TILE_SIZE, TILE_SIZE);
            }

        g.setColor(Color.RED);
        Double2 coords = gameplay.getPlayerCoords().times(TILE_SIZE);
        Double2 faceDirection = gameplay.getPlayerRotation()
                .apply(new Double2(TILE_SIZE / 3.0, 0));
        double r = Player.DIAMETER * TILE_SIZE;
        g.fillOval((int) (coords.x - r / 2), (int) (coords.y - r / 2),
                (int) r, (int) r);

        g.setStroke(new BasicStroke(2));
        g.drawLine((int) coords.x, (int) coords.y,
                (int) coords.add(faceDirection).x,
                (int) coords.add(faceDirection).y);
    }
}
