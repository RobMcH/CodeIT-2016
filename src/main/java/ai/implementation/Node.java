package ai.implementation;

import de.itdesign.codebattle.api.model.Position;

public class Node {

	private Position pos;
	private Node parent;
	private int HCost = 0;
	private char color = 'w';
	private short neighbourCount = 0;
	private Node[] neighbours = new Node[4];

	public int getHCost() {
		return this.HCost;
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

	public Node(int HCost, Position pos) {
		this.HCost = HCost;
		this.pos = pos;
	}

	@Override
	public int hashCode() {
		return this.pos.hashCode();
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

	public short getNeighbourCount() {
		return this.neighbourCount;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getParent() {
		return this.parent;
	}

	public void reset() {
		this.HCost = 0;
		this.parent = null;
		this.color = 'w';
	}

	public char getColor() {
		return color;
	}

	public void setColor(char color) {
		this.color = color;
	}
}
