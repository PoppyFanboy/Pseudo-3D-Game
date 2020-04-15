package poppyfanboy.pseudo3dgame.logic;

import poppyfanboy.pseudo3dgame.util.*;

public class WalkingGameplay {
    private TileField tileField = new TileField(new Int2(10, 10));
    private Player player;
    private Level level;

    public WalkingGameplay() {
        // test code
        player = new Player(new Double2(1.25, 1.25), tileField);
        level = new Level(new Double2(0, 0));
        player.put(tileField);
        level.put(tileField);
    }

    public void tick() {
        player.tick();
    }

    public TileField getTileField() {
        return tileField;
    }

    public void setPlayerVelocity(double vForward, double vAngle) {
        player.setVelocity(vForward, vAngle);
    }

    public Rotation getPlayerRotation() {
        return player.getRotation();
    }

    public Double2 getPlayerCoords() {
        return player.getCoords();
    }

    /**
     * This method definitely does something.
     *
     * @param   angle is relative to the player's direction of view.
     * @param   maxRange is the maximum distance that the ray can travel away
     *          from the player. If no obstacle is found within this range,
     *          this method returns {@code Double.POSITIVE_INFINITY}.
     */
    public double playerRayCast(double angle, double maxRange) {
        angle += player.getRotation().getAngle();
        angle = Rotation.normalizeAngle(angle);
        // distances to the horizontal/vertical walls
        double hd = Double.POSITIVE_INFINITY, vd = Double.POSITIVE_INFINITY;
        final Double2 coords = player.getCoords();
        // intersections with "horizontal" walls
        if (angle != 0 && angle != Math.PI) {
            // up and down are shuffled because of the coordinates system
            boolean downwards = angle > 0 && angle < Math.PI;
            // coordinates of the first possible collision
            double y = downwards ? Math.ceil(coords.y) : Math.floor(coords.y);
            double x = coords.x + (y - coords.y) / Math.tan(angle);
            Double2 current = new Double2(x, y);
            hd = current.sub(coords).norm();
            // every next collisions are placed evenly (intercept theorem)
            double dStep = Math.abs(1 / Math.sin(angle));
            Double2 step = new Double2(
                    (downwards ? 1 : -1) / Math.tan(angle), downwards ? 1 : -1);
            while (hd <= maxRange) {
                Int2 tileCoords
                        = current.add(0, downwards ? 0.1 : -0.1).toInt();
                if (!tileField.isEmpty(tileCoords)) break;
                current = current.add(step);
                hd += dStep;
                if (hd > maxRange) hd = Double.POSITIVE_INFINITY;
            }
        }
        // intersections with "vertical" walls
        if (angle != -Math.PI / 2 && angle != Math.PI / 2) {
            boolean right = angle > -Math.PI / 2 && angle < Math.PI / 2;
            // first collision
            double x = right ? Math.ceil(coords.x) : Math.floor(coords.x);
            double y = coords.y + (x - coords.x) * Math.tan(angle);
            Double2 current = new Double2(x, y);
            vd = current.sub(coords).norm();
            // next collisions
            double dStep = Math.abs(1 / Math.cos(angle));
            Double2 step = new Double2(
                    right ? 1 : -1, (right ? 1 : -1) * Math.tan(angle));
            while (vd <= maxRange) {
                Int2 tileCoords = current.add(right ? 0.1 : -0.1, 0).toInt();
                if (!tileField.isEmpty(tileCoords)) break;
                current = current.add(step);
                vd += dStep;
                if (vd > maxRange) vd = Double.POSITIVE_INFINITY;
            }
        }
        return Math.min(hd, vd);
    }
}
