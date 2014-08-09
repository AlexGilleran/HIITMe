package com.alexgilleran.hiitme.presentation.programdetail;

import com.alexgilleran.hiitme.presentation.programdetail.views.DraggableView;

public interface DragManager {

	void cancelDrag();

	void startDrag(DraggableView view, int downY);
	
	boolean currentlyDragging();
	
	void handleHoverCellMove();
}
