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

    public double getPlayerForwardVelocity() {
        return player.getForwardVelocity();
    }

    public Rotation getPlayerAngleVelocity() {
        return player.getAngleVelocity();
    }

    /**
     * This method definitely does something.
     *
     * @param   maxRange is the maximum distance (in tiles) that the ray can
     *          travel away from the player. If no obstacle is found within
     *          this range, this method returns
     *          {@code Double.POSITIVE_INFINITY}.
     */
    public RayCollision playerRayCast(Double2 coords, Rotation angle,
            int maxRange) {
        // distances to the horizontal/vertical walls
        RayCollision h = new RayCollision(), v = new RayCollision();

        // intersections with "horizontal" walls
        // up and down are shuffled because of the coordinates system
        boolean downwards = angle.sin > 0;
        // coordinates of the first possible collision
        double invSin = 1 / angle.sin, invCos = 1 / angle.cos;
        double tan = invCos / invSin, invTan = 1 / tan;
        double y = downwards ? Math.ceil(coords.y) : Math.floor(coords.y);
        double x = coords.x + (y - coords.y) * invTan;
        Double2 current = new Double2(x, y);
        int tilesDistance = 0;
        // every next collisions are placed evenly (intercept theorem)
        Double2 step = new Double2(
                (downwards ? 1 : -1) * invTan, downwards ? 1 : -1);
        while (tilesDistance <= maxRange) {
            Int2 tileCoords = current.add(0, downwards ? 0.1 : -0.1).toInt();
            if (!tileField.isEmpty(tileCoords)) {
                h.tile = tileCoords;
                // needed for texture sampling
                h.hitPoint = downwards ? current.x % 1 : 1 - current.x % 1;
                break;
            }
            current = current.add(step);
            tilesDistance++;
        }
        if (tilesDistance <= maxRange) {
            h.d = Math.abs((current.y - coords.y) * invSin);
        }

        // intersections with "vertical" walls
        boolean right = angle.cos > 0;
        // first collision
        x = right ? Math.ceil(coords.x) : Math.floor(coords.x);
        y = coords.y + (x - coords.x) * tan;
        current = new Double2(x, y);
        tilesDistance = 0;
        // next collisions
        step = new Double2(right ? 1 : -1, (right ? 1 : -1) * tan);
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
            v.d = Math.abs((current.x - coords.x) * invCos);
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
