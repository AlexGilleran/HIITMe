package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;

public class ExerciseView extends RelativeLayout implements DraggableView {
	private DragManager dragManager;

	private TextView effortLevel;
	private TextView duration;
	private Exercise exercise;
	private ProgramNodeView nodeView;

	public ExerciseView(Context context) {
		super(context);
	}

	public ExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setDragManager(DragManager dragManager) {
		this.dragManager = dragManager;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		effortLevel = (TextView) findViewById(R.id.exercise_effort_level);
		duration = (TextView) findViewById(R.id.exercise_duration);

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
		effortLevel.setText(exercise.getEffortLevel().toString());
		
		int minutes = exercise.getDuration() / 1000 / 60;
		int seconds = exercise.getDuration() / 1000 % 60;
		int ms = exercise.getDuration() % 1000;
		duration.setText(minutes + ":" + seconds + "." + ms);
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