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

import android.view.View;

import com.alexgilleran.hiitme.presentation.programdetail.DragManager;
import com.alexgilleran.hiitme.presentation.programdetail.views.DraggableView;

public class DraggableViewFocusListener implements View.OnFocusChangeListener {
	private final DragManager dragManager;
	private final DraggableView view;

	public DraggableViewFocusListener(DraggableView view, DragManager dragManager) {
		this.dragManager = dragManager;
		this.view = view;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		dragManager.notifyFocused(hasFocus, view);
	}
}
