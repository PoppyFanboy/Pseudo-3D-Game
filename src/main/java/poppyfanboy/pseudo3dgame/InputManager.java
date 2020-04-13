package poppyfanboy.pseudo3dgame;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import poppyfanboy.pseudo3dgame.logic.Player;
import poppyfanboy.pseudo3dgame.logic.WalkingGameplay;
import static java.awt.event.KeyEvent.*;

public class InputManager extends KeyAdapter {
    private EnumMap<Action, State> actionStates;
    private EnumSet<Action> activeActions;

    private WalkingGameplay walkingGameplay;

    public InputManager(WalkingGameplay walkingGameplay) {
        actionStates = new EnumMap<>(Action.class);
        for (Action action : Action.values()) {
            actionStates.put(action, State.INACTIVE);
        }
        activeActions = EnumSet.noneOf(Action.class);
        this.walkingGameplay = walkingGameplay;
    }

    public synchronized void tick() {
        double forwardVelocity = 0, angleVelocity = 0;
        for (Action action : activeActions) {
            State state = actionStates.get(action);
            if (!state.isActive()) {
                continue;
            }
            switch (action) {
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
        walkingGameplay.setPlayerVelocity(forwardVelocity, angleVelocity);

        for (Action action : activeActions) {
            switch (actionStates.get(action)) {
                case FIRED:
                    actionStates.put(action, State.HELD);
                    break;
                case RELEASED:
                    actionStates.put(action, State.INACTIVE);
                    activeActions.remove(action);
                    break;
            }
        }
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        Action action = Action.parse(e.getKeyCode());
        if (action == null) {
            return;
        }
        if (!actionStates.get(action).isActive()) {
            actionStates.put(action, State.FIRED);
            activeActions.add(action);
        }
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        Action action = Action.parse(e.getKeyCode());
        if (action == null) {
            return;
        }
        if (actionStates.get(action) == State.HELD) {
            actionStates.put(action, State.RELEASED);
            activeActions.add(action);
        }
    }

    public enum Action {
        ROTATE_LEFT, ROTATE_RIGHT, MOVE_FORWARDS, MOVE_BACKWARDS;

        public static Action parse(int keyCode) {
            switch (keyCode) {
                case VK_LEFT:
                case VK_A:
                    return ROTATE_LEFT;
                case VK_RIGHT:
                case VK_D:
                    return ROTATE_RIGHT;
                case VK_UP:
                case VK_W:
                    return MOVE_FORWARDS;
                case VK_DOWN:
                case VK_S:
                    return MOVE_BACKWARDS;
            }
            return null;
        }
    }

    public enum State {
        FIRED, HELD, RELEASED, INACTIVE;

        public boolean isActive() {
            return this == FIRED || this == HELD;
        }
    }
}
