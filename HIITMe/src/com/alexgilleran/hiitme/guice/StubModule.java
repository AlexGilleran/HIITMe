package com.alexgilleran.hiitme.guice;

import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.data.ProgramDAOSqlite;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.sound.SoundPlayer;
import com.alexgilleran.hiitme.sound.SoundPlayerImpl;
import com.google.inject.Binder;
import com.google.inject.Module;

public class StubModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(ProgramDAO.class).to(ProgramDAOSqlite.class);
		binder.bind(SoundPlayer.class).to(SoundPlayerImpl.class);

		binder.requestStaticInjection(EffortLevel.class);
	}
}
