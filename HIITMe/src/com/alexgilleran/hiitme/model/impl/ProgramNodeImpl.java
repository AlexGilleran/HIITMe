package com.alexgilleran.hiitme.model.impl;

import java.util.ArrayList;
import java.util.List;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;

public class ProgramNodeImpl implements ProgramNode {
	private int totalReps;
	private int completedReps;
	private int currentChildIndex;
	private List<ProgramNode> children = new ArrayList<ProgramNode>();
	private Exercise exercise;

	public ProgramNodeImpl(int repCount) {
		this.totalReps = repCount;

		reset();
	}

	protected ProgramNodeImpl(int repCount, Exercise exercise) {
		this(repCount);

		this.exercise = exercise;
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
		ProgramNode newNode = new ProgramNodeImpl(repCount);

		children.add(newNode);

		return newNode;
	}

	@Override
	public Exercise addChildExercise(String name, int duration,
			Exercise.EffortLevel effortLevel, int repCount) {
		Exercise exercise = new ExerciseImpl(name, duration, effortLevel, this);

		children.add(new ProgramNodeImpl(repCount, exercise));

		return exercise;
	}

	@Override
	public void next() {
		if (isFinished()) {
			throw new RuntimeException(
					"next() called on finished node - node must be reset before next is called again");
		}

		if (!this.hasChildren()) {
			nextNode();
		} else {
			ProgramNode currentNode = getCurrentNode();
			currentNode.next();

			if (currentNode.isFinished()) {
				nextNode();
			}
		}

	}

	private boolean hasChildren() {
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
		currentChildIndex = 0;

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
	}

	@Override
	public ProgramNode getCurrentNode() {
		if (isFinished()) {
			throw new RuntimeException(
					"getCurrentNode() called on finished ProgramNode");
		}

		if (children.isEmpty()) {
			return this;
		} else {
			return this.children.get(currentChildIndex);
		}
	}

	@Override
	public Exercise getCurrentExercise() {
		if (exercise != null) {
			return exercise;
		}

		return getCurrentNode().getCurrentExercise();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((children == null) ? 0 : children.hashCode());
		result = prime * result
				+ ((exercise == null) ? 0 : exercise.hashCode());
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
		if (exercise == null) {
			if (other.exercise != null)
				return false;
		} else if (!exercise.equals(other.exercise))
			return false;
		if (totalReps != other.totalReps)
			return false;
		return true;
	}
}
