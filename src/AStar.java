import java.util.HashSet;
import java.util.PriorityQueue;

public class AStar {
    private final static float DIAGONAL_COST_FACTOR = 1.41421356237F; // it costs more to travel diagonally, sqrt(2).
    private final static float NORMAL_WEIGHT = 1.F;
    private final static float OBSTACLE_WEIGHT = 2.F;

    private Node[][] nodes;
    private Pair dimensions;
    private PriorityQueue<Node> openNodes;
    private HashSet<Node> closedNodes;
    private Pair initialNodePos;
    private Pair finalNodePos;

    Heuristic heuristic;

    private void initNodes(CellType[][] grid) {
        for (int x = 0; x < dimensions.x; x++) {
            for (int y = 0; y < dimensions.y; y++) {
                switch (grid[x][y]) {
                    case NORMAL:
                        nodes[x][y] = new Node(new Pair(x, y), NORMAL_WEIGHT);
                        break;
                    case OBSTACLE:
                        nodes[x][y] = new Node(new Pair(x, y), OBSTACLE_WEIGHT);
                        break;
                    case IMPASSABLE:
                        // Node is null
                }
            }
        }
    }

    public AStar (Pair startPos, Pair endPos, Pair dimensions, Heuristic heuristic, CellType[][] grid) {
        this.dimensions = dimensions;
        initNodes(grid);
        openNodes = new PriorityQueue<Node>((Node n1, Node n2) -> {
            return Float.compare(n1.getF(), n2.getF());
        });
        closedNodes = new HashSet<>();

    }

    private class Node {
        // g: the cost of the path from start to this node
        // h: the estimated cost from this node to the end node.
        // f: the total estimated from the start node to the end node through this node.
        // f = g + h
        private float g;
        private float h;
        private Pair pos;
        private float weight;
        private Node parent; // The previous best scoring node.

        public Node (Pair pos, float weight) {
            this.pos = pos;
            this.weight = weight;
            g = weight; // Is calculated when this node is reached by the search.
            h = heuristic.calculate(pos, finalNodePos);
            parent = null;
        }

        public void calculateG(Node parent) {
            this.parent = parent;
            boolean isDiagonal = !(pos.x == parent.pos.x || pos.y == parent.pos.y);
            g = isDiagonal ? g + weight : (g * DIAGONAL_COST_FACTOR) + weight;
        }

        public float getG() {
            return g;
        }

        public float getH() {
            return h;
        }

        public float getF() {
            return g + h;
        }

        public Pair getPos() {
            return pos;
        }
    }
}