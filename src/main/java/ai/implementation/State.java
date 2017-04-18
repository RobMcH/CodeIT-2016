package ai.implementation;

import java.util.ArrayList;

import de.itdesign.codebattle.api.model.Position;

public class State {
    private int numberOfEnemies = 0;
    private ArrayList<Position> resources;

    public int getNumberOfEnemies() {
	return numberOfEnemies;
    }

    public void setNumberOfEnemies(int numberOfEnemies) {
	this.numberOfEnemies = numberOfEnemies;
    }

    public ArrayList<Position> getResources() {
	return resources;
    }

    public void setResources(ArrayList<Position> resources) {
	this.resources = resources;
    }

}
