/** Copyright 2012-2014 Kevin Hausmann
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

package net.alliknow.podcatcher;

import static net.alliknow.podcatcher.view.fragments.AuthorizationFragment.USERNAME_PRESET_KEY;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import net.alliknow.podcatcher.listeners.OnLoadDownloadsListener;
import net.alliknow.podcatcher.listeners.OnLoadPlaylistListener;
import net.alliknow.podcatcher.listeners.OnLoadPodcastListener;
import net.alliknow.podcatcher.listeners.OnLoadPodcastLogoListener;
import net.alliknow.podcatcher.listeners.OnReorderEpisodeListener;
import net.alliknow.podcatcher.listeners.OnReverseSortingListener;
import net.alliknow.podcatcher.listeners.OnSelectPodcastListener;
import net.alliknow.podcatcher.listeners.OnToggleFilterListener;
import net.alliknow.podcatcher.model.tasks.remote.LoadPodcastTask.PodcastLoadError;
import net.alliknow.podcatcher.model.types.Episode;
import net.alliknow.podcatcher.model.types.Podcast;
import net.alliknow.podcatcher.model.types.Progress;
import net.alliknow.podcatcher.view.ContentSpinner;
import net.alliknow.podcatcher.view.fragments.AuthorizationFragment;
import net.alliknow.podcatcher.view.fragments.AuthorizationFragment.OnEnterAuthorizationListener;
import net.alliknow.podcatcher.view.fragments.EpisodeListFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Show list of episodes activity. This is thought of as an abstract activity
 * for an app only consisting of an episode list view, the player and the
 * ability to show an {@link ShowEpisodeActivity} on top. Sub-classes could
 * extend or simply show this layout.
 */
public abstract class EpisodeListActivity extends EpisodeActivity implements
        OnLoadPodcastListener, OnEnterAuthorizationListener, OnLoadPodcastLogoListener,
        OnSelectPodcastListener, OnLoadDownloadsListener, OnLoadPlaylistListener,
        OnReorderEpisodeListener, OnToggleFilterListener, OnReverseSortingListener {

    /** Key used to save the current content mode in bundle */
    public static final String MODE_KEY = "mode_key";
    /** Key used to store podcast URL in intent or bundle */
    public static final String PODCAST_URL_KEY = "podcast_url_key";

    /** The current episode list fragment */
    protected EpisodeListFragment episodeListFragment;
    /** The content mode selection spinner view */
    protected ContentSpinner contentSpinner;

    /** The current episode set (ordered) */
    private SortedSet<Episode> currentEpisodeSet = new TreeSet<>();
    /** Load failed counter for all podcasts selected mode */
    private int loadFailedCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the content mode spinner and add it to the action bar
        contentSpinner = new ContentSpinner(this, this);
        getActionBar().setCustomView(contentSpinner);
        // Make sure the action bar has the right display options set
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
    }

    @Override
    protected void findFragments() {
        super.findFragments();

        // The episode list fragment
        if (episodeListFragment == null)
            episodeListFragment = (EpisodeListFragment) findByTagId(R.string.episode_list_fragment_tag);

        // Make sure the episode fragment know our theme colors
        if (episodeListFragment != null)
            episodeListFragment.setThemeColors(themeColor, lightThemeColor);
    }

    @Override
    protected void registerListeners() {
        super.registerListeners();

        // We have to do this here instead of onCreate since we can only react
        // on the call-backs properly once we have our fragment
        podcastManager.addLoadPodcastListener(this);
        podcastManager.addLoadPodcastLogoListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Persist state of episode metadata
        episodeManager.saveState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        podcastManager.removeLoadPodcastListener(this);
        podcastManager.removeLoadPodcastLogoListener(this);
    }

    @Override
    public void onReverseOrder() {
        selection.setEpisodeOrderReversed(!selection.isEpisodeOrderReversed());

        updateEpisodeListUi();
        updateSortingUi();
    }

    @Override
    public void onToggleFilter() {
        selection.setEpisodeFilterEnabled(!selection.isEpisodeFilterEnabled());

        updateEpisodeListUi();
        updateFilterUi();
    }

    @Override
    public void onToggleLoad() {
        super.onToggleLoad();

        // This might have changed the state of some episodes
        episodeListFragment.refresh();
    }

    @Override
    public void onPodcastSelected(Podcast podcast) {
        selection.setPodcast(podcast);
        selection.setMode(ContentMode.SINGLE_PODCAST);

        this.currentEpisodeSet = new TreeSet<>();
        this.loadFailedCounter = 0;

        switch (view) {
            case SMALL_LANDSCAPE:
                // This will go back to the list view in case we are showing
                // episode details
                getFragmentManager().popBackStackImmediate();
                // There is no break here on purpose, we need to run the code
                // below as well
            case LARGE_PORTRAIT:
            case LARGE_LANDSCAPE:
                // List fragment is visible, make it show progress UI
                episodeListFragment.resetAndSpin();
                // Update other UI
                updateSortingUi();
                updateFilterUi();

                // Load podcast...
                podcastManager.load(podcast);
                // ... and special episodes
                episodeManager.getDownloadsAsync(this, podcast);
                episodeManager.getPlaylistAsync(this, podcast);

                break;
            case SMALL_PORTRAIT:
                // This case should be handled by sub-classes
                break;
        }
    }

    @Override
    public void onAllPodcastsSelected() {
        selection.resetPodcast();
        selection.setMode(ContentMode.ALL_PODCASTS);

        this.currentEpisodeSet = new TreeSet<>();
        this.loadFailedCounter = 0;

        switch (view) {
            case SMALL_LANDSCAPE:
                // This will go back to the list view in case we are showing
                // episode details
                getFragmentManager().popBackStackImmediate();
                // There is no break here on purpose, we need to run the code
                // below as well
            case LARGE_PORTRAIT:
            case LARGE_LANDSCAPE:
                // List fragment is visible, make it show progress UI
                if (podcastManager.size() > 0)
                    episodeListFragment.resetAndSpin();
                else
                    episodeListFragment.resetUi();

                episodeListFragment.setShowPodcastNames(true);
                // Update other UI
                updateSortingUi();
                updateFilterUi();

                // Go load all podcasts...
                for (Podcast podcast : podcastManager.getPodcastList())
                    podcastManager.load(podcast);
                // ... and special episodes
                episodeManager.getDownloadsAsync(this);
                episodeManager.getPlaylistAsync(this);

                // Action bar needs update after loading has started
                updateActionBar();
                break;
            case SMALL_PORTRAIT:
                // This case should be handled by sub-classes
                break;
        }
    }

    @Override
    public void onNoPodcastSelected() {
        selection.resetPodcast();
        selection.setMode(ContentMode.SINGLE_PODCAST);

        currentEpisodeSet.clear();

        if (!view.isSmallPortrait()) {
            // If there is an episode list visible, reset it
            episodeListFragment.selectNone();
            episodeListFragment.resetUi();

            // Update other UI
            updateSortingUi();
            updateFilterUi();
        }
    }

    @Override
    public void onDownloadsSelected() {
        selection.resetPodcast();
        selection.setMode(ContentMode.DOWNLOADS);

        this.currentEpisodeSet = new TreeSet<>();

        switch (view) {
            case SMALL_LANDSCAPE:
                // This will go back to the list view in case we are showing
                // episode details
                getFragmentManager().popBackStackImmediate();
                // There is no break here on purpose, we need to run the code
                // below as well
            case LARGE_PORTRAIT:
            case LARGE_LANDSCAPE:
                // List fragment is visible, make it show progress UI
                episodeListFragment.resetAndSpin();
                episodeListFragment.setShowPodcastNames(true);

                episodeManager.getDownloadsAsync(this);

                break;
            case SMALL_PORTRAIT:
                // This case should be handled by sub-classes
                break;
        }
    }

    @Override
    public void onPlaylistSelected() {
        selection.resetPodcast();
        selection.setMode(ContentMode.PLAYLIST);

        this.currentEpisodeSet = new TreeSet<>(new Comparator<Episode>() {

            @Override
            public int compare(Episode one, Episode another) {
                return episodeManager.getPlaylistPosition(one)
                        - episodeManager.getPlaylistPosition(another);
            }
        });

        switch (view) {
            case SMALL_LANDSCAPE:
                // This will go back to the list view in case we are showing
                // episode details
                getFragmentManager().popBackStackImmediate();
                // There is no break here on purpose, we need to run the code
                // below as well
            case LARGE_PORTRAIT:
            case LARGE_LANDSCAPE:
                // List fragment is visible, make it show progress UI
                episodeListFragment.resetAndSpin();
                episodeListFragment.setShowPodcastNames(true);
                episodeListFragment.setEnableSwipeReorder(true);

                episodeManager.getPlaylistAsync(this);

                break;
            case SMALL_PORTRAIT:
                // This case should be handled by sub-classes
                break;
        }
    }

    @Override
    public void onPodcastLoadProgress(Podcast podcast, Progress progress) {
        try {
            if (selection.isSingle() && podcast.equals(selection.getPodcast()))
                episodeListFragment.showProgress(progress);
        } catch (NullPointerException nep) {
            // When the load progress comes to quickly, the fragment might not
            // be present yet, pass...
        }
    }

    @Override
    public void onPodcastLoaded(Podcast podcast) {
        // Update list fragment to show episode list
        if (selection.isAll() || selection.isSingle() && podcast.equals(selection.getPodcast())) {
            currentEpisodeSet.addAll(podcast.getEpisodes());
            updateEpisodeListUi();
        }

        // We may want to auto-download the latest episode
        if (shouldAutoDownloadLatestEpisode(podcast))
            episodeManager.download(podcast.getEpisodes().get(0));

        // Update other UI
        updateActionBar();
        updateSortingUi();
        updateFilterUi();
    }

    @Override
    public void onPodcastLoadFailed(final Podcast failedPodcast, PodcastLoadError code) {
        this.loadFailedCounter++;

        // The podcast we are waiting for failed to load
        if (selection.isSingle() && failedPodcast.equals(selection.getPodcast())) {
            // Podcast needs authorization
            if (code == PodcastLoadError.AUTH_REQUIRED) {
                // Ask the user for authorization
                final AuthorizationFragment authorizationFragment = new AuthorizationFragment();

                if (failedPodcast.getUsername() != null) {
                    // Create bundle to make dialog aware of username to pre-set
                    final Bundle args = new Bundle();
                    args.putString(USERNAME_PRESET_KEY, failedPodcast.getUsername());
                    authorizationFragment.setArguments(args);
                }

                authorizationFragment.show(getFragmentManager(), AuthorizationFragment.TAG);
            } else {
                // We might at least be able to show special episodes
                if (currentEpisodeSet.size() > 0)
                    updateEpisodeListUi();
                else
                    episodeListFragment.showLoadFailed(code);
            }
        }
        // One of potentially many podcasts failed
        else if (selection.isAll()) {
            // The last podcast failed and we have no episodes at all
            if (podcastManager.getLoadCount() == 0 && currentEpisodeSet.isEmpty())
                episodeListFragment.showLoadAllFailed();
            // One of many podcasts failed to load
            else {
                updateEpisodeListUi();

                // Show toast if this happened for the first time
                if (failedPodcast.getFailedLoadAttemptCount() == 1)
                    showToast(getString(R.string.podcast_load_multiple_error,
                            failedPodcast.getName()));
            }
        }

        // Update other UI
        updateActionBar();
        updateSortingUi();
        updateFilterUi();
    }

    @Override
    public void onDownloadsLoaded(List<Episode> downloads) {
        // If showing downloads or in one of the podcast modes, add downloaded
        // episodes to the episode list
        if (!ContentMode.PLAYLIST.equals(selection.getMode())) {
            currentEpisodeSet.addAll(downloads);

            // Update the UI unless the podcast is still loading
            if (!(selection.isSingle() && podcastManager.isLoading(selection.getPodcast())))
                updateEpisodeListUi();
        }

        // Update other UI
        updateActionBar();
        updateSortingUi();
        updateFilterUi();
    }

    @Override
    public void onPlaylistLoaded(List<Episode> playlist) {
        // If showing playlist or in one of the podcast modes, add enqueued
        // episodes to the episode list
        if (!ContentMode.DOWNLOADS.equals(selection.getMode())) {
            currentEpisodeSet.addAll(playlist);

            // Update the UI unless the podcast is still loading
            if (!(selection.isSingle() && podcastManager.isLoading(selection.getPodcast())))
                updateEpisodeListUi();
        }

        // Update other UI
        updateActionBar();
        updateSortingUi();
        updateFilterUi();
    }

    @Override
    public void onSubmitAuthorization(String username, String password) {
        if (selection.isPodcastSet()) {
            final Podcast podcast = selection.getPodcast();
            podcastManager.setCredentials(podcast, username, password);

            // We need to unselect the podcast here in order to make it
            // selectable again...
            selection.setPodcast(null);

            onPodcastSelected(podcast);
        }
    }

    @Override
    public void onCancelAuthorization() {
        if (selection.isPodcastSet())
            onPodcastLoadFailed(selection.getPodcast(), PodcastLoadError.ACCESS_DENIED);
    }

    @Override
    public void onPodcastLogoLoaded(Podcast podcast) {
        // pass
    }

    @Override
    public void onPodcastLogoLoadFailed(Podcast podcast) {
        // pass
    }

    @Override
    public void onEpisodeSelected(Episode selectedEpisode) {
        onEpisodeSelected(selectedEpisode, false);
    }

    protected void onEpisodeSelected(Episode selectedEpisode, boolean forceReload) {
        if (forceReload || !selectedEpisode.equals(selection.getEpisode())) {
            super.onEpisodeSelected(selectedEpisode);

            if (!view.isSmall())
                // Make sure selection matches in list fragment
                updateEpisodeListSelection();
            else if (view.isSmallPortrait()) {
                // Send intent to open episode as a new activity
                Intent intent = new Intent(this, ShowEpisodeActivity.class);

                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }
    }

    @Override
    public void onNoEpisodeSelected() {
        onNoEpisodeSelected(false);
    }

    protected void onNoEpisodeSelected(boolean forceReload) {
        if (forceReload || selection.getEpisode() != null) {
            super.onNoEpisodeSelected();

            if (episodeListFragment != null)
                episodeListFragment.selectNone();
        }
    }

    @Override
    public void onMoveEpisodeDown(Episode episode) {
        // Only accept movements if in playlist mode
        if (ContentMode.PLAYLIST.equals(selection.getMode())) {
            final int currentPosition = episodeManager.getPlaylistPosition(episode);
            final int playlistLength = episodeManager.getPlaylistSize();
            // Only move episode if it is actually in the playlist
            if (currentPosition >= 0) {
                episodeManager.removeFromPlaylist(episode);
                // If the episode is at the end of the playlist, send it back up
                episodeManager.insertAtPlaylistPosition(episode,
                        currentPosition == playlistLength - 1 ? 0 : currentPosition + 1);
            }

            // Clear and reset the playlist
            currentEpisodeSet.clear();
            onPlaylistLoaded(episodeManager.getPlaylist());
        }
    }

    @Override
    public void onMoveEpisodeUp(Episode episode) {
        // Only accept movements if in playlist mode
        if (ContentMode.PLAYLIST.equals(selection.getMode())) {
            final int currentPosition = episodeManager.getPlaylistPosition(episode);
            // Only move episode if it is actually in the playlist
            if (currentPosition > 0) {
                episodeManager.removeFromPlaylist(episode);
                episodeManager.insertAtPlaylistPosition(episode, currentPosition - 1);
            } // When at top, move to the bottom end
            else if (currentPosition == 0) {
                episodeManager.removeFromPlaylist(episode);
                episodeManager.appendToPlaylist(episode);
            }

            // Clear and reset the playlist
            currentEpisodeSet.clear();
            onPlaylistLoaded(episodeManager.getPlaylist());
        }
    }

    @Override
    public void onDownloadProgress(Episode episode, int percent) {
        if (!view.isSmallPortrait())
            super.onDownloadProgress(episode, percent);

        // Check whether the episode is potentially currently displayed
        if (currentEpisodeSet.contains(episode))
            episodeListFragment.showProgress(episode, percent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);

        if (episodeListFragment != null && SettingsActivity.KEY_THEME_COLOR.equals(key))
            // Make the UI reflect the change
            episodeListFragment.setThemeColors(themeColor, lightThemeColor);
    }

    /**
     * Make sure the episode list selection matches current state.
     */
    protected void updateEpisodeListSelection() {
        if (!view.isSmall())
            // Make sure the episode selection in the list is updated
            episodeListFragment.select(selection.getEpisode());
        else
            episodeListFragment.selectNone();
    }

    /**
     * Update the sorting menu icon visibility.
     */
    protected void updateSortingUi() {
        episodeListFragment.setSortMenuItemVisibility(currentEpisodeSet.size() > 1 &&
                !ContentMode.PLAYLIST.equals(selection.getMode()),
                selection.isEpisodeOrderReversed());
    }

    /**
     * Update the filter menu icon visibility.
     */
    protected void updateFilterUi() {
        episodeListFragment.setFilterMenuItemVisibility(!currentEpisodeSet.isEmpty() &&
                !ContentMode.PLAYLIST.equals(selection.getMode()),
                selection.isEpisodeFilterEnabled());
    }

    @Override
    protected void updateDownloadUi() {
        if (!view.isSmallPortrait())
            super.updateDownloadUi();

        episodeListFragment.refresh();
    }

    @Override
    protected void updatePlaylistUi() {
        if (!view.isSmallPortrait())
            super.updatePlaylistUi();

        episodeListFragment.refresh();
    }

    @Override
    protected void updateStateUi() {
        if (!view.isSmallPortrait())
            super.updateStateUi();

        episodeListFragment.refresh();
    }

    /**
     * Set the action bar subtitle to reflect multiple podcast load progress
     */
    protected void updateActionBarSubtitleOnMultipleLoad() {
        final int podcastCount = podcastManager.size();
        final int loadingPodcastCount = podcastManager.getLoadCount();

        // Load finished for all podcasts and there are episodes
        if (loadingPodcastCount == 0 && !currentEpisodeSet.isEmpty()) {
            final int episodeCount = currentEpisodeSet.size();

            if (episodeCount == 0)
                contentSpinner.setSubtitle(null);
            else
                contentSpinner.setSubtitle(getResources()
                        .getQuantityString(R.plurals.episodes, episodeCount, episodeCount));
        }
        // Load finished but no episodes
        else if (loadingPodcastCount == 0)
            contentSpinner.setSubtitle(getResources()
                    .getQuantityString(R.plurals.podcasts, podcastCount, podcastCount));
        // Load in progress
        else
            contentSpinner.setSubtitle(getString(R.string.podcast_load_multiple_progress,
                    (podcastCount - loadingPodcastCount), podcastCount));
    }

    /**
     * Set the current episode list to show in the episode list fragment using
     * {@link #currentEpisodeSet} as the basis. This will filter and reverse the
     * list as needed.
     */
    private void updateEpisodeListUi() {
        final List<Episode> filteredList = new ArrayList<>(currentEpisodeSet);

        // Further refine the episode list if not in playlist mode
        if (!ContentMode.PLAYLIST.equals(selection.getMode())) {
            // Apply the filter
            if (selection.isEpisodeFilterEnabled()) {
                Iterator<Episode> iterator = filteredList.iterator();

                while (iterator.hasNext())
                    if (episodeManager.getState(iterator.next()))
                        iterator.remove();
            }

            // We might need to reverse the order of our list,
            // but there is no need for sorting since we already come
            // from a sorted set.
            if (selection.isEpisodeOrderReversed())
                Collections.reverse(filteredList);
        }

        // Make sure the episode list fragment shows the right empty view
        if (ContentMode.DOWNLOADS.equals(selection.getMode()))
            episodeListFragment.setEmptyStringId(R.string.downloads_none);
        else if (ContentMode.PLAYLIST.equals(selection.getMode()))
            episodeListFragment.setEmptyStringId(R.string.playlist_empty);
        else if (selection.isEpisodeFilterEnabled()
                && filteredList.isEmpty() && !currentEpisodeSet.isEmpty())
            episodeListFragment.setEmptyStringId(R.string.episodes_no_new);
        else if (selection.isAll())
            episodeListFragment.setEmptyStringId(R.string.episode_none_all_podcasts);
        else
            episodeListFragment.setEmptyStringId(R.string.episode_none);

        // Make sure the episode list fragment shows the right info box
        if (ContentMode.PLAYLIST.equals(selection.getMode()) && filteredList.size() > 1)
            episodeListFragment
                    .setShowTopInfoBox(true, getString(R.string.playlist_swipe_reorder));
        else if (selection.isSingle() && loadFailedCounter > 0)
            episodeListFragment
                    .setShowTopInfoBox(true, getString(R.string.podcast_load_error));
        else if (selection.isAll() && loadFailedCounter > 0)
            episodeListFragment
                    .setShowTopInfoBox(true, getString(R.string.podcast_load_multiple_error_some));
        else if (selection.isEpisodeFilterEnabled()) {
            final int filteredCount = currentEpisodeSet.size() - filteredList.size();

            episodeListFragment.setShowTopInfoBox(
                    filteredCount > 0, getResources().getQuantityString(
                            R.plurals.episodes_filtered, filteredCount, filteredCount));
        }
        else
            episodeListFragment.setShowTopInfoBox(false, null);

        // Finally set the list and make sure selection matches
        episodeListFragment.setEpisodeList(filteredList);
        updateEpisodeListSelection();
    }

    private boolean shouldAutoDownloadLatestEpisode(Podcast podcast) {
        if (podcast == null || podcast.getEpisodeCount() == 0)
            return false;
        else {
            final Episode latestEpisode = podcast.getEpisodes().get(0);

            return PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(SettingsActivity.KEY_AUTO_DOWNLOAD, false)
                    && ((Podcatcher) getApplication()).isOnFastConnection()
                    && !episodeManager.getState(latestEpisode);
        }
    }
}
