package com.alexgilleran.hiitme.model.impl.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Exercise.EffortLevel;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.impl.ProgramNodeImpl;

public class ProgramNodeImplTest {
	private ProgramNode simpleNode;
	private ProgramNode nestedNode;
	private ProgramNode subNode1;
	private ProgramNode sub2Node1;
	private ProgramNode sub3Node1;

	@Before
	public void setupNestedNode() {
		nestedNode = new ProgramNodeImpl(2);
		nestedNode.addChildExercise("Step 1", 100, EffortLevel.HARD, 2);

		subNode1 = nestedNode.addChildNode(1);
		sub2Node1 = subNode1.addChildNode(2);
		sub3Node1 = sub2Node1.addChildNode(3);

		sub2Node1.addChildExercise("Step 4", 400, EffortLevel.REST, 1);

		sub3Node1.addChildExercise("Step 2", 200, EffortLevel.EASY, 1);
		sub3Node1.addChildExercise("Step 3", 300, EffortLevel.REST, 3);
	}

	@Test
	public void testNestedTraversal() {
		assertEquals(2, nestedNode.getTotalReps());

		ProgramNode step1Node = nestedNode.getChildren().get(0);
		assertEquals(2, step1Node.getTotalReps());
		assertExercise(step1Node.getAttachedExercise(), "Step 1", 100,
				EffortLevel.HARD);

		subNode1 = nestedNode.getChildren().get(1);
		assertEquals(1, subNode1.getTotalReps());

		sub2Node1 = subNode1.getChildren().get(0);
		assertEquals(2, sub2Node1.getTotalReps());
		ProgramNode step4Node = sub2Node1.getChildren().get(1);
		assertEquals(1, step4Node.getTotalReps());
		assertExercise(step4Node.getAttachedExercise(), "Step 4", 400,
				EffortLevel.REST);

		sub3Node1 = sub2Node1.getChildren().get(0);
		assertEquals(3, sub3Node1.getTotalReps());
		ProgramNode step2Node = sub3Node1.getChildren().get(0);
		assertEquals(1, step2Node.getTotalReps());
		assertExercise(step2Node.getAttachedExercise(), "Step 2", 200,
				EffortLevel.EASY);
		ProgramNode step3Node = sub3Node1.getChildren().get(1);
		assertEquals(3, step3Node.getTotalReps());
		assertExercise(step3Node.getAttachedExercise(), "Step 3", 300,
				EffortLevel.REST);
	}

	@Test
	public void testNestedRunThrough() {
		// Just to make sure we don't use this by accident
		simpleNode = null;

		for (int i = 0; i < 2; i++) {
			assertEquals(i, nestedNode.getCompletedReps());

			ProgramNode step1Node = nestedNode.getCurrentExercise()
					.getParentNode();

			for (int j = 0; j < 2; j++) {
				assertEquals(j, step1Node.getCompletedReps());
				assertExercise(nestedNode.getCurrentExercise(), "Step 1", 100,
						EffortLevel.HARD);
				nestedNode.next();
			}

			// sub2Node1
			for (int j = 0; j < 2; j++) {
				assertEquals(j, sub2Node1.getCompletedReps());

				// sub3node1
				for (int k = 0; k < 3; k++) {
					assertEquals(k, sub3Node1.getCompletedReps());

					ProgramNode step2Node = nestedNode.getCurrentExercise()
							.getParentNode();
					assertExercise(nestedNode.getCurrentExercise(), "Step 2",
							200, EffortLevel.EASY);
					assertEquals(0, step2Node.getCompletedReps());
					nestedNode.next();
					assertEquals(1, step2Node.getCompletedReps());

					ProgramNode step3Node = nestedNode.getCurrentExercise()
							.getParentNode();
					// sub3Node1 exercise 2
					for (int l = 0; l < 3; l++) {
						assertEquals(l, step3Node.getCompletedReps());
						assertExercise(nestedNode.getCurrentExercise(),
								"Step 3", 300, EffortLevel.REST);
						nestedNode.next();
					}
				}

				ProgramNode step4Node = nestedNode.getCurrentExercise()
						.getParentNode();
				assertEquals(0, step4Node.getCompletedReps());
				assertExercise(nestedNode.getCurrentExercise(), "Step 4", 400,
						EffortLevel.REST);
				nestedNode.next();
			}
		}
	}

	@Before
	public void setUpSimpleNode() {
		simpleNode = new ProgramNodeImpl(1);

		simpleNode.addChildExercise("Step 1", 100, EffortLevel.HARD, 2);
		simpleNode.addChildExercise("Step 2", 200, EffortLevel.EASY, 1);
		simpleNode.addChildExercise("Step 3", 300, EffortLevel.REST, 3);
	}

	@Test
	/**
	 * Tests a simple run through, making sure that the next() method moves through one rep at a time.
	 */
	public void testSimpleRunThrough() {
		assertExercise(simpleNode.getCurrentExercise(), "Step 1", 100,
				EffortLevel.HARD);
		assertEquals(0, simpleNode.getCurrentExercise().getParentNode()
				.getCompletedReps());
		simpleNode.next();
		assertExercise(simpleNode.getCurrentExercise(), "Step 1", 100,
				EffortLevel.HARD);
		assertEquals(1, simpleNode.getCurrentExercise().getParentNode()
				.getCompletedReps());
		simpleNode.next();

		assertExercise(simpleNode.getCurrentExercise(), "Step 2", 200,
				EffortLevel.EASY);
		assertEquals(0, simpleNode.getCurrentExercise().getParentNode()
				.getCompletedReps());
		simpleNode.next();

		ProgramNode node3 = simpleNode.getCurrentExercise().getParentNode();
		assertExercise(simpleNode.getCurrentExercise(), "Step 3", 300,
				EffortLevel.REST);
		assertEquals(0, node3.getCompletedReps());
		simpleNode.next();
		assertExercise(simpleNode.getCurrentExercise(), "Step 3", 300,
				EffortLevel.REST);
		assertEquals(1, node3.getCompletedReps());
		simpleNode.next();
		assertExercise(simpleNode.getCurrentExercise(), "Step 3", 300,
				EffortLevel.REST);
		assertEquals(2, node3.getCompletedReps());
		simpleNode.next();
		assertEquals(3, node3.getCompletedReps());

		assertTrue(simpleNode.isFinished());
	}

	@Test
	/**
	 * Tests a traversal of the whole tree, as would be performed to display its contents on the UI
	 */
	public void testSimpleTraversal() {
		assertTrue(simpleNode.hasChildren());
		assertEquals(3, simpleNode.getChildren().size());

		ProgramNode step1 = simpleNode.getChildren().get(0);
		ProgramNode step2 = simpleNode.getChildren().get(1);
		ProgramNode step3 = simpleNode.getChildren().get(2);

		assertEquals(2, step1.getTotalReps());
		assertEquals(1, step2.getTotalReps());
		assertEquals(3, step3.getTotalReps());

		assertFalse(step1.hasChildren());
		assertFalse(step2.hasChildren());
		assertFalse(step3.hasChildren());

		Exercise exercise1 = step1.getAttachedExercise();
		Exercise exercise2 = step2.getAttachedExercise();
		Exercise exercise3 = step3.getAttachedExercise();

		assertExercise(exercise1, "Step 1", 100, EffortLevel.HARD);
		assertExercise(exercise2, "Step 2", 200, EffortLevel.EASY);
		assertExercise(exercise3, "Step 3", 300, EffortLevel.REST);
	}

	private static void assertExercise(Exercise exercise, String name,
			int duration, EffortLevel effortLevel) {
		assertEquals(name, exercise.getName());
		assertEquals(duration, exercise.getDuration());
		assertEquals(effortLevel, exercise.getEffortLevel());
	}
}
