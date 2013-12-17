package com.alexgilleran.hiitme.presentation.programdetail.views;

import com.alexgilleran.hiitme.presentation.programdetail.DragManager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public abstract class DraggableView extends LinearLayout {
	protected LayoutInflater layoutInflater;
	protected DragManager dragManager;
	protected ProgramNodeView parent;

	public DraggableView(Context context) {
		super(context);
		layoutInflater = LayoutInflater.from(context);
	}

	public DraggableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		layoutInflater = LayoutInflater.from(context);
	}

	public void initialise(DragManager dragManager, ProgramNodeView parent) {
		this.dragManager = dragManager;
		this.parent = parent;
	}

	public abstract DraggableView findNextInTree();
}