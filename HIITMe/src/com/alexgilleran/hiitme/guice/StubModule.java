package com.alexgilleran.hiitme.guice;

import com.alexgilleran.hiitme.data.ProgramDao;
import com.alexgilleran.hiitme.data.ProgramDaoActiveAndroid;
import com.google.inject.Binder;
import com.google.inject.Module;

public class StubModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(ProgramDao.class).to(ProgramDaoActiveAndroid.class);

	}
}
