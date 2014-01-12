package com.alexgilleran.hiitme.model.impl.test;

import org.easymock.EasyMock;
import org.junit.Before;

import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;

public class BaseProgramTest {
	protected ProgramDAO dao;
	protected Exercise step1;
	protected Exercise step2;
	protected Exercise step3;
	protected Exercise step4;
	protected Node subNode1;
	protected Node sub2Node1;
	protected Node sub3Node1;

	@Before
	public void mockDao() {
		dao = EasyMock.createMock(ProgramDAO.class);
	}

	protected void setupNestedNode(Node nestedNode) {
		step1 = nestedNode.addChildExercise("Step 1", 100, EffortLevel.HARD, 2);

		subNode1 = nestedNode.addChildNode(1);
		sub2Node1 = subNode1.addChildNode(2);
		sub3Node1 = sub2Node1.addChildNode(3);

		step4 = sub2Node1.addChildExercise("Step 4", 400, EffortLevel.REST, 1);

		step2 = sub3Node1.addChildExercise("Step 2", 200, EffortLevel.EASY, 1);
		step3 = sub3Node1.addChildExercise("Step 3", 300, EffortLevel.REST, 3);
	}

	protected void setupSimpleNode(Node simpleNode) {
		simpleNode.addChildExercise("Step 1", 100, EffortLevel.HARD, 2);
		simpleNode.addChildExercise("Step 2", 200, EffortLevel.EASY, 1);
		simpleNode.addChildExercise("Step 3", 300, EffortLevel.REST, 3);
	}

}