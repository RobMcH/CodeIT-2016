package ai.implementation;

import java.util.ArrayList;

import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.Unit;

public class State {
	private ArrayList<Position> resources;
	ArrayList<Position> enemyUnitPositions;

	public int getNumberOfEnemies() {
		return enemyUnitPositions.size();
	}

	public void setEnemyPositions(ArrayList<Position> enemyUnitPositions) {
		this.enemyUnitPositions = enemyUnitPositions;
	}
	
	public ArrayList<Position> getEnemyPositions() {
		return this.enemyUnitPositions;
	}

	public ArrayList<Position> getResources() {
		return this.resources;
	}

	public void setResources(ArrayList<Position> resources) {
		this.resources = resources;
	}

	public Position getClosestResource(Unit unit) {
		Position unitPos = unit.getPosition();
		Position nearest = null;
		if (this.resources.size() > 0) {
			nearest = this.resources.get(0);
			for (Position pos : resources) {
				if (pos.getDistance(unitPos) < nearest.getDistance(unitPos)) {
					nearest = pos;
				}
			}
		}
		this.resources.remove(nearest);
		return nearest;
	}
	
	public boolean hasResourcesLeft() {
		return !this.resources.isEmpty();
	}
	
	public Position getClosestEnemy(Position unitPos) {
		Position nearest = enemyUnitPositions.get(0);
		for (Position pos : enemyUnitPositions) {
			if (pos.getDistance(unitPos) < nearest.getDistance(unitPos)) {
				nearest = pos;
			}
		}
		return nearest;
	}
}
