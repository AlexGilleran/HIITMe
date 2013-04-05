package com.alexgilleran.hiitme.model.impl.test;

import org.easymock.EasyMock;
import org.junit.Test;

import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;
import com.alexgilleran.hiitme.model.impl.ProgramImpl;

public class ProgramImplTest extends BaseProgramTest {
	@Test
	public void testRunThrough() {
		ProgramNodeObserver observer = EasyMock
				.createStrictMock(ProgramNodeObserver.class);

		Program nestedProgram = new ProgramImpl(0, "Test", "Test description",
				2);
		setupNestedNode(nestedProgram);
		nestedProgram.registerObserver(observer);

		int callCount = 0;

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				observer.onRepFinish(step1.getParentNode(), j + 1);
				EasyMock.expectLastCall();
				observer.onNextExercise(step1);
				EasyMock.expectLastCall();
				callCount++;
			}

			observer.onFinish(step1.getParentNode());

			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 3; k++) {
					observer.onNextExercise(step2);
					EasyMock.expectLastCall();
					callCount++;

					for (int l = 0; l < 3; l++) {
						observer.onNextExercise(step3);
						EasyMock.expectLastCall();
						callCount++;
					}
				}

				observer.onNextExercise(step4);
				EasyMock.expectLastCall();
				callCount++;
			}
		}

		EasyMock.replay(observer);

		nestedProgram.start();
		for (int i = 0; i < callCount; i++) {
			nestedProgram.next();
		}

		EasyMock.verify(observer);
	}
}
