package com.alexgilleran.hiitme.presentation.run;

import android.app.Activity;
import android.os.Bundle;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Program;

/**
 * Activity for running the activity on phones - mainly delegates to {@link RunFragment}.
 * 
 * @author alexgilleran
 * 
 */
public class RunActivity extends Activity {
	private RunFragment runFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_run);

		runFragment = (RunFragment) getFragmentManager().findFragmentById(R.id.run_fragment_run);
		
		if (savedInstanceState == null) {
			runFragment.setProgramId(getIntent().getLongExtra(Program.PROGRAM_ID_NAME, -1));
		}
	}
}
