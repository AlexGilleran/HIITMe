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

package com.alexgilleran.hiitme.presentation.programdetail;

import android.app.FragmentManager;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.views.DraggableView;
import com.alexgilleran.hiitme.presentation.programdetail.views.ExerciseView;
import com.alexgilleran.hiitme.presentation.programdetail.views.NodeView;

public interface DragManager {

	void cancelDrag();

	void startDrag(DraggableView view, int downY);
	
	boolean currentlyDragging();
	
	void handleHoverCellMove();
	
	ExerciseView buildExerciseView(Exercise exercise, DraggableView parent);
	
	NodeView buildNodeView(Node node);

	FragmentManager getFragmentManager();
}
