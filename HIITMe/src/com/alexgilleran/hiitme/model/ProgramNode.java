package com.alexgilleran.hiitme.model;

import java.util.List;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "program_node")
public class ProgramNode extends Model {
	@Column(name = "total_reps")
	private int totalReps;
	@Column(name = "attached_exercise")
	private Exercise attachedExercise;
	@Column(name = "parent")
	private ProgramNode parent;

	private List<ProgramNode> children;

	private int completedReps;
	private int currentChildIndex;

	public ProgramNode() {
		super();
		reset();
	}

	public ProgramNode(int repCount) {
		this();

		setTotalReps(repCount);
	}

	public void prepareForSave() {

	}

	public int getCompletedReps() {
		return completedReps;
	}

	public ProgramNode addChildNode(int repCount) {
		checkCanHaveChildren();

		ProgramNode newNode = new ProgramNode(repCount);
		getChildren().add(newNode);
		newNode.setParent(this);

		return newNode;
	}

	public Exercise addChildExercise(String name, int duration, EffortLevel effortLevel, int repCount) {
		checkCanHaveChildren();

		ProgramNode containerNode = addChildNode(repCount);
		Exercise newExercise = new Exercise(name, duration, effortLevel, containerNode);
		containerNode.setAttachedExercise(newExercise);

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

		currentChildIndex = 0;

		if (!isFinished()) {
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
	}

	private void resetChildren() {
		for (ProgramNode node : getChildren()) {
			node.reset();
		}
	}

	public ProgramNode getCurrentNode() {
		if (isFinished()) {
			throw new RuntimeException("getCurrentNode() called on finished ProgramNode");
		}

		if (this.hasChildren()) {
			return this.getChildren().get(currentChildIndex);
		} else {
			return this;
		}
	}

	public Exercise getCurrentExercise() {
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

	public int getTotalReps() {
		return totalReps;
	}

	public Exercise getAttachedExercise() {
		return attachedExercise;
	}

	public void setTotalReps(int totalReps) {
		this.totalReps = totalReps;
	}

	public List<ProgramNode> getChildren() {
		if (children == null) {
			children = getMany(ProgramNode.class, "parent");
		}

		return children;
	}

	public void setAttachedExercise(Exercise attachedExercise) {
		this.attachedExercise = attachedExercise;
	}

	public ProgramNode getParent() {
		return parent;
	}

	public void setParent(ProgramNode parent) {
		this.parent = parent;
	}

	@Override
	public void save() {
		ActiveAndroid.beginTransaction();
		try {
			// Hack to get around ActiveAndroid's non-handling of circular
			// dependencies.

			for (ProgramNode child : getChildren()) {
				child.setParent(null);
				child.save();
			}
			Exercise attachedExercise = getAttachedExercise();
			this.setAttachedExercise(null);

			super.save();

			for (ProgramNode child : getChildren()) {
				child.setParent(this);
				child.save();
			}

			if (attachedExercise != null) {
				attachedExercise.save();
				this.setAttachedExercise(attachedExercise);
				super.save();
			}

			ActiveAndroid.setTransactionSuccessful();
		} finally {
			ActiveAndroid.endTransaction();
		}
	}
}