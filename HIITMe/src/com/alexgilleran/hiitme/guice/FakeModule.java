package com.alexgilleran.hiitme.guice;

import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.data.stub.ProgramDAOStub;
import com.google.inject.Binder;
import com.google.inject.Module;

public class FakeModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(ProgramDAO.class).to(ProgramDAOStub.class);
	}

}
