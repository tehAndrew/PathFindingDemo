import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

public class AStar {
    public final static double DIAGONAL_COST_FACTOR = 1.41421356237; // it costs more to travel diagonally, sqrt(2).
    public final static double NORMAL_WEIGHT = 1.;
    public final static double OBSTACLE_WEIGHT = 1.5;

    private enum State {
        SEARCHING,
        PATH_FOUND,
        NO_PATH_FOUND
    }

    private Node[][] nodes;
    private Pair dimensions;
    private PriorityQueue<Node> openNodes;
    private HashSet<Node> closedNodes;
    private Node initialNode;
    private Node endNode;

    private Heuristic heuristic;
    private State state;

    public int tempIter = 0;

    private void initNodes(CellType[][] grid, Pair endPos) {
        nodes = new Node[dimensions.x][dimensions.y];
        for (int x = 0; x < dimensions.x; x++) {
            for (int y = 0; y < dimensions.y; y++) {
                switch (grid[x][y]) {
                    case NORMAL:
                        nodes[x][y] = new Node(new Pair(x, y), NORMAL_WEIGHT, endPos);
                        break;
                    case OBSTACLE:
                        nodes[x][y] = new Node(new Pair(x, y), OBSTACLE_WEIGHT, endPos);
                        break;
                    case IMPASSABLE:
                        // Node is null
                }
            }
        }
    }

    private ArrayList<Node> getNeighboringNodes(Node node) {
        ArrayList<Node> neighNodes = new ArrayList<>();
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                Pair neighPos = new Pair(xOffset + node.pos.x, yOffset + node.pos.y);
                // No node out of bounds
                if (neighPos.x < 0 || neighPos.y < 0 || neighPos.x >= dimensions.x || neighPos.y >= dimensions.y)
                    continue;
                // Add only adjacent nodes
                if (neighPos.equals(node.pos))
                    continue;

                Node neighNode = nodes[neighPos.x][neighPos.y];
                // No node found/blocked space
                if (neighNode == null)
                    continue;

                neighNodes.add(neighNode);
            }
        }
        return neighNodes;
    }

    public AStar (Pair startPos, Pair endPos, Pair dimensions, Heuristic heuristic, CellType[][] grid) {
        this.dimensions = dimensions;
        this.heuristic = heuristic;
        state = State.SEARCHING;

        initNodes(grid, endPos);
        initialNode = nodes[startPos.x][startPos.y];
        endNode = nodes[endPos.x][endPos.y];

        openNodes = new PriorityQueue<Node>((Node n1, Node n2) -> {
            return Double.compare(n1.getF(), n2.getF());
        });
        closedNodes = new HashSet<>();

        // Prepare for search
        openNodes.add(initialNode);
    }

    public boolean iterate() {
        if (state == State.PATH_FOUND || state == State.NO_PATH_FOUND)
            return false;

        if (!openNodes.isEmpty()) {
            Node currentNode = openNodes.poll();
            closedNodes.add(currentNode);
            if (currentNode.pos.equals(endNode.pos)) {
                state = State.PATH_FOUND;
            } else {
                ArrayList<Node> neighNodes = getNeighboringNodes(currentNode);
                for (Node neighNode : neighNodes) {
                    if (openNodes.contains(neighNode)) {
                        if (neighNode.calculateG(currentNode) < neighNode.getG()) {
                            neighNode.setParentAndG(currentNode);
                            openNodes.remove(neighNode);
                            openNodes.add(neighNode);
                        }
                    }
                    else if (closedNodes.contains(neighNode)) {
                        if (neighNode.calculateG(currentNode) < neighNode.getG()) {
                            neighNode.setParentAndG(currentNode);
                            closedNodes.remove(neighNode);
                            openNodes.add(neighNode);
                        }
                    }
                    else {
                        neighNode.setParentAndG(currentNode);
                        openNodes.add(neighNode);
                    }
                }
            }
        } else {
            state = State.NO_PATH_FOUND;
        }

        // Return true if the algorithm is still searching.
        return true;
    }

    public void run() {
        System.out.println("########################################");
        long tStamp = System.currentTimeMillis();
        while (iterate()) {tempIter++;}
        long time = System.currentTimeMillis() - tStamp;

        System.out.println("Operations/Nodes searched: " + tempIter);
        System.out.println("Time: " + time + " ms");
        System.out.println("Shortest Path: " + endNode.g + " units");
        System.out.println("########################################");
    }

    public void draw(GraphicsContext g2d, int gridSide) {
        {
            boolean firstSet = false;
            for (Node currentNode : openNodes) {
                //if (!firstSet) {
                    //g2d.setFill(Color.GREEN);
                    firstSet = true;
                //} else {
                    g2d.setFill(Color.GRAY);
                //}
                g2d.fillRect(currentNode.pos.x * gridSide, currentNode.pos.y * gridSide,
                        gridSide, gridSide);

                /*
                g2d.setFill(Color.BLACK);
                g2d.setFont(new Font(15));
                g2d.setTextAlign(TextAlignment.LEFT);
                g2d.setTextBaseline(VPos.CENTER);

                DecimalFormat twoDForm = new DecimalFormat("#.##");
                g2d.fillText(twoDForm.format(currentNode.getF()), currentNode.pos.x * gridSide + 5, currentNode.pos.y * gridSide + 5);*/
            }
            for (Node currentNode : closedNodes) {
                g2d.setFill(Color.LIGHTGRAY);
                g2d.fillRect(currentNode.pos.x * gridSide, currentNode.pos.y * gridSide,
                        gridSide, gridSide);

                /*g2d.setFill(Color.BLACK);
                g2d.setFont(new Font(15));
                g2d.setTextAlign(TextAlignment.LEFT);
                g2d.setTextBaseline(VPos.CENTER);

                DecimalFormat twoDForm = new DecimalFormat("#.##");
                g2d.fillText(twoDForm.format(currentNode.getF()), currentNode.pos.x * gridSide + 5, currentNode.pos.y * gridSide + 5);*/
            }
        }
        if (state == State.PATH_FOUND) {
            Node currentNode = endNode;
            g2d.setLineWidth(4.0);
            while (currentNode != null) {
                if (currentNode.parent == null)
                    break;
                g2d.setStroke(Color.RED);
                g2d.strokeLine(currentNode.pos.x * gridSide + gridSide / 2, currentNode.pos.y * gridSide + gridSide / 2,
                        currentNode.parent.pos.x * gridSide + gridSide / 2, currentNode.parent.pos.y * gridSide + gridSide / 2);
                currentNode = currentNode.parent;
            }
            g2d.setLineWidth(1.0);
        }
    }

    private class Node {
        // g: the cost of the path from start to this node
        // h: the estimated cost from this node to the end node.
        // f: the total estimated from the start node to the end node through this node.
        // f = g + h
        private double g;
        private double h;
        private Pair pos;
        private double weight;
        private Node parent; // The previous best scoring node.

        public Node (Pair pos, double weight, Pair endPos) {
            this.pos = pos;
            this.weight = weight;
            g = 0; // Is calculated when this node is reached by the search.
            h = heuristic.calculate(pos, endPos);
            parent = null;
        }

        public double calculateG(Node parent) {
            boolean isDiagonal = !(pos.x == parent.pos.x || pos.y == parent.pos.y);
            return isDiagonal ? (weight * DIAGONAL_COST_FACTOR) + parent.getG() : weight + parent.getG();
        }

        public void setParentAndG(Node parent) {
            this.parent = parent;
            g = calculateG(parent);
        }

        public double getG() {
            return g;
        }

        public double getH() {
            return h;
        }

        public double getF() {
            return g + h;
        }

        public Pair getPos() {
            return pos;
        }
    }
}