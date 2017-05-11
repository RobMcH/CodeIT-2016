package ai.implementation;

import java.util.ArrayList;

import de.itdesign.codebattle.api.codeinterface.CodeBattleClientImpl;
import de.itdesign.codebattle.api.model.Base;
import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Direction;
import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.Unit;
import de.itdesign.codebattle.api.model.UnitType;

/**
 * The {@link CodeBattleClientImpl} is the API for communicating with the game
 * server.<br>
 * Use the {@link #log(String)} method to log something.
 * 
 * @author <i>Robert McHardy</i>
 */
public class MyCodeBattleClient extends CodeBattleClientImpl {

	private MoveAssistance moveAssistance;
	private State state = new State();
	private ArrayList<Position> enemyUnitPositions = new ArrayList<Position>(30);

	@Override
	protected void processRound(ClientRoundState roundState) {
		long processStartTime = System.currentTimeMillis();
		if (this.moveAssistance == null) {
			this.moveAssistance = new MoveAssistance(roundState);
		}

		// The base contains all collected resources and hidden units.
		Base base = roundState.getBase();
		setState(roundState);
		int numberOfEnemies = this.state.getNumberOfEnemies();
		ArrayList<Position> resources = this.state.getResources();

		int collectedResources = base.getStoredResources();
		int collectorCost = getGameConfiguration().getUnitCost(UnitType.COLLECTOR);
		int warriorCost = getGameConfiguration().getUnitCost(UnitType.WARRIOR);

		long ownCollectorCount = 0;
		long ownWarriorCount = 0;
		for (Unit u : roundState.getOwnUnits()) {
			if (u.getUnitType() == UnitType.COLLECTOR) {
				ownCollectorCount++;
			} else {
				ownWarriorCount++;
			}
		}

		int collectorCount = 5;
		int warriorCount = 0;

		if (numberOfEnemies == 0) {
			warriorCount = 0;
			collectorCount = 8;
		} else {
			warriorCount = 10;
		}

		while (ownCollectorCount < collectorCount && collectedResources >= collectorCost) {
			roundState.getBase().createUnit(UnitType.COLLECTOR);
			collectedResources -= collectorCost;
			ownCollectorCount++;
		}
		while (ownWarriorCount < warriorCount && collectedResources >= warriorCost) {
			roundState.getBase().createUnit(UnitType.WARRIOR);
			collectedResources -= warriorCost;
			ownWarriorCount++;
		}

		moveAssistance.setRoundState(roundState);

		for (Unit unit : roundState.getOwnUnits()) {

			UnitType unitType = unit.getUnitType();
			Position unitPos = unit.getPosition();

			// If health is less than 75 %, heal.
			if (unitPos == base.getPosition()) {
				if ((unit.getHealth() < (getGameConfiguration().getUnitInitialHealth(unitType) * 0.75))
						&& ((collectedResources > getGameConfiguration().getUnitCost(unitType) / 2))) {
					unit.heal();
					collectedResources -= (getGameConfiguration().getUnitCost(unitType) / 2);
				}

			}

			Direction nextStep = null;
			if (unitType == UnitType.COLLECTOR) {
				// Send collectors to collect resources.
				Position resourcePosition = null;
				if (!isUnitFull(unit)) {
					while ((nextStep == null || nextStep == Direction.STAY) && !resources.isEmpty()) {
						resourcePosition = getResourcePosition(roundState, unit, resources);
						resources.remove(resourcePosition);
						nextStep = this.moveAssistance.suggestDirection(unit, resourcePosition);
					}
				}
			} else if (unitType == UnitType.WARRIOR) {
				// Send warriors to the enemy base.
				if (numberOfEnemies > 0) {
					nextStep = this.moveAssistance.suggestDirection(unit, getClosestEnemy(unitPos, enemyUnitPositions));
				} else if ((numberOfEnemies == 0 || ownCollectorCount == 0) && !isUnitFull(unit)) {
					Position resourcePosition = null;
					while ((nextStep == null || nextStep == Direction.STAY) && !resources.isEmpty()) {
						resourcePosition = getResourcePosition(roundState, unit, resources);
						resources.remove(resourcePosition);
						nextStep = this.moveAssistance.suggestDirection(unit, resourcePosition);
					}
				} else {
					nextStep = this.moveAssistance.suggestDirection(unit, base.getPosition());
				}
			}
			if (nextStep == null || nextStep == Direction.STAY) {
				// Move unit back to base.
				nextStep = this.moveAssistance.suggestDirection(unit, base.getPosition());
			}
			unit.moveAggressively(nextStep);
		}
		this.enemyUnitPositions.clear();
		long processEndTime = System.currentTimeMillis();

		log("Processed round " + roundState.getRoundNumber() + " in " + (processEndTime - processStartTime) + " ms");
	}

	private Position getResourcePosition(ClientRoundState roundState, Unit unit, ArrayList<Position> resources) {
		Position unitPos = unit.getPosition();
		Position nearest = null;
		if (resources.size() > 0) {
			nearest = resources.get(0);
			for (Position pos : resources) {
				if (pos.getDistance(unitPos) < nearest.getDistance(unitPos)) {
					nearest = pos;
				}
			}
		}
		return nearest;
	}

	private void setState(ClientRoundState roundState) {
		ArrayList<Position> resources = new ArrayList<Position>(100);
		int enemyCount = 0;
		Field[][] fieldMap = roundState.getMap();
		for (int x = 1; x < roundState.getWidth() - 1; x++) {
			for (int y = 1; y < roundState.getHeight() - 1; y++) {
				Unit unit = fieldMap[x][y].getUnitOnField();
				if (fieldMap[x][y].getResourceCount() > 10) {
					if (roundState.getHeight() == 35) {
						if (setStateHelper(roundState, x, y, 4)) {
							resources.add(Position.get(x, y));
						}
					} else {
						if ((x > 1 && fieldMap[x - 1][y].getType().isWalkable())
								|| (x < roundState.getWidth() - 2 && fieldMap[x + 1][y].getType().isWalkable())
								|| (y > 1 && fieldMap[x][y - 1].getType().isWalkable())
								|| (y < roundState.getHeight() - 2 && fieldMap[x][y + 1].getType().isWalkable())) {
							resources.add(Position.get(x, y));
						}
					}
				}
				if (unit != null && !unit.getOwner().equals(getGameConfiguration().getOwnPlayerName())) {
					this.enemyUnitPositions.add(enemyCount, Position.get(x, y));
					enemyCount++;
				}
			}
		}
		state.setNumberOfEnemies(enemyCount);
		state.setResources(resources);
	}

	/**
	 * @return true when the unit cannot collect any more resources. In that
	 *         case they can be stored in your {@link Base}.
	 */
	private boolean isUnitFull(Unit unit) {
		return unit.getResourceCount() == getGameConfiguration().getUnitMaxResources(unit.getUnitType());
	}

	private Position getClosestEnemy(Position unitPos, ArrayList<Position> enemyPos) {
		Position nearest = enemyPos.get(0);
		for (Position pos : enemyPos) {
			if (pos.getDistance(unitPos) < nearest.getDistance(unitPos)) {
				nearest = pos;
			}
		}
		return nearest;
	}

	private boolean setStateHelper(ClientRoundState roundState, int x, int y, int range) {
		boolean add = false;
		Field[][] fieldMap = roundState.getMap();

		if (range > 0) {
			if (x > 1 && fieldMap[x - 1][y].getType().isWalkable()) {
				return setStateHelper(roundState, x - 1, y, range - 1);
			}
			if (x < roundState.getWidth() - 2 && fieldMap[x + 1][y].getType().isWalkable()) {
				return setStateHelper(roundState, x + 1, y, range - 1);
			}
			if (y > 1 && fieldMap[x][y - 1].getType().isWalkable()) {
				return setStateHelper(roundState, x, y - 1, range - 1);
			}
			if (y < roundState.getHeight() - 2 && fieldMap[x][y + 1].getType().isWalkable()) {
				return setStateHelper(roundState, x, y + 1, range - 1);
			}
		} else {
			if ((x > 1 && fieldMap[x - 1][y].getType().isWalkable())
					|| (x < roundState.getWidth() - 2 && fieldMap[x + 1][y].getType().isWalkable())
					|| (y > 1 && fieldMap[x][y - 1].getType().isWalkable())
					|| (y < roundState.getHeight() - 2 && fieldMap[x][y + 1].getType().isWalkable())) {
				return true;
			}
		}
		return add;
	}
}
