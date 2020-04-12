package poppyfanboy.pseudo3dgame.util;

public class Double2 {
    public final double x, y;

    public Double2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Double2 add(Double2 other) {
        return new Double2(this.x + other.x, this.y + other.y);
    }

    public Double2 add(double x, double y) {
        return new Double2(this.x + x, this.y + y);
    }

    public Int2 toInt() {
        return new Int2((int) x, (int) y);
    }
}
