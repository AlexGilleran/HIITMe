package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.model.Exercise;

public class EditExerciseView extends LinearLayout {
	private EditText repCountPicker;
	private Spinner effortSpinner;
	private EditText durationMinutes;
	private EditText durationSeconds;

	private Exercise exercise;

	public EditExerciseView(Context context) {
		super(context);
	}

	public EditExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EditExerciseView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		repCountPicker = (EditText) findViewById(R.id.exercise_edit_rep_count);

		effortSpinner = (Spinner) findViewById(R.id.exercise_edit_effort_level);
		ArrayAdapter<EffortLevel> effortAdapter = new ArrayAdapter<EffortLevel>(getContext(),
				android.R.layout.simple_spinner_item, EffortLevel.values());
		effortSpinner.setAdapter(effortAdapter);

		durationMinutes = (EditText) findViewById(R.id.exercise_edit_duration_minutes);
		durationSeconds = (EditText) findViewById(R.id.exercise_edit_duration_seconds);

		durationMinutes.setFilters(new InputFilter[] { new InputFilterMinMax("0", "99") });
		durationSeconds.setFilters(new InputFilter[] { new InputFilterMinMax("0", "59") });
	}

	public Exercise update() {
		exercise.setDuration(getDuration());
		exercise.setEffortLevel((EffortLevel) effortSpinner.getSelectedItem());
		exercise.getParentNode().setTotalReps(Integer.parseInt(repCountPicker.getText().toString()));

		return exercise;
	}

	private int getDuration() {
		int totalDuration = 0;
		totalDuration += Integer.parseInt(durationMinutes.getText().toString()) * 60;
		totalDuration += Integer.parseInt(durationSeconds.getText().toString());
		totalDuration = totalDuration * 1000;

		return totalDuration;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;

		repCountPicker.setText(Integer.toString(exercise.getParentNode().getTotalReps()));

		effortSpinner.setSelection(exercise.getEffortLevel().ordinal());

		int totalSeconds = exercise.getDuration() / 1000;
		int totalMinutes = totalSeconds / 60;

		durationMinutes.setText(String.format("%02d", totalMinutes));
		durationSeconds.setText(String.format("%02d", totalSeconds));
	}
}