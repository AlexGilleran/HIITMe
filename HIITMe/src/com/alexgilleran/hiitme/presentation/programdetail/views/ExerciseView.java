package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.ClipData;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;

public class ExerciseView extends TableRow {
	private Spinner effortLevel;
	private EditText duration;
	private Exercise exercise;
	private ProgramNodeView nodeView;

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
		effortLevel.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(ExerciseView.this);
				ExerciseView.this.startDrag(data, shadowBuilder, ExerciseView.this, 0);
				// view.setVisibility(View.INVISIBLE);
				return true;
			}
		});
	}

	public ProgramNodeView getNodeView() {
		return nodeView;
	}

	public void setNodeView(ProgramNodeView nodeView) {
		this.nodeView = nodeView;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;

		render();
	}

	private void render() {
		effortLevel.setSelection(exercise.getEffortLevel().ordinal());
		duration.setText(Integer.toString(exercise.getDuration() / 1000));
	}
}