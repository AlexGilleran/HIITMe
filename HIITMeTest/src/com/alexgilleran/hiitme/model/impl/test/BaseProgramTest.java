package com.alexgilleran.hiitme.model.impl.test;

import org.easymock.EasyMock;
import org.junit.Before;

import com.alexgilleran.hiitme.data.ProgramDao;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Exercise.EffortLevel;
import com.alexgilleran.hiitme.model.ProgramNode;

public class BaseProgramTest {
	protected ProgramDao dao;
	protected Exercise step1;
	protected Exercise step2;
	protected Exercise step3;
	protected Exercise step4;
	protected ProgramNode subNode1;
	protected ProgramNode sub2Node1;
	protected ProgramNode sub3Node1;

	@Before
	public void mockDao() {
		dao = EasyMock.createMock(ProgramDao.class);
	}

	protected void setupNestedNode(ProgramNode nestedNode) {
		step1 = nestedNode.addChildExercise("Step 1", 100, EffortLevel.HARD, 2);

		subNode1 = nestedNode.addChildNode(1);
		sub2Node1 = subNode1.addChildNode(2);
		sub3Node1 = sub2Node1.addChildNode(3);

		step4 = sub2Node1.addChildExercise("Step 4", 400, EffortLevel.REST, 1);

		step2 = sub3Node1.addChildExercise("Step 2", 200, EffortLevel.EASY, 1);
		step3 = sub3Node1.addChildExercise("Step 3", 300, EffortLevel.REST, 3);
	}

	protected void setupSimpleNode(ProgramNode simpleNode) {
		simpleNode.addChildExercise("Step 1", 100, EffortLevel.HARD, 2);
		simpleNode.addChildExercise("Step 2", 200, EffortLevel.EASY, 1);
		simpleNode.addChildExercise("Step 3", 300, EffortLevel.REST, 3);
	}

}