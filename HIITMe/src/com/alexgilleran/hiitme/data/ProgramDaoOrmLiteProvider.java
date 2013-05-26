package com.alexgilleran.hiitme.data;

import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.android.apptools.OpenHelperManager;

@Singleton
public class ProgramDaoOrmLiteProvider implements ProgramDaoProvider {
	@Inject
	private Context context;

	@Override
	public ProgramDao get() {
		HIITMeSqliteOpenHelper helper = OpenHelperManager.getHelper(context,
				HIITMeSqliteOpenHelper.class);
		return new ProgramDaoOrmLite(helper);
	}

	@Override
	public void release() {
		OpenHelperManager.releaseHelper();
	}
}
