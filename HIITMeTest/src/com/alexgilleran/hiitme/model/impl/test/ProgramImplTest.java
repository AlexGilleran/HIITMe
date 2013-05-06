package com.alexgilleran.hiitme.model.impl.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;
import com.alexgilleran.hiitme.model.impl.ProgramImpl;

public class ProgramImplTest extends BaseProgramTest {
	private List<ProgramNodeObserver> observers = new ArrayList<ProgramNodeObserver>();

	@Test
	public void testNestedRunThrough() {
		Program nestedProgram = new ProgramImpl(0, "Test", "Test description",
				2);
		setupNestedNode(nestedProgram);

		ProgramNodeObserver step1Observer = addObserver(step1.getParentNode());
		ProgramNodeObserver step2Observer = addObserver(step2.getParentNode());
		ProgramNodeObserver step3Observer = addObserver(step3.getParentNode());
		ProgramNodeObserver step4Observer = addObserver(step4.getParentNode());
		ProgramNodeObserver sub3Node1Observer = addObserver(sub3Node1);
		ProgramNodeObserver sub2Node1Observer = addObserver(sub2Node1);
		ProgramNodeObserver subNode1Observer = addObserver(subNode1);
		ProgramNodeObserver programObserver = addObserver(nestedProgram);

		int callCount = 0;
		for (int i = 0; i < 2; i++) {
			step1Observer.onNextExercise(step1);
			EasyMock.expectLastCall();
			callCount++;
			step1Observer.onRepFinish(step1.getParentNode(), 1);
			EasyMock.expectLastCall();
			callCount++;
			step1Observer.onRepFinish(step1.getParentNode(), 2);
			EasyMock.expectLastCall();
			step1Observer.onFinish(step1.getParentNode());
			EasyMock.expectLastCall();
			if (i < 1) {
				step1Observer.onReset(step1.getParentNode());
				EasyMock.expectLastCall();
			}

			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 3; k++) {
					step2Observer.onNextExercise(step2);
					EasyMock.expectLastCall();
					callCount++;
					step2Observer.onRepFinish(step2.getParentNode(), 1);
					EasyMock.expectLastCall();
					step2Observer.onFinish(step2.getParentNode());
					EasyMock.expectLastCall();
					if (!(k == 2 && j == 1 && i == 1)) {
						step2Observer.onReset(step2.getParentNode());
						EasyMock.expectLastCall();
					}
					step3Observer.onNextExercise(step3);
					EasyMock.expectLastCall();
					for (int l = 0; l < 3; l++) {
						callCount++;
						step3Observer.onRepFinish(step3.getParentNode(), l + 1);
						EasyMock.expectLastCall();
					}

					step3Observer.onFinish(step3.getParentNode());
					EasyMock.expectLastCall();

					if (!(k == 2 && j == 1 && i == 1)) {
						step3Observer.onReset(step3.getParentNode());
						EasyMock.expectLastCall();
					}

					sub3Node1Observer.onRepFinish(sub3Node1, k + 1);
					EasyMock.expectLastCall();
				}

				sub3Node1Observer.onFinish(sub3Node1);
				EasyMock.expectLastCall();

				if (!(j == 1 && i == 1)) {
					sub3Node1Observer.onReset(sub3Node1);
					EasyMock.expectLastCall();
				}

				step4Observer.onNextExercise(step4);
				EasyMock.expectLastCall();
				callCount++;
				step4Observer.onRepFinish(step4.getParentNode(), 1);
				EasyMock.expectLastCall();
				step4Observer.onFinish(step4.getParentNode());
				EasyMock.expectLastCall();

				if (!(j == 1 && i == 1)) {
					step4Observer.onReset(step4.getParentNode());
					EasyMock.expectLastCall();
				}

				sub2Node1Observer.onRepFinish(sub2Node1, j + 1);
				EasyMock.expectLastCall();
			}

			sub2Node1Observer.onFinish(sub2Node1);
			EasyMock.expectLastCall();

			if (i < 1) {
				sub2Node1Observer.onReset(sub2Node1);
				EasyMock.expectLastCall();
			}

			subNode1Observer.onRepFinish(subNode1, 1);
			EasyMock.expectLastCall();
			subNode1Observer.onFinish(subNode1);
			EasyMock.expectLastCall();

			if (i < 1) {
				subNode1Observer.onReset(subNode1);
				EasyMock.expectLastCall();
			}

			programObserver.onRepFinish(nestedProgram, i + 1);
			EasyMock.expectLastCall();
		}
		programObserver.onFinish(nestedProgram);
		EasyMock.expectLastCall();

		for (ProgramNodeObserver observer : observers) {
			EasyMock.replay(observer);
		}

		nestedProgram.start();
		for (int i = 0; i < callCount; i++) {
			nestedProgram.next();
		}

		for (ProgramNodeObserver observer : observers) {
			EasyMock.verify(observer);
		}

		try {
			nestedProgram.next();
			Assert.fail();
		} catch (RuntimeException e) {
			// yay
		}
	}

	private ProgramNodeObserver addObserver(ProgramNode node) {
		ProgramNodeObserver observer = EasyMock
				.createStrictMock(ProgramNodeObserver.class);
		observers.add(observer);
		node.registerObserver(observer);
		return observer;
	}
}
