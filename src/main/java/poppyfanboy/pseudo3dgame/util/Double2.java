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

    public Double2 add(Int2 other) {
        return new Double2(this.x + other.x, this.y + other.y);
    }

    public Double2 times(double coeff) {
        return new Double2(coeff * x, coeff * y);
    }

    public Int2 toInt() {
        return new Int2((int) x, (int) y);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (this.getClass() != other.getClass())
            return false;

        return ((Double2) other).x == this.x && ((Double2) other).y == this.y;
    }
}
