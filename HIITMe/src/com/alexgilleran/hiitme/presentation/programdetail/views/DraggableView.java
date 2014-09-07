package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.view.View;

import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;

public interface DraggableView {

	void setDragManager(DragManager dragManager);

	Node getNode();

	View asView();

	NodeView getParentNode();

	void setEditable(boolean editable);

	void setBeingDragged(boolean beingDragged);

	int getTopForDrag();

	int getBottomForDrag();
}