package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ScrollView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;

public class ProgramDetailView extends ScrollView implements DragManager {
	private int INVALID_ID = -1;
	private OnScrollListener onScrollListener;
	private LayoutInflater layoutInflater;
	private ProgramNodeView nodeView;
	private boolean isMobileScrolling;
	private int downX, downY, activePointerId, lastEventY, totalOffset;
	private int INVALID_POINTER_ID = -1;
	private Rect hoverCellCurrentBounds, hoverCellOriginalBounds;
	private BitmapDrawable hoverCell;
	private DraggableView dragView, aboveView, belowView;
	private Program program;
	private int scrollState;

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
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
	}

	@Override
	public void onFinishInflate() {
		nodeView = (ProgramNodeView) layoutInflater.inflate(R.layout.view_program_node, null);
		nodeView.setDragManager(this);
		((ViewGroup) this.findViewById(R.id.frag_programdetail_scrollcontainer)).addView(nodeView);
	}

	public void setProgram(Program program) {
		this.program = program;
		nodeView.setProgramNode(program.getAssociatedNode());
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (hoverCell != null) {
			hoverCell.draw(canvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			downX = (int) event.getX();
			downY = (int) event.getY();
			activePointerId = event.getPointerId(0);
			break;
		case MotionEvent.ACTION_MOVE:
			if (activePointerId == INVALID_POINTER_ID) {
				break;
			}

			int pointerIndex = event.findPointerIndex(activePointerId);

			lastEventY = (int) event.getY(pointerIndex);
			int deltaY = lastEventY - downY;

			if (hoverCell != null && hoverCellCurrentBounds != null && hoverCellOriginalBounds != null) {
				hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left, hoverCellOriginalBounds.top + deltaY
						+ totalOffset);
				hoverCell.setBounds(hoverCellCurrentBounds);
				invalidate();

				handleCellSwitch();

				isMobileScrolling = false;
				// handleMobileCellScroll();

				return false;
			}
			break;
		case MotionEvent.ACTION_UP:
			touchEventsEnded();
			break;
		case MotionEvent.ACTION_CANCEL:
			// touchEventsCancelled();
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

		return true;
	}

	private void handleMobileCellScroll() {
		isMobileScrolling = handleMobileCellScroll(hoverCellCurrentBounds);
	}

	/**
	 * This method is in charge of determining if the hover cell is above or
	 * below the bounds of the listview. If so, the listview does an appropriate
	 * upward or downward smooth scroll so as to reveal new items.
	 */
	public boolean handleMobileCellScroll(Rect r) {
		// int offset = computeVerticalScrollOffset();
		// int height = getHeight();
		// int extent = computeVerticalScrollExtent();
		// int range = computeVerticalScrollRange();
		// int hoverViewTop = r.top;
		// int hoverHeight = r.height();
		//
		// if (hoverViewTop <= 0 && offset > 0) {
		// smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
		// return true;
		// }
		//
		// if (hoverViewTop + hoverHeight >= height && (offset + extent) <
		// range) {
		// smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
		// return true;
		// }
		//
		return false;
	}

	private void handleCellSwitch() {
		final int deltaY = lastEventY - downY;
		int deltaYTotal = hoverCellOriginalBounds.top + totalOffset + deltaY;

		// View belowView = getViewForID(mBelowItemId);
		// View mobileView = getViewForID(mMobileItemId);
		// View aboveView = getViewForID(mAboveItemId);

		// boolean isBelow = (belowView != null) && (deltaYTotal >
		// belowView.getTop());
		// boolean isAbove = (aboveView != null) && (deltaYTotal <
		// aboveView.getTop());

		// if (isBelow || isAbove) {
		//
		// final long switchItemID = isBelow ? mBelowItemId : mAboveItemId;
		// View switchView = isBelow ? belowView : aboveView;
		// final int originalItem = getPositionForView(mobileView);
		//
		// if (switchView == null) {
		// updateNeighborViewsForID(mMobileItemId);
		// return;
		// }
		//
		// swapElements(mCheeseList, originalItem,
		// getPositionForView(switchView));
		//
		// ((BaseAdapter) getAdapter()).notifyDataSetChanged();
		//
		// mDownY = mLastEventY;
		//
		// final int switchViewStartTop = switchView.getTop();
		//
		// mobileView.setVisibility(View.VISIBLE);
		// switchView.setVisibility(View.INVISIBLE);
		//
		// updateNeighborViewsForID(mMobileItemId);
		//
		// final ViewTreeObserver observer = getViewTreeObserver();
		// observer.addOnPreDrawListener(new
		// ViewTreeObserver.OnPreDrawListener() {
		// public boolean onPreDraw() {
		// observer.removeOnPreDrawListener(this);
		//
		// View switchView = getViewForID(switchItemID);
		//
		// mTotalOffset += deltaY;
		//
		// int switchViewNewTop = switchView.getTop();
		// int delta = switchViewStartTop - switchViewNewTop;
		//
		// switchView.setTranslationY(delta);
		//
		// ObjectAnimator animator = ObjectAnimator.ofFloat(switchView,
		// View.TRANSLATION_Y, 0);
		// animator.setDuration(MOVE_DURATION);
		// animator.start();
		//
		// return true;
		// }
		// });
		// }
	}

	@Override
	public void startDrag(DraggableView view) {
		dragView = view;
		hoverCell = getAndAddHoverView(view);

		int w = view.getWidth();
		int h = view.getHeight();
		int top = view.getTop();
		int left = view.getLeft();

		hoverCellCurrentBounds = new Rect(left, top, left + w, top + h);
		hoverCellOriginalBounds = new Rect(hoverCellCurrentBounds);
		hoverCell.setBounds(hoverCellCurrentBounds);
		view.setVisibility(View.INVISIBLE);
	}

	/**
	 * Creates the hover cell with the appropriate bitmap and of appropriate
	 * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
	 * single time an invalidate call is made.
	 */
	private BitmapDrawable getAndAddHoverView(View v) {
		Bitmap b = getBitmapWithBorder(v);

		BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

		return drawable;
	}

	/** Draws a black border over the screenshot of the view passed in. */
	private Bitmap getBitmapWithBorder(View v) {
		Bitmap bitmap = getBitmapFromView(v);
		Canvas can = new Canvas(bitmap);

		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setColor(Color.BLACK);

		can.drawBitmap(bitmap, 0, 0, null);
		can.drawRect(rect, paint);

		return bitmap;
	}

	/** Returns a bitmap showing a screenshot of the view passed in. */
	private Bitmap getBitmapFromView(View v) {
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		v.draw(canvas);
		return bitmap;
	}

	@Override
	public BitmapDrawable getHoverCell() {
		return hoverCell;
	}

	private boolean cellIsMobile() {
		return hoverCell != null;
	}

	boolean waitingForScrollToFinish;

	private OnScrollListener scrollListener = new OnScrollListener() {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub

		}

	};

	/**
	 * Resets all the appropriate fields to a default state while also animating
	 * the hover cell back to its correct location.
	 */
	private void touchEventsEnded() {
		if (cellIsMobile() || waitingForScrollToFinish) {
			// hoverCell = null;
			waitingForScrollToFinish = false;
			// mIsMobileScrolling = false;
			// mActivePointerId = INVALID_POINTER_ID;

			// If the autoscroller has not completed scrolling, we need to wait
			// for it to
			// finish in order to determine the final location of where the
			// hover cell
			// should be animated to.
			if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
				waitingForScrollToFinish = true;
				return;
			}

			hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left, dragView.getTop());

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
					aboveView = null;
					belowView = null;
					dragView.setVisibility(VISIBLE);
					hoverCell = null;
					setEnabled(true);
					invalidate();
				}
			});
			hoverViewAnimator.start();
		} else {
			// touchEventsCancelled();
		}
	}

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