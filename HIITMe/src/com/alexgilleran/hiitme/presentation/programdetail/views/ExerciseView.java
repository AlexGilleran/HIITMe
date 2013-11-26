package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;

public class ExerciseView extends LinearLayout {

	private Spinner effortLevel;
	private EditText duration;

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

		effortLevel = (Spinner) findViewById(R.id.exercise_effort_level);
		duration = (EditText) findViewById(R.id.exercise_duration);

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item, new String[] { "Hard", "Easy", "Rest" });
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		effortLevel.setAdapter(arrayAdapter);
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
	}

}