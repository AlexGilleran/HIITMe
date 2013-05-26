package com.alexgilleran.hiitme.guice;

import com.alexgilleran.hiitme.data.ProgramDao;
import com.alexgilleran.hiitme.data.ProgramDAOStub;
import com.google.inject.Binder;
import com.google.inject.Module;

public class FakeModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(ProgramDao.class).to(ProgramDAOStub.class);
	}

}
