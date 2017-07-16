package ai.implementation;

import de.itdesign.codebattle.api.codeinterface.CodeBattleClientImpl;
import de.itdesign.codebattle.api.model.Base;
import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Direction;
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

	@Override
	protected void processRound(ClientRoundState roundState) {
		long processStartTime = System.currentTimeMillis();
		if (this.moveAssistance == null) {
			this.moveAssistance = new MoveAssistance(roundState, getGameConfiguration().getOwnPlayerName());
		}

		// The base contains all collected resources and hidden units.
		Base base = roundState.getBase();
		this.moveAssistance.getGraph().setState(this.state, roundState);

		int numberOfEnemies = this.state.getNumberOfEnemies();
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
					while ((nextStep == null || nextStep == Direction.STAY) && this.state.hasResourcesLeft()) {
						resourcePosition = this.state.getClosestResource(unit);
						nextStep = this.moveAssistance.suggestDirection(unit, resourcePosition);
					}
				}
			} else if (unitType == UnitType.WARRIOR) {
				// Send warriors to the enemy base.
				if (numberOfEnemies > 0) {
					nextStep = this.moveAssistance.suggestDirection(unit, this.state.getClosestEnemy(unitPos));
				} else if ((numberOfEnemies == 0 || ownCollectorCount == 0) && !isUnitFull(unit)) {
					Position resourcePosition = null;
					while ((nextStep == null || nextStep == Direction.STAY) && this.state.hasResourcesLeft()) {
						resourcePosition = this.state.getClosestResource(unit);
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
			log("Moving unit " + unit + " towards " + nextStep);
			unit.moveAggressively(nextStep);
		}
		long processEndTime = System.currentTimeMillis();

		log("Processed round " + roundState.getRoundNumber() + " in " + (processEndTime - processStartTime) + " ms");
	}

	/**
	 * @return true when the unit cannot collect any more resources. In that case
	 *         they can be stored in your {@link Base}.
	 */
	private boolean isUnitFull(Unit unit) {
		return unit.getResourceCount() == getGameConfiguration().getUnitMaxResources(unit.getUnitType());
	}
}
