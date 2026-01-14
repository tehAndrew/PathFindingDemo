package org.pathfindingdemo.AStar;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

import javafx.scene.text.Font;
import org.pathfindingdemo.Helpers.Pair;

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

    private void initNodes(CellType[][] grid, Pair endPos) {
        nodes = new Node[dimensions.getX()][dimensions.getY()];
        for (int x = 0; x < dimensions.getX(); x++) {
            for (int y = 0; y < dimensions.getY(); y++) {
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
                Pair neighPos = new Pair(xOffset + node.pos.getX(), yOffset + node.pos.getY());
                // No node out of bounds
                if (neighPos.getX() < 0 || neighPos.getY() < 0 ||
                        neighPos.getX() >= dimensions.getX() || neighPos.getY() >= dimensions.getY())
                    continue;
                // Add only adjacent nodes
                if (neighPos.equals(node.pos))
                    continue;

                Node neighNode = nodes[neighPos.getX()][neighPos.getY()];
                // No node found/blocked space
                if (neighNode == null)
                    continue;

                neighNodes.add(neighNode);
            }
        }
        return neighNodes;
    }

    private ArrayList<Node> getNeighboringNodes4(Node node) {
        ArrayList<Node> neighNodes = new ArrayList<>();
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                Pair neighPos = new Pair(xOffset + node.pos.getX(), yOffset + node.pos.getY());

                if (!(node.pos.getX() == neighPos.getX() || node.pos.getY() == neighPos.getY()))
                    continue;
                // No node out of bounds
                if (neighPos.getX() < 0 || neighPos.getY() < 0 ||
                        neighPos.getX() >= dimensions.getX() || neighPos.getY() >= dimensions.getY())
                    continue;
                // Add only adjacent nodes
                if (neighPos.equals(node.pos))
                    continue;

                Node neighNode = nodes[neighPos.getX()][neighPos.getY()];
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
        initialNode = nodes[startPos.getX()][startPos.getY()];
        endNode = nodes[endPos.getX()][endPos.getY()];

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

    public void drawNormal(GraphicsContext g2d, int gridSide) {
        {
            for (Node currentNode : openNodes) {
                g2d.setFill(Color.GRAY);
                g2d.fillRect(currentNode.pos.getX() * gridSide, currentNode.pos.getY() * gridSide,
                        gridSide, gridSide);
            }
            for (Node currentNode : closedNodes) {
                g2d.setFill(Color.LIGHTGRAY);
                g2d.fillRect(currentNode.pos.getX() * gridSide, currentNode.pos.getY() * gridSide,
                        gridSide, gridSide);
            }
        }
        if (state == State.PATH_FOUND) {
            Node currentNode = endNode;
            g2d.setLineWidth(4.0);
            while (currentNode != null) {
                if (currentNode.parent == null)
                    break;
                g2d.setStroke(Color.RED);
                g2d.strokeLine(currentNode.pos.getX() * gridSide + gridSide / 2, currentNode.pos.getY() * gridSide + gridSide / 2,
                        currentNode.parent.pos.getX() * gridSide + gridSide / 2, currentNode.parent.pos.getY() * gridSide + gridSide / 2);
                currentNode = currentNode.parent;
            }
            g2d.setLineWidth(1.0);
        }
        else {
            g2d.setFill(Color.BLUE);
            g2d.fillRect(openNodes.peek().pos.getX() * gridSide, openNodes.peek().pos.getY() * gridSide,
                    gridSide, gridSide);
        }
    }

    public void drawF(GraphicsContext g2d, int gridSide) {
        // Draw F value
        for (Node n : openNodes) {
            g2d.setFill(Color.BLACK);
            g2d.setFont(new Font(20));
            String rounded = String.format("%.1f", n.getF());
            g2d.fillText(rounded, 3 + n.pos.getX() * gridSide, 20 + n.pos.getY() * gridSide);
        }
        for (Node n : closedNodes) {
            g2d.setFill(Color.BLACK);
            g2d.setFont(new Font(20));
            String rounded = String.format("%.1f", n.getF());
            g2d.fillText(rounded, 3 + n.pos.getX() * gridSide, 20 + n.pos.getY() * gridSide);
        }
    }

    public void drawPaths(GraphicsContext g2d, int gridSide) {
        g2d.setLineWidth(2.0);
        for (int x = 0; x < dimensions.getX(); x++) {
            for (int y = 0; y < dimensions.getY(); y++) {
                Node n = nodes[x][y];
                if (n == null)
                    continue;
                if (n.parent == null)
                    continue;

                g2d.setStroke(Color.DARKGRAY);
                g2d.strokeLine(n.pos.getX() * gridSide + gridSide / 2, n.pos.getY() * gridSide + gridSide / 2,
                        n.parent.pos.getX() * gridSide + gridSide / 2, n.parent.pos.getY() * gridSide + gridSide / 2);
            }
        }
        g2d.setLineWidth(1.0);
        if (state == State.PATH_FOUND) {
            Node currentNode = endNode;
            g2d.setLineWidth(4.0);
            while (currentNode != null) {
                if (currentNode.parent == null)
                    break;
                g2d.setStroke(Color.RED);
                g2d.strokeLine(currentNode.pos.getX() * gridSide + gridSide / 2, currentNode.pos.getY() * gridSide + gridSide / 2,
                        currentNode.parent.pos.getX() * gridSide + gridSide / 2, currentNode.parent.pos.getY() * gridSide + gridSide / 2);
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
        double g;
        double h;
        Pair pos;
        double weight;
        Node parent; // The previous best scoring node.

        Node (Pair pos, double weight, Pair endPos) {
            this.pos = pos;
            this.weight = weight;
            g = 0; // Is calculated when this node is reached by the search.
            h = heuristic.calculate(pos, endPos);
            parent = null;
        }

        public double calculateG(Node parent) {
            boolean isDiagonal = !(pos.getX() == parent.pos.getX() || pos.getY() == parent.pos.getY());
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