package com.alexgilleran.hiitme.programrunner;

import java.util.ArrayList;
import java.util.List;

import roboguice.service.RoboIntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;
import com.alexgilleran.hiitme.programrunner.ExerciseCountDown.CountDownObserver;
import com.google.inject.Inject;

public class ProgramRunService extends RoboIntentService {

	@Inject
	private ProgramDAO programDao;

	private Program program;

	private ExerciseCountDown exerciseCountDown;
	private ExerciseCountDown programCountDown;

	boolean isRunning = false;

	private Notification notification;

	private List<CountDownObserver> exerciseObservers = new ArrayList<CountDownObserver>();
	private List<CountDownObserver> programObservers = new ArrayList<CountDownObserver>();

	public ProgramRunService() {
		super("HIIT Me");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Notification.Builder builder = new Notification.Builder(
				this.getBaseContext());
		builder.setContentTitle("HIIT Me");
		builder.setSmallIcon(R.drawable.ic_launcher);
		notification = builder.getNotification();

		long programId = intent.getLongExtra(Program.PROGRAM_ID_NAME, -1);
		program = programDao.getProgram(programId);
		program.registerObserver(programObserver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ProgramBinder();
	}

	private void next() {
		program.next();
		if (!program.isFinished()) {
			newCountDown();
		}
	}

	private void newCountDown() {
		exerciseCountDown = new ExerciseCountDown(program.getCurrentExercise()
				.getDuration(), exerciseCountDownObs);
		exerciseCountDown.start();
	}

	private ProgramNodeObserver programObserver = new ProgramNodeObserver() {
		@Override
		public void onNextExercise(Exercise newExercise) {
		}

		@Override
		public void onRepFinish(ProgramNode superset, int remainingReps) {
		}

		@Override
		public void onFinish(ProgramNode node) {
			stopForeground(true);
			isRunning = false;
		}

		@Override
		public void onReset(ProgramNode node) {

		}
	};

	public class ProgramBinder extends Binder {
		boolean isPaused = false;

		public void start() {
			isRunning = true;

			if (isPaused) {
				exerciseCountDown.start();
				programCountDown.start();

			} else {
				startForeground(1, notification);
				programCountDown = new ExerciseCountDown(
						program.getTotalDuration(), programCountDownObs);
				program.start();

				newCountDown();
				programCountDown.start();
			}
		}

		public void stop() {
			isRunning = false;
			exerciseCountDown.cancel();
			programCountDown.cancel();
		}

		public void pause() {
			isPaused = true;
			isRunning = false;
			exerciseCountDown = exerciseCountDown.pause();
			programCountDown = programCountDown.pause();
		}

		public Program getProgram() {
			return program;
		}

		public boolean isRunning() {
			return isRunning;
		}

		public void regExerciseCountDownObs(CountDownObserver observer) {
			exerciseObservers.add(observer);
		}

		public void regProgCountDownObs(CountDownObserver observer) {
			programObservers.add(observer);
		}

		public ProgramNode getCurrentSuperset() {
			return program.getCurrentNode();
		}

		public Exercise getCurrentExercise() {
			return program.getCurrentExercise();
		}
	}

	private CountDownObserver exerciseCountDownObs = new CountDownObserver() {
		@Override
		public void onTick(long msecondsRemaining) {
			for (CountDownObserver observer : exerciseObservers) {
				observer.onTick(msecondsRemaining);
			}
		}

		@Override
		public void onFinish() {
			next();
		}
	};

	private CountDownObserver programCountDownObs = new CountDownObserver() {
		@Override
		public void onTick(long msecondsRemaining) {
			for (CountDownObserver observer : programObservers) {
				observer.onTick(msecondsRemaining);
			}
		}

		@Override
		public void onFinish() {

		}
	};
}
