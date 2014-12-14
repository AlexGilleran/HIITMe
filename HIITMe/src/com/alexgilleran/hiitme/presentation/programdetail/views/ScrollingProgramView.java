package com.alexgilleran.hiitme.presentation.programdetail.views;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.presentation.programdetail.views.NodeView.InsertionPoint;
import com.alexgilleran.hiitme.util.ViewUtils;

public class ScrollingProgramView extends ScrollView {
	private static final int DRAG_SCROLL_INTERVAL = 100;
	private static final float DRAG_SCROLL_THRESHOLD_FRACTION = 0.2f;

	private LayoutInflater layoutInflater;

	private int dragScrollUpThreshold = -1;
	private int dragScrollDownThreshold = -1;
	private Timer scrollTimer;
	private NodeView nodeView;
	private DragManager dragManager;

	public ScrollingProgramView(Context context) {
		super(context);
		layoutInflater = LayoutInflater.from(context);
	}

	public ScrollingProgramView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		layoutInflater = LayoutInflater.from(context);
	}

	public ScrollingProgramView(Context context, AttributeSet attrs) {
		super(context, attrs);
		layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public void onFinishInflate() {
		getViewTreeObserver().addOnGlobalLayoutListener(scrollThresholdListener);
	}

	public InsertionPoint findInsertionPoint(int top, DraggableView viewToSwapIn) {
		return nodeView.findInsertionPoint(top - nodeView.getTop(), viewToSwapIn);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		handleTouchEvent(event);

		if (!handleTouchEvent(event)) {
			return false;
		} else {
			return super.onTouchEvent(event);
		}
	}

	/**
	 * Handles scrolling for a drag without processing it as an actual touch event on the scroll.
	 */
	public boolean handleTouchEvent(MotionEvent event) {
		stopScrolling();

		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
			if (dragManager.currentlyDragging()) {
				scrollIfNecessary((int) event.getRawY());
				return false;
			}
		}

		return true;
	}

	public NodeView getNodeView() {
		return nodeView;
	}

	public void init(DragManager dragManager, Node programNode) {
		this.dragManager = dragManager;

		LinearLayout root = (LinearLayout) findViewById(R.id.root);

		if (nodeView != null) {
			root.removeView(nodeView);
		}

		nodeView = dragManager.buildNodeView(programNode);
		root.addView(nodeView);
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
			dragManager.handleHoverCellMove();
		}
	};

	public void setEditable(boolean editable) {
		nodeView.setEditable(editable);
	}

	public Node rebuildNode() {
		return nodeView.rebuildNode();
	}

	private boolean scrollParamsSet() {
		return dragScrollDownThreshold >= 0 && dragScrollUpThreshold >= 0;
	}

	/**
	 * Scrolls the view up or down if the last touch was close enough to either end of the view.
	 */
	private void scrollIfNecessary(int eventY) {
		if (scrollParamsSet()) {
			if (eventY > dragScrollDownThreshold) {
				startScrolling((eventY - dragScrollDownThreshold) / 2);
			} else if (eventY < dragScrollUpThreshold) {
				startScrolling((eventY - dragScrollUpThreshold) / 2);
			}
		}
	}

	/**
	 * Determines how far from the top/bottom of the screen a touch should be before it triggers scrolling.
	 */
	private OnGlobalLayoutListener scrollThresholdListener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			int yCoordOnScreen = ViewUtils.getYCoordOnScreen(ScrollingProgramView.this);
			int thresholdFractionPx = (int) (getHeight() * DRAG_SCROLL_THRESHOLD_FRACTION);
			dragScrollUpThreshold = yCoordOnScreen + thresholdFractionPx;
			dragScrollDownThreshold = yCoordOnScreen + getHeight() - thresholdFractionPx;
		}
	};
}