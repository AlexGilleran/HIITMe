package com.alexgilleran.hiitme.programrunner;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;

public class ProgramNodeState {
	private Node wrappedNode;
	private ProgramNodeState[] children;

	private int completedReps;
	private int currentChildIndex;

	public ProgramNodeState(Node node) {
		this.wrappedNode = node;

		children = new ProgramNodeState[node.getChildren().size()];
		for (int i = 0; i < node.getChildren().size(); i++) {
			children[i] = new ProgramNodeState(node.getChildren().get(i));
		}
		reset();
	}

	public int getCompletedReps() {
		return completedReps;
	}

	private void nextNode() {
		currentChildIndex++;

		if (currentChildIndex >= children.length) {
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
		return completedReps >= wrappedNode.getTotalReps();
	}

	public void reset() {
		completedReps = 0;
		currentChildIndex = 0;

		resetChildren();
	}

	private void resetChildren() {
		for (ProgramNodeState child : children) {
			child.reset();
		}
	}

	public Node getCurrentNode() {
		return getCurrentNodeState().wrappedNode;
	}

	private ProgramNodeState getCurrentNodeState() {
		if (isFinished()) {
			throw new RuntimeException("getCurrentNode() called on finished ProgramNode");
		}

		if (wrappedNode.hasChildren()) {
			return children[currentChildIndex];
		} else {
			return this;
		}
	}

	public Exercise getCurrentExercise() {
		if (wrappedNode.getAttachedExercise() != null) {
			return wrappedNode.getAttachedExercise();
		}

		ProgramNodeState currentNodeState = getCurrentNodeState();
		if (currentNodeState != this) {
			return currentNodeState.getCurrentExercise();
		} else {
			return null;
		}
	}

	public void next() {
		if (isFinished()) {
			throw new RuntimeException(
					"next() called on finished node - node must be reset before next() is called again");
		}

		if (wrappedNode.hasChildren()) {
			// the next node.
			ProgramNodeState currentNodeState = getCurrentNodeState();
			currentNodeState.next();

			if (currentNodeState.isFinished()) {
				// Current node finished, go to the next one
				nextNode();
			}
		} else {
			nextNode();
		}
	}
}
