package com.alexgilleran.hiitme.presentation.programdetail.views;

import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.util.ViewUtils;

public class ExerciseView extends RelativeLayout implements DraggableView {
	private TextView name;
	private ImageView effortLevel;
	private TextView duration;
	private Exercise exercise;
	private ImageButton moveButton;
	private DraggableView nodeView;

	private boolean editable;

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

		effortLevel = (ImageView) findViewById(R.id.exercise_effort_level);
		duration = (TextView) findViewById(R.id.exercise_duration);
		moveButton = (ImageButton) findViewById(R.id.button_move);
		name = (TextView) findViewById(R.id.exercise_name);

		moveButton.setOnDragListener(new OnDragListener() {
			@Override
			public boolean onDrag(View v, DragEvent event) {
				return false;
			}
		});

		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				effortLevel.setLayoutParams(new RelativeLayout.LayoutParams(effortLevel.getWidth(), ExerciseView.this
						.getHeight() - ExerciseView.this.getPaddingTop() - ExerciseView.this.getPaddingBottom()));
			}
		});
	}

	public DraggableView getNodeView() {
		return nodeView;
	}

	public void setNodeView(DraggableView nodeView) {
		this.nodeView = nodeView;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;

		render();
	}

	public void render() {
		effortLevel.setContentDescription(exercise.getEffortLevel().toString());
		effortLevel.setImageResource(exercise.getEffortLevel().getIconId());
		effortLevel.setBackgroundResource(exercise.getEffortLevel().getBackgroundId());

		int minutes = exercise.getDuration() / 1000 / 60;
		int seconds = exercise.getDuration() / 1000 % 60;
		duration.setText(timeToString(minutes) + "." + timeToString(seconds));

		if (exercise.getName() != null && !exercise.getName().trim().isEmpty()) {
			name.setVisibility(VISIBLE);
			name.setText(exercise.getName());
		} else {
			effortLevel.setLayoutParams(new RelativeLayout.LayoutParams(ViewUtils.getPxForDp(getContext(), 24),
					effortLevel.getHeight() - name.getHeight()));
			name.setVisibility(GONE);
		}
	}

	private String timeToString(int number) {
		return String.format(Locale.ENGLISH, "%02d", number);
	}

	@Override
	public Node rebuildNode() {
		Node node = new Node();
		node.setTotalReps(1);

		Exercise exercise = this.exercise.clone();
		exercise.setNode(node);

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
		this.editable = editable;

		moveButton.setVisibility(ViewUtils.getVisibilityInt(editable));
	}

	@Override
	public boolean isEditable() {
		return editable;
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