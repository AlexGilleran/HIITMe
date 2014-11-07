package com.alexgilleran.hiitme.presentation.programdetail.views;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.presentation.programdetail.views.NodeView.InsertionPoint;
import com.alexgilleran.hiitme.util.ViewUtils;

public class ProgramDetailView extends RelativeLayout implements DragManager {
	private static final int MOVE_DURATION = 150;
	private LayoutInflater layoutInflater;
	private int downY, lastEventY;
	private Rect hoverCellCurrentBounds, hoverCellOriginalBounds;
	private BitmapDrawable hoverCell;
	private DraggableView dragView;
	private View recycleBin;
	private ScrollingProgramView scrollingView;
	private ObjectAnimator hoverViewAnimator;

	private TextView nameReadOnly;
	private EditText nameEditable;
	private ImageButton addExerciseButton;
	private ImageButton addNodeButton;

	private Program program;
	private boolean editable = false;
	private boolean dragging = false;
	private int locationOnScreen;

	private OnLongClickListener exerciseLongClickListener;
	private OnLongClickListener nodeLongClickListener;

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
		recycleBin = (View) findViewById(R.id.recycle_bin);

		scrollingView = (ScrollingProgramView) findViewById(R.id.view_scrolling);

		nameReadOnly = (TextView) findViewById(R.id.name_ro);
		nameEditable = (EditText) findViewById(R.id.name_edit);
		addExerciseButton = (ImageButton) findViewById(R.id.button_add_exercise);
		addNodeButton = (ImageButton) findViewById(R.id.button_add_node);

		addExerciseButton.setOnTouchListener(addExerciseListener);
		addNodeButton.setOnTouchListener(addNodeListener);

		getViewTreeObserver().addOnGlobalLayoutListener(TODOListener);
	}

	public String getName() {
		return nameEditable.getText().toString();
	}

	public void setProgram(Program program) {
		this.program = program;

		scrollingView.init(this, program.getAssociatedNode());
		nameReadOnly.setText(program.getName());
		nameEditable.setText(program.getName());
	}

	public Program getProgram() {
		program.setName(nameEditable.getText().toString());
		program.setAssociatedNode(scrollingView.getNode());

		return program;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
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

		return false;
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

	public void setEditable(boolean editable) {
		this.editable = editable;

		scrollingView.setEditable(editable);
		nameReadOnly.setVisibility(ViewUtils.getVisibilityInt(!editable));

		int editableVisibility = ViewUtils.getVisibilityInt(editable);
		nameEditable.setVisibility(editableVisibility);
		addExerciseButton.setVisibility(editableVisibility);
		addNodeButton.setVisibility(editableVisibility);
		recycleBin.setVisibility(editableVisibility);
		((RelativeLayout.LayoutParams) scrollingView.getLayoutParams()).addRule(RelativeLayout.BELOW,
				editable ? R.id.name_edit : R.id.name_ro);

		if (!editable) {
			nameReadOnly.setText(nameEditable.getText());
		}
	}

	public boolean isEditable() {
		return editable;
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

		hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left, hoverCellOriginalBounds.top + deltaY);
		if (hoverCell != null) {
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
		if (hoverCellCurrentBounds.top > recycleBin.getTop() && hoverCellCurrentBounds.top < recycleBin.getBottom()) {
			if (dragView.getParentNode() != null) {
				dragView.getParentNode().removeChild(dragView);
			}
		} else {
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

	public void startDrag(DraggableView view, int downY, int startTop) {
		if (hoverViewAnimator != null && hoverViewAnimator.isRunning()) {
			hoverViewAnimator.end();
		}

		dragging = true;

		view.setBeingDragged(true);

		this.downY = downY;

		dragView = view;
		hoverCell = getAndAddHoverView(view);

		int w = view.asView().getWidth();
		int h = view.asView().getHeight();
		int top = startTop - locationOnScreen;
		int left = getCompleteLeft(view.asView(), 0);

		hoverCellCurrentBounds = new Rect(left, top, left + w, top + h);
		hoverCellOriginalBounds = new Rect(hoverCellCurrentBounds);
		hoverCell.setBounds(hoverCellCurrentBounds);
		view.asView().setVisibility(View.INVISIBLE);

		invalidate();
	}

	@Override
	public void startDrag(DraggableView view, int downY) {
		startDrag(view, downY, ViewUtils.getYCoordOnScreen(view.asView()));
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
			dragging = false;

			if (dragView.getParentNode() == null) {
				cleanUpAfterDragEnd();
			} else {
				// Animate removing the view.
				animateRestoreHoverCell();
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

		hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left, ViewUtils.getYCoordOnScreen(dragView.asView())
				- locationOnScreen);

		hoverViewAnimator = ObjectAnimator.ofObject(hoverCell, "bounds", boundEvaluator, hoverCellCurrentBounds);
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
		if (hoverViewAnimator != null && hoverViewAnimator.isRunning()) {
			hoverViewAnimator.cancel();
		}
		hoverCell = null;
		setEnabled(true);
		invalidate();
	}

	// TODO: Name this.
	private OnGlobalLayoutListener TODOListener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			locationOnScreen = ViewUtils.getYCoordOnScreen(ProgramDetailView.this);
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

	public void setExerciseLongClickListener(OnLongClickListener listener) {
		this.exerciseLongClickListener = listener;
	}

	public void setNodeLongClickListener(OnLongClickListener listener) {
		this.nodeLongClickListener = listener;
	}

	@Override
	public ExerciseView buildExerciseView(Exercise exercise, NodeView parent) {
		ExerciseView exerciseView = (ExerciseView) layoutInflater.inflate(R.layout.view_exercise, this, false);

		exerciseView.setExercise(exercise);
		exerciseView.setNodeView(parent);
		exerciseView.setDragManager(this);
		exerciseView.setOnLongClickListener(exerciseLongClickListener);

		return exerciseView;
	}

	@Override
	public NodeView buildNodeView(Node node) {
		NodeView nodeView = (NodeView) layoutInflater.inflate(R.layout.view_node, this, false);

		nodeView.setDragManager(this);
		nodeView.init(node);
		nodeView.setId(ViewUtils.generateViewId());
		nodeView.setOnLongClickListener(nodeLongClickListener);

		return nodeView;
	}

	private OnTouchListener addExerciseListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, final MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				dragging = true;

				InsertionPoint insertionPoint = findInsertionPoint(scrollingView.getTop(), null);
				final ExerciseView view = insertionPoint.parent.addExercise(new Exercise(), insertionPoint.index);

				post(new Runnable() {
					@Override
					public void run() {
						startDrag(view, (int) event.getRawY(), ViewUtils.getYCoordOnScreen(addExerciseButton));
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
				dragging = true;

				// FIXME: This is a copy of a lot of the stuff that happens in NodeView...
				Node node = new Node();
				node.setParent(node);
				final NodeView view = (NodeView) layoutInflater.inflate(R.layout.view_node,
						scrollingView.getNodeView(), false);
				view.init(node);
				view.setDragManager(ProgramDetailView.this);
				insertAt(view, findInsertionPoint(scrollingView.getTop(), view));
				view.setEditable(true);

				post(new Runnable() {
					@Override
					public void run() {
						startDrag(view, (int) event.getRawY(), ViewUtils.getYCoordOnScreen(addExerciseButton));
					}
				});
			}
			return false;
		}
	};
}
