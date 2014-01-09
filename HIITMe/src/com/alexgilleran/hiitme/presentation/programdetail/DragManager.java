package com.alexgilleran.hiitme.presentation.programdetail;

import android.view.MotionEvent;

import com.alexgilleran.hiitme.presentation.programdetail.views.DraggableView;

public interface DragManager {

	void startDrag(DraggableView draggedView, MotionEvent event);

}
