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

	@Before
	public void setUpSimpleNode() {
		simpleNode = new ProgramNodeImpl(1);

		simpleNode.addChildExercise("Step 1", 100, EffortLevel.HARD, 2);
		simpleNode.addChildExercise("Step 2", 200, EffortLevel.EASY, 1);
		simpleNode.addChildExercise("Step 3", 300, EffortLevel.REST, 3);
	}

	@Before
	public void setupNestedNode() {
		nestedNode = new ProgramNodeImpl(2);
		nestedNode.addChildExercise("Step 1", 100, EffortLevel.HARD, 2);

		ProgramNode subNode1 = nestedNode.addChildNode(1);
		ProgramNode sub2Node1 = subNode1.addChildNode(2);
		ProgramNode sub3Node1 = sub2Node1.addChildNode(3);

		sub2Node1.addChildExercise("Step 4", 400, EffortLevel.REST, 1);

		sub3Node1.addChildExercise("Step 2", 200, EffortLevel.EASY, 1);
		sub3Node1.addChildExercise("Step 3", 300, EffortLevel.REST, 3);
	}

	@Test
	public void testNestedRunThrough() {
		// Just to make sure we don't use this by accident
		simpleNode = null;

		for (int i = 1; i <= 2; i++) {
			for (int j = 1; j <= 2; j++) {
				assertExercise(nestedNode.getCurrentExercise(), "Step 1", 100,
						EffortLevel.HARD);
				nestedNode.next();
			}

			// sub2Node1
			for (int j = 1; j <= 2; j++) {
				// sub3node1
				for (int k = 1; k <= 3; k++) {
					assertExercise(nestedNode.getCurrentExercise(), "Step 2",
							200, EffortLevel.EASY);
					nestedNode.next();

					// sub3Node1 exercise 2
					for (int l = 1; l <= 3; l++) {
						assertExercise(nestedNode.getCurrentExercise(),
								"Step 3", 300, EffortLevel.REST);
						nestedNode.next();
					}
				}

				assertExercise(nestedNode.getCurrentExercise(), "Step 4", 400,
						EffortLevel.REST);
				nestedNode.next();
			}
		}
	}

	@Test
	/**
	 * Tests a simple run through, making sure that the next() method moves through one rep at a time.
	 */
	public void testSimpleRunThrough() {
		assertExercise(simpleNode.getCurrentExercise(), "Step 1", 100,
				EffortLevel.HARD);
		simpleNode.next();
		assertExercise(simpleNode.getCurrentExercise(), "Step 1", 100,
				EffortLevel.HARD);
		simpleNode.next();

		assertExercise(simpleNode.getCurrentExercise(), "Step 2", 200,
				EffortLevel.EASY);
		simpleNode.next();

		assertExercise(simpleNode.getCurrentExercise(), "Step 3", 300,
				EffortLevel.REST);
		simpleNode.next();
		assertExercise(simpleNode.getCurrentExercise(), "Step 3", 300,
				EffortLevel.REST);
		simpleNode.next();
		assertExercise(simpleNode.getCurrentExercise(), "Step 3", 300,
				EffortLevel.REST);
		simpleNode.next();

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

	private void assertExercise(Exercise exercise, String name, int duration,
			EffortLevel effortLevel) {
		assertEquals(name, exercise.getName());
		assertEquals(duration, exercise.getDuration());
		assertEquals(effortLevel, exercise.getEffortLevel());
	}
}
