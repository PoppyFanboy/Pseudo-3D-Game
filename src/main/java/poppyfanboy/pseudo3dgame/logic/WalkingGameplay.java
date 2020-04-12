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
        player = new Player(new Double2(1.5, 1.5));
        level = new Level();
        player.put(tileField);
        level.put(tileField);
    }

    public TileField getTileField() {
        return tileField;
    }

    private void playerControl(Double2 v) {
        if (!level.collides(player, v.toInt())) {
            player.shift(tileField, v);
        }
    }

    @Override
    public void control(Iterator<KeyManager.InputEntry> inputs) {
        Double2 playerShift = new Double2(0, 0);

        while (inputs.hasNext()) {
            var input = inputs.next();
            if (input.state != KeyManager.State.FIRED) {
                continue;
            }
            switch (input.action) {
                case MOVE_UP:
                    playerShift = playerShift.add(0, -1);
                    break;
                case MOVE_DOWN:
                    playerShift = playerShift.add(0, 1);
                    break;
                case MOVE_LEFT:
                    playerShift = playerShift.add(-1, 0);
                    break;
                case MOVE_RIGHT:
                    playerShift = playerShift.add(1, 0);
                    break;
            }
        }

        playerControl(playerShift);
    }
}
