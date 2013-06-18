package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TableRow;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;

public class ExerciseView extends TableRow implements ProgramNodeObserver {

	private TextView repCount;
	private TextView effortLevel;
	private TextView duration;

	private Exercise exercise;

	public ExerciseView(Context context) {
		super(context);
	}

	public ExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		repCount = (TextView) findViewById(R.id.exercise_rep_count);
		effortLevel = (TextView) findViewById(R.id.exercise_effort_level);
		duration = (TextView) findViewById(R.id.exercise_duration);
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
		exercise.getParentNode().registerObserver(this);

		onReset(exercise.getParentNode());
	}

	@Override
	public void onNextExercise(Exercise newExercise) {
		this.setBackgroundColor(Color.GREEN);
	}

	@Override
	public void onRepFinish(ProgramNode node, int completedReps) {
		repCount.setText(completedReps + "/"
				+ exercise.getParentNode().getTotalReps());
	}

	@Override
	public void onFinish(ProgramNode node) {
		this.setBackgroundColor(Color.TRANSPARENT);
	}

	@Override
	public void onReset(ProgramNode node) {
		repCount.setText(exercise.getParentNode().getTotalReps() + "x");
		effortLevel.setText(exercise.getEffortLevel().name());
		duration.setText(exercise.getDuration() + "secs");
	}

	@Override
	public void onChange(ProgramNode node) {
		onReset(node);
	}
}