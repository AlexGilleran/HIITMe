package com.alexgilleran.hiitme.presentation.run;

import javax.inject.Inject;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectFragment;
import android.os.Bundle;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Program;

/**
 * Activity for running the activity on phones - mainly delegates to
 * {@link RunFragment}.
 * 
 * @author alexgilleran
 * 
 */
public class RunActivity extends RoboFragmentActivity {
	@InjectFragment(R.id.run_fragment_run)
	private RunFragment runFragment;

	@Inject
	private ProgramDAO programDAO;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_run);

		Program program = programDAO.getProgram(getIntent().getLongExtra(
				Program.PROGRAM_ID_NAME, -1));

		runFragment.setProgram(program);
	}
}
