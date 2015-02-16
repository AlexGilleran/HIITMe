/*
 * HIIT Me, a High Intensity Interval Training app for Android
 * Copyright (C) 2015 Alex Gilleran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alexgilleran.hiitme.presentation.list;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;

import com.alexgilleran.hiitme.R;

/**
 * Created by Alex on 2015-02-16.
 */
public class ListItem extends FrameLayout implements Checkable {
	public ListItem(Context context) {
		super(context);
	}

	public ListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListItem(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void setChecked(boolean checked) {
		this.setActivated(checked);

		this.setBackgroundResource(checked ? R.color.accent : android.R.color.transparent);
	}

	@Override
	public boolean isChecked() {
		return isActivated();
	}

	@Override
	public void toggle() {
		setChecked(!isChecked());
	}
}
