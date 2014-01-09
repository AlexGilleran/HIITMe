package com.alexgilleran.hiitme.presentation.programdetail.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
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
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.util.ViewUtils;

public class ProgramNodeView extends LinearLayout implements DraggableView {
	private LayoutInflater layoutInflater;
	private DragManager dragManager;
	private ProgramNodeView parent;

	private ProgramNode programNode;

	private TextView repCountView;
	private ImageButton moveButton;

	private static final int[] BG_COLOURS = new int[] { 0xFFC5EAF8, 0xFFE2F4FB };

	public ProgramNodeView(Context context) {
		super(context);
		layoutInflater = LayoutInflater.from(context);
	}

	public ProgramNodeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		layoutInflater = LayoutInflater.from(context);
	}

	public ProgramNodeView(Context context, AttributeSet attrs, int defStyle) {
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
			dragManager.startDrag(ProgramNodeView.this, event);

			return false;
		}
	};

	public void edit() {
		// for (View nodeView : subViews) {
		// nodeView.edit();
		// }
	}

	public void setProgramNode(ProgramNode programNode) {
		this.programNode = programNode;

		render();
	}

	private void render() {
		for (int i = 1; i < getChildCount(); i++) {
			removeViewAt(i);
		}

		for (int i = 0; i < programNode.getChildren().size(); i++) {
			ProgramNode child = programNode.getChildren().get(i);

			if (child.getAttachedExercise() != null) {
				initialiseChild(buildExerciseView(child.getAttachedExercise()));
			} else {
				ProgramNodeView programNodeView = buildProgramNodeView(child);
				programNodeView.setParent(parent);
				initialiseChild(programNodeView);
			}
		}

		repCountView.setText("x" + programNode.getTotalReps());
		LayerDrawable background = (LayerDrawable) getBackground().mutate();
		((GradientDrawable) background.findDrawableByLayerId(R.id.card)).setColor(determineBgColour());
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

	public void setParent(ProgramNodeView parent) {
		this.parent = parent;
	}

	private int determineBgColour() {
		int colorIndex = programNode.getDepth() % BG_COLOURS.length;
		return BG_COLOURS[colorIndex];
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
	 * Populates an existing row meant to contain a {@link ProgramNodeView}.
	 * 
	 * @param row
	 *            The row to populate.
	 * @param node
	 *            The child node to pass to the {@link ProgramNodeView}.
	 */
	private ProgramNodeView buildProgramNodeView(ProgramNode node) {
		ProgramNodeView nodeView = (ProgramNodeView) layoutInflater.inflate(R.layout.view_program_node, this, false);
		nodeView.setProgramNode(node);

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
			View child = getChildAt(i);
			if (top >= child.getTop() && top < child.getTop() + child.getHeight()) {
				// TODO: there's gotta be a better way than instanceofs
				// everywhere
				if (child != viewToSwapIn && child instanceof ProgramNodeView) {
					return ((ProgramNodeView) child).findViewAtTop(top - child.getTop(), viewToSwapIn);
				}

				return new InsertionPoint(i, this, child);
			}
		}

		return new InsertionPoint(-1, this, null);
	}

	public class InsertionPoint {
		int index;
		ProgramNodeView parent;
		View swapWith;

		public InsertionPoint(int index, ProgramNodeView parent, View swapWith) {
			this.index = index;
			this.parent = parent;
			this.swapWith = swapWith;
		}
	}

	@Override
	public ProgramNode getProgramNode() {
		ProgramNode programNode = new ProgramNode();
		
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
	public ProgramNodeView getParentProgramNodeView() {
		return (ProgramNodeView) getParent();
	}
}
