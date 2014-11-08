package com.alexgilleran.hiitme.util;

import java.util.concurrent.atomic.AtomicInteger;

import roboguice.util.Strings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewUtils {
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

	/**
	 * Generate a value suitable for use in {@link #setId(int)}. This value will not collide with ID values generated at
	 * build time by aapt for R.id.
	 * <p>
	 * This is copied from <a href=" http://developer.android.com/reference/android/view/View
	 * .html#generateViewId%28%29">the android code</a>, which is only present in API > 17.
	 * 
	 * @return a generated ID value
	 */
	public static int generateViewId() {
		for (;;) {
			final int result = sNextGeneratedId.get();
			// aapt-generated IDs have the high byte nonzero; clamp to the range
			// under that.
			int newValue = result + 1;
			if (newValue > 0x00FFFFFF)
				newValue = 1; // Roll over to 1, not 0.
			if (sNextGeneratedId.compareAndSet(result, newValue)) {
				return result;
			}
		}
	}

	public static int getVisibilityInt(boolean visible) {
		return visible ? View.VISIBLE : View.GONE;
	}

	public static int getTopIncludingMargin(View view) {
		return view.getTop() - getTopMargin(view);
	}

	public static int getTopMargin(View view) {
		if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
			return ((LinearLayout.LayoutParams) view.getLayoutParams()).topMargin;
		} else if (view.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
			return ((RelativeLayout.LayoutParams) view.getLayoutParams()).topMargin;
		} else if (view.getLayoutParams() instanceof FrameLayout.LayoutParams) {
			return ((FrameLayout.LayoutParams) view.getLayoutParams()).topMargin;
		}
		return 0;
	}

	public static int getBottomIncludingMargin(View view) {
		return view.getBottom() + getBottomMargin(view);
	}

	public static int getBottomMargin(View view) {
		if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
			return ((LinearLayout.LayoutParams) view.getLayoutParams()).bottomMargin;
		} else if (view.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
			return ((RelativeLayout.LayoutParams) view.getLayoutParams()).bottomMargin;
		} else if (view.getLayoutParams() instanceof FrameLayout.LayoutParams) {
			return ((FrameLayout.LayoutParams) view.getLayoutParams()).bottomMargin;
		}
		return 0;
	}

	public static int getYCoordOnScreen(View view) {
		int[] ints = new int[2];
		view.getLocationOnScreen(ints);
		return ints[1];
	}

	public static int getIntFromTextViewSafe(TextView view) {
		if (view.getText() == null || Strings.isEmpty(view.getText().toString())) {
			return 0;
		}

		return Integer.parseInt(view.getText().toString());
	}
}