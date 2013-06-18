package com.alexgilleran.hiitme.presentation.programdetail.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;
import com.alexgilleran.hiitme.presentation.programdetail.views.EditExerciseFragment.EditExerciseListener;

public class ProgramNodeView extends LinearLayout implements
		ProgramNodeObserver {
	private final Map<Exercise, TableRow> exerciseRows = new HashMap<Exercise, TableRow>();
	private final Map<ProgramNode, TextView> repViews = new HashMap<ProgramNode, TextView>();
	private final List<ProgramNodeView> subViews = new ArrayList<ProgramNodeView>();

	private EditExerciseListener editListener;
	private ProgramNode programNode;
	private TextView repView;

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
		TableLayout repLayout = (TableLayout) this
				.findViewById(R.id.layout_reps);

		for (ProgramNode child : programNode.getChildren()) {
			TableRow newRow;

			if (child.getAttachedExercise() != null) {
				newRow = buildExerciseView(child.getAttachedExercise());
				exerciseRows.put(child.getAttachedExercise(), newRow);
			} else {
				newRow = buildProgramNodeView(child);
			}

			repLayout.addView(newRow);
		}

		TextView repCountView = (TextView) this
				.findViewById(R.id.textview_repcount);
		repCountView.setText("x" + programNode.getTotalReps());
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
}
