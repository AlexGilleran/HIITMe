package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.presentation.programdetail.EditDialogUpdateListener;

import java.util.Locale;

import static com.alexgilleran.hiitme.util.ViewUtils.getBottomIncludingMargin;
import static com.alexgilleran.hiitme.util.ViewUtils.getPxForDp;
import static com.alexgilleran.hiitme.util.ViewUtils.getTopIncludingMargin;

public class ExerciseView extends RelativeLayout implements DraggableView {
	private TextView name;
	private ImageView effortLevel;
	private TextView duration;
	private Exercise exercise;
	private DraggableView nodeView;
	private DragManager dragManager;
	private Handler longPressHandler = new Handler();

	private boolean editable;
	private boolean newlyCreated = false;

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

		effortLevel = (ImageView) findViewById(R.id.imageview_effort_level);
		duration = (TextView) findViewById(R.id.exercise_duration);
		name = (TextView) findViewById(R.id.exercise_name);
	}

	public DraggableView getNodeView() {
		return nodeView;
	}

	public void setNodeView(DraggableView nodeView) {
		this.nodeView = nodeView;
	}

	public void render() {
		if (exercise.getEffortLevel().isBlank()) {
			effortLevel.setVisibility(View.INVISIBLE);
		} else {
			effortLevel.setVisibility(View.VISIBLE);
			effortLevel.setContentDescription(exercise.getEffortLevel().toString());
			effortLevel.setImageResource(exercise.getEffortLevel().getIconId());
			effortLevel.setBackgroundResource(exercise.getEffortLevel().getBackgroundId());
		}

		duration.setText(timeToString(exercise.getMinutes()) + "." + timeToString(exercise.getSeconds()));

		if (exercise.getName() != null && !exercise.getName().trim().isEmpty()) {
			name.setVisibility(VISIBLE);
			name.setText(exercise.getName());
		} else {
			effortLevel.setLayoutParams(new RelativeLayout.LayoutParams(getPxForDp(getContext(), 32),
					effortLevel.getHeight() - name.getHeight()));
			name.setVisibility(GONE);
		}

		this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				effortLevel.getLayoutParams().height = ExerciseView.this
						.getHeight() - ExerciseView.this.getPaddingTop() - ExerciseView.this.getPaddingBottom();
				effortLevel.setLayoutParams(effortLevel.getLayoutParams());
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
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
	public boolean isEditable() {
		return editable;
	}

	@Override
	public void setEditable(boolean editable) {
		this.editable = editable;

		setOnLongClickListener(editable ? longClickListener : null);
		setOnTouchListener(editable ? touchListener : null);
		setBackgroundResource(editable ? R.drawable.card_base : R.drawable.card_bg);
	}

	@Override
	public void setBeingDragged(boolean beingDragged) {
		setBackgroundResource(beingDragged ? R.drawable.card_dragged : R.drawable.card_base);
	}

	@Override
	public int getTopForDrag() {
		return getTopIncludingMargin(this);
	}

	@Override
	public int getBottomForDrag() {
		return getBottomIncludingMargin(this);
	}

	public Exercise getExercise() {
		return exercise;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;

		render();
	}

	@Override
	public boolean isNewlyCreated() {
		return newlyCreated;
	}

	public void setNewlyCreated(boolean placed) {
		this.newlyCreated = placed;
	}

	// We have to have a long press handler to trigger the ripple in Android 5+
	private final OnLongClickListener longClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			return true;
		}
	};

	private final OnTouchListener touchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				longPressHandler.postDelayed(longPressRunnable, 1200);
			}
			if ((event.getAction() == MotionEvent.ACTION_MOVE) || (event.getAction() == MotionEvent.ACTION_UP)) {
				longPressHandler.removeCallbacks(longPressRunnable);
			}
			if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
				dragManager.startDrag(ExerciseView.this, (int) event.getRawY());
				return true;
			}

			return false;
		}
	};

	private Runnable longPressRunnable = new Runnable() {
		public void run() {
			EditExerciseFragment dialog = new EditExerciseFragment();
			dialog.setExercise(getExercise());

			dialog.setDialogUpdateListener(new EditDialogUpdateListener() {
				@Override
				public void onUpdated() {
					render();
				}
			});

			dialog.show(dragManager.getFragmentManager(), "edit_exercise");
		}
	};
}