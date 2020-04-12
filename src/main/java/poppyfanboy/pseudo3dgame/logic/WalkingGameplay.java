package poppyfanboy.pseudo3dgame.logic;

import java.util.Iterator;
import poppyfanboy.pseudo3dgame.KeyManager;
import poppyfanboy.pseudo3dgame.util.Double2;
import poppyfanboy.pseudo3dgame.util.Int2;

public class WalkingGameplay implements KeyManager.Controllable {
    private TileField tileField = new TileField(new Int2(10, 10));
    private Player player;
    private Level level;

    public WalkingGameplay() {
        // test code
        Player player = new Player(new Double2(1.5, 1.5));
        Level level = new Level();
        player.put(tileField);
        level.put(tileField);
    }

    public TileField getTileField() {
        return tileField;
    }

    @Override
    public void control(Iterator<KeyManager.InputEntry> inputs) {
        while (inputs.hasNext()) {
            var input = inputs.next();
            switch (input.action) {
                case MOVE_UP:
                    break;
                case MOVE_DOWN:
                    break;
                case MOVE_LEFT:
                    break;
                case MOVE_RIGHT:
                    break;
            }
        }
    }
}
