package ai.implementation;

import java.util.ArrayList;

import de.itdesign.codebattle.api.model.Position;

public class Node {

    final private String id;
    private Position pos;
    private int GCost = Integer.MAX_VALUE;
    private int HCost = 0;
    private Node parent;
    private ArrayList<Node> neighbours = new ArrayList<Node>(4);

    public int getGCost() {
	return GCost;
    }

    public void setGCost(int GCost) {
	this.GCost = GCost;
    }

    public int getHCost() {
	return this.HCost;
    }

    public int getFCost() {
	return this.GCost + this.HCost;
    }

    public void setHCost(int HCost) {
	this.HCost = HCost;
    }

    public void setHCost(Position target) {
	this.HCost = this.pos.getDistance(target);
    }

    public Node(String id, Position pos) {
	this.id = id;
	this.pos = pos;
    }

    public Node(String id, int GCost, int HCost, Position pos) {
	this.id = id;
	this.GCost = GCost;
	this.HCost = HCost;
	this.pos = pos;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {

	if (this == obj) {
	    return true;
	}

	if (!(obj instanceof Node)) {
	    return false;
	}

	Node other = (Node) obj;
	if (this.id.equals(other.id)) {
	    return true;
	}
	return false;
    }

    public Position getPosition() {
	return this.pos;
    }

    public void setPosition(Position pos) {
	this.pos = pos;
    }

    public String getID() {
	return this.id;
    }

    public void addNeighbour(Node neighbour) {
	this.neighbours.add(neighbour);
    }

    public ArrayList<Node> getNeighbours() {
	return this.neighbours;
    }

    public void setParent(Node parent) {
	this.parent = parent;
    }

    public Node getParent() {
	return this.parent;
    }
}
