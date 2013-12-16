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

package net.alliknow.podcatcher.model.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.alliknow.podcatcher.listeners.OnLoadPlaylistListener;
import net.alliknow.podcatcher.model.EpisodeManager;
import net.alliknow.podcatcher.model.types.Episode;
import net.alliknow.podcatcher.model.types.Podcast;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

/**
 * Get the playlist from the episode manager.
 */
public class LoadPlaylistTask extends AsyncTask<Void, Void, List<Episode>> {

    /** Call back */
    private final WeakReference<OnLoadPlaylistListener> listener;

    /** Filter */
    private final Podcast podcast;

    /**
     * Create new task.
     * 
     * @param listener Callback to be alerted on completion. The listener is
     *            held as a weak reference, so you can safely call this from an
     *            activity without leaking it.
     * @param podcast Podcast to use as a filter. The returned playlist will
     *            only contain episodes that belong to the given podcast.
     *            Setting this to <code>null</code> disables the filter and all
     *            episodes in the playlist are returned.
     */
    public LoadPlaylistTask(OnLoadPlaylistListener listener, Podcast podcast) {
        this.listener = new WeakReference<OnLoadPlaylistListener>(listener);
        this.podcast = podcast;
    }

    @Override
    protected List<Episode> doInBackground(Void... nothing) {
        try {
            // 0. Block if episode metadata not yet available
            EpisodeManager.getInstance().blockUntilEpisodeMetadataIsLoaded();

            // 1. Get the playlist
            final List<Episode> playlist = EpisodeManager.getInstance().getPlaylist();

            // 2. Filter the playlist if podcast is set
            if (podcast != null && !playlist.isEmpty()) {
                final Iterator<Episode> episodes = playlist.iterator();

                while (episodes.hasNext()) {
                    final Episode current = episodes.next();

                    if (!current.getPodcast().equals(podcast))
                        episodes.remove();
                }
            }

            // 3. Return the result
            return playlist;
        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), "Load failed for playlist", e);

            cancel(true);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Episode> playlist) {
        // Playlist available
        final OnLoadPlaylistListener listener = this.listener.get();

        if (listener != null)
            listener.onPlaylistLoaded(playlist);
        else
            Log.w(getClass().getSimpleName(), "Playlist loaded, but no listener attached");
    }
}
