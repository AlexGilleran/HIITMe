/*
 * HIIT Me, a High Intensity Interval Training app for Android
 * Copyright (C) 2015 Alex Gilleran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import com.alexgilleran.hiitme.util.ViewUtils;

import static com.alexgilleran.hiitme.util.ViewUtils.getBottomIncludingMargin;
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
			effortLevel.setImageResource(exercise.getEffortLevel().getLightIconId());
			effortLevel.setBackgroundResource(exercise.getEffortLevel().getBackgroundId());
		}

		duration.setText(ViewUtils.getTimeText(exercise.getDuration()));

		if (exercise.getName() != null && !exercise.getName().trim().isEmpty()) {
			name.setVisibility(VISIBLE);
			name.setText(exercise.getName());
		} else {
			effortLevel.setLayoutParams(new RelativeLayout.LayoutParams(effortLevel.getLayoutParams().width,
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

	@Override
	public Node rebuildNode() {
		Node node = new Node(1);

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

	@Override
	public void edit() {
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
			edit();
		}
	};
}