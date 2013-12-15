package com.alexgilleran.hiitme.presentation.programdetail;

import android.graphics.drawable.BitmapDrawable;

import com.alexgilleran.hiitme.presentation.programdetail.views.DraggableView;

public interface DragManager {

	void startDrag(DraggableView draggedView);

	BitmapDrawable getHoverCell();
}
