package ai.implementation;

import de.itdesign.codebattle.api.model.Position;

public class Node {

	private Position pos;
	private int GCost = Integer.MAX_VALUE;
	private int HCost = 0;
	private Node parent;
	private Node[] neighbours = new Node[4];
	private int neighbourCount = 0;

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

	public Node(Position pos) {
		this.pos = pos;
	}

	public Node(int GCost, int HCost, Position pos) {
		this.GCost = GCost;
		this.HCost = HCost;
		this.pos = pos;
	}

	@Override
	public int hashCode() {
		return this.neighbours.hashCode();
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
		if (this.neighbours.equals(other.getNeighbours())) {
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

	public void addNeighbour(Node neighbour) {
		this.neighbours[this.neighbourCount] = neighbour;
		this.neighbourCount++;
	}

	public Node[] getNeighbours() {
		return this.neighbours;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getParent() {
		return this.parent;
	}
}
