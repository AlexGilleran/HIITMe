/*
 * HIIT Me, a High Intensity Interval Training app for Android
 * Copyright (C) 2015 Alex Gilleran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alexgilleran.hiitme.programrunner;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;

import java.util.ArrayList;
import java.util.List;

public class ProgramNodeState {
	private Node wrappedNode;
	private List<ProgramNodeState> children;

	private int completedReps;
	private int currentChildIndex;

	public ProgramNodeState(Node node) {
		this.wrappedNode = node;

		children = new ArrayList<ProgramNodeState>(node.getChildren().size());
		for (int i = 0; i < node.getChildren().size(); i++) {
			Node child = node.getChildren().get(i);
			if (!child.isEmpty() && child.getTotalReps() > 0) {
				children.add(new ProgramNodeState(child));
			}
		}
		reset();
	}

	public int getCompletedReps() {
		return completedReps;
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
			return children.get(currentChildIndex);
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
