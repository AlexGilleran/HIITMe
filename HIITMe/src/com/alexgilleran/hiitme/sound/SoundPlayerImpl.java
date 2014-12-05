package com.alexgilleran.hiitme.sound;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;

public class SoundPlayerImpl implements SoundPlayer {
	private AudioManager audioManager;
	private MediaPlayer mediaPlayer;
	private Context context;

	public SoundPlayerImpl(Context context, AudioManager audioManager) {
		this.context = context;
		this.audioManager = audioManager;

		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
		mediaPlayer.setOnCompletionListener(soundCompleteListener);
	}

	@Override
	public void playExerciseStart(Exercise exercise) {
		requestFocusAndPlay();
	}

	@Override
	public void playEnd() {
		// TODO Auto-generated method stub

	}

	private void requestFocusAndPlay() {
		int result = audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_NOTIFICATION,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			play();
		}
	}

	private void play() {
		try {
			AssetFileDescriptor afd = context.getResources().openRawResourceFd(R.raw.fart);
			mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			mediaPlayer.prepare();
			mediaPlayer.start();
			afd.close();
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalStateException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private void abandonAudioFocus() {
		audioManager.abandonAudioFocus(afChangeListener);
	}

	private OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			// FIXME: fuck you the audio focus is ALL MINE
		}
	};

	private OnCompletionListener soundCompleteListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			abandonAudioFocus();
			mp.reset();
		}
	};
}