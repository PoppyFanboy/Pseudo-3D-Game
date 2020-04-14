package poppyfanboy.pseudo3dgame.util;

public class Int2 {
    public final int x, y;

    public Int2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Int2 add(Int2 other) {
        return new Int2(this.x + other.x, this.y + other.y);
    }

    public Double2 add(Double2 other) {
        return new Double2(this.x + other.x, this.y + other.y);
    }

    public Int2 add(int x, int y) {
        return new Int2(this.x + x, this.y + y);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (this.getClass() != other.getClass())
            return false;

        return ((Int2) other).x == this.x && ((Int2) other).y == this.y;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }
}
