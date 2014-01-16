package com.alexgilleran.hiitme.presentation.programdetail.views;

import static com.alexgilleran.hiitme.util.ViewUtils.getBottomIncludingMargin;
import static com.alexgilleran.hiitme.util.ViewUtils.getTopIncludingMargin;
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
	private static final int FIRST_DRAGGABLE_VIEW_INDEX = 1;

	private LayoutInflater layoutInflater;
	private DragManager dragManager;

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

	public void init(Node programNode) {
		this.programNode = programNode;

		render();
	}

	private void render() {
		for (int i = FIRST_DRAGGABLE_VIEW_INDEX; i < getLastDraggableChildIndex(); i++) {
			removeViewAt(i);
		}

		for (int i = 0; i < programNode.getChildren().size(); i++) {
			Node child = programNode.getChildren().get(i);

			if (child.getAttachedExercise() != null) {
				addChild(buildExerciseView(child.getAttachedExercise()));
			} else {
				NodeView programNodeView = buildProgramNodeView(child);
				addChild(programNodeView);
			}
		}

		repCountView.setText("x" + programNode.getTotalReps());

		setBackground(determineBgDrawableRes());
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		setBackground(determineBgDrawableRes());
	}

	private void setBackground(int resourceId) {
		// Setting background resource kills padding, obviously. Jesus christ is
		// Android ever retarded sometimes.
		int bottom = getPaddingBottom();
		int top = getPaddingTop();
		int right = getPaddingRight();
		int left = getPaddingLeft();
		setBackgroundResource(resourceId);
		setPadding(left, top, right, bottom);
	}

	private <V extends View & DraggableView> void addChild(V newView) {
		newView.setDragManager(dragManager);

		newView.setId(ViewUtils.generateViewId());

		// TypedArray ta = getContext().obtainStyledAttributes(
		// new int[] { android.R.attr.paddingLeft, android.R.attr.paddingTop,
		// android.R.attr.paddingRight,
		// android.R.attr.paddingBottom });
		// newView.setPadding(ta.getDimensionPixelSize(0, 0),
		// ta.getDimensionPixelSize(1, 0), ta.getDimensionPixelSize(2, 0),
		// ta.getDimensionPixelSize(3, 0));
		// newView.setPadding(0, 0, 0, 50);

		addView(newView);
	}

	@Override
	public void setDragManager(DragManager dragManager) {
		this.dragManager = dragManager;

		for (DraggableView child : getChildren()) {
			child.setDragManager(dragManager);
		}
	}

	private int determineBgDrawableRes() {
		int depth = getDepth();

		return depth % 2 == 0 ? R.drawable.card_nested_even_depth : R.drawable.card_nested_odd_depth;
	}

	private int getDepth() {
		if (getParentProgramNodeView() != null) {
			return 1 + getParentProgramNodeView().getDepth();
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
		nodeView.init(node);

		return nodeView;
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

	public InsertionPoint findViewAtTop(int top, DraggableView viewToSwapIn) {
		DraggableView firstChild = getFirstChild();
		if (firstChild != null && top < firstChild.asView().getTop()) {
			return new InsertionPoint(1, this, null);
		}

		// TODO: Guess the correct place instead of going top-to-bottom
		for (int i = FIRST_DRAGGABLE_VIEW_INDEX; i <= getLastDraggableChildIndex(); i++) {
			View childView = getChildAt(i);

			if (top >= getTopIncludingMargin(childView) && top <= getBottomIncludingMargin(childView)) {
				if (top <= childView.getTop()) {
					// In the margin above the view.
					return new InsertionPoint(i, this, (DraggableView) childView);
				} else if (top <= childView.getBottom()) {
					// In the actual view
					if (childView != viewToSwapIn && childView instanceof NodeView) {
						return ((NodeView) childView).findViewAtTop(top - (childView.getTop()), viewToSwapIn);
					}

					if (childView instanceof DraggableView) {
						return new InsertionPoint(i, this, (DraggableView) childView);
					} else {
						throw new IllegalStateException("Non-draggable view as a child of a node view: "
								+ childView.getClass().getName());
					}
				} else {
					// in the margin below the view
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
		if (getParent() instanceof NodeView) {
			return (NodeView) getParent();
		}
		return null;
	}

	@Override
	public void setEditable(boolean editable) {
		if (getDepth() > 0) {
			moveButton.setVisibility(getVisibilityInt(editable));
		}

		for (DraggableView child : getChildren()) {
			child.setEditable(editable);
		}
	}

	@Override
	public void setBeingDragged(boolean beingDragged) {
		setBackgroundResource(beingDragged ? R.drawable.card_dragged : determineBgDrawableRes());
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

	private OnTouchListener moveListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				dragManager.startDrag(NodeView.this, event);
			} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
				dragManager.cancelDrag();
			}

			return false;
		}
	};

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
