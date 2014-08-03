package com.alexgilleran.hiitme.presentation.programdetail.views;

import java.util.Timer;
import java.util.TimerTask;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.presentation.programdetail.views.NodeView.InsertionPoint;
import com.alexgilleran.hiitme.util.ViewUtils;

public class ProgramDetailView extends ScrollView implements DragManager {
	private static final int DRAG_SCROLL_INTERVAL = 100;
	private static final int MOVE_DURATION = 150;
	private static final float DRAG_SCROLL_THRESHOLD_FRACTION = 0.2f;

	private ImageButton addExerciseButton;
	private ImageButton addNodeButton;
	private View recycleBin;

	private LayoutInflater layoutInflater;
	private NodeView nodeView;
	private int downY, lastEventY;
	private Rect hoverCellCurrentBounds, hoverCellOriginalBounds;
	private BitmapDrawable hoverCell;
	private DraggableView dragView;

	private int dragScrollUpThreshold = -1;
	private int dragScrollDownThreshold = -1;
	private int downScrollY;
	private Timer scrollTimer;
	private boolean isBeingEdited = false;

	public ProgramDetailView(Context context) {
		super(context);
		layoutInflater = LayoutInflater.from(context);
	}

	public ProgramDetailView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		layoutInflater = LayoutInflater.from(context);
	}

	public ProgramDetailView(Context context, AttributeSet attrs) {
		super(context, attrs);
		layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public void onFinishInflate() {
		RelativeLayout root = (RelativeLayout) this.findViewById(R.id.layout_root);

		addExerciseButton = (ImageButton) findViewById(R.id.button_add_exercise);
		addNodeButton = (ImageButton) findViewById(R.id.button_add_node);
		recycleBin = (View) findViewById(R.id.layout_recycle_bin);

		getViewTreeObserver().addOnGlobalLayoutListener(scrollListener);
		addExerciseButton.setOnTouchListener(addExerciseListener);
		addNodeButton.setOnTouchListener(addNodeListener);

		nodeView = (NodeView) layoutInflater.inflate(R.layout.view_node, root, false);
		nodeView.setDragManager(this);
		nodeView.setId(ViewUtils.generateViewId());

		RelativeLayout.LayoutParams nodeViewLayoutParams = (RelativeLayout.LayoutParams) nodeView.getLayoutParams();
		nodeViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.name_ro);
		nodeViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.layout_recycle_bin);

		root.addView(nodeView);
	}

	public void setProgramNode(Node programNode) {
		nodeView.init(programNode);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (hoverCell != null) {
			hoverCell.draw(canvas);
		}
	}

	private void stopScrolling() {
		if (scrollTimer != null) {
			scrollTimer.cancel();
			scrollTimer = null;
		}
	}

	private void startScrolling(final int scrollY) {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				scrollBy(0, scrollY);
				post(handlePointerMoveRunnable);
			}
		};
		scrollTimer = new Timer();
		scrollTimer.scheduleAtFixedRate(timerTask, 0, DRAG_SCROLL_INTERVAL);
	}

	private Runnable handlePointerMoveRunnable = new Runnable() {
		@Override
		public void run() {
			handleHoverCellMove();
		}
	};

	public boolean isBeingEdited() {
		return isBeingEdited;
	}

	public void startEditing() {
		isBeingEdited = true;

		refreshEditability();
	}

	public void stopEditing() {
		isBeingEdited = false;

		refreshEditability();
	}

	private void refreshEditability() {
		int visibility = ViewUtils.getVisibilityInt(isBeingEdited);
		addExerciseButton.setVisibility(visibility);
		addNodeButton.setVisibility(visibility);
		recycleBin.setVisibility(visibility);

		nodeView.setEditable(isBeingEdited);
	}

	public Node getProgramNode() {
		return nodeView.getProgramNode();
	}

	private boolean scrollParamsSet() {
		return dragScrollDownThreshold >= 0 && dragScrollUpThreshold >= 0;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		onTouchEvent(ev);

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		stopScrolling();

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE:
			if (currentlyDragging()) {
				lastEventY = (int) event.getRawY();

				scrollIfNecessary();

				handleHoverCellMove();

				return true;
			}

			break;
		case MotionEvent.ACTION_UP:
			touchEventsEnded();
			return true;
		}

		return super.onTouchEvent(event);
	}

	/**
	 * Scrolls the view up or down if the last touch was close enough to either end of the view.
	 */
	private void scrollIfNecessary() {
		if (scrollParamsSet()) {
			if (lastEventY > dragScrollDownThreshold) {
				startScrolling((int) (lastEventY - dragScrollDownThreshold) / 2);
			} else if (lastEventY < dragScrollUpThreshold) {
				startScrolling((int) (lastEventY - dragScrollUpThreshold) / 2);
			}
		}
	}

	@Override
	public void cancelDrag() {
		touchEventsEnded();
	}

	/**
	 * Are we currently dragging a view?
	 */
	private boolean currentlyDragging() {
		return hoverCell != null && hoverCellCurrentBounds != null && hoverCellOriginalBounds != null;
	}

	/**
	 * Handles a move of the touch pointer.
	 */
	private void handleHoverCellMove() {
		int deltaY = lastEventY - downY + getScrollY() - downScrollY;

		hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left, hoverCellOriginalBounds.top + deltaY);
		hoverCell.setBounds(hoverCellCurrentBounds);
		invalidate();

		moveDragViewIfNecessary();
	}

	/**
	 * Checks whether the drag view needs to be moved into another view or swapped with an existing view and if so does
	 * it.
	 */
	private void moveDragViewIfNecessary() {
		if (hoverCellCurrentBounds.top > recycleBin.getTop() && hoverCellCurrentBounds.top < recycleBin.getBottom()) {
			if (dragView.getParentNodeView() != null) {
				dragView.getParentNodeView().removeChild(dragView);
			}
		} else {
			final InsertionPoint insertionPoint = nodeView.findInsertionPoint(hoverCellCurrentBounds.top
					- getCompleteTop(nodeView, 0), dragView);

			if (insertionPoint != null && insertionPoint.swapWith != dragView) {
				insertAt(dragView, insertionPoint);
			}
		}
	}

	/**
	 * Inserts a {@link DraggableView} at the supplied {@link InsertionPoint}.
	 */
	private void insertAt(final DraggableView draggedView, final InsertionPoint insertionPoint) {
		if (canSwap(insertionPoint, draggedView)) {
			int dragViewIndex = getChildIndex(draggedView.asView());

			draggedView.getParentNodeView().removeViewAt(dragViewIndex);

			if (insertionPoint.index == -1) {
				insertionPoint.parent.addChild(draggedView);
			} else {
				insertionPoint.parent.addChild(draggedView, insertionPoint.index);
			}

			insertionPoint.parent.removeView(insertionPoint.swapWith.asView());
			draggedView.getParentNodeView().addView(insertionPoint.swapWith.asView(), dragViewIndex);

			final int animationStartTop = insertionPoint.swapWith.asView().getTop();

			animateMove(insertionPoint.swapWith.asView(), animationStartTop);
		} else {
			if (dragView.getParentNodeView() != null) {
				draggedView.getParentNodeView().removeChild(draggedView);
			}
			insertionPoint.parent.addChild(draggedView, insertionPoint.index);
		}
	}

	/**
	 * Determines whether inserting the draggedView at this insertion point can be achieved by swapping the two views.
	 */
	private boolean canSwap(final InsertionPoint insertionPoint, DraggableView draggedView) {
		return draggedView.getParentNodeView() == insertionPoint.parent && insertionPoint.swapWith != null;
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

	private void startDrag(DraggableView view, int downY, int startTop) {
		view.setBeingDragged(true);

		this.downY = downY;
		downScrollY = getScrollY();

		dragView = view;
		hoverCell = getAndAddHoverView(view);

		int w = view.asView().getWidth();
		int h = view.asView().getHeight();
		int top = startTop;
		int left = getCompleteLeft(view.asView(), 0);

		hoverCellCurrentBounds = new Rect(left, top, left + w, top + h);
		hoverCellOriginalBounds = new Rect(hoverCellCurrentBounds);
		hoverCell.setBounds(hoverCellCurrentBounds);
		view.asView().setVisibility(View.INVISIBLE);

		invalidate();
	}

	@Override
	public void startDrag(DraggableView view, int downY) {
		startDrag(view, downY, getCompleteTop(view.asView(), 0));
	}

	/**
	 * Recursively determines how far the top edge of a view is from the edge of its outermost ancestor (not its
	 * immediate parent).
	 */
	private int getCompleteTop(View view, int topSoFar) {
		topSoFar += view.getTop();
		if (view.getParent() != null && view.getParent() instanceof View && !(view.getParent() instanceof ScrollView)) {
			return getCompleteTop((View) view.getParent(), topSoFar);
		} else {
			return topSoFar;
		}
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

	/** Returns a bitmap showing a screenshot of the view passed in. */
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
			if (dragView.getParentNodeView() == null) {
				// Animate removing the view.
				cleanUpAfterDragEnd();
			} else {
				animateRestoreHoverCell();
			}
		}
	}

	/**
	 * Animates the hover cell back to the actual position of the view it represents.
	 */
	private void animateRestoreHoverCell() {
		hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left, getCompleteTop(dragView.asView(), 0));

		ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverCell, "bounds", boundEvaluator,
				hoverCellCurrentBounds);
		hoverViewAnimator.addUpdateListener(hoverCancelAnimatorUpdateListener);
		hoverViewAnimator.addListener(hoverCancelAnimatorListener);

		hoverViewAnimator.start();
	}

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
			dragView.asView().setVisibility(VISIBLE);
			cleanUpAfterDragEnd();
		}
	};

	private void cleanUpAfterDragEnd() {
		dragView.setBeingDragged(false);
		hoverCell = null;
		setEnabled(true);
		ProgramDetailView.this.invalidate();
	}

	/**
	 * Determines how far from the top/bottom of the screen a touch should be before it triggers scrolling.
	 */
	private OnGlobalLayoutListener scrollListener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			int[] location = new int[2];
			getLocationOnScreen(location);

			int thresholdFractionPx = (int) (getHeight() * DRAG_SCROLL_THRESHOLD_FRACTION);
			dragScrollUpThreshold = location[1] + thresholdFractionPx;
			dragScrollDownThreshold = location[1] + getHeight() - thresholdFractionPx;
		}
	};

	/**
	 * This TypeEvaluator is used to animate the BitmapDrawable back to its final location when the user lifts his
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
				// FIXME: This is a copy of a lot of the stuff that happens in NodeView...
				Exercise exercise = new Exercise();
				exercise.setNode(getProgramNode());
				final ExerciseView view = (ExerciseView) layoutInflater
						.inflate(R.layout.view_exercise, nodeView, false);
				view.setExercise(exercise);
				view.setNodeView(nodeView);
				view.setEditable(true);
				view.setDragManager(ProgramDetailView.this);
				nodeView.addChild(view, 1);

				post(new Runnable() {
					@Override
					public void run() {
						startDrag(view, (int) event.getRawY(), getCompleteTop(addExerciseButton, 0));
					}
				});
			}
			return false;
		}
	};

	private OnTouchListener addNodeListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, final MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				// FIXME: This is a copy of a lot of the stuff that happens in NodeView...
				Node node = new Node();
				node.setParent(node);
				final NodeView view = (NodeView) layoutInflater.inflate(R.layout.view_node, nodeView, false);
				view.init(node);
				view.setEditable(true);
				view.setDragManager(ProgramDetailView.this);
				nodeView.addChild(view, 1);

				post(new Runnable() {
					@Override
					public void run() {
						startDrag(view, (int) event.getRawY(), getCompleteTop(addNodeButton, 0));
					}
				});
			}
			return false;
		}
	};
}