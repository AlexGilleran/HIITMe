package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.util.ViewUtils;

public class ExerciseView extends RelativeLayout implements DraggableView {
	private TextView name;
	private TextView effortLevel;
	private TextView duration;
	private Exercise exercise;
	private ImageButton moveButton;
	private NodeView nodeView;

	public ExerciseView(Context context) {
		super(context);
	}

	public ExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setDragManager(DragManager dragManager) {
		moveButton.setOnTouchListener(new MoveButtonListener(this, dragManager));
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		effortLevel = (TextView) findViewById(R.id.exercise_effort_level);
		duration = (TextView) findViewById(R.id.exercise_duration);
		moveButton = (ImageButton) findViewById(R.id.button_move);
		name = (TextView) findViewById(R.id.exercise_name);

		moveButton.setOnDragListener(new OnDragListener() {
			@Override
			public boolean onDrag(View v, DragEvent event) {
				return false;
			}
		});
	}

	public NodeView getNodeView() {
		return nodeView;
	}

	public void setNodeView(NodeView nodeView) {
		this.nodeView = nodeView;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;

		render();
	}

	public void render() {
		effortLevel.setText(exercise.getEffortLevel().toString());

		int minutes = exercise.getDuration() / 1000 / 60;
		int seconds = exercise.getDuration() / 1000 % 60;
		duration.setText(timeToString(minutes) + "." + timeToString(seconds));
		name.setText(exercise.getName());
	}

	private String timeToString(int number) {
		return String.format("%02d", number);
	}

	@Override
	public Node getNode() {
		Node node = new Node();
		node.setTotalReps(1);

		// TODO: Create a whole new exercise.
		// Exercise exercise = new Exercise();
		node.setAttachedExercise(exercise);

		return node;
	}

	@Override
	public View asView() {
		return this;
	}

	@Override
	public NodeView getParentNode() {
		return (NodeView) getParent();
	}

	@Override
	public void setEditable(boolean editable) {
		moveButton.setVisibility(ViewUtils.getVisibilityInt(editable));
	}

	@Override
	public void setBeingDragged(boolean beingDragged) {
		setBackgroundResource(beingDragged ? R.drawable.card_dragged : R.drawable.card_base);
	}

	@Override
	public int getTopForDrag() {
		return ViewUtils.getTopIncludingMargin(this);
	}

	@Override
	public int getBottomForDrag() {
		return ViewUtils.getBottomIncludingMargin(this);
	}

	public Exercise getExercise() {
		return exercise;
	}
}