/*
 * HIIT Me, a High Intensity Interval Training app for Android
 * Copyright (C) 2015 Alex Gilleran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alexgilleran.hiitme.sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;

import java.util.HashMap;

public class TextToSpeechPlayer implements SoundPlayer, OnInitListener {
	private HashMap<String, String> speechParams = new HashMap<String, String>();
	private TextToSpeech textToSpeech;
	private boolean init = false;
	private String missedExText = null;
	private AudioManager audioManager;
	private Context context;

	public TextToSpeechPlayer(Context context, AudioManager audioManager) {
		this.audioManager = audioManager;
		this.context = context;

		textToSpeech = new TextToSpeech(context, this);
		textToSpeech.setSpeechRate(1.3f);
		textToSpeech.setOnUtteranceProgressListener(utteranceListener);;

		speechParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
		// mandatory to listen to utterances even though we don't care about the ID.
		speechParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
	}

	@Override
	public void playExerciseStart(Exercise exercise) {
		StringBuilder exText = new StringBuilder();

		if (exercise.getName() != null && !exercise.getName().isEmpty()) {
			exText.append(exercise.getName()).append(", ");
		}

		if (!exercise.getEffortLevel().isBlank()) {
			exText.append(exercise.getEffortLevel().getString(context)).append(", ");
		}

		if (exercise.getMinutes() > 0) {
			exText.append(exercise.getMinutes()).append(" " + context.getString(R.string.time_unit_minute));

			if (exercise.getMinutes() > 1) {
				exText.append(context.getString(R.string.time_unit_pluraliser));
			}

			exText.append(" ");
		}

		if (exercise.getSeconds() > 0) {
			exText.append(exercise.getSeconds()).append(" " + context.getString(R.string.time_unit_second));

			if (exercise.getSeconds() > 1) {
				exText.append(context.getString(R.string.time_unit_pluraliser));
			}

			exText.append(" ");
		}

		if (init) {
			speak(exText.toString());
		} else {
			missedExText = exText.toString();
		}
	}

	private void speak(String message) {
		textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, speechParams);
	}

	@Override
	public void playEnd() {
		textToSpeech.speak(context.getString(R.string.finish_message), TextToSpeech.QUEUE_FLUSH, speechParams);
	}

	@Override
	public void cleanUp() {
		textToSpeech.stop();
		textToSpeech.shutdown();
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			init = true;
			if (missedExText != null) {
				speak(missedExText);
			}
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
