/** Copyright 2012, 2013 Kevin Hausmann
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

import android.os.Bundle;
import android.view.MenuItem;

import net.alliknow.podcatcher.model.types.Podcast;
import net.alliknow.podcatcher.view.fragments.EpisodeListFragment;

/**
 * Activity to show only the episode list and possibly the player. Used in small
 * portrait view mode only.
 */
public class ShowEpisodeListActivity extends EpisodeListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // In large or landscape layouts we do not need this activity at
        // all, so finish it. Also we need to avoid the case where the Android
        // system recreates this activity after the app has been killed and the
        // activity would show up with an endless progress indication because
        // there is no content selected.
        if (!view.isSmallPortrait() || (!selection.isAll() && !selection.isPodcastSet()))
            finish();
        else {
            // 1. Set the content view
            setContentView(R.layout.main);
            // 2. Set, find, create the fragments
            findFragments();
            // During initial setup, plug in the episode list fragment.
            if (savedInstanceState == null && episodeListFragment == null) {
                episodeListFragment = new EpisodeListFragment();
                episodeListFragment.setThemeColors(themeColor, lightThemeColor);

                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.content, episodeListFragment,
                                getString(R.string.episode_list_fragment_tag))
                        .commit();
            }

            // 3. Register the listeners needed to function as a controller
            registerListeners();

            // 4. Act according to selection
            if (selection.isAll())
                onAllPodcastsSelected();
            else if (selection.isSingle() && selection.isPodcastSet())
                onPodcastSelected(selection.getPodcast());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Unselect podcast
                selection.resetPodcast();

                // This is called when the Home (Up) button is pressed
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Unselect podcast
        selection.resetPodcast();

        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void onPodcastSelected(Podcast podcast) {
        super.onPodcastSelected(podcast);

        // Init the list view...
        episodeListFragment.resetAndSpin();
        // ...and start loading
        podcastManager.load(podcast);
    }

    @Override
    public void onAllPodcastsSelected() {
        super.onAllPodcastsSelected();

        // Init the list view...
        if (podcastManager.size() > 0)
            episodeListFragment.resetAndSpin();
        else
            episodeListFragment.resetUi();
        episodeListFragment.setShowPodcastNames(true);
        // ...and go get the data
        for (Podcast podcast : podcastManager.getPodcastList())
            podcastManager.load(podcast);

        updateActionBar();
    }

    @Override
    public void onPodcastLoaded(Podcast podcast) {
        super.onPodcastLoaded(podcast);

        // We might want to show the progress bar on top of the list
        if (selection.isAll())
            episodeListFragment.setShowTopProgress(podcastManager.getLoadCount() > 0);
    }

    @Override
    public void onPodcastLoadFailed(Podcast failedPodcast) {
        super.onPodcastLoadFailed(failedPodcast);

        // We might want to show the progress bar on top of the list
        if (selection.isAll())
            episodeListFragment.setShowTopProgress(podcastManager.getLoadCount() > 0);
    }

    @Override
    protected void updateActionBar() {
        contentSpinner.setTitle(getString(R.string.app_name));

        // Single podcast selected
        if (selection.isPodcastSet()) {
            final int episodeCount = selection.getPodcast().getEpisodeNumber();

            if (episodeCount == 0)
                contentSpinner.setSubtitle(null);
            else
                contentSpinner.setSubtitle(getResources()
                        .getQuantityString(R.plurals.episodes, episodeCount, episodeCount));

        } // Multiple podcast mode
        else if (selection.isAll())
            updateActionBarSubtitleOnMultipleLoad();

        // Enable navigation
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void updatePlayerUi() {
        super.updatePlayerUi();

        // Make sure to show episode title in player
        playerFragment.setLoadMenuItemVisibility(false, false);
        playerFragment.setPlayerTitleVisibility(true);
    }
}
