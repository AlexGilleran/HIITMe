package com.alexgilleran.hiitme.data;

import com.google.inject.Provider;

public interface ProgramDaoProvider extends Provider<ProgramDao> {
	void release();
}
