package com.alexgilleran.hiitme.sound;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;

import com.alexgilleran.hiitme.model.Exercise;
import com.google.inject.Inject;

public class TextToSpeechPlayer implements SoundPlayer, OnInitListener {
	private HashMap<String, String> speechParams = new HashMap<String, String>();
	private TextToSpeech textToSpeech;
	private AudioManager audioManager;

	@Inject
	public TextToSpeechPlayer(Context context, AudioManager audioManager) {
		this.audioManager = audioManager;

		textToSpeech = new TextToSpeech(context, this);

		speechParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
		speechParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id"); // mandatory to listen to utterances even
																			// though we don't care about the ID.
	}

	@Override
	public void playExerciseStart(Exercise exercise) {
		StringBuilder exText = new StringBuilder();
		exText.append(exercise.getName()).append(", ");
		exText.append(exercise.getEffortLevel()).append(", ");
		exText.append(exercise.getMinutes()).append(" minutes ");
		exText.append(exercise.getSeconds()).append(" seconds");

		textToSpeech.speak(exText.toString(), TextToSpeech.QUEUE_FLUSH, speechParams);
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {

		} else {

		}
	}

	private UtteranceProgressListener utteranceListener = new UtteranceProgressListener() {
		private OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
			public void onAudioFocusChange(int focusChange) {
				if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
					// Pause playback
					audioManager.abandonAudioFocus(afChangeListener);
				} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
					// we don't duck, just abandon focus
					audioManager.abandonAudioFocus(afChangeListener);
				} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
					// Resume playback
				} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
					audioManager.abandonAudioFocus(afChangeListener);
				}
			}
		};

		@Override
		public void onDone(String utteranceId) {
			audioManager.abandonAudioFocus(afChangeListener);
		}

		@Override
		public synchronized void onError(String utteranceId) {
			audioManager.abandonAudioFocus(afChangeListener);
		}

		@Override
		public void onStart(String utteranceId) {
			audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_NOTIFICATION,
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
		}
	};
}
