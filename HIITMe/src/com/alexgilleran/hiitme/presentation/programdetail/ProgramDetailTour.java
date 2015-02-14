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

package com.alexgilleran.hiitme.presentation.programdetail;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.activity.MainActivity;
import com.alexgilleran.hiitme.presentation.programdetail.views.ExerciseView;
import com.alexgilleran.hiitme.presentation.programdetail.views.NodeView;
import com.alexgilleran.hiitme.util.ViewUtils;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Does a tour
 */
public class ProgramDetailTour {
	private final MainActivity activity;
	private final NodeView exampleNodeView;
	private final ExerciseView exampleExerciseView;

	private Handler handler = new Handler();
	private RelativeLayout.LayoutParams params;

	public ProgramDetailTour(MainActivity activity, int editButtonId, NodeView exampleNodeView, ExerciseView exampleExerciseView) {
		this.activity = activity;
		this.exampleNodeView = exampleNodeView;
		this.exampleExerciseView = exampleExerciseView;

		params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.height = 0;
		params.width = 0;
		params.setMargins(0, 0, 0, ViewUtils.getSoftButtonsHeight(activity) + ViewUtils.getPxForDp(activity, 50));

	}

	public void init() {
		waitForViewToExist(R.id.button_add_exercise, new ViewExistenceListener() {
			@Override
			public void onViewExists(View view) {
				tourAddExerciseButton((ImageButton) view);
			}
		}, 299);
	}

	private MotionEvent buildTouchEvent(int type) {
		MotionEvent event = MotionEvent.obtain(50, 50, type, 0, 0, 0);
		return event;
	}

	private void tourAddExerciseButton(final ImageButton view) {
		showShowCase("Touch and drag the add exercise button to add an exercise", view, new OnNextListener() {
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				tourAddGroupButton();
			}
		});
	}

	private void tourAddGroupButton() {
		showShowCase("Touch and drag the add group button to add a group", activity.findViewById(R.id.button_add_node), new OnNextListener() {
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				tourNodeView();
			}
		});
	}

	private void tourNodeView() {
		exampleNodeView.getHeader().requestFocus();
		showShowCase("Tap the top of a group to select it, or hold down then drag to move it", exampleNodeView.getHeader(), new OnNextListener() {
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				waitForViewToExist(R.id.button_edit, new ViewExistenceListener() {
					@Override
					public void onViewExists(View view) {
						tourEditButton(view);
					}
				}, 100);
			}
		});
	}

	private void tourEditButton(final View editButton) {
		showShowCase("With a group or exercise selected, you can edit by tapping this button", editButton, new OnNextListener() {
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				if (exampleExerciseView != null) {
					tourExerciseView();
				}
			}
		});
	}

	private void tourExerciseView() {
		showShowCase("You can also tap exercises to select them, or hold down and drag to move them", exampleExerciseView, OnShowcaseEventListener.NONE);
	}

	private void showShowCase(String text, View target, OnShowcaseEventListener listener) {
		final ShowcaseView showCase = new ShowcaseView.Builder(activity)
				.setTarget(new ViewTarget(target))
				.setStyle(R.style.TourOverlay)
				.setContentText(text)
				.hideOnTouchOutside()
				.setShowcaseEventListener(listener)
				.build();
		showCase.setButtonPosition(params);
	}

	public void waitForViewToExist(final int id, final ViewExistenceListener listener, int delay) {
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				final View view = activity.getWindow().findViewById(id);

				if (view != null) {
					timer.cancel();
					handler.post(new Runnable() {
						@Override
						public void run() {
							listener.onViewExists(view);
						}
					});
				}
			}
		}, delay, 100);
	}

	private interface ViewExistenceListener {
		void onViewExists(View view);
	}

	public abstract class OnNextListener implements OnShowcaseEventListener {
//		@Override
//		public void onShowcaseViewHide(ShowcaseView showcaseView) {
//
//		}

		@Override
		public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

		}

		@Override
		public void onShowcaseViewShow(ShowcaseView showcaseView) {

		}
	}
}
