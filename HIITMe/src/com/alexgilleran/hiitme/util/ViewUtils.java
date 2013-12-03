package com.alexgilleran.hiitme.util;

import java.util.concurrent.atomic.AtomicInteger;

public class ViewUtils {
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

	/**
	 * Generate a value suitable for use in {@link #setId(int)}. This value will
	 * not collide with ID values generated at build time by aapt for R.id.
	 * <p>
	 * This is copied from <a href="
	 * http://developer.android.com/reference/android/view/View
	 * .html#generateViewId%28%29">the android code</a>, which is only present
	 * in API > 17.
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
}
