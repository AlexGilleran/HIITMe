package com.alexgilleran.hiitme.model.impl.test;

import static org.junit.Assert.assertEquals;
import static org.easymock.EasyMock.*;

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
	public void testGetDuration() {
		Program nestedProgram = new ProgramImpl(dao, "Hello", "Hello", 1);
		setupNestedNode(nestedProgram);

		assertEquals(900, step3.getParentNode().getDuration());
		assertEquals(200, step2.getParentNode().getDuration());
		assertEquals(3300, sub3Node1.getDuration());
		assertEquals(400, step4.getParentNode().getDuration());
		assertEquals(7400, sub2Node1.getDuration());
		assertEquals(200, step2.getParentNode().getDuration());
		assertEquals(7600, nestedProgram.getDuration());
	}

	@Test
	public void testNestedRunThrough() {
		Program nestedProgram = new ProgramImpl(dao, "Test",
				"Test description", 2);
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
			expectLastCall();
			callCount++;
			step1Observer.onRepFinish(step1.getParentNode(), 1);
			expectLastCall();
			callCount++;
			step1Observer.onRepFinish(step1.getParentNode(), 2);
			expectLastCall();
			step1Observer.onFinish(step1.getParentNode());
			expectLastCall();
			if (i < 1) {
				step1Observer.onReset(step1.getParentNode());
				expectLastCall();
			}

			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 3; k++) {
					step2Observer.onNextExercise(step2);
					expectLastCall();
					callCount++;
					step2Observer.onRepFinish(step2.getParentNode(), 1);
					expectLastCall();
					step2Observer.onFinish(step2.getParentNode());
					expectLastCall();
					if (!(k == 2 && j == 1 && i == 1)) {
						step2Observer.onReset(step2.getParentNode());
						expectLastCall();
					}
					step3Observer.onNextExercise(step3);
					expectLastCall();
					for (int l = 0; l < 3; l++) {
						callCount++;
						step3Observer.onRepFinish(step3.getParentNode(), l + 1);
						expectLastCall();
					}

					step3Observer.onFinish(step3.getParentNode());
					expectLastCall();

					if (!(k == 2 && j == 1 && i == 1)) {
						step3Observer.onReset(step3.getParentNode());
						expectLastCall();
					}

					sub3Node1Observer.onRepFinish(sub3Node1, k + 1);
					expectLastCall();
				}

				sub3Node1Observer.onFinish(sub3Node1);
				expectLastCall();

				if (!(j == 1 && i == 1)) {
					sub3Node1Observer.onReset(sub3Node1);
					expectLastCall();
				}

				step4Observer.onNextExercise(step4);
				expectLastCall();
				callCount++;
				step4Observer.onRepFinish(step4.getParentNode(), 1);
				expectLastCall();
				step4Observer.onFinish(step4.getParentNode());
				expectLastCall();

				if (!(j == 1 && i == 1)) {
					step4Observer.onReset(step4.getParentNode());
					expectLastCall();
				}

				sub2Node1Observer.onRepFinish(sub2Node1, j + 1);
				expectLastCall();
			}

			sub2Node1Observer.onFinish(sub2Node1);
			expectLastCall();

			if (i < 1) {
				sub2Node1Observer.onReset(sub2Node1);
				expectLastCall();
			}

			subNode1Observer.onRepFinish(subNode1, 1);
			expectLastCall();
			subNode1Observer.onFinish(subNode1);
			expectLastCall();

			if (i < 1) {
				subNode1Observer.onReset(subNode1);
				expectLastCall();
			}

			// Can't compare the actual Program here because it'll return the
			// underlying ProgramNode.
			programObserver
					.onRepFinish(anyObject(ProgramNode.class), eq(i + 1));
			expectLastCall();
		}
		programObserver.onFinish(anyObject(ProgramNode.class));
		expectLastCall();

		for (ProgramNodeObserver observer : observers) {
			replay(observer);
		}

		nestedProgram.start();
		for (int i = 0; i < callCount; i++) {
			nestedProgram.next();
		}

		for (ProgramNodeObserver observer : observers) {
			verify(observer);
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
