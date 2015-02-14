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

package com.github.amlcurran.showcaseview.targets;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.app.ToolbarActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewParent;

import com.github.amlcurran.showcaseview.R;

/**
 * Reflector which finds action items in the standard API 11 ActionBar implementation
 */
class HIITMeActionBarReflector implements Reflector {

    private Activity mActivity;

    public HIITMeActionBarReflector(Activity activity) {
        mActivity = activity;
    }

    @Override
    public ViewParent getActionBarView() {
        return getHomeButton().getParent();
    }

    @Override
    public View getHomeButton() {
	    return ((Toolbar) mActivity.findViewById(R.id.action_bar)).getChildAt(0);

//        View homeButton = mActivity.findViewById(android.R.id.home);
//        if (homeButton == null) {
//            throw new RuntimeException(
//                    "insertShowcaseViewWithType cannot be used when the theme " +
//                            "has no ActionBar");
//        }
//        return homeButton;
    }

    @Override
    public void showcaseActionItem(int itemId) {

    }
}
