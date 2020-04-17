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
     * @param   maxRange is the maximum distance (in tiles) that the ray can
     *          travel away from the player. If no obstacle is found within
     *          this range, this method returns
     *          {@code Double.POSITIVE_INFINITY}.
     */
    public RayCollision playerRayCast(double angle, int maxRange) {
        angle += player.getRotation().getAngle();
        angle = Rotation.normalizeAngle(angle);
        // distances to the horizontal/vertical walls
        RayCollision h = new RayCollision(), v = new RayCollision();
        final Double2 coords = player.getCoords();
        // intersections with "horizontal" walls
        if (angle != 0 && angle != Math.PI) {
            // up and down are shuffled because of the coordinates system
            boolean downwards = angle > 0 && angle < Math.PI;
            // coordinates of the first possible collision
            double y = downwards ? Math.ceil(coords.y) : Math.floor(coords.y);
            double x = coords.x + (y - coords.y) / Math.tan(angle);
            Double2 current = new Double2(x, y);
            int tilesDistance = 0;
            // every next collisions are placed evenly (intercept theorem)
            Double2 step = new Double2(
                    (downwards ? 1 : -1) / Math.tan(angle), downwards ? 1 : -1);
            while (tilesDistance <= maxRange) {
                Int2 tileCoords
                        = current.add(0, downwards ? 0.1 : -0.1).toInt();
                if (!tileField.isEmpty(tileCoords)) {
                    h.tile = tileCoords;
                    // needed for texture sampling
                    h.hitPoint = downwards
                            ? current.x % 1 : 1 - current.x % 1;
                    break;
                }
                current = current.add(step);
                tilesDistance++;
            }
            if (tilesDistance <= maxRange) {
                h.d = Math.abs((current.y - coords.y) / Math.sin(angle));
            }
        }
        // intersections with "vertical" walls
        if (angle != -Math.PI / 2 && angle != Math.PI / 2) {
            boolean right = angle > -Math.PI / 2 && angle < Math.PI / 2;
            // first collision
            double x = right ? Math.ceil(coords.x) : Math.floor(coords.x);
            double y = coords.y + (x - coords.x) * Math.tan(angle);
            Double2 current = new Double2(x, y);
            int tilesDistance = 0;
            // next collisions
            Double2 step = new Double2(
                    right ? 1 : -1, (right ? 1 : -1) * Math.tan(angle));
            while (tilesDistance <= maxRange) {
                Int2 tileCoords = current.add(right ? 0.1 : -0.1, 0).toInt();
                if (!tileField.isEmpty(tileCoords)) {
                    v.tile = tileCoords;
                    v.hitPoint = right ? 1 - current.y % 1 : current.y % 1;
                    break;
                }
                current = current.add(step);
                tilesDistance++;
            }
            if (tilesDistance <= maxRange) {
                v.d = Math.abs((current.x - coords.x) / Math.cos(angle));
            }
        }
        if (h.d < v.d) {
            return h;
        } else {
            return v;
        }
    }

    public static class RayCollision {
        public double d = Double.POSITIVE_INFINITY;
        public Int2 tile;
        public double hitPoint;
    }
}
