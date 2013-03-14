//package com.alexgilleran.hiitme.programrunner;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.alexgilleran.hiitme.model.Exercise;
//import com.alexgilleran.hiitme.model.Program;
//import com.alexgilleran.hiitme.model.ProgramNode;
//
//public class ProgramTracker2 implements IProgramTracker {
//	private static final int INITIAL_REP_COUNT = 1;
//	private Program program;
//	private int currentRepCount = INITIAL_REP_COUNT;
//	private List<ProgramTrackerObserver> observers = new ArrayList<ProgramTrackerObserver>();
//
//	public ProgramTrackerOld(Program program, ProgramTrackerObserver listener) {
//		this.program = program;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * com.alexgilleran.hiitme.programrunner.IProgramTracker#getCurrentSuperset
//	 * ()
//	 */
//	@Override
//	public ProgramNode getCurrentSuperset() {
//		if (currentSupersetIndex == FINISHED_FLAG) {
//			return null;
//		}
//
//		return program.getChildren().get(currentSupersetIndex);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * com.alexgilleran.hiitme.programrunner.IProgramTracker#getCurrentExercise
//	 * ()
//	 */
//	@Override
//	public Exercise getCurrentExercise() {
//		if (currentExerciseIndex == FINISHED_FLAG) {
//			return null;
//		}
//
//		return null; // (Exercise)
//						// this.getCurrentSuperset().getExercise().get(currentExerciseIndex);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.alexgilleran.hiitme.programrunner.IProgramTracker#getProgram()
//	 */
//	@Override
//	public Program getProgram() {
//		return program;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.alexgilleran.hiitme.programrunner.IProgramTracker#isFinished()
//	 */
//	@Override
//	public boolean isFinished() {
//		return (currentExerciseIndex == FINISHED_FLAG
//				|| currentSupersetIndex == FINISHED_FLAG || currentRepCount == FINISHED_FLAG);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.alexgilleran.hiitme.programrunner.IProgramTracker#getRepCount()
//	 */
//	@Override
//	public int getRepCount() {
//		if (this.getCurrentSuperset() != null) {
//			return this.currentRepCount;
//		} else {
//			return -1;
//		}
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.alexgilleran.hiitme.programrunner.IProgramTracker#start()
//	 */
//	@Override
//	public void start() {
//		this.broadcastNextExercise(getCurrentExercise());
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.alexgilleran.hiitme.programrunner.IProgramTracker#next()
//	 */
//	@Override
//	public void next() {
//		
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * com.alexgilleran.hiitme.programrunner.IProgramTracker#registerObserver
//	 * (com
//	 * .alexgilleran.hiitme.programrunner.ProgramTracker.ProgramTrackerObserver)
//	 */
//	@Override
//	public void registerObserver(ProgramTrackerObserver observer) {
//		observers.add(observer);
//	}
//
//	private void broadcastNextExercise(Exercise newExercise) {
//		for (ProgramTrackerObserver observer : observers) {
//			observer.onNextExercise(newExercise);
//		}
//	}
//
//	private void broadcastFinish() {
//		for (ProgramTrackerObserver observer : observers) {
//			observer.onFinish();
//		}
//	}
//
//	private void broadcastRepFinish(ProgramNode superset, int remainingReps) {
//		for (ProgramTrackerObserver observer : observers) {
//			observer.onRepFinish(superset, remainingReps);
//		}
//	}
//}
