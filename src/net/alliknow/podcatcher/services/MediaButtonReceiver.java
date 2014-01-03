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

package net.alliknow.podcatcher.services;

import static net.alliknow.podcatcher.services.PlayEpisodeService.ACTION_FORWARD;
import static net.alliknow.podcatcher.services.PlayEpisodeService.ACTION_PAUSE;
import static net.alliknow.podcatcher.services.PlayEpisodeService.ACTION_PLAY;
import static net.alliknow.podcatcher.services.PlayEpisodeService.ACTION_PREVIOUS;
import static net.alliknow.podcatcher.services.PlayEpisodeService.ACTION_REWIND;
import static net.alliknow.podcatcher.services.PlayEpisodeService.ACTION_SKIP;
import static net.alliknow.podcatcher.services.PlayEpisodeService.ACTION_STOP;
import static net.alliknow.podcatcher.services.PlayEpisodeService.ACTION_TOGGLE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * Our media button receiver. Handles media button presses (e.g. from headsets)
 * and send the appropriate intent to the episode playback service.
 */
public class MediaButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Only react if this actually is a media button event
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            try {
                // Find out if the event was a button press
                KeyEvent event = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    // Send appropriate action to the episode playback service.
                    // Since we are only registered if the service is running,
                    // there
                    // should not be a SecurityException problem (see
                    // BecomingNoisyReceiver).
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_HEADSETHOOK:
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            context.startService(new Intent(ACTION_TOGGLE));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            context.startService(new Intent(ACTION_PLAY));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            context.startService(new Intent(ACTION_PAUSE));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            context.startService(new Intent(ACTION_PREVIOUS));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            context.startService(new Intent(ACTION_SKIP));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_REWIND:
                            context.startService(new Intent(ACTION_REWIND));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                            context.startService(new Intent(ACTION_FORWARD));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_STOP:
                        case KeyEvent.KEYCODE_MEDIA_EJECT:
                            context.startService(new Intent(ACTION_STOP));
                            break;
                    }
            } catch (SecurityException se) {
                // This might happen if called from the outside since our
                // service is not exported, just do nothing.
            }
        }
    }
}
