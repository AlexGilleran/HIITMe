package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;

public class ExerciseView extends LinearLayout {

	private Spinner effortLevel;
	private EditText duration;
	private Exercise exercise;

	final LayoutInflater inflater;

	public ExerciseView(Context context) {
		super(context);
		inflater = LayoutInflater.from(context);
	}

	public ExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflater = LayoutInflater.from(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		effortLevel = (Spinner) findViewById(R.id.exercise_effort_level);
		duration = (EditText) findViewById(R.id.exercise_duration);

		effortLevel.setAdapter(new EffortLevelAdapter());
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;

		render();
	}

	private void render() {
		effortLevel.setSelection(exercise.getEffortLevel().ordinal());
	}
}