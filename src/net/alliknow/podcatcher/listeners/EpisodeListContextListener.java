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

package net.alliknow.podcatcher.listeners;

import static net.alliknow.podcatcher.view.fragments.DeleteDownloadsConfirmationDialogFragment.EPISODE_COUNT_KEY;
import static net.alliknow.podcatcher.view.fragments.DeleteDownloadsConfirmationDialogFragment.TAG;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;

import net.alliknow.podcatcher.R;
import net.alliknow.podcatcher.adapters.EpisodeListAdapter;
import net.alliknow.podcatcher.model.EpisodeManager;
import net.alliknow.podcatcher.model.types.Episode;
import net.alliknow.podcatcher.view.fragments.DeleteDownloadsConfirmationDialogFragment;
import net.alliknow.podcatcher.view.fragments.EpisodeListFragment;

/**
 * Listener for the episode list context mode.
 */
public class EpisodeListContextListener implements MultiChoiceModeListener {

    /** The maximum number of episodes to download at once */
    private static final int MAX_DOWNLOADS = 25;
    /** The maximum number of episodes to enqueue in playlist at once */
    private static final int MAX_TO_PLAYLIST = 50;

    /** The owning fragment */
    private final EpisodeListFragment fragment;
    /** The episode manager handle */
    private final EpisodeManager episodeManager;

    /** The mark new menu item */
    private MenuItem newMenuItem;
    /** The mark old menu item */
    private MenuItem oldMenuItem;
    /** The download menu item */
    private MenuItem downloadMenuItem;
    /** The delete menu item */
    private MenuItem deleteMenuItem;
    /** The add to playlist menu item */
    private MenuItem addToPlaylistMenuItem;
    /** The remove from playlist menu item */
    private MenuItem removeFromPlaylistMenuItem;
    /** The select all menu item */
    private MenuItem selectAllMenuItem;

    /**
     * This is the number of items selected that are not downloaded or currently
     * downloading
     */
    private int deletesTriggered = 0;
    /**
     * Flag to indicate whether the mode should do potentially expensive UI
     * updates when a list item is checked
     */
    private boolean updateUi = true;

    /**
     * Create new listener for the episode list context mode.
     * 
     * @param fragment The episode list fragment to call back to.
     */
    public EpisodeListContextListener(EpisodeListFragment fragment) {
        this.fragment = fragment;
        this.episodeManager = EpisodeManager.getInstance();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.episode_list_context, menu);

        newMenuItem = menu.findItem(R.id.episode_new_contextmenuitem);
        oldMenuItem = menu.findItem(R.id.episode_old_contextmenuitem);
        downloadMenuItem = menu.findItem(R.id.episode_download_contextmenuitem);
        deleteMenuItem = menu.findItem(R.id.episode_remove_contextmenuitem);
        addToPlaylistMenuItem = menu
                .findItem(R.id.episode_add_to_playlist_contextmenuitem);
        removeFromPlaylistMenuItem = menu
                .findItem(R.id.episode_remove_from_playlist_contextmenuitem);
        selectAllMenuItem = menu.findItem(R.id.episode_select_all_contextmenuitem);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        update(mode);

        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        boolean markNew = false;
        boolean append = false;

        final SparseBooleanArray checkedItems = fragment.getListView().getCheckedItemPositions();

        switch (item.getItemId()) {
            case R.id.episode_new_contextmenuitem:
                markNew = true;
                // No break here, code blow should run
            case R.id.episode_old_contextmenuitem:
                for (int position = 0; position < fragment.getListAdapter().getCount(); position++)
                    if (checkedItems.get(position)) {
                        Episode episode = (Episode) fragment.getListAdapter().getItem(position);

                        episodeManager.setState(episode, !markNew);
                    }

                // Action picked, so close the CAB
                mode.finish();
                return true;
            case R.id.episode_download_contextmenuitem:
                for (int position = 0; position < fragment.getListAdapter().getCount(); position++)
                    if (checkedItems.get(position))
                        episodeManager.download(
                                (Episode) fragment.getListAdapter().getItem(position));

                // Action picked, so close the CAB
                mode.finish();
                return true;
            case R.id.episode_remove_contextmenuitem:
                final DeleteDownloadsConfirmationDialogFragment confirmationDialog =
                        new DeleteDownloadsConfirmationDialogFragment();
                // Create bundle to make dialog aware of selection count
                final Bundle args = new Bundle();
                args.putInt(EPISODE_COUNT_KEY, deletesTriggered);
                confirmationDialog.setArguments(args);
                // Set the callback
                confirmationDialog.setListener(new OnDeleteDownloadsConfirmationListener() {

                    @Override
                    public void onConfirmDeletion() {
                        // Go delete the downloads
                        for (int position = 0; position < fragment.getListAdapter().getCount(); position++)
                            if (checkedItems.get(position))
                                episodeManager.deleteDownload(
                                        (Episode) fragment.getListAdapter().getItem(position));

                        // Action picked, so close the CAB
                        mode.finish();
                    }

                    @Override
                    public void onCancelDeletion() {
                        // Nothing to do here
                    }
                });
                // Finally show the dialog
                confirmationDialog.show(fragment.getFragmentManager(), TAG);

                return true;
            case R.id.episode_add_to_playlist_contextmenuitem:
                append = true;
                // No break here, code blow should run
            case R.id.episode_remove_from_playlist_contextmenuitem:
                for (int position = 0; position < fragment.getListAdapter().getCount(); position++)
                    if (checkedItems.get(position)) {
                        Episode episode = (Episode) fragment.getListAdapter().getItem(position);

                        if (append)
                            episodeManager.appendToPlaylist(episode);
                        else
                            episodeManager.removeFromPlaylist(episode);
                    }

                // Action picked, so close the CAB
                mode.finish();
                return true;
            case R.id.episode_select_all_contextmenuitem:
                // Disable expensive UI updates
                updateUi = false;
                for (int index = 0; index < fragment.getListAdapter().getCount(); index++)
                    fragment.getListView().setItemChecked(index, true);

                // Re-enable UI updates
                updateUi = true;
                update(mode);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        ((EpisodeListAdapter) fragment.getListAdapter()).setCheckedPositions(null);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        update(mode);
    }

    private void update(ActionMode mode) {
        // This also avoids crashed when the app has been hidden for some time
        // while the context mode was activated and (parts of) the fragment is
        // (are) gone
        if (updateUi && fragment != null &&
                fragment.getListAdapter() != null && fragment.getListView() != null) {
            updateMenuItems();

            // Let list adapter know which items to mark checked (row color)
            ((EpisodeListAdapter) fragment.getListAdapter()).setCheckedPositions(
                    fragment.getListView().getCheckedItemPositions());

            // Update the mode title text
            final int checkedItemCount = fragment.getListView().getCheckedItemCount();
            mode.setTitle(fragment.getResources()
                    .getQuantityString(R.plurals.episodes, checkedItemCount, checkedItemCount));
        }
    }

    private void updateMenuItems() {
        // Initialize counters for the number of downloads and playlist
        // additions the current selection would trigger
        this.deletesTriggered = 0;
        int downloadsTriggered = 0;
        int playlistAdditions = 0;

        // Make all menu items invisible
        newMenuItem.setVisible(false);
        oldMenuItem.setVisible(false);
        downloadMenuItem.setVisible(false);
        deleteMenuItem.setVisible(false);
        addToPlaylistMenuItem.setVisible(false);
        removeFromPlaylistMenuItem.setVisible(false);

        SparseBooleanArray checkedItems = fragment.getListView().getCheckedItemPositions();

        // Check which option apply to current selection and make corresponding
        // menu items visible
        for (int position = 0; position < fragment.getListAdapter().getCount(); position++) {
            if (checkedItems.get(position)) {
                Episode episode = (Episode) fragment.getListAdapter().getItem(position);

                if (episodeManager.getState(episode))
                    newMenuItem.setVisible(true);
                else
                    oldMenuItem.setVisible(true);

                if (episodeManager.isDownloadingOrDownloaded(episode)) {
                    deletesTriggered++;
                    deleteMenuItem.setVisible(true);
                } else {
                    downloadsTriggered++;
                    downloadMenuItem.setVisible(true);
                }

                if (episodeManager.isInPlaylist(episode))
                    removeFromPlaylistMenuItem.setVisible(true);
                else {
                    playlistAdditions++;
                    addToPlaylistMenuItem.setVisible(true);
                }
            }
        }

        // Do not show some actions if too many episodes are selected
        if (downloadsTriggered > MAX_DOWNLOADS)
            downloadMenuItem.setVisible(false);
        if (playlistAdditions > MAX_TO_PLAYLIST)
            addToPlaylistMenuItem.setVisible(false);

        // Hide the select all item if all items are selected
        selectAllMenuItem.setVisible(fragment.getListView().getCheckedItemCount() !=
                fragment.getListAdapter().getCount());
    }
}
