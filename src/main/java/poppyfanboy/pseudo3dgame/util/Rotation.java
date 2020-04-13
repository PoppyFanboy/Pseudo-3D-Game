package poppyfanboy.pseudo3dgame.util;

public class Rotation {
    private double cosX, sinX;

    public Rotation(double angle) {
        cosX = Math.cos(angle);
        sinX = Math.sin(angle);
    }

    private Rotation(double cosX, double sinX) {
        this.cosX = cosX;
        this.sinX = sinX;
    }

    public Double2 apply(Double2 v) {
        return new Double2(cosX * v.x - sinX * v.y, sinX * v.x + cosX * v.y);
    }

    public Rotation combine(Rotation other) {
        return new Rotation(
                this.cosX * other.cosX - this.sinX * other.sinX,
                this.sinX * other.cosX + this.cosX * other.sinX);
    }

    public double getAngle() {
        return Math.atan2(sinX, cosX);
    }
}
