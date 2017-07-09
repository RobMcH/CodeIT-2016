package ai.implementation;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Direction;
import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.Unit;

public class MoveAssistance {

	private Graph graph;
	private Field[][] map;
	PriorityQueue<Node> openList = new PriorityQueue<Node>(100, MoveAssistance.nodeComparator);
	HashSet<Node> closedList = new HashSet<Node>(100);
	private static final Comparator<Node> nodeComparator = new Comparator<Node>() {

		@Override
		public int compare(Node o1, Node o2) {
			if (o1.getHCost() < o2.getHCost()) {
				return -1;
			} else if (o1.getHCost() > o2.getHCost()) {
				return 1;
			}
			return 0;
		}
	};

	public MoveAssistance(ClientRoundState roundState) {
		this.map = roundState.getMap();
		this.graph = new Graph(roundState);
	}

	private Node shortestPath(Unit unit, Position target) {
		this.openList.clear();
		this.closedList.clear();

		Node start = this.graph.getNodeAt(unit.getPosition());
		start.setHCost(target);
		this.openList.add(start);

		Node currentNode;
		while (!this.openList.isEmpty()) {
			currentNode = this.openList.remove();
			if (currentNode.getPosition().equals(target)) {
				return currentNode;
			}
			this.closedList.add(currentNode);

			for (Node adjacentNode : currentNode.getNeighbours()) {
				if (!this.map[adjacentNode.getPosition().getX()][adjacentNode.getPosition().getY()].isCrossable(unit)
						|| this.closedList.contains(adjacentNode)) {
					continue;
				} else if ((currentNode.getHCost() + 1) >= adjacentNode.getHCost()
						&& this.openList.contains(adjacentNode)) {
					continue;
				}
				adjacentNode.setParent(currentNode);
				this.openList.remove(adjacentNode);
				adjacentNode.setHCost(target);
				this.openList.add(adjacentNode);
			}
		}
		return null;
	}

	public void setRoundState(ClientRoundState roundState) {
		this.map = roundState.getMap();
	}

	public Direction suggestDirection(Unit unit, Position target) {
		this.graph.resetGraph();
		Node currentNode = shortestPath(unit, target);
		Direction suggestion = Direction.STAY;

		if (currentNode == null || currentNode.getParent() == null) {
			return suggestion;
		}

		Node unitNode = this.graph.getNodeAt(unit.getPosition());
		while (!unitNode.equals(currentNode.getParent())) {
			currentNode = currentNode.getParent();
		}
		
		int x = unit.getPosition().getX(), y = unit.getPosition().getY();
		int dx = currentNode.getPosition().getX() - x;
		int dy = currentNode.getPosition().getY() - y;

		if (dy > 0) {
			suggestion = Direction.SOUTH;
			y++;
		} else if (dy < 0) {
			suggestion = Direction.NORTH;
			y--;
		} else if (dx > 0) {
			suggestion = Direction.EAST;
			x++;
		} else if (dx < 0) {
			suggestion = Direction.WEST;
			x--;
		}

		this.map[x][y].setUnitOnField(unit);
		this.map[unit.getPosition().getX()][unit.getPosition().getY()].setUnitOnField(null);
		return suggestion;
	}
}