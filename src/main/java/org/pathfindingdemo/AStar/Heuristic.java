package org.pathfindingdemo.AStar;

import org.pathfindingdemo.Helpers.Pair;

public interface Heuristic {
    double calculate(Pair fromPos, Pair endPos);
}
