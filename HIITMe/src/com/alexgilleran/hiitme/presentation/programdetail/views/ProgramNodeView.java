package com.alexgilleran.hiitme.presentation.programdetail.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;
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
		this.repView = (TextView) this.findViewById(R.id.textview_repcount);
		this.addExerciseButton = (ImageButton) this
				.findViewById(R.id.button_add_exercise);
		this.addGroupButton = (ImageButton) this
				.findViewById(R.id.button_add_group);
		this.repLayout = (TableLayout) this.findViewById(R.id.layout_reps);

		addGroupButton.setOnClickListener(addGroupListener);
		addExerciseButton.setOnClickListener(addExerciseListener);
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
				newRow = buildExerciseView(child.getAttachedExercise());
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

	private OnClickListener addExerciseListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			programNode.addChildExercise("", 0, EffortLevel.HARD, 5);
			render();
		}
	};

	private OnClickListener addGroupListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			programNode.addChildNode(1);
			render();
		}
	};
}
