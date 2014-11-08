package com.alexgilleran.hiitme.presentation.programdetail;

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
}
