package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.ClipData;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;

public class ExerciseView extends DraggableView {
	private Spinner effortLevel;
	private EditText duration;
	private Exercise exercise;
	private ProgramNodeView nodeView;

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

		effortLevel.setAdapter(new EffortLevelAdapter());
		effortLevel.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(ExerciseView.this);
				ExerciseView.this.startDrag(data, shadowBuilder, ExerciseView.this, 0);
				ExerciseView.this.setVisibility(View.INVISIBLE);
				return true;
			}
		});

		setOnTouchListener(startDragListener);
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

	private OnTouchListener startDragListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				dragManager.startDrag(ExerciseView.this);
			}

			return false;
		}
	};
}