package com.alexgilleran.hiitme.model;

import android.content.Context;

import com.alexgilleran.hiitme.R;
import com.google.inject.Inject;

/** A simple representation of the effort level of the set */
public enum EffortLevel {
	HARD(R.string.effort_hard), EASY(R.string.effort_easy), REST(
			R.string.effort_rest);
	@Inject
	private static Context context;

	private int stringId;

	private EffortLevel(int stringId) {
		this.stringId = stringId;
	}

	@Override
	public String toString() {
		return context.getString(stringId);
	}
}