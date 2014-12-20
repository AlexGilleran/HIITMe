package com.alexgilleran.hiitme.util;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import static android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT;
import static android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM;
import static android.graphics.drawable.GradientDrawable.RADIAL_GRADIENT;

public class Shadow {
	private static final int START_COLOR = Color.parseColor("#55000000");
	private static final int END_COLOR = Color.parseColor("#00000000");
	private static final int SHADOW_LENGTH = (int) (3 * Resources.getSystem().getDisplayMetrics().density);

	private static GradientDrawable linearGradient;
	private static GradientDrawable radialGradient;
	private static int[] colors;

	static {
		colors = new int[]{START_COLOR, END_COLOR};
		linearGradient = new GradientDrawable(TOP_BOTTOM, colors);
		radialGradient = new GradientDrawable();
		radialGradient.setGradientType(RADIAL_GRADIENT);
		radialGradient.setColors(colors);
		radialGradient.setGradientRadius(SHADOW_LENGTH);
	}

	public static void onDraw(View view, Canvas canvas) {
		int height = view.getHeight();
		int width = view.getWidth();

		int left = view.getLeft();
		int top = view.getTop();

//		Rect bottomBounds = new Rect(0, height - SHADOW_LENGTH, width, height);
//		linearGradient.setBounds(bottomBounds);
//		linearGradient.setOrientation(TOP_BOTTOM);
//		linearGradient.draw(canvas);

		Rect rightBounds = new Rect(width, SHADOW_LENGTH, width - SHADOW_LENGTH, height);
		linearGradient.setBounds(rightBounds);
		linearGradient.setOrientation(LEFT_RIGHT);
		linearGradient.draw(canvas);

		Rect cornerBLBounds = new Rect(SHADOW_LENGTH, height - SHADOW_LENGTH * 2, SHADOW_LENGTH * 2, height - SHADOW_LENGTH);
		radialGradient.setBounds(cornerBLBounds);
		radialGradient.setGradientCenter(1, 0);
		radialGradient.draw(canvas);

		Rect cornerBRBounds = new Rect(width, height, width + SHADOW_LENGTH, height + SHADOW_LENGTH);
		radialGradient.setBounds(cornerBRBounds);
		radialGradient.setGradientCenter(0, 0);
		radialGradient.draw(canvas);

		Rect cornerTRBounds = new Rect(width, 0, width + SHADOW_LENGTH, SHADOW_LENGTH);
		radialGradient.setBounds(cornerTRBounds);
		radialGradient.setGradientCenter(0, 1);
		radialGradient.draw(canvas);
	}
}