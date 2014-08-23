package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.alexgilleran.hiitme.presentation.programdetail.DragManager;

final class MoveButtonListener implements OnTouchListener {
	private DraggableView draggableView;
	private DragManager dragManager;

	public MoveButtonListener(DraggableView draggableView, DragManager dragManager) {
		this.draggableView = draggableView;
		this.dragManager = dragManager;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			dragManager.startDrag(draggableView, (int) event.getRawY());
		}

		return false;
	}
}