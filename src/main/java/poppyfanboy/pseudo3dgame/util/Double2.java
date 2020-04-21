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

    public Double2 add(double x, double y) {
        return new Double2(this.x + x, this.y + y);
    }

    public Double2 sub(Double2 other) {
        return new Double2(this.x - other.x, this.y - other.y);
    }

    public Double2 times(double coeff) {
        return new Double2(coeff * x, coeff * y);
    }

    public Int2 toInt() {
        return new Int2((int) x, (int) y);
    }

    public Double2 ceil() {
        return new Double2(Math.ceil(x), Math.ceil(y));
    }

    public Double2 floor() {
        return new Double2(Math.floor(x), Math.floor(y));
    }

    public Double2 normalized() {
        double norm = norm();
        return new Double2(x / norm, y / norm);
    }

    public double dSqr(Double2 other) {
        return (other.x - this.x) * (other.x - this.x)
                + (other.y - this.y) * (other.y - this.y);
    }

    public double d(Double2 other) {
        return Math.sqrt((this.x - other.x) * (this.x - other.x)
                + (this.y - other.y) * (this.y - other.y));
    }

    public double norm() {
        return Math.sqrt(x * x + y * y);
    }

    public double normSqr() {
        return x * x + y * y;
    }

    public boolean nonZero() {
        return x != 0 || y != 0;
    }

    public boolean nonZero(double eps) {
        return Math.abs(x) >= eps || Math.abs(y) >= eps;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (this.getClass() != other.getClass())
            return false;

        return ((Double2) other).x == this.x && ((Double2) other).y == this.y;
    }

    @Override
    public String toString() {
        return String.format("(%f, %f)", x, y);
    }
}
