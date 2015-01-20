package com.alexgilleran.hiitme.model;

import java.util.ArrayList;
import java.util.List;

public class Node extends DatabaseModel {
	private int totalReps;
	private Exercise attachedExercise;
	private Node parent;

	private List<Node> children;

	public Node() {
		this(1);
	}

	public Node(int repCount) {
		super();

		setTotalReps(repCount);
	}

	public Node addChildNode(int repCount) {
		Node newNode = new Node(repCount);

		addChildNode(newNode);

		return newNode;
	}

	public void addChildNode(Node node) {
		addChildNode(node, getChildren().size());
	}

	public void addChildNode(Node node, int index) {
		checkCanHaveChildren();

		getChildren().add(index, node);
		node.setParent(this);
	}

	public void removeChild(Node child) {
		child.setParent(null);
		getChildren().remove(child);
	}

	public Exercise addChildExercise(String name, int duration, EffortLevel effortLevel, int repCount) {
		checkCanHaveChildren();

		Node containerNode = addChildNode(repCount);
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

			for (Node child : getChildren()) {
				total += child.getDuration();
			}

			return total * getTotalReps();
		}
	}

	public int getTotalReps() {
		return totalReps;
	}

	public boolean isEmpty() {
		return !hasChildren() && getAttachedExercise() == null;
	}

	public Exercise getAttachedExercise() {
		return attachedExercise;
	}

	public void setTotalReps(int totalReps) {
		this.totalReps = totalReps;
	}

	public List<Node> getChildren() {
		if (children == null) {
			// TODO
			children = new ArrayList<Node>();
		}

		return children;
	}

	public void setAttachedExercise(Exercise attachedExercise) {
		if (attachedExercise != null) {
			attachedExercise.setNode(this);
		}
		this.attachedExercise = attachedExercise;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public int getDepth() {
		if (this.parent == null) {
			return 0;
		} else {
			return parent.getDepth() + 1;
		}
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

}