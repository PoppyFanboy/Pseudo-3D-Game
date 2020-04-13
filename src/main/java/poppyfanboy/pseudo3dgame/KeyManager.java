package poppyfanboy.pseudo3dgame;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import static java.awt.event.KeyEvent.*;

public class KeyManager extends KeyAdapter {
    private EnumMap<Action, State> actionStates;
    private EnumSet<Action> activeActions;
    private List<Controllable> listeners = new LinkedList<>();

    public KeyManager() {
        actionStates = new EnumMap<>(Action.class);
        for (Action action : Action.values()) {
            actionStates.put(action, State.INACTIVE);
        }
        activeActions = EnumSet.noneOf(Action.class);
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
                Iterator<Action> actions = activeActions.iterator();

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
    public void keyPressed(KeyEvent e) {
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
    public void keyReleased(KeyEvent e) {
        Action action = Action.parse(e.getKeyCode());
        if (action == null) {
            return;
        }
        if (actionStates.get(action) == State.HELD) {
            actionStates.put(action, State.RELEASED);
            activeActions.add(action);
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
