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

package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.presentation.programdetail.EditDialogUpdateListener;
import com.alexgilleran.hiitme.util.DraggableViewFocusListener;
import com.alexgilleran.hiitme.util.DraggableViewTouchListener;
import com.alexgilleran.hiitme.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import static com.alexgilleran.hiitme.util.ViewUtils.getBottomIncludingMargin;
import static com.alexgilleran.hiitme.util.ViewUtils.getTopIncludingMargin;

public class NodeView extends LinearLayout implements DraggableView {
	private static final int FIRST_DRAGGABLE_VIEW_INDEX = 1;
	private static final int MARGIN = (int) (5 * Resources.getSystem().getDisplayMetrics().density);
	private static final int SIDE_MARGIN = (int) (20 * Resources.getSystem().getDisplayMetrics().density);

	private DragManager dragManager;
	private Node programNode;
	private TextView repCountView;
	private FrameLayout header;
	private boolean editable;
	private boolean newlyCreated = false;
	private OnTouchListener touchListener;

	public NodeView(Context context) {
		super(context);
	}

	public NodeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NodeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private static boolean topWithinViewBounds(int top, View childView) {
		return top >= getTopIncludingMargin(childView) && top < getBottomIncludingMargin(childView);
	}

	@Override
	public void onFinishInflate() {
		this.repCountView = (TextView) this.findViewById(R.id.textview_repcount);
		this.header = (FrameLayout) findViewById(R.id.layout_header);

		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	public void init(Node programNode) {
		this.programNode = programNode;

		render();
	}

	public void render() {
		for (int i = FIRST_DRAGGABLE_VIEW_INDEX; i < getLastDraggableChildIndex(); i++) {
			removeViewAt(i);
		}

		for (int i = 0; i < programNode.getChildren().size(); i++) {
			Node child = programNode.getChildren().get(i);

			if (child.getAttachedExercise() != null) {
				addExercise(child.getAttachedExercise());
			} else {
				NodeView programNodeView = dragManager.buildNodeView(child);
				addChild(programNodeView);
			}
		}

		updateRepCount();
	}

	public void updateRepCount() {
		repCountView.setText("x" + programNode.getTotalReps());
	}

	public void addNode(Node node) {
		addNode(node, getChildCount());
	}

	public void addNode(Node node, int index) {
		addChild(dragManager.buildNodeView(node), index);
	}

	public View addExercise(Exercise exercise) {
		return addExercise(exercise, getChildCount());
	}

	public ExerciseView addExercise(Exercise exercise, int index) {
		ExerciseView view = dragManager.buildExerciseView(exercise, this);
		addChild(view, index);
		return view;
	}

	private <V extends View & DraggableView> void addChild(V newView) {
		newView.setDragManager(dragManager);

		newView.setId(View.generateViewId());

		addView(newView, addMargin(newView));
	}

	private ViewGroup.LayoutParams addMargin(View view) {
		LinearLayout.LayoutParams params = new LayoutParams(view.getLayoutParams());
		params.setMargins(SIDE_MARGIN, MARGIN, SIDE_MARGIN, MARGIN);
		return params;
	}

	@Override
	public void setDragManager(final DragManager dragManager) {
		this.dragManager = dragManager;

		for (DraggableView child : getChildren()) {
			child.setDragManager(dragManager);
		}

		touchListener = new OnTouchListener() {
			private DraggableViewTouchListener delegate = new DraggableViewTouchListener(NodeView.this, dragManager);

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					ViewUtils.setBackgroundPreservePadding(NodeView.this, R.drawable.card_bg_raised);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					ViewUtils.setBackgroundPreservePadding(NodeView.this, R.drawable.card_base);
				}

				return delegate.onTouch(v, event);
			}
		};

		header.setOnFocusChangeListener(new DraggableViewFocusListener(this, dragManager));
	}

	private int getDepth() {
		if (getParentNode() != null) {
			return 1 + getParentNode().getDepth();
		}
		return 0;
	}

	@Override
	public void removeView(View view) {
		super.removeView(view);
	}

	private DraggableView getFirstChild() {
		if (getChildCount() >= FIRST_DRAGGABLE_VIEW_INDEX + 1) {
			View firstChild = getChildAt(FIRST_DRAGGABLE_VIEW_INDEX);
			if (firstChild instanceof DraggableView) {
				return (DraggableView) firstChild;
			} else {
				throw new IllegalStateException("Node view has a child that's not a "
						+ DraggableView.class.getSimpleName() + ": " + firstChild.getClass().getSimpleName());
			}
		}
		return null;
	}

	public InsertionPoint findInsertionPoint(int top, DraggableView viewToSwapIn) {
		DraggableView firstChild = getFirstChild();
		if (firstChild != null && top < firstChild.asView().getTop()) {
			return new InsertionPoint(1, this, null, top);
		}

		for (int i = FIRST_DRAGGABLE_VIEW_INDEX; i <= getLastDraggableChildIndex(); i++) {
			View childView = getChildAt(i);

			if (!topWithinViewBounds(top, childView)) {
				// Go to the next one.
				continue;
			}

			if (top <= childView.getTop() && viewToSwapIn.getParentNode() != this) {
				// in the margin above the view
				return new InsertionPoint(i, this, (DraggableView) childView, top);
			}
			if (top >= childView.getBottom() && viewToSwapIn.getParentNode() != this) {
				// in the margin below the view
				return new InsertionPoint(Math.max(i + 1, getChildCount() - 1), this, (DraggableView) childView, top);
			}

			if (childView instanceof NodeView && childView != viewToSwapIn) {
				// In the actual view
				int topInChildView = top - childView.getTop();
				NodeView childAsNodeView = (NodeView) childView;
				return childAsNodeView.findInsertionPoint(topInChildView, viewToSwapIn);
			}

			if (childView instanceof DraggableView) {
				return new InsertionPoint(i, this, (DraggableView) childView, top);
			}

			throw new IllegalStateException("Non-draggable view as a child of a node view: "
					+ childView.getClass().getName());
		}

		return new InsertionPoint(-1, this, null, top);
	}

	@Override
	public Node rebuildNode() {
		Node programNode = new Node();

		// Can't change this in this view.
		programNode.setTotalReps(this.programNode.getTotalReps());

		for (DraggableView child : getChildren()) {
			programNode.addChildNode(child.rebuildNode());
		}

		return programNode;
	}

	public Node getCurrentNode() {
		return programNode;
	}

	private List<DraggableView> getChildren() {
		List<DraggableView> children = new ArrayList<DraggableView>();

		for (int i = FIRST_DRAGGABLE_VIEW_INDEX; i <= getLastDraggableChildIndex(); i++) {
			View child = getChildAt(i);
			if (child instanceof DraggableView) {
				children.add((DraggableView) child);
			}
		}

		return children;
	}

	@Override
	public View asView() {
		return this;
	}

	public void addChild(DraggableView child) {
		addChild(child, 0);
	}

	public void addChild(DraggableView child, int index) {
		child.setEditable(editable);
		child.setDragManager(dragManager);

		LinearLayout.LayoutParams params = new LayoutParams(child.asView().getLayoutParams());
		params.setMargins(SIDE_MARGIN, SIDE_MARGIN, SIDE_MARGIN, SIDE_MARGIN);

		addView(child.asView(), index, addMargin(child.asView()));
	}

	public void removeChild(DraggableView view) {
		removeView(view.asView());
	}

	@Override
	public NodeView getParentNode() {
		if (getParent() instanceof NodeView) {
			return (NodeView) getParent();
		}
		return null;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public void setEditable(boolean editable) {
		this.editable = editable;

		header.setOnLongClickListener(editable ? longClickListener : null);
		header.setOnTouchListener(editable ? touchListener : null);
		header.setBackgroundResource(editable ? R.drawable.node_top_bg : R.drawable.node_top_bg_standard);

		header.setFocusable(editable);
		header.setFocusableInTouchMode(editable);

		for (DraggableView child : getChildren()) {
			child.setEditable(editable);
		}
	}

	@Override
	public void setBeingDragged(boolean beingDragged) {
		header.setBackgroundResource(beingDragged ? R.drawable.node_top_bg_focused : R.drawable.node_top_bg);
	}

	@Override
	public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
		return header.requestFocus(direction, previouslyFocusedRect);
	}

	@Override
	public int getTopForDrag() {
		return ViewUtils.getTopIncludingMargin(this);
	}

	@Override
	public int getBottomForDrag() {
		return ViewUtils.getBottomIncludingMargin(this);
	}

	private int getLastDraggableChildIndex() {
		return getChildCount() - 1;
	}

	@Override
	public boolean isNewlyCreated() {
		return newlyCreated;
	}

	public void setNewlyCreated(boolean placed) {
		this.newlyCreated = placed;
	}

	public void edit() {
		EditNodeFragment dialog = new EditNodeFragment();
		dialog.setNode(getCurrentNode());

		dialog.setDialogUpdateListener(new EditDialogUpdateListener() {
			@Override
			public void onUpdated() {
				updateRepCount();
			}
		});

		dialog.show(dragManager.getFragmentManager(), "edit_node");
	}

	// Have to have this to cause ripples
	private final OnLongClickListener longClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			return false;
		}
	};

	public class InsertionPoint {
		int index;
		NodeView parent;
		DraggableView swapWith;
		int topInView;

		public InsertionPoint(int index, NodeView parent, DraggableView swapWith, int topInView) {
			this.index = index;
			this.parent = parent;
			this.swapWith = swapWith;
			this.topInView = topInView;
		}
	}

}
