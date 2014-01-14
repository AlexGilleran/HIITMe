package com.alexgilleran.hiitme.presentation.programdetail;

import com.alexgilleran.hiitme.presentation.programdetail.views.NodeView;

import android.view.View;

/**
 * Facilitates dragging a view across various {@link NodeView}s that
 * don't know about each other.
 * 
 * @author Alex Gilleran
 */
public interface DragProvider {
	View getDragPlaceholder();
}
