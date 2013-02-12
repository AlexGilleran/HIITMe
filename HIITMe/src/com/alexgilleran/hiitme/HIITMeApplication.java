package com.alexgilleran.hiitme;

import roboguice.RoboGuice;
import android.app.Application;

import com.alexgilleran.hiitme.guice.FakeModule;

public class HIITMeApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE,
				RoboGuice.newDefaultRoboModule(this), new FakeModule());
	}
}
