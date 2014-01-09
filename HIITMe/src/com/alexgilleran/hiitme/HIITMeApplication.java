package com.alexgilleran.hiitme;

import roboguice.RoboGuice;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.app.Application;
import com.activeandroid.query.Delete;
import com.alexgilleran.hiitme.guice.StubModule;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;

public class HIITMeApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE,
				RoboGuice.newDefaultRoboModule(this), new StubModule());

		ActiveAndroid.beginTransaction();
		try {
			new Delete().from(Program.class).execute();
			new Delete().from(ProgramNode.class).execute();
			new Delete().from(Exercise.class).execute();

			Program tabata = new Program("Tabata", "The tabata protocol", 8);
			tabata.getAssociatedNode().addChildExercise("Hard", 2000, EffortLevel.HARD, 1);
			tabata.getAssociatedNode().addChildExercise("Rest", 1000, EffortLevel.REST, 1);
			tabata.save();

			Program nestTest = new Program("NestTest", "A nested test program", 3);
			ProgramNode nestNode1 = nestTest.getAssociatedNode().addChildNode(2);
			ProgramNode nestNode11 = nestNode1.addChildNode(2);
			ProgramNode nestNode111 = nestNode11.addChildNode(1);
			nestNode111.addChildExercise("Ex1", 1000, EffortLevel.HARD, 2);
			nestNode111.addChildExercise("Ex2", 2000, EffortLevel.HARD, 3);
			ProgramNode nestNode12 = nestNode1.addChildNode(3);
			nestNode12.addChildExercise("Ex3", 1500, EffortLevel.EASY, 1);
			ProgramNode nestNode2 = nestTest.getAssociatedNode().addChildNode(1);
			nestNode2.addChildExercise("Ex4", 1000, EffortLevel.REST, 1);
			nestTest.save();
			ActiveAndroid.setTransactionSuccessful();
		} finally {
			ActiveAndroid.endTransaction();
		}
	}
}
