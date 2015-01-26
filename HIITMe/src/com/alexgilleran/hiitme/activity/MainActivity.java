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

package com.alexgilleran.hiitme.activity;

import android.app.AlertDialog;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAOSqlite;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programdetail.ProgramDetailFragment;
import com.alexgilleran.hiitme.presentation.run.RunFragment;
import com.alexgilleran.hiitme.util.ViewUtils;


/**
 * The main activity - this is a single activity app so this holds all the logic that can't fit into
 * a fragment, particularly around the action bar.
 */
public class MainActivity extends ActionBarActivity implements ProgramListFragment.Callbacks, RunFragment.Callbacks {
	public static final String ARG_PROGRAM_ID = "PROGRAM_ID";
	public static final String ARG_PROGRAM_NAME = "PROGRAM_NAME";

	public static final String ACTION_CONTINUE_RUN = "CONTINUE_RUN";

	private static final String LIST_FRAGMENT_TAG = "LIST_FRAGMENT_TAG";
	private static final String RUN_FRAGMENT_TAG = "RUN_FRAGMENT_TAG";
	private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";

	private ProgramListFragment listFragment;
	private RunFragment runFragment;
	private ProgramDetailFragment detailFragment;

	private long currentProgramId;
	private String currentProgramName;
	private OnBackStackChangedListener backStackListener = new OnBackStackChangedListener() {
		@Override
		public void onBackStackChanged() {
			invalidateOptionsMenu();

			if (!isDetailFragmentVisible() && !isRunFragmentVisible()) {
				currentProgramId = 0;
				currentProgramName = null;
			}

			updateTitle();
		}
	};
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
	 */
	private boolean tabletLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		tabletLayout = findViewById(R.id.program_detail_container) != null;
		getFragmentManager().addOnBackStackChangedListener(backStackListener);

		if (savedInstanceState == null) {
			listFragment = new ProgramListFragment();
			if (tabletLayout) {
				((ProgramListFragment) getFragmentManager().findFragmentById(R.id.program_list))
						.setActivateOnItemClick(true);
			} else {
				getFragmentManager().beginTransaction()
						.replace(R.id.single_activity_container, listFragment, LIST_FRAGMENT_TAG).commit();
			}
		} else {
			currentProgramId = savedInstanceState.getLong(ARG_PROGRAM_ID, 0);

			listFragment = (ProgramListFragment) getFragmentManager().findFragmentByTag(LIST_FRAGMENT_TAG);
			detailFragment = (ProgramDetailFragment) getFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
			runFragment = (RunFragment) getFragmentManager().findFragmentByTag(RUN_FRAGMENT_TAG);
		}

		if (ACTION_CONTINUE_RUN.equals(getIntent().getAction())) {
			onProgramSelected(getIntent().getLongExtra(ARG_PROGRAM_ID, 0), getIntent().getStringExtra(ARG_PROGRAM_NAME));

			if (!tabletLayout) {
				run();
			}
		}

		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void updateTitle() {
		String title;

		if (isEditing()) {
			title = getString(R.string.title_edit);
		} else if (isRunFragmentVisible()) {
			title = getString(R.string.title_run);
		} else if (isDetailFragmentVisible()) {
			title = getString(R.string.title_view);
		} else {
			title = getString(R.string.title_select);
		}

		setTitle(title);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (currentProgramId > 0) {
			outState.putLong(ARG_PROGRAM_ID, currentProgramId);
			outState.putString(ARG_PROGRAM_NAME, currentProgramName);
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	/**
	 * Callback method from {@link ProgramListFragment.Callbacks} indicating that the item with the given ID was
	 * selected.
	 */
	@Override
	public void onProgramSelected(long id, String name) {
		this.currentProgramId = id;
		this.currentProgramName = name;

		FragmentTransaction tran = getFragmentManager().beginTransaction();

		if (detailFragment != null) {
			tran.remove(detailFragment);
		}
		if (runFragment != null) {
			tran.remove(runFragment);
		}

		Bundle arguments = buildProgramIdBundle();

		detailFragment = new ProgramDetailFragment();
		detailFragment.setArguments(arguments);

		if (tabletLayout) {
			tran.replace(R.id.program_detail_container, detailFragment, DETAIL_FRAGMENT_TAG);

			runFragment = new RunFragment();
			runFragment.setArguments(arguments);

			tran.setBreadCrumbShortTitle(currentProgramName);

			tran.replace(R.id.program_run_container, runFragment, RUN_FRAGMENT_TAG);
		} else {
			tran.replace(R.id.single_activity_container, detailFragment, DETAIL_FRAGMENT_TAG);
			tran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		}

		tran.addToBackStack("name").commit();
	}

	@Override
	public void onProgramRunStarted() {
		invalidateOptionsMenu();
	}

	@Override
	public void onProgramRunStopped() {
		invalidateOptionsMenu();
	}

	private Bundle buildProgramIdBundle() {
		Bundle arguments = new Bundle();
		arguments.putLong(ARG_PROGRAM_ID, currentProgramId);
		return arguments;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				this.onBackPressed();
				break;
			case R.id.actionbar_icon_about:
				showAbout();
				break;
			case R.id.actionbar_icon_new_program:
				openNewProgram();
				break;
			case R.id.actionbar_icon_run:
				run();
				break;
			case R.id.actionbar_icon_edit:
				startEditing();
				break;
			case R.id.actionbar_icon_delete_program:
				deleteCurrentProgram();
				break;
			case R.id.actionbar_icon_save:
				stopEditing(true);
				break;
			case R.id.actionbar_icon_discard_changes:
				stopEditing(false);
				break;
		}

		updateTitle();

		return true;
	}

	private void showAbout() {
		new AlertDialog.Builder(this).setMessage(R.string.about_message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setCancelable(false).show();
	}

	private void openNewProgram() {
		Program program = new Program("New Program");

		long id = ProgramDAOSqlite.getInstance(getApplicationContext()).saveProgram(program);
		onProgramSelected(id, null);

		listFragment.refresh();
	}

	private void run() {
		if (tabletLayout) {
			throw new IllegalStateException("Somehow the run button on the Action Bar got pressed in tablet mode wtf?");
		}

		FragmentTransaction tran = getFragmentManager().beginTransaction();
		runFragment = new RunFragment();
		runFragment.setArguments(buildProgramIdBundle());
		tran.replace(R.id.single_activity_container, runFragment, RUN_FRAGMENT_TAG);
		tran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();
	}

	private void deleteCurrentProgram() {
		if (!isViewingProgram() && currentProgramId > 0) {
			throw new IllegalStateException(
					"Attempted to delete program when there was no current program being viewed");
		}

		new AlertDialog.Builder(this) //
				.setMessage("Are you sure you want to delete this program?")//
				.setPositiveButton(android.R.string.yes, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getFragmentManager().popBackStack();
						ProgramDAOSqlite.getInstance(getApplicationContext()).deleteProgram(currentProgramId);
						listFragment.refresh();
						dialog.dismiss();
					}
				})//
				.setNegativeButton(android.R.string.no, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	private void startEditing() {
		detailFragment.startEditing();

		if (runFragment != null) {
			runFragment.preventRun();
		}

		invalidateOptionsMenu();
	}

	private void stopEditing(boolean save) {
		detailFragment.stopEditing(save);

		if (runFragment != null) {
			runFragment.allowRun();
		}

		invalidateOptionsMenu();
	}

	@Override
	public void onBackPressed() {
		if (isRunning()) {
			runFragment.stop();
		} else if (isEditing()) {
			stopEditing(true);
		} else if (getFragmentManager().getBackStackEntryCount() > 0 && !ViewUtils.isLarge(getResources())) {
			getFragmentManager().popBackStack();
		} else {
			navigateUpTo(new Intent(this, MainActivity.class));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_bar_menu, menu);

		menu.findItem(R.id.actionbar_icon_about).setVisible(shouldShowListButtons());
		menu.findItem(R.id.actionbar_icon_new_program).setVisible(shouldShowListButtons());

		menu.findItem(R.id.actionbar_icon_delete_program).setVisible(isViewingProgram());
		menu.findItem(R.id.actionbar_icon_edit).setVisible(isViewingProgram());
		menu.findItem(R.id.actionbar_icon_run).setVisible(shouldShowRunButton());

		menu.findItem(R.id.actionbar_icon_save).setVisible(isEditing());
		menu.findItem(R.id.actionbar_icon_discard_changes).setVisible(isEditing());

		return true;
	}

	private boolean shouldShowListButtons() {
		return listFragment != null && listFragment.isVisible();
	}

	private boolean isViewingProgram() {
		return !isEditing() && !isRunning() && isDetailFragmentVisible();
	}

	private boolean shouldShowRunButton() {
		return !tabletLayout && isViewingProgram();
	}

	private boolean isDetailFragmentVisible() {
		return detailFragment != null && detailFragment.isVisible();
	}

	private boolean isRunFragmentVisible() {
		return runFragment != null && runFragment.isVisible();
	}

	private boolean isRunning() {
		return runFragment != null && runFragment.isRunning();
	}

	private boolean isEditing() {
		return isDetailFragmentVisible() && detailFragment.isBeingEdited();
	}
}
