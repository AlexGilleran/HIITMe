package com.alexgilleran.hiitme.model;

import java.util.HashSet;
import java.util.Set;


public class ProgramNode extends ProgramNodeData {
	private int completedReps;
	private int currentChildIndex;
	private final Set<ProgramNodeObserver> observers = new HashSet<ProgramNodeObserver>();

	public ProgramNode() {
		reset();
	}

	public ProgramNode(int repCount) {
		this();

		setTotalReps(repCount);
	}

	public int getCompletedReps() {
		return completedReps;
	}

	public ProgramNode addChildNode(int repCount) {
		checkCanHaveChildren();

		ProgramNode newNode = new ProgramNode(repCount);
		getChildren().add(newNode);

		return newNode;
	}

	public ExerciseData addChildExercise(String name, int duration,
			Exercise.EffortLevel effortLevel, int repCount) {
		checkCanHaveChildren();

		ProgramNode containerNode = new ProgramNode(repCount);
		Exercise newExercise = new Exercise(name, duration, effortLevel,
				containerNode);
		containerNode.setAttachedExercise(newExercise);

		getChildren().add(containerNode);

		return newExercise;
	}

	private void checkCanHaveChildren() {
		if (getAttachedExercise() != null) {
			throw new RuntimeException(
					"This ProgramNode was created with an attached exercise - it cannot have getChildren().");
		}
	}

	public void next() {
		if (isFinished()) {
			throw new RuntimeException(
					"next() called on finished node - node must be reset before next() is called again");
		}

		if (this.hasChildren()) {
			// the next node.
			ProgramNode currentNode = getCurrentNode();
			currentNode.next();

			if (currentNode.isFinished()) {
				// Current node finished, go to the next one
				nextNode();

				if (!this.isFinished()) {
					getCurrentExercise().getParentNode()
							.triggerExerciseBroadcast();
				}
			}
		} else {
			nextNode();
		}
	}

	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	private void nextNode() {
		currentChildIndex++;

		if (currentChildIndex >= getChildren().size()) {
			nextRep();
		}
	}

	private void nextRep() {
		completedReps++;
		broadcastRepFinish();

		currentChildIndex = 0;

		if (isFinished()) {
			broadcastFinish();
		} else {
			resetChildren();
		}
	}

	public boolean isFinished() {
		return completedReps >= getTotalReps();
	}

	public void reset() {
		completedReps = 0;
		currentChildIndex = 0;

		resetChildren();

		broadcastReset();
	}

	private void resetChildren() {
		for (ProgramNode node : getChildren()) {
			node.reset();
		}
	}

	public ProgramNode getCurrentNode() {
		if (isFinished()) {
			throw new RuntimeException(
					"getCurrentNode() called on finished ProgramNode");
		}

		if (this.hasChildren()) {
			return this.getChildren().get(currentChildIndex);
		} else {
			return this;
		}
	}

	public ExerciseData getCurrentExercise() {
		if (getAttachedExercise() != null) {
			return getAttachedExercise();
		}

		ProgramNode currentNode = getCurrentNode();
		if (currentNode != this) {
			return getCurrentNode().getCurrentExercise();
		} else {
			return null;
		}
	}

	private Set<ProgramNodeObserver> getObservers() {
		return observers;
	}

	private void broadcastFinish() {
		for (ProgramNodeObserver observer : observers) {
			observer.onFinish(this);
		}
	}

	private void broadcastRepFinish() {
		for (ProgramNodeObserver observer : observers) {
			observer.onRepFinish(this, completedReps);
		}
	}

	protected void broadcastNextExercise() {
		for (ProgramNodeObserver observer : getObservers()) {
			observer.onNextExercise(getCurrentExercise());
		}
	}

	private void broadcastReset() {
		for (ProgramNodeObserver observer : getObservers()) {
			observer.onReset(this);
		}
	}

	public void registerObserver(ProgramNodeObserver observer) {
		observers.add(observer);
	}

	public void triggerExerciseBroadcast() {
		this.broadcastNextExercise();
	}

	public void start() {
		ProgramNode node = getCurrentNode();

		if (this == node) {
			broadcastNextExercise();
		} else {
			node.start();
		}
	}

	public int getDuration() {
		if (this.getAttachedExercise() != null) {
			return getAttachedExercise().getDuration() * getTotalReps();
		} else {
			int total = 0;

			for (ProgramNode child : getChildren()) {
				total += child.getDuration();
			}

			return total * getTotalReps();
		}
	}
}