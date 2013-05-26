package com.alexgilleran.hiitme.model.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alexgilleran.hiitme.data.ProgramDao;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;

public class ProgramNodeImpl implements ProgramNode {
	public static final long DEFAULT_PROGRAMME_NODE_ID = -1;

	private ProgramDao dao;
	private long programNodeId = DEFAULT_PROGRAMME_NODE_ID;

	private int totalReps;
	private int completedReps;
	private int currentChildIndex;
	private List<ProgramNode> children = new ArrayList<ProgramNode>();
	private Exercise attachedExercise;
	private Set<ProgramNodeObserver> observers = new HashSet<ProgramNodeObserver>();

	public ProgramNodeImpl(ProgramDao dao, int repCount) {
		this.dao = dao;
		this.totalReps = repCount;

		reset();
	}

	public ProgramNodeImpl(ProgramDao dao, int repCount, long programNodeId) {
		this(dao, repCount);

		this.programNodeId = programNodeId;
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
		if (children == null) {
			if (existsInDatabase()) {
				children = dao.getChildrenOfNode(programNodeId);
			} else {
				children = new ArrayList<ProgramNode>();
			}
		}

		return children;
	}

	private boolean existsInDatabase() {
		return programNodeId != DEFAULT_PROGRAMME_NODE_ID;
	}

	@Override
	public ProgramNode addChildNode(int repCount) {
		checkCanHaveChildren();

		ProgramNode newNode = new ProgramNodeImpl(dao, repCount);
		getChildren().add(newNode);

		return newNode;
	}

	@Override
	public Exercise addChildExercise(String name, int duration,
			Exercise.EffortLevel effortLevel, int repCount) {
		checkCanHaveChildren();

		ProgramNodeImpl containerNode = new ProgramNodeImpl(dao, repCount);
		Exercise newExercise = new ExerciseImpl(name, duration, effortLevel,
				containerNode);
		containerNode.setAttachedExercise(newExercise);

		getChildren().add(containerNode);

		return newExercise;
	}

	private void checkCanHaveChildren() {
		if (attachedExercise != null) {
			throw new RuntimeException(
					"This ProgramNode was created with an attached exercise - it cannot have getChildren().");
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

	@Override
	public boolean isFinished() {
		return completedReps >= totalReps;
	}

	@Override
	public void reset() {
		completedReps = 0;
		currentChildIndex = 0;

		resetChildren();

		broadcastReset();
	}

	private void resetChildren() {
		for (ProgramNode node : children) {
			node.reset();
		}
	}

	@Override
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

	@Override
	public Exercise getCurrentExercise() {
		if (attachedExercise != null) {
			return attachedExercise;
		}

		ProgramNode currentNode = getCurrentNode();
		if (currentNode != this) {
			return getCurrentNode().getCurrentExercise();
		} else {
			return null;
		}
	}

	@Override
	public Exercise getAttachedExercise() {
		if (attachedExercise == null) {
			attachedExercise = dao.getExerciseForNode(programNodeId);
		}

		return attachedExercise;
	}

	private void setAttachedExercise(Exercise exercise) {
		this.attachedExercise = exercise;
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

	@Override
	public void triggerExerciseBroadcast() {
		this.broadcastNextExercise();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((children == null) ? 0 : getChildren().hashCode());
		result = prime
				* result
				+ ((attachedExercise == null) ? 0 : getAttachedExercise()
						.hashCode());
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
		} else if (!getChildren().equals(other.children))
			return false;
		if (attachedExercise == null) {
			if (other.attachedExercise != null)
				return false;
		} else if (!getAttachedExercise().equals(other.attachedExercise))
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

	@Override
	public void start() {
		ProgramNode node = getCurrentNode();

		if (this == node) {
			broadcastNextExercise();
		} else {
			node.start();
		}
	}

	@Override
	public int getDuration() {
		if (this.attachedExercise != null) {
			return getAttachedExercise().getDuration() * totalReps;
		} else {
			int total = 0;

			for (ProgramNode child : children) {
				total += child.getDuration();
			}

			return total * totalReps;
		}
	}
}