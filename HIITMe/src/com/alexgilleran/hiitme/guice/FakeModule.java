package com.alexgilleran.hiitme.guice;

import com.alexgilleran.hiitme.data.ProgramDao;
import com.alexgilleran.hiitme.data.ProgramDaoProvider;
import com.alexgilleran.hiitme.data.ProgramDaoStubProvider;
import com.google.inject.Binder;
import com.google.inject.Module;

public class FakeModule implements Module {

	@Override
	public void configure(Binder binder) {
		// binder.bind(ProgramDaoProvider.class).to(
		// ProgramDaoOrmLiteProvider.class);
		binder.bind(ProgramDao.class).toProvider(ProgramDaoStubProvider.class);
		binder.bind(ProgramDaoProvider.class).to(ProgramDaoStubProvider.class);
	}
}
