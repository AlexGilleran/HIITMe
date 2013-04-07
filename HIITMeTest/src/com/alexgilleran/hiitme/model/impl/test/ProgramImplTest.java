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
			if (i > 0) {
				observer.onNextExercise(step1);
				EasyMock.expectLastCall();
			}
			callCount++;
			observer.onRepFinish(step1.getParentNode(), 1);
			EasyMock.expectLastCall();
			observer.onNextExercise(step1);
			EasyMock.expectLastCall();
			callCount++;
			observer.onRepFinish(step1.getParentNode(), 2);
			EasyMock.expectLastCall();
			observer.onFinish(step1.getParentNode());
			EasyMock.expectLastCall();

			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 3; k++) {
					observer.onNextExercise(step2);
					EasyMock.expectLastCall();
					callCount++;
					observer.onRepFinish(step2.getParentNode(), 1);
					EasyMock.expectLastCall();
					observer.onFinish(step2.getParentNode());
					EasyMock.expectLastCall();

					for (int l = 0; l < 3; l++) {
						observer.onNextExercise(step3);
						EasyMock.expectLastCall();
						callCount++;
						observer.onRepFinish(step3.getParentNode(), l + 1);
						EasyMock.expectLastCall();
					}

					observer.onFinish(step3.getParentNode());
					EasyMock.expectLastCall();
					observer.onRepFinish(sub3Node1, k + 1);
					EasyMock.expectLastCall();
				}

				observer.onFinish(sub3Node1);
				EasyMock.expectLastCall();

				observer.onNextExercise(step4);
				EasyMock.expectLastCall();
				callCount++;
				observer.onRepFinish(step4.getParentNode(), 1);
				EasyMock.expectLastCall();
				observer.onFinish(step4.getParentNode());
				EasyMock.expectLastCall();

				observer.onRepFinish(sub2Node1, j + 1);
				EasyMock.expectLastCall();
			}

			observer.onFinish(sub2Node1);
			EasyMock.expectLastCall();
			observer.onRepFinish(subNode1, 1);
			EasyMock.expectLastCall();
			observer.onFinish(subNode1);
			EasyMock.expectLastCall();
			observer.onRepFinish(nestedProgram, i + 1);
			EasyMock.expectLastCall();
		}
		observer.onFinish(nestedProgram);
		EasyMock.expectLastCall();

		EasyMock.replay(observer);

		nestedProgram.start();
		for (int i = 0; i < callCount; i++) {
			nestedProgram.next();
		}

		EasyMock.verify(observer);
	}
}
