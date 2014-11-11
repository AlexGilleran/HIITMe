package com.alexgilleran.hiitme.model;

import android.content.Context;

import com.alexgilleran.hiitme.R;
import com.google.inject.Inject;

/** A simple representation of the effort level of the set */
public enum EffortLevel {
	HARD(R.string.effort_hard, R.drawable.ic_run_small, R.drawable.effort_icon_bg_hard, R.color.effort_level_hard), //
	EASY(R.string.effort_easy, R.drawable.ic_walk_small, R.drawable.effort_icon_bg_easy, R.color.effort_level_easy), //
	REST(R.string.effort_rest, R.drawable.ic_bench_small, R.drawable.effort_icon_bg_rest, R.color.effort_level_rest);//

	@Inject
	private static Context context;

	private int stringId;
	private int iconId;
	private int backgroundId;
	private int colorId;

	private EffortLevel(int stringId, int iconId, int backgroundId, int colorId) {
		this.stringId = stringId;
		this.iconId = iconId;
		this.backgroundId = backgroundId;
		this.colorId = colorId;
	}

	public int getIconId() {
		return iconId;
	}

	public int getBackgroundId() {
		return backgroundId;
	}

	public int getColorId() {
		return colorId;
	}

	@Override
	public String toString() {
		return context.getString(stringId);
	}
}