package com.alexgilleran.hiitme.presentation.programdetail.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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
	private Map<ProgramNode, TextView> repViews = new HashMap<ProgramNode, TextView>();
	private List<ProgramNodeView> subViews = new ArrayList<ProgramNodeView>();
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

	public void setProgramNode(ProgramNode programNode) {
		this.programNode = programNode;
		programNode.registerObserver(this);

		repViews.put(programNode,
				(TextView) this.findViewById(R.id.textview_repcount));

		render();
	}

	private void render() {
		TableLayout repLayout = (TableLayout) this
				.findViewById(R.id.layout_reps);

		for (ProgramNode child : programNode.getChildren()) {
			TableRow newRow = new TableRow(this.getContext());

			if (child.getAttachedExercise() != null) {
				populateExerciseRow(newRow, child.getAttachedExercise());
				child.registerObserver(this);
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
		TextView repCountView = new TextView(this.getContext());
		repCountView.setText(exercise.getParentNode().getTotalReps() + "x");
		row.addView(repCountView);
		repViews.put(exercise.getParentNode(), repCountView);

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
		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ProgramNodeView nodeView = (ProgramNodeView) inflater.inflate(
				R.layout.view_program_node, null);
		nodeView.setProgramNode(node);
		subViews.add(nodeView);

		row.addView(nodeView);
	}

	public void setRemainingReps(ProgramNode node, int repsLeft) {
		TextView targetView = repViews.get(node);

		if (targetView != null) {
			targetView.setText(repsLeft + "/" + node.getTotalReps());
		}
	}

	@Override
	public void onNextExercise(Exercise newExercise) {
		highlightExercise(newExercise);
	}

	public void highlightExercise(Exercise exercise) {
		if (currentRow != null) {
			currentRow.setBackgroundColor(Color.TRANSPARENT);
		}

		TableRow newRow = exerciseRows.get(exercise);

		if (newRow != null) {
			newRow.setBackgroundColor(Color.GREEN);
			currentRow = newRow;
		}
	}

	public void resetRepcounts() {
		for (ProgramNodeView view : subViews) {
			view.resetRepcounts();
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
		highlightExercise(null);
	}
}
