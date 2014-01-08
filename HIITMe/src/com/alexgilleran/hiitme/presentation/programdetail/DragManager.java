package com.alexgilleran.hiitme.presentation.programdetail;

import android.view.MotionEvent;
import android.view.View;

public interface DragManager {

	void startDrag(View draggedView, MotionEvent event);

}
