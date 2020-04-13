package poppyfanboy.pseudo3dgame.logic;

import java.util.Iterator;
import poppyfanboy.pseudo3dgame.KeyManager;
import poppyfanboy.pseudo3dgame.util.Double2;
import poppyfanboy.pseudo3dgame.util.Int2;

public class WalkingGameplay implements KeyManager.Controllable {
    private TileField tileField = new TileField(new Int2(10, 10));
    public Player player;
    public Level level;

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

    public void tick() {
        player.tick();
    }

    @Override
    public void control(Iterator<KeyManager.InputEntry> inputs) {
        double forwardVelocity = 0, angleVelocity = 0;
        while (inputs.hasNext()) {
            var input = inputs.next();
            if (!input.state.isActive()) {
                continue;
            }
            switch (input.action) {
                case MOVE_FORWARDS:
                    forwardVelocity += Player.FORWARD_VELOCITY;
                    break;
                case MOVE_BACKWARDS:
                    forwardVelocity -= Player.FORWARD_VELOCITY;
                    break;
                case ROTATE_LEFT:
                    angleVelocity -= Player.ANGLE_VELOCITY;
                    break;
                case ROTATE_RIGHT:
                    angleVelocity += Player.ANGLE_VELOCITY;
                    break;
            }
        }
        player.setAngleVelocity(angleVelocity);
        player.setForwardVelocity(forwardVelocity);
    }
}
