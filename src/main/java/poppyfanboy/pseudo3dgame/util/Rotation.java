package poppyfanboy.pseudo3dgame.util;

public class Rotation {
    public final double cos, sin;

    public Rotation(double angle) {
        cos = Math.cos(angle);
        sin = Math.sin(angle);
    }

    private Rotation(double cos, double sin) {
        this.cos = cos;
        this.sin = sin;
    }

    public Double2 apply(Double2 v) {
        return new Double2(cos * v.x - sin * v.y, sin * v.x + cos * v.y);
    }

    public Rotation combine(Rotation other) {
        return new Rotation(
                this.cos * other.cos - this.sin * other.sin,
                this.sin * other.cos + this.cos * other.sin);
    }

    public double getAngle() {
        return Math.atan2(sin, cos);
    }

    public static double normalizeAngle(double angle) {
        return fMod(angle + Math.PI, 2 * Math.PI) - Math.PI;
    }

    private static double fMod(double x, double y) {
        return x - y * Math.floor(x / y);
    }
}
