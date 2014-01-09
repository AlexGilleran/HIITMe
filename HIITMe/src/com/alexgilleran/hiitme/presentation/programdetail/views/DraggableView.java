package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.view.View;

import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.presentation.programdetail.DragManager;

public interface DraggableView {
	void setDragManager(DragManager dragManager);

	ProgramNode getProgramNode();

	View asView();

	ProgramNodeView getParentProgramNodeView();

}