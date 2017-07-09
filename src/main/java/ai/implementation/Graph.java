package ai.implementation;

import java.util.ArrayList;

import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.Position;

public class Graph {
	private final ArrayList<Node> nodes;
	private int width;

	public Graph(ClientRoundState roundState) {
		this.nodes = new ArrayList<Node>(roundState.getHeight() * roundState.getWidth());
		createGraph(roundState);
	}

	public ArrayList<Node> getNodes() {
		return this.nodes;
	}

	private void createGraph(ClientRoundState roundState) {
		Field[][] map = roundState.getMap();
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
}