/** Copyright 2012 Kevin Hausmann
 *
 * This file is part of PodCatcher Deluxe.
 *
 * PodCatcher Deluxe is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * PodCatcher Deluxe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PodCatcher Deluxe. If not, see <http://www.gnu.org/licenses/>.
 */
package net.alliknow.podcatcher.fragments;

import java.util.Timer;
import java.util.TimerTask;

import net.alliknow.podcatcher.Podcatcher;
import net.alliknow.podcatcher.R;
import net.alliknow.podcatcher.listeners.PlayServiceListener;
import net.alliknow.podcatcher.services.PlayEpisodeService;
import net.alliknow.podcatcher.services.PlayEpisodeService.PlayServiceBinder;
import net.alliknow.podcatcher.types.Episode;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Fragment showing episode details
 * 
 * @author Kevin Hausmann
 */
public class EpisodeFragment extends Fragment implements PlayServiceListener {

	/** The load episode menu bar item */
	private MenuItem loadMenuItem;
	/** The episode title view */
	private TextView episodeTitleView;
	/** The podcast title view */
	private TextView podcastTitleView;
	/** The episode description web view */
	private WebView episodeDetailView;
	/** The player title view */
	private View playerDividerView;
	/** The player title view */
	private TextView playerTitleView;
	/** The player view */
	private View playerView;
	/** The player seek bar */
	private ProgressBar playerSeekBar;
	/** The play/pause button */
	private Button playerButton;
		
	/** Current episode */
	private Episode episode;
	/** Play service */
	private PlayEpisodeService service;
		
	/** Play update timer task */
	private Timer playUpdateTimer = new Timer();
	/** Play update timer task */
	private TimerTask playUpdateTimerTask;
	/** The actual task to regularly update the UI on playback */
	private class PlayProgressTask extends TimerTask {

		@Override
		public void run() {
			// This will only work, if our callback actually exists
			if (EpisodeFragment.this == null || EpisodeFragment.this.getActivity() == null) return;
			
			// Need to run on UI thread, since we want to update the play button
			EpisodeFragment.this.getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					updatePlayerSeekBar();
					updatePlayerButton();
				}
			});
		}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setRetainInstance(true);
    	setHasOptionsMenu(true);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.episode, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		episodeTitleView = (TextView) getView().findViewById(R.id.episode_title);
		podcastTitleView = (TextView) getView().findViewById(R.id.podcast_title);
		episodeDetailView = (WebView) getView().findViewById(R.id.episode_description);
		
		playerDividerView = getView().findViewById(R.id.player_divider);
		playerTitleView = (TextView) getView().findViewById(R.id.player_title);
		playerView = view.findViewById(R.id.player);
		playerSeekBar = (ProgressBar) view.findViewById(R.id.player_seekbar);
		playerButton = (Button) view.findViewById(R.id.player_button);
		playerButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				togglePlay();
			}
		});
		playerButton.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				onPlaybackComplete();
				
				return true;
			}
		});
		
		// Restore from configuration change 
		if (episode != null) setEpisode(episode);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.episode_menu, menu);
		
		loadMenuItem = menu.findItem(R.id.load);
		updateLoadMenuItem();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// Make sure the service runs as long as this fragment exists
    	getActivity().startService(new Intent(getActivity(), PlayEpisodeService.class));
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Attach to play service via this fragment's activity
		Intent intent = new Intent(getActivity(), PlayEpisodeService.class);
    	activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);    	
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    	case R.id.load:
	    		if (service.isWorkingWith(episode)) onPlaybackComplete();
				else loadEpisode();
	    		
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (service != null && service.isPrepared()) service.showNotification(true);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (service != null) service.showNotification(false);
		
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		// Detach from service callbacks
		if (service != null) service.setPlayServiceListener(null);
		
		// Detach from play service via this fragment's activity
		getActivity().unbindService(connection);
				
		// Stop progress update task if existing
		stopPlayProgressTimer();
	}
		
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		playUpdateTimer.cancel();
	}
	
	/**
	 * Set the displayed episode, all UI will be updated
	 * @param selectedEpisode Episode to show (cannot be null)
	 */
	public void setEpisode(Episode selectedEpisode) {
		if (selectedEpisode != null) {
			this.episode = selectedEpisode;
			
			getView().findViewById(android.R.id.empty).setVisibility(View.GONE);
			
			episodeTitleView.setVisibility(View.VISIBLE);
			episodeTitleView.setText(episode.getName());
			podcastTitleView.setText(episode.getPodcast().getName());
			podcastTitleView.setVisibility(View.VISIBLE);
			getView().findViewById(R.id.episode_divider).setVisibility(View.VISIBLE);
							
			episodeDetailView.loadDataWithBaseURL(null, episode.getDescription(), "text/html", "utf-8", null);
			episodeDetailView.setVisibility(View.VISIBLE);
			
			updateLoadMenuItem();
			updatePlayer();
		}
	}
		
	@Override
	public void onReadyToPlay() {
		updatePlayer();
		
		startPlayProgressTimer();
	}
	
	@Override
	public void onStopForBuffering() {
		stopPlayProgressTimer();
		
		updatePlayer();
	}

	@Override
	public void onResumeFromBuffering() {
		onReadyToPlay();
	}
	
	@Override
	public void onBufferUpdate(int seconds) {
		playerSeekBar.setSecondaryProgress(seconds);
	}

	@Override
	public void onPlaybackComplete() {
		stopPlayProgressTimer();
		
		service.reset();
		
		updateLoadMenuItem();
		updatePlayer();
	}
	
	@Override
	public void onError() {
		service.reset();
		
		updateLoadMenuItem();
		updatePlayer();
		
		getView().findViewById(R.id.player_error).setVisibility(View.VISIBLE);
		
		Log.w(getClass().getSimpleName(), "Play service send an error");
	}
	
	private void loadEpisode() {
		if (episode != null && service != null) {		
			// Episode should not be loaded
			if (! service.isWorkingWith(episode)) {
				stopPlayProgressTimer();
				
				service.playEpisode(episode);
								
				updateLoadMenuItem();
				updatePlayer();
			}
		} else Log.d(getClass().getSimpleName(), "Cannot load episode (episode or service are null)");
	}
	
	private void togglePlay() {
		if (service != null && service.isPrepared()) {		
			// Player is playing
			if (service.isPlaying()) {
				service.pause();
				stopPlayProgressTimer();
			} // Player in pause
			else {
				service.resume();
				startPlayProgressTimer();
			}
			
			updatePlayer();
		} else Log.d(getClass().getSimpleName(), "Cannot play/pause episode (service null or unprepared)");
	}
	
	private void updateLoadMenuItem() {
		if (loadMenuItem == null) return;
		
		loadMenuItem.setVisible(episode != null && service != null);
		
		if (loadMenuItem.isVisible()) {
			loadMenuItem.setTitle(service.isWorkingWith(episode) ? R.string.stop : R.string.play );
			loadMenuItem.setIcon(service.isWorkingWith(episode) ? R.drawable.ic_media_stop : R.drawable.ic_media_play);
		}
	}
	
	private void updatePlayer() {
		getView().findViewById(R.id.player_error).setVisibility(View.GONE);
		
		playerDividerView.setVisibility(service.isWorkingWith(episode) ? View.GONE : View.VISIBLE);
		playerTitleView.setVisibility(service.isWorkingWith(episode) ? View.GONE : View.VISIBLE);
		playerTitleView.setText(service.getCurrentEpisodeName() + " - " 
					+ service.getCurrentEpisodePodcastName());
			
		updatePlayerSeekBar();
		updatePlayerButton();
				
		playerView.setVisibility(service.isPrepared() || service.isPreparing() ? View.VISIBLE : View.GONE);
	}
	
	private void updatePlayerSeekBar() {
		playerSeekBar.setIndeterminate(service.isBuffering());
		
		if (service.isPrepared()) {
			playerSeekBar.setMax(service.getDuration());
			playerSeekBar.setProgress(service.getCurrentPosition());
		}
	}

	private void updatePlayerButton() {
		playerButton.setEnabled(! service.isBuffering());
		playerButton.setBackgroundResource(service.isPlaying() ? R.drawable.button_red : R.drawable.button_green);
		playerButton.setCompoundDrawablesWithIntrinsicBounds(
				service.isPlaying() ? R.drawable.ic_media_pause : R.drawable.ic_media_play, 0, 0, 0);
		
		if (service.isBuffering()) {
			playerButton.setText(R.string.buffering);
			playerButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_rotate, 0, 0, 0);
		}
		else {
			playerButton.setText(service.isPlaying() ? R.string.pause : R.string.resume);
			// Resources are only available when fragment is added...
			if (isAdded() && service.isPrepared()) {
				final String position = Podcatcher.formatTime(service.getCurrentPosition());
				final String duration = Podcatcher.formatTime(service.getDuration());
				
				playerButton.setText(playerButton.getText() + " " + getResources().getString(R.string.at) +
						" " + position + " " + getResources().getString(R.string.of) + " " + duration);
			}
		}
	}
	
	private void startPlayProgressTimer() {
		playUpdateTimerTask = new PlayProgressTask();
		playUpdateTimer.schedule(playUpdateTimerTask, 0, 1000);
	}
	
	private void stopPlayProgressTimer() {
		if (playUpdateTimerTask != null) playUpdateTimerTask.cancel();
	}

	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
        	service = ((PlayServiceBinder) serviceBinder).getService();
            Log.d(EpisodeFragment.this.getClass().getSimpleName(), "Bound to playback service");
            
            // Register listener and notification
            service.setPlayServiceListener(EpisodeFragment.this);
            service.showNotification(false);
            
            // Update UI to reflect service status
            updateLoadMenuItem();
            updatePlayer();
            // Restart play progress timer task if service is playing
            if (service.isPlaying()) startPlayProgressTimer();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(EpisodeFragment.this.getClass().getSimpleName(), "Unbound from playback service");
        }
    };
}
