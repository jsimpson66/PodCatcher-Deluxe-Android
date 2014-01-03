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

package net.alliknow.podcatcher.listeners;

/**
 * Interface definition for a callback to be invoked when a podcast is added.
 */
public interface OnAddPodcastListener {

    /**
     * Called on listener when podcast url is given.
     * 
     * @param podcastUrl Podcast URL spec to add.
     */
    public void onAddPodcast(String podcastUrl);

    /**
     * Called on listener if the user wants to see suggestions for podcasts to
     * add.
     */
    public void onShowSuggestions();

    /**
     * Called on listener if the user wants to import an OPML file.
     */
    public void onImportOpml();
}
