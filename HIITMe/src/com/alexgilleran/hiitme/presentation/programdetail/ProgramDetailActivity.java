package com.alexgilleran.hiitme.presentation.programdetail;

import roboguice.activity.RoboFragmentActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDao;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programlist.ProgramListActivity;
import com.alexgilleran.hiitme.programrunner.ProgramRunService;
import com.google.inject.Inject;

/**
 * An activity representing a single Program detail screen. This activity is
 * only used on handset devices. On tablet-size devices, item details are
 * presented side-by-side with a list of items in a {@link ProgramListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ProgramDetailFragment}.
 */
public class ProgramDetailActivity extends RoboFragmentActivity {
	@Inject
	private ProgramDao programDao;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_program_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Bind to LocalService
		Intent intent = new Intent(this, ProgramRunService.class);
		intent.putExtra(Program.PROGRAM_ID_NAME,
				this.getIntent().getLongExtra(Program.PROGRAM_ID_NAME, -1));
		this.getApplicationContext().startService(intent);

		if (savedInstanceState == null) {
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();

			ProgramDetailFragment detailFragment = new ProgramDetailFragment();
			transaction.add(R.id.program_detail_container, detailFragment);

			ProgramRunFragment runFragment = new ProgramRunFragment();
			transaction.add(R.id.program_run_container, runFragment);

			transaction.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpTo(this, new Intent(this,
					ProgramListActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
