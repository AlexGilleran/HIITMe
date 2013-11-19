package com.alexgilleran.hiitme.presentation.programdetail.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;
import com.alexgilleran.hiitme.presentation.programdetail.DragPlaceholderProvider;
import com.alexgilleran.hiitme.presentation.programdetail.views.EditExerciseFragment.EditExerciseListener;

public class ProgramNodeView extends LinearLayout implements
		ProgramNodeObserver {
	private final List<TableRow> tableRows = new LinkedList<TableRow>();
	private final Map<Exercise, TableRow> exerciseRows = new HashMap<Exercise, TableRow>();
	private final Map<ProgramNode, TextView> repViews = new HashMap<ProgramNode, TextView>();
	private final List<ProgramNodeView> subViews = new ArrayList<ProgramNodeView>();

	private ProgramNode programNode;

	private EditExerciseListener editListener;
	private TextView repView;
	private ImageButton addExerciseButton;
	private ImageButton addGroupButton;
	private TableLayout repLayout;

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

	public ProgramNodeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
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
		this.repLayout = (TableLayout) this.findViewById(R.id.layout_reps);

		addGroupButton.setOnClickListener(addGroupListener);
		addExerciseButton.setOnClickListener(addExerciseListener);

		this.setOnDragListener(dragListener);
	}

	public void edit() {
		for (ProgramNodeView nodeView : subViews) {
			nodeView.edit();
		}
	}

	public void setProgramNode(ProgramNode programNode) {
		this.programNode = programNode;
		programNode.registerObserver(this);

		render();
	}

	private void render() {
		for (TableRow row : tableRows) {
			repLayout.removeView(row);
		}

		for (int i = 0; i < programNode.getChildren().size(); i++) {
			ProgramNode child = programNode.getChildren().get(i);
			TableRow newRow;

			if (child.getAttachedExercise() != null) {
				newRow = new TableRow(getContext());
				newRow.addView(buildExerciseView(child.getAttachedExercise()));
				exerciseRows.put(child.getAttachedExercise(), newRow);
			} else {
				newRow = buildProgramNodeView(child);
			}

			tableRows.add(newRow);
			repLayout.addView(newRow, i);
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

		exerciseView.setExercise(exercise);
		exerciseView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				editListener.onEditExercise(exercise);
			}
		});

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
	private TableRow buildProgramNodeView(ProgramNode node) {
		TableRow row = new TableRow(this.getContext());

		ProgramNodeView nodeView = (ProgramNodeView) inflater.inflate(
				R.layout.view_program_node, null);
		nodeView.setProgramNode(node);
		subViews.add(nodeView);

		row.addView(nodeView);

		return row;
	}

	public void setRemainingReps(ProgramNode node, int repsLeft) {
		repView.setText(repsLeft + "/" + node.getTotalReps());
	}

	@Override
	public void onNextExercise(Exercise newExercise) {
		// highlightExercise(newExercise);
	}

	public void resetRepCounts() {
		for (ProgramNodeView view : subViews) {
			view.resetRepCounts();
		}

		for (ProgramNode node : repViews.keySet()) {
			setRemainingReps(node, 0);
		}
	}

	@Override
	public void onRepFinish(ProgramNode node, int completedReps) {
		setRemainingReps(node, completedReps);
	}

	@Override
	public void onFinish(ProgramNode node) {
	}

	@Override
	public void onReset(ProgramNode node) {
		if (node == this.programNode) {
			this.resetRepCounts();
		}
	}

	public void setEditExerciseListener(EditExerciseListener listener) {
		this.editListener = listener;
	}

	@Override
	public void onChange(ProgramNode node) {
		render();
	}

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

	private void insertAfter(float y, View view) {
		y = y - tableRows.get(0).getHeight() / 2;
		if (y < (tableRows.get(0).getY() + tableRows.get(0).getHeight() / 2)) {
			repLayout.addView(view, 0);
			return;
		}
		y = y + tableRows.get(0).getHeight() / 2;
		for (int i = 0; i < tableRows.size(); i++) {
			TableRow row = tableRows.get(i);
			if (y > row.getY() - row.getHeight() / 2
					&& y < row.getY() + row.getHeight() / 2) {
				repLayout.addView(view, i);
				return;
			}
		}
		repLayout.addView(view, tableRows.size());
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

	OnDragListener dragListener = new OnDragListener() {
		// Drawable enterShape = getResources().getDrawable(
		// R.drawable.shape_droptarget);
		// Drawable normalShape = getResources().getDrawable(R.drawable.shape);

		@Override
		public boolean onDrag(View v, DragEvent event) {
			int action = event.getAction();
			float y = event.getY();
			View view;
			ViewGroup owner;
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
				owner = (ViewGroup) view.getParent();
				owner.removeView(view);

				insertAfter(y, view);
				// LinearLayout container = (LinearLayout) v;
				// container.addView(view);
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
}
