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
     * @param   maxRange is the maximum distance (in tiles) that the ray can
     *          travel away from the player. Returns {@code null}
     *          if no obstacle is found within this range.
     */
    public RayCollision playerRayCast(Double2 coords, Rotation angle,
            double maxRange) {
        // distances to the horizontal/vertical walls
        RayCollision h = null, v = null;
        // trigonometry
        double invSin = 1 / angle.sin, invCos = 1 / angle.cos;
        double tan = angle.sin * invCos, invTan = angle.cos * invSin;

        // intersections with "horizontal" walls
        // up and down are shuffled because of the coordinates system
        boolean downwards = angle.sin > 0;
        double yh = downwards ? Math.ceil(coords.y) : Math.floor(coords.y);
        double xh = coords.x + (yh - coords.y) * invTan;
        Double2 hCurrent = new Double2(xh, yh);
        Double2 hStep = new Double2(
                (downwards ? 1 : -1) * invTan, downwards ? 1 : -1);
        double hStepDist = Math.abs(invSin);
        double hd = Math.abs((hCurrent.y - coords.y) * invSin);

        // intersections with "vertical" walls
        boolean right = angle.cos > 0;
        double xv = right ? Math.ceil(coords.x) : Math.floor(coords.x);
        double yv = coords.y + (xv - coords.x) * tan;
        Double2 vCurrent = new Double2(xv, yv);
        Double2 vStep = new Double2(right ? 1 : -1, (right ? 1 : -1) * tan);
        double vStepDist = Math.abs(invCos);
        double vd = Math.abs((vCurrent.y - coords.y) * invSin);

        boolean hHit = false, vHit = false;
        while (vd <= maxRange || hd <= maxRange) {
            // horizontal intersections
            if (!hHit && hd <= maxRange) {
                Int2 tileCoords
                        = hCurrent.add(0, downwards ? 0.1 : -0.1).toInt();
                if (!tileField.isEmpty(tileCoords)) {
                    h = new RayCollision();
                    h.tile = tileCoords;
                    h.hitPoint = downwards
                            ? hCurrent.x % 1
                            : 1 - hCurrent.x % 1;
                    h.d = Math.abs((hCurrent.y - coords.y) * invSin);
                    hHit = true;
                }
            }
            // vertical intersections
            if (!vHit && vd <= maxRange) {
                Int2 tileCoords = vCurrent.add(right ? 0.1 : -0.1, 0).toInt();
                if (!tileField.isEmpty(tileCoords)) {
                    v = new RayCollision();
                    v.tile = tileCoords;
                    v.hitPoint = right ? 1 - vCurrent.y % 1 : vCurrent.y % 1;
                    v.d = Math.abs((vCurrent.x - coords.x) * invCos);
                    vHit = true;
                }
            }
            // optimization
            if (vHit && !hHit && vd < hd) return v;
            if (!vHit && hHit && hd < vd) return h;
            if (vHit && hHit) {
                if (h.d < v.d) return h;
                else return v;
            }
            // extend the rays
            if (!vHit && vd <= maxRange) {
                vCurrent = vCurrent.add(vStep);
                vd += vStepDist;
            }
            if (!hHit && hd <= maxRange) {
                hCurrent = hCurrent.add(hStep);
                hd += hStepDist;
            }
        }
        return null;
    }

    public static class RayCollision {
        public double d = Double.POSITIVE_INFINITY;
        public Int2 tile;
        public double hitPoint;
    }
}
