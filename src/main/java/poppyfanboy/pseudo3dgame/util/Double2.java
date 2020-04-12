package poppyfanboy.pseudo3dgame.util;

public class Double2 {
    public final double x, y;

    public Double2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Int2 toInt() {
        return new Int2((int) x, (int) y);
    }
}
