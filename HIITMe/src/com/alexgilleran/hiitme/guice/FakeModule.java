package com.alexgilleran.hiitme.guice;

import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.data.fake.ProgramDAOFake;
import com.google.inject.Binder;
import com.google.inject.Module;

public class FakeModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(ProgramDAO.class).to(ProgramDAOFake.class);
	}

}
