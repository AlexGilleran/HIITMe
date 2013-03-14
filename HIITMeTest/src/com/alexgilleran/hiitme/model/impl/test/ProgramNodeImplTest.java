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

	@Before
	public void setUp() {
		simpleNode = new ProgramNodeImpl(1);

		simpleNode.addChildExercise("Step 1", 100, EffortLevel.HARD, 2);
		simpleNode.addChildExercise("Step 2", 200, EffortLevel.EASY, 1);
		simpleNode.addChildExercise("Step 3", 300, EffortLevel.REST, 3);
	}

	@Test
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

	private void assertExercise(Exercise exercise, String name, int duration,
			EffortLevel effortLevel) {
		assertEquals(name, exercise.getName());
		assertEquals(duration, exercise.getDuration());
		assertEquals(effortLevel, exercise.getEffortLevel());
	}
}
