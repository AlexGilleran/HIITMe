package com.alexgilleran.hiitme.presentation.programdetail.views;

import static com.alexgilleran.hiitme.util.ViewUtils.getVisibilityInt;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.util.ViewUtils;

public class NodeView extends LinearLayout implements DraggableView {
	private LayoutInflater layoutInflater;
	private DragManager dragManager;
	private NodeView parent;

	private Node programNode;

	private TextView repCountView;
	private ImageButton moveButton;

	public NodeView(Context context) {
		super(context);
		layoutInflater = LayoutInflater.from(context);
	}

	public NodeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		layoutInflater = LayoutInflater.from(context);
	}

	public NodeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public void onFinishInflate() {
		this.repCountView = (TextView) this.findViewById(R.id.textview_repcount);
		this.moveButton = (ImageButton) this.findViewById(R.id.button_move_program_group);

		moveButton.setOnTouchListener(moveListener);
	}

	private OnTouchListener moveListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			dragManager.startDrag(NodeView.this, event);

			return false;
		}
	};

	public void edit() {
		// for (View nodeView : subViews) {
		// nodeView.edit();
		// }
	}

	public void init(Node programNode, NodeView parent) {
		this.programNode = programNode;
		this.parent = parent;

		render();
	}

	private void render() {
		for (int i = 1; i < getChildCount(); i++) {
			removeViewAt(i);
		}

		for (int i = 0; i < programNode.getChildren().size(); i++) {
			Node child = programNode.getChildren().get(i);

			if (child.getAttachedExercise() != null) {
				initialiseChild(buildExerciseView(child.getAttachedExercise()));
			} else {
				NodeView programNodeView = buildProgramNodeView(child);
				initialiseChild(programNodeView);
			}
		}

		repCountView.setText("x" + programNode.getTotalReps());
		setBackgroundResource(determineBgDrawableRes());
	}

	private <V extends View & DraggableView> void initialiseChild(V newView) {
		newView.setDragManager(dragManager);

		newView.setId(ViewUtils.generateViewId());

		addView(newView);
	}

	@Override
	public void setDragManager(DragManager dragManager) {
		this.dragManager = dragManager;

		for (int i = 1; i < getChildCount(); i++) {
			((DraggableView) getChildAt(i)).setDragManager(dragManager);
		}
	}

	private int determineBgDrawableRes() {
		int depth = getDepth();

		return depth % 2 == 0 ? R.drawable.card_nested_even_depth : R.drawable.card_nested_odd_depth;
	}

	private int getDepth() {
		if (parent != null) {
			return 1 + parent.getDepth();
		}
		return 0;
	}

	/**
	 * Populates an existing row meant to contain details of an exercise.
	 * 
	 * @param row
	 *            The {@link TableRow} to populate.
	 * @param exercise
	 *            The {@link Exercise} to source data from.
	 */
	private ExerciseView buildExerciseView(final Exercise exercise) {
		ExerciseView exerciseView = (ExerciseView) layoutInflater.inflate(R.layout.view_exercise, this, false);

		exerciseView.setNodeView(this);
		exerciseView.setExercise(exercise);

		return exerciseView;
	}

	/**
	 * Populates an existing row meant to contain a {@link NodeView}.
	 * 
	 * @param row
	 *            The row to populate.
	 * @param node
	 *            The child node to pass to the {@link NodeView}.
	 */
	private NodeView buildProgramNodeView(Node node) {
		NodeView nodeView = (NodeView) layoutInflater.inflate(R.layout.view_program_node, this, false);
		nodeView.init(node, this);

		return nodeView;
	}

	@Override
	public void removeView(View view) {
		super.removeView(view);
	}

	public DraggableView findNextAfter(DraggableView view) {
		int index = 0;

		// TODO: This is O(clusterfuck), improve it if we can.
		for (int i = 1; i < getChildCount(); i++) {
			if (getChildAt(i) == view) {
				index = i;
			}
		}

		if (index + 1 < getChildCount()) {
			return (DraggableView) getChildAt(index + 1);
		}

		if (parent == null) {
			return null;
		}

		return parent.findNextAfter(this);
	}

	public InsertionPoint findViewAtTop(int top, DraggableView viewToSwapIn) {
		if (top < getChildAt(0).getTop() + getChildAt(0).getHeight()) {
			return new InsertionPoint(1, this, null);
		}

		// TODO: Guess the correct place instead of going top-to-bottom
		for (int i = 1; i < getChildCount(); i++) {
			View childView = getChildAt(i);
			int upperBound = i + 1 < getChildCount() ? getChildAt(i + 1).getTop() : getTop() + getHeight();

			if (top >= childView.getTop() && top < upperBound) {
				if (childView != viewToSwapIn && childView instanceof NodeView) {
					return ((NodeView) childView).findViewAtTop(top - (childView.getTop()), viewToSwapIn);
				} else if (childView instanceof DraggableView) {
					return new InsertionPoint(i, this, (DraggableView) childView);
				}
			}
		}

		return new InsertionPoint(-1, this, null);
	}

	@Override
	public Node getProgramNode() {
		Node programNode = new Node();

		// TODO: jesus christ.
		programNode.setTotalReps(Integer.parseInt(repCountView.getText().toString().substring(1)));

		for (DraggableView child : getChildren()) {
			programNode.addChildNode(child.getProgramNode());
		}

		return programNode;
	}

	private List<DraggableView> getChildren() {
		List<DraggableView> children = new ArrayList<DraggableView>();

		for (int i = 1; i < getChildCount(); i++) {
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
		addView(child.asView());
	}

	public void addChild(DraggableView child, int index) {
		addView(child.asView(), index);
	}

	public void removeChild(DraggableView view) {
		// TODO: Would by index be quicker? It certainly is more perilous.
		removeView(view.asView());
	}

	@Override
	public NodeView getParentProgramNodeView() {
		return (NodeView) getParent();
	}

	@Override
	public void setEditable(boolean editable) {
		moveButton.setVisibility(getVisibilityInt(editable));

		for (DraggableView child : getChildren()) {
			child.setEditable(editable);
		}
	}

	@Override
	public void setBeingDragged(boolean beingDragged) {
		setBackgroundResource(beingDragged ? R.drawable.card_dragged : determineBgDrawableRes());
	}

	public class InsertionPoint {
		int index;
		NodeView parent;
		DraggableView swapWith;

		public InsertionPoint(int index, NodeView parent, DraggableView swapWith) {
			this.index = index;
			this.parent = parent;
			this.swapWith = swapWith;
		}
	}
}
