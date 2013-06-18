package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Spinner;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.model.Exercise;

public class EditExerciseView extends LinearLayout implements
		OnItemSelectedListener, OnValueChangeListener {
	private NumberPicker repCountPicker;
	private Spinner effortSpinner;
	private NumberPicker durationMinutes;
	private NumberPicker durationSeconds;

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

		repCountPicker = (NumberPicker) findViewById(R.id.exercise_edit_rep_count);
		repCountPicker.setMinValue(0);
		repCountPicker.setMaxValue(100);
		repCountPicker.setWrapSelectorWheel(false);
		repCountPicker.setOnValueChangedListener(this);

		effortSpinner = (Spinner) findViewById(R.id.exercise_edit_effort_level);
		ArrayAdapter<EffortLevel> effortAdapter = new ArrayAdapter<EffortLevel>(
				getContext(), android.R.layout.simple_spinner_item,
				EffortLevel.values());
		effortSpinner.setAdapter(effortAdapter);
		effortSpinner.setOnItemSelectedListener(this);

		durationMinutes = (NumberPicker) findViewById(R.id.exercise_edit_duration_minutes);
		durationMinutes.setMinValue(0);
		durationMinutes.setMaxValue(60);
		durationMinutes.setWrapSelectorWheel(false);
		durationMinutes.setOnValueChangedListener(this);

		durationSeconds = (NumberPicker) findViewById(R.id.exercise_edit_duration_seconds);
		durationSeconds.setMinValue(0);
		durationSeconds.setMaxValue(60);
		durationSeconds.setWrapSelectorWheel(false);
		durationSeconds.setOnValueChangedListener(this);
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		if (picker == repCountPicker) {
			exercise.getExerciseGroup().setTotalReps(newVal);
		} else {
			exercise.setDuration(getDuration());
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long newItemId) {
		exercise.setEffortLevel(EffortLevel.values()[position]);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	public Exercise getValues() {
		exercise.setDuration(getDuration());
		exercise.setEffortLevel((EffortLevel) effortSpinner.getSelectedItem());
		exercise.getParentNode().setTotalReps(repCountPicker.getValue());

		return exercise;
	}

	private int getDuration() {
		int totalDuration = 0;
		totalDuration += durationMinutes.getValue() * 60;
		totalDuration += durationSeconds.getValue();
		totalDuration = totalDuration * 1000;

		return totalDuration;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;

		repCountPicker.setValue(exercise.getParentNode().getTotalReps());

		effortSpinner.setSelection(exercise.getEffortLevel().ordinal());

		int totalSeconds = exercise.getDuration() / 1000;
		int totalMinutes = totalSeconds / 60;

		durationMinutes.setValue(totalMinutes);
		durationSeconds.setValue(totalSeconds);
	}
}
