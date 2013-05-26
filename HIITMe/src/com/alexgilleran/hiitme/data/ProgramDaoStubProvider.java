package com.alexgilleran.hiitme.data;

public class ProgramDaoStubProvider implements ProgramDaoProvider {

	@Override
	public ProgramDao get() {
		return new ProgramDaoStub();
	}

	@Override
	public void release() {
		// no-op.
	}

}
