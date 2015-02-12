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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.presentation.programdetail.views.NodeView.InsertionPoint;
import com.alexgilleran.hiitme.util.ViewUtils;

public class ProgramDetailView extends LinearLayout implements DragManager {
	private static final int MOVE_DURATION = 150;

	private LayoutInflater layoutInflater;

	private ScrollingProgramView scrollingView;
	private TextView nameReadOnly;
	private EditText nameEditable;
	private LinearLayout editBar;
	private ImageButton editButton;
	private ImageButton addExerciseButton;
	private ImageButton addNodeButton;
	private View recycleBin;

	private Program program;
	private boolean editable = false;
	private boolean dragging = false;
	private boolean draggableViewFocused = false;
	private int locationOnScreen;
	private int downY, lastEventY;
	private Rect hoverCellCurrentBounds, hoverCellOriginalBounds;

	private FragmentManager fragmentManager;
	private BitmapDrawable hoverCell;
	private DraggableView dragView;
	private DraggableView lastFocusedView;
	private ObjectAnimator hoverViewAnimator;


	public ProgramDetailView(Context context) {
		super(context);
	}

	public ProgramDetailView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ProgramDetailView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Gets the index of a child {@link View} with its parent {@link ViewGroup} if possible. Returns -1 otherwise.
	 */
	private static int getChildIndex(View child) {
		if (child.getParent() instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) child.getParent();
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				if (viewGroup.getChildAt(i) == child) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		layoutInflater = LayoutInflater.from(getContext());

		recycleBin = (View) findViewById(R.id.recycle_bin);

		scrollingView = (ScrollingProgramView) findViewById(R.id.view_scrolling);

		nameReadOnly = (TextView) findViewById(R.id.name_ro);
		nameEditable = (EditText) findViewById(R.id.name_edit);
		editBar = (LinearLayout) findViewById(R.id.layout_edit_button_bar);
		editButton = (ImageButton) findViewById(R.id.button_edit);
		addExerciseButton = (ImageButton) findViewById(R.id.button_add_exercise);
		addNodeButton = (ImageButton) findViewById(R.id.button_add_node);

		addExerciseButton.setOnTouchListener(addExerciseListener);
		addNodeButton.setOnTouchListener(addNodeListener);
		editButton.setOnClickListener(editListener);

		getViewTreeObserver().addOnGlobalLayoutListener(yOffsetObserver);

		if (program != null) {
			render();
		}

		Interpolator interpolator = new AccelerateDecelerateInterpolator();

		LayoutTransition openTransition = new LayoutTransition();
		openTransition.setStartDelay(LayoutTransition.APPEARING, 0);
		openTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
		openTransition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
		openTransition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
		openTransition.setInterpolator(LayoutTransition.APPEARING, interpolator);
		openTransition.setInterpolator(LayoutTransition.CHANGE_DISAPPEARING, interpolator);
		openTransition.setInterpolator(LayoutTransition.DISAPPEARING, interpolator);
		openTransition.setInterpolator(LayoutTransition.CHANGE_APPEARING, interpolator);
//		openTransition.setStagger(LayoutTransition.APPEARING, 0);
//		openTransition.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 0);
//		openTransition.setDuration(LayoutTransition.DISAPPEARING, 210);
//		openTransition.setDuration(LayoutTransition.APPEARING, 210);
		openTransition.setAnimator(LayoutTransition.DISAPPEARING, ObjectAnimator.ofFloat(null, View.TRANSLATION_Y, 0, editBar.getLayoutParams().height));
		openTransition.setAnimator(LayoutTransition.APPEARING, ObjectAnimator.ofFloat(null, View.TRANSLATION_Y, editBar.getLayoutParams().height, 0));
		setLayoutTransition(openTransition);
	}

	public String getName() {
		return nameEditable.getText().toString();
	}

	public void setProgram(Program program) {
		this.program = program;

		if (scrollingView != null) {
			render();
		}

		requestLayout();
	}

	private void render() {
		scrollingView.init(this, program.getAssociatedNode());
		nameReadOnly.setText(program.getName());
		nameEditable.setText(program.getName());
	}

	public Program rebuildProgram() {
		program.setName(nameEditable.getText().toString());
		program.setAssociatedNode(scrollingView.rebuildNode());

		return program;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// We do this so that new exercises/nodes drop straight down.
		onTouchEvent(ev);

		return currentlyDragging();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		scrollingView.handleTouchEvent(event);

		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_MOVE:
				if (currentlyDragging()) {
					lastEventY = (int) event.getRawY();
					handleHoverCellMove();

					return true;
				}

				break;
			case MotionEvent.ACTION_UP:
				touchEventsEnded();
				return true;
			case MotionEvent.ACTION_CANCEL:
				touchEventsEnded();
				return true;
		}

		return true;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (hoverCell != null) {
			hoverCell.draw(canvas);
		}
	}

	@Override
	public void cancelDrag() {
		touchEventsEnded();
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(final boolean editable) {
		this.editable = editable;

		scrollingView.setEditable(editable);
		nameReadOnly.setVisibility(ViewUtils.getVisibilityInt(!editable));
		requestLayout();

		post(new Runnable() {
			@Override
			public void run() {
				editBar.setVisibility(ViewUtils.getVisibilityInt(editable));

//				if (editable) {
//					editBar.setTranslationY(editBar.getHeight());
//					editBar.animate().translationY(0);
//				} else {
//					editBar.setTranslationY(0);
//					editBar.animate().translationY(editBar.getHeight());
//				}
			}
		});

		int editableVisibility = ViewUtils.getVisibilityInt(editable);
		nameEditable.setVisibility(editableVisibility);

		if (!editable) {
			nameReadOnly.setText(nameEditable.getText());
		}

	}

	private void animateScrollViewSlide(boolean editable) {
//		LayoutParams params = (LayoutParams) editBar.getLayoutParams();
//		final float editBarHeight = params.height;
//		final int originalHeight = getLayoutParams().height;
//		getLayoutParams().height = getHeight() + (int) editBarHeight;
//
//		if (editable) {
//			editBar.setVisibility(VISIBLE);
//			setTranslationY(editBarHeight);
//			ViewUtils.collapse(this);
//			animate().translationY(0).setListener(new AnimatorListenerAdapter() {
//				@Override
//				public void onAnimationEnd(Animator animation) {
//					getLayoutParams().height = originalHeight;
//					requestLayout();
//				}
//			});
//		} else {
//			ViewUtils.expand(this);
//			animate().translationY(0).setListener(new AnimatorListenerAdapter() {
//				@Override
//				public void onAnimationEnd(Animator animation) {
//					editBar.setVisibility(GONE);
//					setTranslationY(editBarHeight);
//					getLayoutParams().height = originalHeight;
//					requestLayout();
//				}
//			});
//		}
	}

	/**
	 * Are we currently dragging a view?
	 */
	public boolean currentlyDragging() {
		return dragging;
	}

	/**
	 * Handles a move of the touch pointer.
	 */
	public void handleHoverCellMove() {
		int deltaY = lastEventY - downY;

		if (hoverCellCurrentBounds != null && hoverCell != null) {
			hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left, hoverCellOriginalBounds.top + deltaY);
			hoverCell.setBounds(hoverCellCurrentBounds);
			invalidate();
		}

		moveDragViewIfNecessary();
	}

	/**
	 * Checks whether the drag view needs to be moved into another view or swapped with an existing view and if so does
	 * it.
	 */
	private void moveDragViewIfNecessary() {
		if (hoverCellCurrentBounds == null) {
			return;
		}

		if (hoverCellCurrentBounds.top > recycleBin.getTop() && hoverCellCurrentBounds.top < recycleBin.getBottom()) {
			recycleBin.setBackgroundResource(R.drawable.recycle_bin_bg_hover);
			if (dragView.getParentNode() != null) {
				dragView.getParentNode().removeChild(dragView);
			}
		} else {
			recycleBin.setBackgroundResource(R.drawable.recycle_bin_bg);
			final InsertionPoint insertionPoint = findInsertionPoint(hoverCellCurrentBounds.top, dragView);

			if (insertionPoint != null && insertionPoint.swapWith != dragView) {
				insertAt(dragView, insertionPoint);
			}
		}
	}

	private InsertionPoint findInsertionPoint(int y, DraggableView viewToInsert) {
		return scrollingView.findInsertionPoint(y + scrollingView.getScrollY() - scrollingView.getTop(), viewToInsert);
	}

	/**
	 * Inserts a {@link DraggableView} at the supplied {@link InsertionPoint}.
	 */
	private void insertAt(final DraggableView draggedView, final InsertionPoint insertionPoint) {
		if (canSwap(insertionPoint, draggedView)) {
			// If the difference between where the user's finger is and the top of the view it's hovering over is
			// greater than the height of the view we're inserting into that space, it'll mean that even after we swap
			// the views, insertionPoint.swapWith is *still* going to be under the user's finger which will cause a swap
			// every time a touch event comes in, which means flickering. Hence just do nothing if this is the case.
			int insertionPointDiff = insertionPoint.topInView - insertionPoint.swapWith.asView().getTop();
			if (insertionPointDiff > draggedView.asView().getHeight()) {
				return;
			}

			int dragViewIndex = getChildIndex(draggedView.asView());

			draggedView.getParentNode().removeViewAt(dragViewIndex);

			if (insertionPoint.index == -1) {
				insertionPoint.parent.addChild(draggedView);
			} else {
				insertionPoint.parent.addChild(draggedView, insertionPoint.index);
			}

			insertionPoint.parent.removeView(insertionPoint.swapWith.asView());
			draggedView.getParentNode().addView(insertionPoint.swapWith.asView(), dragViewIndex);

			final int animationStartTop = insertionPoint.swapWith.asView().getTop();

			animateMove(insertionPoint.swapWith.asView(), animationStartTop);
		} else {
			if (draggedView.getParentNode() != null) {
				draggedView.getParentNode().removeChild(draggedView);
			}
			insertionPoint.parent.addChild(draggedView, insertionPoint.index);
		}
	}

	/**
	 * Determines whether inserting the draggedView at this insertion point can be achieved by swapping the two views.
	 */
	private boolean canSwap(final InsertionPoint insertionPoint, DraggableView draggedView) {
		return draggedView.getParentNode() == insertionPoint.parent && insertionPoint.swapWith != null;
	}

	/**
	 * Animates a view from a starting top value to its current position.
	 */
	private void animateMove(final View swapWith, final int startingTop) {
		final ViewTreeObserver observer = getViewTreeObserver();
		observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			public boolean onPreDraw() {
				observer.removeOnPreDrawListener(this);

				int endingTop = swapWith.getTop();
				int delta = startingTop - endingTop;

				swapWith.setTranslationY(delta);

				ObjectAnimator animator = ObjectAnimator.ofFloat(swapWith, View.TRANSLATION_Y, 0);
				animator.setDuration(MOVE_DURATION);
				animator.start();

				return true;
			}
		});
	}

	@Override
	public void startDrag(DraggableView view, int downY) {
		startDrag(view, downY, ViewUtils.getYCoordOnScreen(view.asView()));

		performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
	}

	public void startDrag(final DraggableView view, int downY, final int startTop) {
		dragView = view;
		this.downY = downY;
		dragging = true;

		post(new Runnable() {
			@Override
			public void run() {
				if (hoverViewAnimator != null && hoverViewAnimator.isRunning()) {
					hoverViewAnimator.end();
				}

				// Set the focus on the outermost layout to defocus whatever was draggableViewFocused before,
				// without focusing on the name button.
				requestFocus();
				// Make sure the edit button stays visible during the drag.
				editButton.setVisibility(VISIBLE);

				dragView.setBeingDragged(true);
				hoverCell = getAndAddHoverView(dragView);

				int w = dragView.asView().getWidth();
				int h = dragView.asView().getHeight();
				int top = startTop - locationOnScreen;
				int left = getCompleteLeft(dragView.asView(), 0);

				hoverCellCurrentBounds = new Rect(left, top, left + w, top + h);
				hoverCellOriginalBounds = new Rect(hoverCellCurrentBounds);
				hoverCell.setBounds(hoverCellCurrentBounds);
				dragView.asView().setVisibility(View.INVISIBLE);

				invalidate();
			}
		});
	}

	/**
	 * Recursively determines how far the left edge of a view is from the edge of its outermost ancestor (not its
	 * immediate parent).
	 */
	private int getCompleteLeft(View view, int leftSoFar) {
		leftSoFar += view.getLeft();
		if (view.getParent() != null && view.getParent() instanceof View && !(view.getParent() instanceof ScrollView)) {
			return getCompleteLeft((View) view.getParent(), leftSoFar);
		} else {
			return leftSoFar;
		}
	}

	/**
	 * Creates the hover cell with the appropriate bitmap and of appropriate size. The hover cell's BitmapDrawable is
	 * drawn on top of the bitmap every single time an invalidate call is made.
	 */
	private BitmapDrawable getAndAddHoverView(DraggableView v) {
		Bitmap b = getBitmapFromView(v.asView());

		BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

		return drawable;
	}

	/**
	 * Returns a bitmap showing a screenshot of the view passed in.
	 */
	private Bitmap getBitmapFromView(View v) {
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		v.draw(canvas);
		return bitmap;
	}

	/**
	 * Resets all the appropriate fields to a default state while also animating the hover cell back to its correct
	 * location.
	 */
	private void touchEventsEnded() {
		if (currentlyDragging()) {
			dragging = false;

			if (dragView.getParentNode() == null) {
				// Deleted
				cleanUpAfterDragEnd();
				notifyFocused(false, null);
			} else {
				if (dragView.isNewlyCreated()) {
					dragView.edit();
					dragView.setNewlyCreated(false);
				}

				animateRestoreHoverCell();
			}

			if (recycleBin != null) {
				recycleBin.setBackgroundResource(R.drawable.recycle_bin_bg);
			}
		}
	}

	/**
	 * Animates the hover cell back to the actual position of the view it represents.
	 */
	private void animateRestoreHoverCell() {
		if (hoverViewAnimator != null && hoverViewAnimator.isRunning()) {
			hoverViewAnimator.end();
		}

		if (hoverCellCurrentBounds == null) {
			return;
		}

		hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left, ViewUtils.getYCoordOnScreen(dragView.asView())
				- locationOnScreen);

		hoverViewAnimator = ObjectAnimator.ofObject(hoverCell, "bounds", boundEvaluator, hoverCellCurrentBounds);
		hoverViewAnimator.addUpdateListener(hoverCancelAnimatorUpdateListener);
		hoverViewAnimator.addListener(hoverCancelAnimatorListener);

		hoverViewAnimator.start();
	}

	private void cleanUpAfterDragEnd() {
		dragView.asView().setVisibility(VISIBLE);
		dragView.asView().requestFocus();
		dragView.setBeingDragged(false);
		if (hoverViewAnimator != null && hoverViewAnimator.isRunning()) {
			hoverViewAnimator.cancel();
		}
		hoverCell = null;
		invalidate();
	}

	@Override
	public ExerciseView buildExerciseView(Exercise exercise, DraggableView parent) {
		ExerciseView exerciseView = (ExerciseView) layoutInflater.inflate(R.layout.view_exercise, this, false);

		exerciseView.setExercise(exercise);
		exerciseView.setNodeView(parent);
		exerciseView.setDragManager(this);

		return exerciseView;
	}

	@Override
	public NodeView buildNodeView(Node node) {
		NodeView nodeView = (NodeView) layoutInflater.inflate(R.layout.view_node, this, false);

		nodeView.setDragManager(this);
		nodeView.init(node);
		nodeView.setId(View.generateViewId());

		return nodeView;
	}

	public void setFragmentManager(FragmentManager fragmentManager) {
		this.fragmentManager = fragmentManager;
	}

	@Override
	public FragmentManager getFragmentManager() {
		return fragmentManager;
	}

	private Runnable hideEditButtonRunnable = new Runnable() {
		@Override
		public void run() {
			editButton.setVisibility(ViewUtils.getVisibilityInt(draggableViewFocused || currentlyDragging()));
		}
	};

	@Override
	public void notifyFocused(boolean focused, DraggableView focusedView) {
		draggableViewFocused = focused;
		if (focused) {
			lastFocusedView = (DraggableView) focusedView;
		}
		postDelayed(hideEditButtonRunnable, 10);
	}

	private final OnClickListener editListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			lastFocusedView.edit();
		}
	};

	/**
	 * This TypeEvaluator is used to animate the BitmapDrawable back to its final location when the user lifts their
	 * finger by modifying the BitmapDrawable's bounds.
	 */
	private final static TypeEvaluator<Rect> boundEvaluator = new TypeEvaluator<Rect>() {
		public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
			return new Rect(interpolate(startValue.left, endValue.left, fraction), interpolate(startValue.top,
					endValue.top, fraction), interpolate(startValue.right, endValue.right, fraction), interpolate(
					startValue.bottom, endValue.bottom, fraction));
		}

		public int interpolate(int start, int end, float fraction) {
			return (int) (start + fraction * (end - start));
		}
	};

	private OnTouchListener addExerciseListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, final MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				InsertionPoint insertionPoint = findInsertionPoint(scrollingView.getTop(), null);
				final ExerciseView view = insertionPoint.parent.addExercise(new Exercise(), insertionPoint.index);
				view.setNewlyCreated(true);
				scrollingView.lockScrollUntilTouchInMiddle();

				post(new Runnable() {
					@Override
					public void run() {
						startDrag(view, (int) event.getRawY(), ViewUtils.getYCoordOnScreen(addExerciseButton));
					}
				});
			}
			addExerciseButton.onTouchEvent(event);
			return false;
		}
	};

	private OnTouchListener addNodeListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, final MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				Node node = new Node(1);
				node.setParent(node);

				final NodeView view = (NodeView) layoutInflater.inflate(R.layout.view_node,
						scrollingView.getNodeView(), false);
				view.init(node);
				view.setDragManager(ProgramDetailView.this);
				insertAt(view, findInsertionPoint(scrollingView.getTop(), view));
				view.setEditable(true);
				view.setNewlyCreated(true);
				scrollingView.lockScrollUntilTouchInMiddle();

				post(new Runnable() {
					@Override
					public void run() {
						startDrag(view, (int) event.getRawY(), ViewUtils.getYCoordOnScreen(addExerciseButton));
					}
				});
			}
			addNodeButton.onTouchEvent(event);
			return false;
		}
	};

	private OnGlobalLayoutListener yOffsetObserver = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			locationOnScreen = ViewUtils.getYCoordOnScreen(ProgramDetailView.this);
		}
	};
	/**
	 * Update listener for animating the hover cell back to its original position - makes sure that each frame of the
	 * animation is actually rendered.
	 */
	private ValueAnimator.AnimatorUpdateListener hoverCancelAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator valueAnimator) {
			invalidate();
		}
	};

	/**
	 * Listener for animating the hover cell back to its original position (for when a drag is ended).
	 */
	private AnimatorListenerAdapter hoverCancelAnimatorListener = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationStart(Animator animation) {
			setEnabled(false);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			cleanUpAfterDragEnd();
		}
	};
}
