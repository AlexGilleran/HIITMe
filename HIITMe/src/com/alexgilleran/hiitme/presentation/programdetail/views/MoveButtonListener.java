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