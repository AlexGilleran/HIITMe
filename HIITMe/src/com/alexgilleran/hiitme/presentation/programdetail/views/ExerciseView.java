package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;

public class ExerciseView extends RelativeLayout implements DraggableView {
	private DragManager dragManager;

	private TextView effortLevel;
	private TextView duration;
	private Exercise exercise;
	private ImageButton moveButton;
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
		moveButton = (ImageButton) findViewById(R.id.button_move);

		moveButton.setOnTouchListener(startDragListener);
		moveButton.setOnDragListener(new OnDragListener() {
			@Override
			public boolean onDrag(View v, DragEvent event) {
				return false;
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
		effortLevel.setText(exercise.getEffortLevel().toString());

		int minutes = exercise.getDuration() / 1000 / 60;
		int seconds = exercise.getDuration() / 1000 % 60;
		duration.setText(timeToString(minutes) + "." + timeToString(seconds));
	}

	private String timeToString(int number) {
		if (number >= 10) {
			return Integer.toString(number);
		} else if (number == 0) {
			return "00";
		} else {
			return "0" + number;
		}
	}

	private OnTouchListener startDragListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				dragManager.startDrag(ExerciseView.this, event);
			}

			return false;
		}
	};

	@Override
	public Node getProgramNode() {
		Node node = new Node();
		node.setTotalReps(1);

		// TODO: Create a whole new exercise.
//		Exercise exercise = new Exercise();
		node.setAttachedExercise(exercise);

		return node;
	}

	@Override
	public View asView() {
		return this;
	}

	@Override
	public ProgramNodeView getParentProgramNodeView() {
		return (ProgramNodeView) getParent();
	}
}