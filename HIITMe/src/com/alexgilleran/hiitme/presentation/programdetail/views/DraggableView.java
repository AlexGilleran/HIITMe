package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.view.View;

import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;

public interface DraggableView {

	void setDragManager(DragManager dragManager);

	Node rebuildNode();

	View asView();

	NodeView getParentNode();

	void setEditable(boolean editable);

	void setBeingDragged(boolean beingDragged);

	int getTopForDrag();

	int getBottomForDrag();

	void render();

	public abstract boolean isEditable();

	boolean isNewlyCreated();

	void setNewlyCreated(boolean newlyCreated);

	void edit();
}