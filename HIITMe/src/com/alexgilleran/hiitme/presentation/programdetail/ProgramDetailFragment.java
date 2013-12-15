package com.alexgilleran.hiitme.presentation.programdetail;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programdetail.views.DraggableView;
import com.alexgilleran.hiitme.presentation.programdetail.views.EditExerciseFragment;
import com.alexgilleran.hiitme.presentation.programdetail.views.EditExerciseFragment.EditExerciseListener;
import com.alexgilleran.hiitme.presentation.programdetail.views.ProgramNodeView;
import com.alexgilleran.hiitme.presentation.programlist.ProgramListActivity;

/**
 * A fragment representing a single Program detail screen. This fragment is
 * either contained in a {@link ProgramListActivity} in two-pane mode (on
 * tablets) or a {@link ProgramDetailActivity} on handsets.
 */
public class ProgramDetailFragment extends RoboFragment implements EditExerciseListener, DragManager {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	private LayoutInflater inflater;

	private Program program;

	@InjectView(R.id.frag_programdetail_scrollcontainer)
	private FrameLayout container;

	/** Mandatory empty constructor */
	public ProgramDetailFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void setProgram(Program program) {
		this.program = program;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;

		View rootView = inflater.inflate(R.layout.fragment_program_detail, container, false);
		rootView.setOnTouchListener(onTouchListener);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ProgramNodeView nodeView = (ProgramNodeView) inflater.inflate(R.layout.view_program_node, null);
		nodeView.setProgramNode(program.getAssociatedNode());
		nodeView.setDragManager(this);
		((ProgramDetailFragmentView) view.findViewById(R.id.root)).setDragManager(this);

		container.addView(nodeView);
	}

	@Override
	public void onEditExercise(Exercise exerciseToEdit) {
		EditExerciseFragment editExercise = new EditExerciseFragment();
		editExercise.setExercise(exerciseToEdit);
		editExercise.show(getFragmentManager(), "editexercise");
	}

	boolean isMobileScrolling;
	int downX, downY, activePointerId, lastEventY, totalOffset;
	int INVALID_POINTER_ID = -1;
	Rect hoverCellCurrentBounds, hoverCellOriginalBounds;
	BitmapDrawable hoverCell;

	private OnTouchListener onTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
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
					getView().invalidate();

					handleCellSwitch();

					isMobileScrolling = false;
					// handleMobileCellScroll();

					return false;
				}
				break;
			case MotionEvent.ACTION_UP:
				// touchEventsEnded();
				break;
			case MotionEvent.ACTION_CANCEL:
				// touchEventsCancelled();
				break;
			case MotionEvent.ACTION_POINTER_UP:
				/*
				 * If a multitouch event took place and the original touch
				 * dictating the movement of the hover cell has ended, then the
				 * dragging event ends and the hover cell is animated to its
				 * corresponding position in the listview.
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
	};

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

	public static class ProgramDetailFragmentView extends ScrollView {
		private DragManager dragManager;

		public ProgramDetailFragmentView(Context context) {
			super(context);
		}

		public ProgramDetailFragmentView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public ProgramDetailFragmentView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public void setDragManager(DragManager dragManager) {
			this.dragManager = dragManager;
		}

		@Override
		protected void dispatchDraw(Canvas canvas) {
			super.dispatchDraw(canvas);
			if (dragManager.getHoverCell() != null) {
				dragManager.getHoverCell().draw(canvas);
			}
		}
	}

	@Override
	public BitmapDrawable getHoverCell() {
		return hoverCell;
	}
}
