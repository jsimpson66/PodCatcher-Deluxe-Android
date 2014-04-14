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

package net.alliknow.podcatcher.model.sync.dropbox;

import android.content.Context;
import android.util.Log;

import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;

import net.alliknow.podcatcher.model.types.Podcast;

import java.util.Iterator;

/**
 * A sync controller for the Dropbox service dealing with the podcast list.
 */
abstract class DropboxPodcastListSyncController extends DropboxSettingsSyncController {

    /** The subscription table name */
    private static final String SUBSCRIPTION_TABLE = "subscriptions";
    /** The podcast name record in the subscription table */
    private static final String PODCAST_NAME = "name";
    /** The podcast url record in the subscription table */
    private static final String PODCAST_URL = "url";
    /** The podcast user record in the subscription table */
    private static final String PODCAST_USER = "user";
    /** The podcast password record in the subscription table */
    private static final String PODCAST_PASS = "pass";

    /** The subscription table handle */
    private DbxTable podcastTable;
    /** The is first sync flag */
    private boolean isFirstSync = true;

    protected DropboxPodcastListSyncController(Context context) {
        super(context);

        // Since the store might not be available, we might not have a table
        // handle and need to catch NPEs in all actions below.
        if (store != null)
            this.podcastTable = store.getTable(SUBSCRIPTION_TABLE);
    }

    @Override
    public synchronized void syncPodcastList() {
        try {
            // Send out the current podcast list
            if (isFirstSync || SyncMode.SEND_ONLY.equals(mode)) {
                isFirstSync = false;

                for (Podcast podcast : podcastManager.getPodcastList())
                    insertPodcastIfNotPresent(podcast);
            }

            // Sync our changes, this will also cover the second sync mode
            syncStore();
        } catch (DbxException | NullPointerException e) {
            // The NPE might occur if the podcast manager is not up yet
            Log.d(TAG, "Sending out local podcast list failed!", e);
        }
    }

    @Override
    protected void onSyncStoreComplete() {
        super.onSyncStoreComplete();

        // The sync is done. If in receive mode and everybody is prepared,
        // update the local model.
        if (SyncMode.SEND_RECEIVE.equals(mode))
            try {
                // Remove podcasts not in Dropbox data store
                for (Podcast podcast : podcastManager.getPodcastList())
                    if (podcastTable.get(toRecordId(podcast)) == null) {
                        podcastManager.removePodcast(podcastManager.indexOf(podcast));
                        Log.d(TAG, "Remove podcast: " + podcast.getName());
                    }

                // Add podcasts not in local list
                final Iterator<DbxRecord> iterator = podcastTable.query().iterator();

                while (iterator.hasNext()) {
                    final DbxRecord record = iterator.next();

                    // Make sure name and URL are present
                    if (record.hasField(PODCAST_NAME) && record.hasField(PODCAST_URL)) {
                        final Podcast podcast = new Podcast(record.getString(PODCAST_NAME),
                                record.getString(PODCAST_URL));

                        // Only add if not already in the podcast list
                        if (!podcastManager.contains(podcast)) {
                            // Also add auth information if available
                            if (record.hasField(PODCAST_USER) && record.hasField(PODCAST_PASS)) {
                                podcast.setUsername(record.getString(PODCAST_USER));
                                podcast.setPassword(new String(record.getBytes(PODCAST_PASS), utf8));
                            }

                            podcastManager.addPodcast(podcast);
                            Log.d(TAG, "Add podcast: " + podcast.getName());
                        }
                    } else
                        Log.d(TAG, "Skipped incomplete record " + record);
                }
            } catch (DbxException | NullPointerException e) {
                Log.d(TAG, "Updating local podcast list failed!", e);
            }
    }

    @Override
    public void onPodcastAdded(Podcast podcast) {
        try {
            insertPodcastIfNotPresent(podcast);

            syncStore();
        } catch (DbxException | NullPointerException e) {
            Log.d(TAG, "Podcast " + podcast + " cannot be added", e);
        }
    }

    @Override
    public void onPodcastRemoved(Podcast podcast) {
        try {
            final DbxRecord podcastRecord = podcastTable.get(toRecordId(podcast));

            if (podcastRecord != null) {
                podcastRecord.deleteRecord();

                syncStore();
            }
        } catch (DbxException | NullPointerException e) {
            Log.d(TAG, "Podcast " + podcast + " cannot be removed", e);
        }
    }

    private void insertPodcastIfNotPresent(Podcast podcast) throws DbxException {
        final DbxRecord podcastRecord = podcastTable.getOrInsert(toRecordId(podcast));

        podcastRecord.set(PODCAST_URL, podcast.getUrl());
        podcastRecord.set(PODCAST_NAME, podcast.getName());

        if (podcast.getAuthorization() != null) {
            podcastRecord.set(PODCAST_USER, podcast.getUsername());
            // We store the bytes of the password here, so if the a user
            // shows his data store to anyone, it would not be immediately
            // obvious. This is not encryption, of course. Encryption would
            // ask for a shared secret between apps and a podcast password
            // is not considered a high value target justifying the
            // complexity of putting some real crypto here. The Dropbox data
            // store itself and the network transfer are private and guarded
            // by the user's Dropbox account security.
            podcastRecord.set(PODCAST_PASS, podcast.getPassword().getBytes(utf8));
        }
    }

    private String toRecordId(Podcast podcast) {
        return toValidDataStoreId(podcast.getUrl());
    }
}
