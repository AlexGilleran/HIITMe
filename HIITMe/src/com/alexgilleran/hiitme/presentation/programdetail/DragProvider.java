package com.alexgilleran.hiitme.presentation.programdetail;

import com.alexgilleran.hiitme.presentation.programdetail.views.ProgramNodeView;

import android.view.View;

/**
 * Facilitates dragging a view across various {@link ProgramNodeView}s that
 * don't know about each other.
 * 
 * @author Alex Gilleran
 */
public interface DragProvider {
	View getDragPlaceholder();
}
