package ai.implementation;

import java.util.ArrayList;

import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.FieldType;
import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.Unit;

public class Graph {
	private final ArrayList<Node> nodes;
	private int width;
	private ClientRoundState roundState;
	private final String playerName;

	public Graph(ClientRoundState roundState, String playerName) {
		this.nodes = new ArrayList<Node>(roundState.getHeight() * roundState.getWidth());
		this.roundState = roundState;
		this.playerName = playerName;
		createGraph();
	}

	public ArrayList<Node> getNodes() {
		return this.nodes;
	}

	private void createGraph() {
		Field[][] map = this.roundState.getMap();
		this.width = map[0].length;
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[0].length; x++) {
				this.nodes.add(new Node(Position.get(x, y)));
			}
		}
		int cx, cy, ox, oy;
		for (Node currentNode : this.nodes) {
			cx = currentNode.getPosition().getX();
			cy = currentNode.getPosition().getY();
			for (Node otherNode : this.nodes) {
				ox = otherNode.getPosition().getX();
				oy = otherNode.getPosition().getY();
				if (((cx - 1 == ox || cx + 1 == ox) && cy == oy) || ((cy - 1 == oy || cy + 1 == oy) && cx == ox)) {
					currentNode.addNeighbour(otherNode);
				}
			}
		}
	}

	public void resetGraph() {
		for (Node currentNode : this.nodes) {
			currentNode.reset();
		}
	}

	public Node getNodeAt(Position pos) {
		return this.nodes.get(pos.getY() * width + pos.getX());
	}

	public void setState(State state, ClientRoundState roundState) {
		ArrayList<Position> resources = new ArrayList<Position>(100);
		ArrayList<Position> enemyUnitPositions = new ArrayList<>(30);
		Node start = null;
		for (Node node : this.nodes) {
			Position nodePos = node.getPosition();
			FieldType nodeType = roundState.getMap()[nodePos.getX()][nodePos.getY()].getType();
			if (nodeType == FieldType.BASE || nodeType == FieldType.LAND) {
				start = node;
				break;
			}
		}
		if (start == null) {
			return;
		}
		this.roundState = roundState;
		setStateHelper(start, resources, enemyUnitPositions);

		state.setEnemyPositions(enemyUnitPositions);
		state.setResources(resources);
	}

	private void setStateHelper(Node currentNode, ArrayList<Position> resources,
			ArrayList<Position> enemyUnitPositions) {
		if (currentNode.getColor() == 'w') {
			currentNode.setColor('g');
			Node adjacentNode;
			Node[] adjacentNodes = currentNode.getNeighbours();
			for (short i = 0; i < currentNode.getNeighbourCount(); i++) {
				adjacentNode = adjacentNodes[i];
				Position pos = adjacentNode.getPosition();
				int posX = pos.getX();
				int posY = pos.getY();
				FieldType posType = this.roundState.getMap()[pos.getX()][pos.getY()].getType();
				if (adjacentNode.getColor() == 'w' && (posType == FieldType.LAND || posType == FieldType.BASE)) {
					Field[][] map = this.roundState.getMap();
					Unit unit = map[posX][posY].getUnitOnField();
					if (map[posX][posY].getResourceCount() > 0) {
						resources.add(pos);
					}
					if (unit != null && !unit.getOwner().equals(this.playerName)) {
						enemyUnitPositions.add(pos);
					}
				}
				setStateHelper(adjacentNode, resources, enemyUnitPositions);
			}
		}
	}
}