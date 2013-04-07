package com.alexgilleran.hiitme.presentation.programdetail.views;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;

public class ProgramNodeView extends LinearLayout implements
		ProgramNodeObserver {
	private Map<Exercise, TableRow> exerciseRows = new HashMap<Exercise, TableRow>();
	private ProgramNode programNode;
	private TableRow currentRow;

	public ProgramNodeView(Context context) {
		super(context);
	}

	public ProgramNodeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ProgramNodeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onFinishInflate() {

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
			TableRow newRow = new TableRow(this.getContext());

			if (child.getAttachedExercise() != null) {
				populateExerciseRow(newRow, child.getAttachedExercise());
				exerciseRows.put(child.getAttachedExercise(), newRow);
			} else {
				populateProgramNodeRow(newRow, child);
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
	private void populateExerciseRow(TableRow row, Exercise exercise) {
		TextView repLabelView = new TextView(this.getContext());
		repLabelView.setText(exercise.getName());
		row.addView(repLabelView);

		TextView repDurationView = new TextView(this.getContext());
		repDurationView.setText((exercise.getDuration() / 1000) + " seconds");
		row.addView(repDurationView);
	}

	/**
	 * Populates an existing row meant to contain a {@link ProgramNodeView}.
	 * 
	 * @param row
	 *            The row to populate.
	 * @param node
	 *            The child node to pass to the {@link ProgramNodeView}.
	 */
	private void populateProgramNodeRow(TableRow row, ProgramNode node) {
		ProgramNodeView childView = new ProgramNodeView(row.getContext());

		childView.setProgramNode(node);
	}

	public void setRemainingReps(int repsLeft) {
		TextView repCountTextView = (TextView) this
				.findViewById(R.id.textview_repcount);
		repCountTextView.setText(repsLeft + "/"
				+ this.programNode.getTotalReps());
	}

	@Override
	public void onNextExercise(Exercise newExercise) {
		TableRow newRow = exerciseRows.get(newExercise);

		if (newRow != null) {
			currentRow.setBackgroundColor(Color.TRANSPARENT);
			newRow.setBackgroundColor(Color.GREEN);
			currentRow = newRow;
		}
	}

	@Override
	public void onRepFinish(ProgramNode node, int completedReps) {
		if (this == node) {
			this.setRemainingReps(completedReps);
		}
	}

	@Override
	public void onFinish(ProgramNode node) {
		
	}
}
