package poppyfanboy.pseudo3dgame.util;

public class Int2 {
    public final int x, y;

    public Int2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (this.getClass() != other.getClass())
            return false;

        return ((Int2) other).x == this.x && ((Int2) other).y == this.y;
    }
}
