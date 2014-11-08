package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.util.ViewUtils;

public class EditExerciseView extends TableLayout {
	private Spinner effortSpinner;
	private EditText durationMinutes;
	private EditText durationSeconds;
	private EditText name;

	private Exercise exercise;

	public EditExerciseView(Context context) {
		super(context);
	}

	public EditExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		effortSpinner = (Spinner) findViewById(R.id.exercise_edit_effort_level);
		ArrayAdapter<EffortLevel> effortAdapter = new ArrayAdapter<EffortLevel>(getContext(),
				R.layout.spinner_effort_level, EffortLevel.values());
		effortSpinner.setAdapter(effortAdapter);

		durationMinutes = (EditText) findViewById(R.id.exercise_edit_duration_minutes);
		durationSeconds = (EditText) findViewById(R.id.exercise_edit_duration_seconds);

		durationMinutes.setFilters(new InputFilter[] { new InputFilterMinMax("0", "99") });
		durationSeconds.setFilters(new InputFilter[] { new InputFilterMinMax("0", "59") });

		name = (EditText) findViewById(R.id.exercise_edit_name);
	}

	public Exercise update() {
		exercise.setDuration(getDuration());
		exercise.setEffortLevel((EffortLevel) effortSpinner.getSelectedItem());
		exercise.setName(name.getText().toString());

		return exercise;
	}

	private int getDuration() {
		int totalDuration = 0;
		totalDuration += ViewUtils.getIntFromTextViewSafe(durationMinutes) * 60;
		totalDuration += ViewUtils.getIntFromTextViewSafe(durationSeconds);
		totalDuration = totalDuration * 1000;

		return totalDuration;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;

		effortSpinner.setSelection(exercise.getEffortLevel().ordinal());

		int totalSeconds = exercise.getDuration() / 1000;
		int totalMinutes = totalSeconds / 60;

		durationMinutes.setText(String.format("%02d", totalMinutes));
		durationSeconds.setText(String.format("%02d", totalSeconds));

		name.setText(exercise.getName());
	}
}