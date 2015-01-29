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

package com.alexgilleran.hiitme.util;

import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.presentation.programdetail.views.DraggableView;

public class DraggableViewTouchListener implements View.OnTouchListener {
	private final Handler longPressHandler = new Handler();
	private final float touchSlop;
	private final DraggableView view;
	private final DragManager dragManager;

	private float lastRawY = 0;

	public DraggableViewTouchListener(DraggableView view, DragManager dragManager) {
		this.view = view;
		this.dragManager = dragManager;

		touchSlop = ViewConfiguration.get(view.asView().getContext()).getScaledTouchSlop();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		lastRawY = event.getRawY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (viewHasParent()) {
					longPressHandler.postDelayed(dragRunnable, ViewConfiguration.getLongPressTimeout());
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				longPressHandler.removeCallbacks(dragRunnable);
				break;
			case MotionEvent.ACTION_MOVE:
				if (viewHasParent() && !ViewUtils.isPointInView(v, event.getX(), event.getY(), touchSlop)) {
					longPressHandler.removeCallbacks(dragRunnable);
					return true;
				}
		}

		return false;
	}

	private boolean viewHasParent() {
		return view.getParentNode() != null;
	}

	private Runnable dragRunnable = new Runnable() {
		@Override
		public void run() {
			dragManager.startDrag(view, (int) lastRawY);
		}
	};
}
