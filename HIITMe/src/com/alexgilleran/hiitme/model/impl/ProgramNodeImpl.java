package com.alexgilleran.hiitme.model.impl;

import java.util.ArrayList;
import java.util.List;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;

public class ProgramNodeImpl implements ProgramNode {
	private int totalReps;
	private int completedReps;
	private int currentChildIndex;
	private List<ProgramNode> children = new ArrayList<ProgramNode>();
	private Exercise attachedExercise;
	private List<ProgramNodeObserver> observers = new ArrayList<ProgramNodeObserver>();

	public ProgramNodeImpl(int repCount) {
		this.totalReps = repCount;

		reset();
	}

	protected ProgramNodeImpl(ProgramNode parent, int repCount,
			Exercise exercise) {
		this(repCount);

		this.attachedExercise = exercise;
	}

	@Override
	public int getTotalReps() {
		return totalReps;
	}

	@Override
	public int getCompletedReps() {
		return completedReps;
	}

	@Override
	public List<ProgramNode> getChildren() {
		return children;
	}

	@Override
	public ProgramNode addChildNode(int repCount) {
		checkCanHaveChildren();

		ProgramNode newNode = new ProgramNodeImpl(repCount);
		children.add(newNode);

		return newNode;
	}

	@Override
	public Exercise addChildExercise(String name, int duration,
			Exercise.EffortLevel effortLevel, int repCount) {
		checkCanHaveChildren();

		ProgramNodeImpl containerNode = new ProgramNodeImpl(repCount);
		Exercise newExercise = new ExerciseImpl(name, duration, effortLevel,
				containerNode);
		containerNode.setAttachedExercise(newExercise);

		children.add(containerNode);

		return newExercise;
	}

	private void checkCanHaveChildren() {
		if (attachedExercise != null) {
			throw new RuntimeException(
					"This ProgramNode was created with an attached exercise - it cannot have children.");
		}
	}

	@Override
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

	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	private void nextNode() {
		currentChildIndex++;

		if (currentChildIndex >= children.size()) {
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

	private void resetChildren() {
		for (ProgramNode node : children) {
			node.reset();
		}
	}

	@Override
	public boolean isFinished() {
		return completedReps >= totalReps;
	}

	@Override
	public void reset() {
		completedReps = 0;
		currentChildIndex = 0;

		resetChildren();
	}

	@Override
	public ProgramNode getCurrentNode() {
		if (isFinished()) {
			throw new RuntimeException(
					"getCurrentNode() called on finished ProgramNode");
		}

		if (this.hasChildren()) {
			return this.children.get(currentChildIndex);
		} else {
			return this;
		}
	}

	@Override
	public Exercise getCurrentExercise() {
		if (attachedExercise != null) {
			return attachedExercise;
		}

		return getCurrentNode().getCurrentExercise();
	}

	@Override
	public Exercise getAttachedExercise() {
		return attachedExercise;
	}

	private void setAttachedExercise(Exercise exercise) {
		this.attachedExercise = exercise;
	}

	protected List<ProgramNodeObserver> getObservers() {
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

	private void broadcastNextExercise() {
		for (ProgramNodeObserver observer : getObservers()) {
			observer.onNextExercise(getCurrentExercise());
		}
	}

	public void registerObserver(ProgramNodeObserver observer) {
		observers.add(observer);
	}

	@Override
	public void triggerExerciseBroadcast() {
		this.broadcastNextExercise();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((children == null) ? 0 : children.hashCode());
		result = prime
				* result
				+ ((attachedExercise == null) ? 0 : attachedExercise.hashCode());
		result = prime * result + totalReps;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProgramNodeImpl other = (ProgramNodeImpl) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (attachedExercise == null) {
			if (other.attachedExercise != null)
				return false;
		} else if (!attachedExercise.equals(other.attachedExercise))
			return false;
		if (totalReps != other.totalReps)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProgramNodeImpl [children=" + children + ", exercise="
				+ attachedExercise + "]";
	}
}
