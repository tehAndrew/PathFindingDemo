package org.pathfindingdemo.Helpers;

public class Pair {
    private final int x;
    private final int y;

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Pair))
            return false;

        Pair p = (Pair) o;

        return x == p.x && y == p.y;
    }
}