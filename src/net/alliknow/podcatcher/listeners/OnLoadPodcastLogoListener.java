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

import net.alliknow.podcatcher.model.types.Podcast;

/**
 * Interface definition for a callback to be invoked when a podcast logo is
 * loaded.
 */
public interface OnLoadPodcastLogoListener {

    /**
     * Called on completion.
     * 
     * @param podcast The podcast we are loading the logo for.
     */
    public void onPodcastLogoLoaded(Podcast podcast);

    /**
     * Called when loading the podcast logo failed.
     * 
     * @param podcast Podcast logo could not be loaded for.
     */
    public void onPodcastLogoLoadFailed(Podcast podcast);
}
