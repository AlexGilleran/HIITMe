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
import com.alexgilleran.hiitme.model.Superset;

public class SupersetView extends LinearLayout {
	private Map<Exercise, TableRow> exerciseTextViews = new HashMap<Exercise, TableRow>();
	private Superset repGroup;

	public SupersetView(Context context) {
		super(context);
	}

	public SupersetView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SupersetView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onFinishInflate() {

	}

	public void setRepGroup(Superset repGroup) {
		this.repGroup = repGroup;

		updateView();
	}

	public void setCurrentExercise(Exercise newCurrentExercise) {
		for (Exercise exercise : repGroup.getExercises()) {
			if (exercise.equals(newCurrentExercise)) {
				exerciseTextViews.get(exercise).setBackgroundColor(Color.BLUE);
			} else {
				exerciseTextViews.get(exercise).setBackgroundColor(
						Color.TRANSPARENT);
			}
		}
	}

	public void setRemainingReps(int repsLeft) {
		TextView repCountTextView = (TextView) this
				.findViewById(R.id.textview_repcount);
		repCountTextView.setText(repsLeft + "/" + this.repGroup.getRepCount());
	}

	private void updateView() {
		updateReps();

		TextView repCountView = (TextView) this
				.findViewById(R.id.textview_repcount);
		repCountView.setText("x" + repGroup.getRepCount());
	}

	private void updateReps() {
		TableLayout repLayout = (TableLayout) this
				.findViewById(R.id.layout_reps);

		for (Exercise rep : repGroup.getExercises()) {
			TableRow repRow = new TableRow(this.getContext());

			TextView repLabelView = new TextView(this.getContext());
			repLabelView.setText(rep.getName());
			repRow.addView(repLabelView);

			TextView repDurationView = new TextView(this.getContext());
			repDurationView.setText((rep.getDuration() / 1000) + " seconds");
			repRow.addView(repDurationView);

			repLayout.addView(repRow);
			this.exerciseTextViews.put(rep, repRow);
		}
	}
}
