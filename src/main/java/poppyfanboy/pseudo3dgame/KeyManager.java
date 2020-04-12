package poppyfanboy.pseudo3dgame;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import static java.awt.event.KeyEvent.*;

public class KeyManager extends KeyAdapter {
    private EnumMap<Action, State> actionStates;
    // actions updated during the current tick
    private EnumSet<Action> updatedActions;
    private List<Controllable> listeners = new LinkedList<>();

    public KeyManager() {
        actionStates = new EnumMap<>(Action.class);
        for (Action action : Action.values()) {
            actionStates.put(action, State.INACTIVE);
        }
        updatedActions = EnumSet.noneOf(Action.class);
    }

    public void addListener(Controllable listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Controllable listener) {
        listeners.remove(listener);
    }

    public synchronized void tick() {
        for (Controllable listener : listeners) {
            listener.control(new Iterator<>() {
                Iterator<Action> actions = updatedActions.iterator();

                @Override
                public boolean hasNext() {
                    return actions.hasNext();
                }

                @Override
                public InputEntry next() {
                    Action action = actions.next();
                    State state = actionStates.get(action);
                    return new InputEntry(action, state);
                }
            });
        }

        for (Action action : updatedActions) {
            switch (actionStates.get(action)) {
                case FIRED:
                    actionStates.put(action, State.HELD);
                    break;
                case RELEASED:
                    actionStates.put(action, State.INACTIVE);
                    break;
                default:
                    updatedActions.remove(action);
                    break;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Action action = Action.parse(e.getKeyCode());
        if (action == null) {
            return;
        }
        if (!actionStates.get(action).isActive()) {
            actionStates.put(action, State.FIRED);
            updatedActions.add(action);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Action action = Action.parse(e.getKeyCode());
        if (action == null) {
            return;
        }
        if (actionStates.get(action) == State.HELD) {
            actionStates.put(action, State.RELEASED);
            updatedActions.add(action);
        }
    }

    public interface Controllable {
        void control(Iterator<InputEntry> inputs);
    }

    public static class InputEntry {
        public final Action action;
        public final State state;

        public InputEntry(Action action, State state) {
            this.action = action;
            this.state = state;
        }
    }

    public enum Action {
        MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN;

        public static Action parse(int keyCode) {
            switch (keyCode) {
                case VK_LEFT:
                case VK_A:
                    return MOVE_LEFT;
                case VK_RIGHT:
                case VK_D:
                    return MOVE_RIGHT;
                case VK_UP:
                case VK_W:
                    return MOVE_UP;
                case VK_DOWN:
                case VK_S:
                    return MOVE_DOWN;
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
