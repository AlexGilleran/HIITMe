package com.alexgilleran.hiitme.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.presentation.programdetail.ProgramDetailFragment;
import com.alexgilleran.hiitme.presentation.run.RunFragment;

/**
 * An activity representing a list of Programs. This activity has different presentations for handset and tablet-size
 * devices. On handsets, the activity presents a list of items, which when touched, lead to a
 * {@link ProgramDetailActivity} representing item details. On tablets, the activity presents the list of items and item
 * details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link ProgramListFragment} and the item details
 * (if present) is a {@link ProgramDetailFragment}.
 * <p>
 * This activity also implements the required {@link ProgramListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class MainActivity extends Activity implements ProgramListFragment.Callbacks {
	public static final String ARG_PROGRAM_ID = "PROGRAM_ID";

	private RunFragment runFragment;
	private ProgramDetailFragment detailFragment;
	private long currentProgramId;

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
	 */
	private boolean tabletLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (findViewById(R.id.program_detail_container) != null) {
			tabletLayout = true;

			// In two-pane mode, list items should be given the 'activated' state when touched.
			((ProgramListFragment) getFragmentManager().findFragmentById(R.id.program_list))
					.setActivateOnItemClick(true);
		} else {
			getFragmentManager().beginTransaction().replace(R.id.single_activity_container, new ProgramListFragment())
					.commit();
		}

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onStop() {
		super.onStop();

		detailFragment.save();
	}

	/**
	 * Callback method from {@link ProgramListFragment.Callbacks} indicating that the item with the given ID was
	 * selected.
	 */
	@Override
	public void onProgramSelected(long id) {
		this.currentProgramId = id;

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
			tran.replace(R.id.program_detail_container, detailFragment);

			runFragment = new RunFragment();
			runFragment.setArguments(arguments);

			tran.replace(R.id.program_run_container, runFragment);
		} else {
			tran.replace(R.id.single_activity_container, detailFragment);
			tran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		}

		tran.addToBackStack(null).commit();
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
			navigateUpTo(new Intent(this, MainActivity.class));
			return true;
		case R.id.actionbar_icon_run:
			if (tabletLayout) {
				throw new IllegalStateException(
						"Somehow the run button on the Action Bar got pressed in tablet mode wtf?");
			}
			detailFragment.save();

			FragmentTransaction tran = getFragmentManager().beginTransaction();
			if (runFragment != null) {
				tran.remove(runFragment);
			}
			runFragment = new RunFragment();
			runFragment.setArguments(buildProgramIdBundle());
			tran.replace(R.id.single_activity_container, runFragment);
			tran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();

			return true;
		case R.id.actionbar_icon_save:
			stopEditing();
			return true;
		case R.id.actionbar_icon_edit:
			detailFragment.startEditing();
			invalidateOptionsMenu();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (isEditing()) {
			stopEditing();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.run_menu, menu);

		menu.findItem(R.id.actionbar_icon_save).setVisible(isEditing());
		menu.findItem(R.id.actionbar_icon_edit).setVisible(!isEditing());
		menu.findItem(R.id.actionbar_icon_run).setVisible(!tabletLayout);

		return true;
	}

	private boolean isEditing() {
		return detailFragment != null && detailFragment.isBeingEdited();
	}

	private void stopEditing() {
		detailFragment.stopEditing();
		invalidateOptionsMenu();
	}
}
