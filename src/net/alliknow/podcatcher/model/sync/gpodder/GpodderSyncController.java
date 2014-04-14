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

package net.alliknow.podcatcher.model.sync.gpodder;

import android.content.Context;

import net.alliknow.podcatcher.model.sync.ControllerImpl;

/**
 * A sync controller for the gpodder.net service.
 */
public class GpodderSyncController extends GpodderPodcastListSyncController {

    /**
     * Create new sync controller for the gpodder.net service.
     * 
     * @param context The context to read the configuration from, in particular
     *            {@link #USERNAME_KEY}, {@link #PASSWORD_KEY}, and
     *            {@link #DEVICE_ID_KEY}.
     */
    public GpodderSyncController(Context context) {
        super(context);
    }

    @Override
    public ControllerImpl getImpl() {
        return ControllerImpl.GPODDER;
    }

    @Override
    public boolean isRunning() {
        return super.isRunning();
    }

    @Override
    public void syncSettings() {
        // pass, settings are not yet supported by this sync controller
    }
}
