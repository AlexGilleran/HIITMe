package com.alexgilleran.hiitme.model;

import android.content.Context;

import com.alexgilleran.hiitme.R;

/**
 * A simple representation of the effort level of the set
 */
public enum EffortLevel {
	HARD(R.string.effort_hard, R.drawable.ic_hard_white, R.drawable.ic_hard_colour, R.drawable.effort_icon_bg_hard, R.color.effort_level_hard), //
	EASY(R.string.effort_easy, R.drawable.ic_easy_white, R.drawable.ic_easy_colour, R.drawable.effort_icon_bg_easy, R.color.effort_level_easy), //
	REST(R.string.effort_rest, R.drawable.ic_rest_white, R.drawable.ic_rest_colour, R.drawable.effort_icon_bg_rest, R.color.effort_level_rest),//
	NONE(R.string.effort_none, -1, -1, -1, R.color.accent);//

	private int stringId;
	private int lightIconId;
	private int colourIconId;
	private int backgroundId;
	private int colourId;

	private EffortLevel(int stringId, int lightIconId, int colourIconId, int backgroundId, int colourId) {
		this.stringId = stringId;
		this.lightIconId = lightIconId;
		this.colourIconId = colourIconId;
		this.backgroundId = backgroundId;
		this.colourId = colourId;
	}

	public int getLightIconId() {
		return lightIconId;
	}

	public int getColourIconId() {
		return colourIconId;
	}

	public int getBackgroundId() {
		return backgroundId;
	}

	public int getColorId(Context context) {
		return context.getResources().getColor(colourId);
	}

	public String getString(Context context) {
		return context.getString(stringId);
	}

	public boolean isBlank() {
		return colourIconId == -1;
	}
}