package byow.Map;

public class Rectangle {
    public int x1, y1, x2, y2;

    public Rectangle(int x,int y,int w,int h) {
        this.x1 = x;
        this.y1 = y;
        this.x2 = x1 + w;
        this.y2 = y1 + h;
    }

    public boolean intersect(Rectangle other) {
        return !notIntersect(other);
    }

    private boolean notIntersect(Rectangle other) {
        return x1 > other.x2 || x2 < other.x1 || y1 > other.y2 || y2 < other.y1;
    }

    public Position center() {
        return new Position((x1 + x2) /2, (y1 + y2) / 2);
    }
}