package com.alexgilleran.hiitme.presentation.programdetail.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ClipData;
import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.presentation.programdetail.DragPlaceholderProvider;
import com.alexgilleran.hiitme.presentation.programdetail.views.EditExerciseFragment.EditExerciseListener;

public class ProgramNodeView extends RelativeLayout {
	private final Map<Exercise, View> exerciseViews = new HashMap<Exercise, View>();
	private final List<ProgramNodeView> subViews = new ArrayList<ProgramNodeView>();

	private ProgramNode programNode;

	private TextView repView;
	private ImageButton addExerciseButton;
	private ImageButton addGroupButton;
	private ImageButton moveButton;
	private LinearLayout repLayout;

	private DragPlaceholderProvider placeholderProvider;

	private final LayoutInflater inflater;

	public ProgramNodeView(Context context) {
		super(context);

		inflater = LayoutInflater.from(context);
	}

	public ProgramNodeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void onFinishInflate() {
		if (getContext() instanceof DragPlaceholderProvider) {
			placeholderProvider = (DragPlaceholderProvider) getContext();
		}

		this.repView = (TextView) this.findViewById(R.id.textview_repcount);

		this.addExerciseButton = (ImageButton) this
				.findViewById(R.id.button_add_exercise);
		this.addGroupButton = (ImageButton) this
				.findViewById(R.id.button_add_group);
		this.moveButton = (ImageButton) this
				.findViewById(R.id.button_move_program_group);
		this.repLayout = (LinearLayout) this.findViewById(R.id.layout_reps);

		addGroupButton.setOnClickListener(addGroupListener);
		addExerciseButton.setOnClickListener(addExerciseListener);
		moveButton.setOnTouchListener(moveListener);

		this.setOnDragListener(dragListener);
	}

	public void edit() {
		for (ProgramNodeView nodeView : subViews) {
			nodeView.edit();
		}
	}

	public void setProgramNode(ProgramNode programNode) {
		this.programNode = programNode;

		render();
	}

	private void render() {
		repLayout.removeAllViews();

		for (int i = 0; i < programNode.getChildren().size(); i++) {
			ProgramNode child = programNode.getChildren().get(i);
			View newView;

			if (child.getAttachedExercise() != null) {
				newView = buildExerciseView(child.getAttachedExercise());
				exerciseViews.put(child.getAttachedExercise(), newView);
			} else {
				newView = buildProgramNodeView(child);
			}

			repLayout.addView(newView, i);
		}

		TextView repCountView = (TextView) this
				.findViewById(R.id.textview_repcount);
		repCountView.setText(Integer.toString(programNode.getTotalReps()));
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
		ExerciseView exerciseView = (ExerciseView) inflater.inflate(
				R.layout.view_exercise, null);

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
	private View buildProgramNodeView(ProgramNode node) {
		ProgramNodeView nodeView = (ProgramNodeView) inflater.inflate(
				R.layout.view_program_node, null);
		nodeView.setProgramNode(node);
		subViews.add(nodeView);

		return nodeView;
	}

	private void insertAfter(float y, View view) {
		// TODO: This is a mess but it feels roughly right... improve it.
		y = y - view.getHeight() / 2;
		if (repLayout.getChildCount() == 0
				|| y < (repLayout.getChildAt(0).getY() + repLayout
						.getChildAt(0).getHeight() / 2)) {
			repLayout.addView(view, 0);
			return;
		}
		for (int i = 0; i < repLayout.getChildCount(); i++) {
			View row = (View) repLayout.getChildAt(i);
			if (y > row.getY() - row.getHeight() / 2
					&& y < row.getY() + row.getHeight() / 2) {
				repLayout.addView(view, i);
				return;
			}
		}
		repLayout.addView(view, repLayout.getChildCount());
		view.requestLayout();
	}

	private void movePlaceholder(float y) {
		clearPlaceholder();

		insertAfter(y, placeholderProvider.getDragPlaceholder());
	}

	private void clearPlaceholder() {
		if (placeholderProvider.getDragPlaceholder().getParent() != null) {
			((ViewGroup) placeholderProvider.getDragPlaceholder().getParent())
					.removeView(placeholderProvider.getDragPlaceholder());
		}
	}

	private OnDragListener dragListener = new OnDragListener() {
		@Override
		public boolean onDrag(View v, DragEvent event) {
			int action = event.getAction();
			float y = event.getY();
			View view;

			switch (action) {
			case DragEvent.ACTION_DRAG_STARTED:
				// do nothing
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				movePlaceholder(event.getY());
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				// ProgramNodeView.this.setOnTouchListener(null);
				clearPlaceholder();
				break;
			case DragEvent.ACTION_DROP:
				clearPlaceholder();
				view = (View) event.getLocalState();
				// Dropped, reassign View to ViewGroup
				ViewGroup owner = (ViewGroup) view.getParent();
				owner.removeView(view);
				insertAfter(y, view);
				view.setVisibility(View.VISIBLE);
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				clearPlaceholder();
				break;
			case DragEvent.ACTION_DRAG_LOCATION:
				movePlaceholder(event.getY());
				break;
			default:
				break;
			}
			return true;
		}
	};

	private final OnClickListener addExerciseListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			programNode.addChildExercise("", 0, EffortLevel.HARD, 5);
			render();
		}
	};

	private final OnClickListener addGroupListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			programNode.addChildNode(1);
			render();
		}
	};

	private final OnTouchListener moveListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			ClipData data = ClipData.newPlainText("", "");
			DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
					ProgramNodeView.this);
			startDrag(data, shadowBuilder, ProgramNodeView.this, 0);
			ProgramNodeView.this.setVisibility(GONE);
			return true;
		}
	};
}
