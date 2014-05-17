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
import android.widget.ScrollView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.presentation.programdetail.views.NodeView.InsertionPoint;

public class ProgramDetailView extends ScrollView implements DragManager {
	private static final int DRAG_SCROLL_INTERVAL = 100;
	private static final int MOVE_DURATION = 150;
	private static final float DRAG_SCROLL_THRESHOLD_FRACTION = 0.2f;

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
		nodeView = (NodeView) layoutInflater.inflate(R.layout.view_program_node, null);
		nodeView.setDragManager(this);
		((ViewGroup) this.findViewById(R.id.root_node_view_container)).addView(nodeView);

		getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
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
			handlePointerMove();
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
		nodeView.setEditable(isBeingEdited);
	}

	public Node getProgramNode() {
		return nodeView.getProgramNode();
	}

	private boolean scrollParamsSet() {
		return dragScrollDownThreshold >= 0 && dragScrollUpThreshold >= 0;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		stopScrolling();

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE:
			if (currentlyDragging()) {
				lastEventY = (int) event.getRawY();

				if (scrollParamsSet()) {
					if (lastEventY > dragScrollDownThreshold) {
						startScrolling((int) (lastEventY - dragScrollDownThreshold) / 2);
					} else if (lastEventY < dragScrollUpThreshold) {
						startScrolling((int) (lastEventY - dragScrollUpThreshold) / 2);
					}
				}

				handlePointerMove();

				return true;
			}

			break;
		case MotionEvent.ACTION_UP:
			touchEventsEnded();
			return true;
		case MotionEvent.ACTION_CANCEL:
			System.out.println("blah");
			break;
		case MotionEvent.ACTION_POINTER_UP:
			/*
			 * If a multitouch event took place and the original touch dictating
			 * the movement of the hover cell has ended, then the dragging event
			 * ends and the hover cell is animated to its corresponding position
			 * in the listview.
			 */
			// pointerIndex = (event.getAction() &
			// MotionEvent.ACTION_POINTER_INDEX_MASK) >>
			// MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			// final int pointerId = event.getPointerId(pointerIndex);
			// if (pointerId == mActivePointerId) {
			// touchEventsEnded();
			// }
			break;
		default:
			break;
		}

		return super.onTouchEvent(event);
	}

	@Override
	public void cancelDrag() {
		touchEventsEnded();
	}

	private boolean currentlyDragging() {
		return hoverCell != null && hoverCellCurrentBounds != null && hoverCellOriginalBounds != null;
	}

	private void handlePointerMove() {
		int deltaY = lastEventY - downY + getScrollY() - downScrollY;

		hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left, hoverCellOriginalBounds.top + deltaY);
		hoverCell.setBounds(hoverCellCurrentBounds);
		invalidate();

		handleCellSwitch();
	}

	private void handleCellSwitch() {
		final InsertionPoint insertionPoint = nodeView.findViewAtTop(
				hoverCellCurrentBounds.top - getCompleteTop(nodeView, 0), dragView);

		if (insertionPoint != null && insertionPoint.swapWith != dragView) {
			insertAt(dragView, insertionPoint);
		}
	}

	private void insertAt(final DraggableView dragView, final InsertionPoint insertionPoint) {
		NodeView dragViewParent = dragView.getParentProgramNodeView();

		if (dragViewParent == insertionPoint.parent && insertionPoint.swapWith != null) {
			int dragViewIndex = getChildIndex(dragViewParent, dragView.asView());

			dragViewParent.removeViewAt(dragViewIndex);

			if (insertionPoint.index == -1) {
				insertionPoint.parent.addChild(dragView);
			} else {
				insertionPoint.parent.addChild(dragView, insertionPoint.index);
			}

			insertionPoint.parent.removeView(insertionPoint.swapWith.asView());
			dragViewParent.addView(insertionPoint.swapWith.asView(), dragViewIndex);

			final int switchViewStartTop = insertionPoint.swapWith.asView().getTop();

			final ViewTreeObserver observer = getViewTreeObserver();
			observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				public boolean onPreDraw() {
					observer.removeOnPreDrawListener(this);

					int switchViewNewTop = insertionPoint.swapWith.asView().getTop();
					int delta = switchViewStartTop - switchViewNewTop;

					insertionPoint.swapWith.asView().setTranslationY(delta);

					ObjectAnimator animator = ObjectAnimator.ofFloat(insertionPoint.swapWith.asView(),
							View.TRANSLATION_Y, 0);
					animator.setDuration(MOVE_DURATION);
					animator.start();

					return true;
				}
			});
		} else {
			dragViewParent.removeChild(dragView);
			insertionPoint.parent.addChild(dragView, insertionPoint.index);
		}
	}

	private int getChildIndex(ViewGroup viewGroup, View child) {
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			if (viewGroup.getChildAt(i) == child) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void startDrag(DraggableView view, MotionEvent event) {
		view.setBeingDragged(true);

		downY = (int) event.getRawY();
		downScrollY = getScrollY();

		dragView = view;
		hoverCell = getAndAddHoverView(view);

		int w = view.asView().getWidth();
		int h = view.asView().getHeight();
		int top = getCompleteTop(view.asView(), 0);
		int left = getCompleteLeft(view.asView(), 0);

		hoverCellCurrentBounds = new Rect(left, top, left + w, top + h);
		hoverCellOriginalBounds = new Rect(hoverCellCurrentBounds);
		hoverCell.setBounds(hoverCellCurrentBounds);
		view.asView().setVisibility(View.INVISIBLE);

		invalidate();
	}

	private int getCompleteTop(View view, int topSoFar) {
		topSoFar += view.getTop();
		if (view.getParent() != null && view.getParent() instanceof View && !(view.getParent() instanceof ScrollView)) {
			return getCompleteTop((View) view.getParent(), topSoFar);
		} else {
			return topSoFar;
		}
	}

	private int getCompleteLeft(View view, int leftSoFar) {
		leftSoFar += view.getLeft();
		if (view.getParent() != null && view.getParent() instanceof View && !(view.getParent() instanceof ScrollView)) {
			return getCompleteLeft((View) view.getParent(), leftSoFar);
		} else {
			return leftSoFar;
		}
	}

	/**
	 * Creates the hover cell with the appropriate bitmap and of appropriate
	 * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
	 * single time an invalidate call is made.
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

	private boolean cellIsMobile() {
		return hoverCell != null;
	}

	/**
	 * Resets all the appropriate fields to a default state while also animating
	 * the hover cell back to its correct location.
	 */
	private void touchEventsEnded() {
		if (cellIsMobile()) {
			hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left, getCompleteTop(dragView.asView(), 0));

			ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverCell, "bounds", boundEvaluator,
					hoverCellCurrentBounds);
			hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					invalidate();
				}
			});
			hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					setEnabled(false);
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					dragView.setBeingDragged(false);
					dragView.asView().setVisibility(VISIBLE);
					hoverCell = null;
					setEnabled(true);
					ProgramDetailView.this.invalidate();
				}
			});
			hoverViewAnimator.start();
		} else {
			// touchEventsCancelled();
		}
	}

	private OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener() {
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
	 * This TypeEvaluator is used to animate the BitmapDrawable back to its
	 * final location when the user lifts his finger by modifying the
	 * BitmapDrawable's bounds.
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
}