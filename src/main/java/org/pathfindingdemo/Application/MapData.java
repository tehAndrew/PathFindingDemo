package org.pathfindingdemo.Application;

import org.pathfindingdemo.AStar.CellType;
import org.pathfindingdemo.Helpers.Pair;

public class MapData {
    private Pair startPos;
    private Pair endPos;
    private int width;
    private int height;
    private CellType[][] map;

    public MapData(Pair startPos, Pair endPos, int width, int height, CellType[][] map) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.width = width;
        this.height = height;
        this.map = map;
    }

    public final Pair getStartPos() {
        return startPos;
    }

    public final Pair getEndPos() {
        return endPos;
    }

    public final CellType[][] getMap() {
        return map;
    }

    public final int getMapWidth() {
        return width;
    }

    public final int getMapHeight() {
        return height;
    }
}