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

	public ProgramNode() {
		super();
	}

	public ProgramNode(int repCount) {
		this();

		setTotalReps(repCount);
	}

	public void prepareForSave() {

	}

	public ProgramNode addChildNode(int repCount) {
		ProgramNode newNode = new ProgramNode(repCount);

		addChildNode(newNode);

		return newNode;
	}

	public void addChildNode(ProgramNode node) {
		addChildNode(node, getChildren().size());
	}

	public void addChildNode(ProgramNode node, int index) {
		checkCanHaveChildren();

		getChildren().add(index, node);
		node.setParent(this);
	}

	public void removeChild(ProgramNode child) {
		child.setParent(null);
		getChildren().remove(child);
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

	public boolean hasChildren() {
		return !getChildren().isEmpty();
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
		if (attachedExercise != null) {
			attachedExercise.setExerciseGroup(this);
		}
		this.attachedExercise = attachedExercise;
	}

	public ProgramNode getParent() {
		return parent;
	}

	public void setParent(ProgramNode parent) {
		this.parent = parent;
	}

	public int getDepth() {
		if (this.parent == null) {
			return 0;
		} else {
			return parent.getDepth() + 1;
		}
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