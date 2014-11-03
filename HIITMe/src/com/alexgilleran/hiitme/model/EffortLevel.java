package com.alexgilleran.hiitme.model;

import android.content.Context;

import com.alexgilleran.hiitme.R;
import com.google.inject.Inject;

/** A simple representation of the effort level of the set */
public enum EffortLevel {
	HARD(R.string.effort_hard, R.drawable.ic_run), EASY(R.string.effort_easy, R.drawable.ic_walking), REST(
			R.string.effort_rest, R.drawable.ic_bench);

	@Inject
	private static Context context;

	private int stringId;
	private int iconId;

	private EffortLevel(int stringId, int iconId) {
		this.stringId = stringId;
		this.iconId = iconId;
	}

	public int getIconId() {
		return iconId;
	}

	@Override
	public String toString() {
		return context.getString(stringId);
	}
}