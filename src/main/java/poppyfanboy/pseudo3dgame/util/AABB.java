package poppyfanboy.pseudo3dgame.util;

public class AABB {
    public final Double2 min, max;

    public AABB(Double2 min, Double2 max) {
        if (min.x > max.x || min.y > max.y) {
            throw new IllegalArgumentException(String.format(
                    "Expected: min < max. Got: min = %s, max = %s",
                    min, max));
        }
        this.min = min;
        this.max = max;
    }

    /**
     * AABBs are considered to be half-open, so that the adjacent ones do not
     * intersect.
     */
    public boolean inside(Double2 point) {
        return point.x >= min.x && point.x < max.x && point.y >= min.y
                && point.y < max.y;
    }

    /**
     * Returns an offset that pushes the circle inside the AABB in case the
     * center of the specified circle lies within the AABB, and
     * "pushes" the circle outside, if the center is outside of the AABB.
     */
    public Double2 circleCollision(Double2 center, double r) {
        r = Math.max(0, r);
        if (inside(center)) {
            Double2 offset = new Double2(0, 0);
            if (center.y - min.y < r) {
                offset = offset.add(0, r - (center.y - min.y));
            }
            if (max.y - center.y < r) {
                offset = offset.add(0, -r + (max.y - center.y));
            }
            if (center.x - min.x < r) {
                offset = offset.add(r - (center.x - min.x), 0);
            }
            if (max.x - center.x < r) {
                offset = offset.add(-r + (max.x - center.x), 0);
            }
            return offset;
        } else {
            Double2 closest = new Double2(clamp(min.x, max.x, center.x),
                    clamp(min.y, max.y, center.y));
            if (center.dSqr(closest) < r * r) {
                double xOffset = r - Math.abs(center.x - closest.x);
                double yOffset = r - Math.abs(center.y - closest.y);
                // angle case
                if (center.x != closest.x && center.y != closest.y) {
                    double d = center.d(closest);
                    return center.sub(closest).times(r / d - 1);
                }
                if (xOffset < yOffset) {
                    return new Double2(
                            Math.signum(center.x - closest.x) * xOffset, 0);
                } else {
                    return new Double2(
                            0, Math.signum(center.y - closest.y) * yOffset);
                }
            } else return new Double2(0, 0);
        }
    }

    private static double clamp(double min, double max, double value) {
        return Math.min(max, Math.max(min, value));
    }

    @Override
    public String toString() {
        return String.format("[ min = %s, max = %s ]", min, max);
    }
}
