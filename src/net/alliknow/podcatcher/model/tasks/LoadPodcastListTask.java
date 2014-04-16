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

package net.alliknow.podcatcher.model.tasks;

import static net.alliknow.podcatcher.model.PodcastManager.OPML_FILENAME;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;

import net.alliknow.podcatcher.listeners.OnLoadPodcastListListener;
import net.alliknow.podcatcher.model.PodcastManager;
import net.alliknow.podcatcher.model.tags.OPML;
import net.alliknow.podcatcher.model.types.Podcast;
import net.alliknow.podcatcher.model.types.Progress;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads the default podcast list from the file system asynchronously. Use
 * {@link OnLoadPodcastListListener} as a call-back for this task.
 */
public class LoadPodcastListTask extends AsyncTask<Void, Progress, List<Podcast>> {

    /** The listener callback */
    private final OnLoadPodcastListListener listener;
    /** Our context */
    private final Context context;

    /** The file that we read from. */
    protected Uri importFile;
    /** The exception that might have been occurred */
    protected Exception exception;

    /**
     * Create new task.
     * 
     * @param context Context to read file from (not <code>null</code>).
     * @param listener Callback to be alerted on completion. Could be
     *            <code>null</code>, but then nobody would ever know that this
     *            task finished.
     * @see PodcastManager#OPML_FILENAME
     */
    public LoadPodcastListTask(Context context, OnLoadPodcastListListener listener) {
        this.context = context;
        this.listener = listener;

        // This is the default case, read from the app's private dir
        this.importFile = Uri.fromFile(new File(context.getFilesDir(), OPML_FILENAME));
    }

    /**
     * Define where the task should read the podcast OPML file from. Not setting
     * this (or given <code>null</code> here) will result in the file being read
     * from the private app directory.
     * 
     * @param location The location to read from.
     */
    public void setCustomLocation(Uri location) {
        this.importFile = location;
    }

    @Override
    protected List<Podcast> doInBackground(Void... params) {
        // Create resulting data structure and file stream
        final List<Podcast> result = new ArrayList<>();
        InputStream fileStream = null;

        try {
            // 1. Open the OPML file
            fileStream = context.getContentResolver().openInputStream(importFile);

            // 2. Build parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            // Create the parser to use
            XmlPullParser parser = factory.newPullParser();

            // 3. Parse the OPML file
            parser.setInput(fileStream, PodcastManager.OPML_FILE_ENCODING);
            int eventType = parser.next();

            // Read complete document
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // We only need start tags here
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();

                    // Podcast found, add it
                    if (tagName.equalsIgnoreCase(OPML.OUTLINE)) {
                        final Podcast listItem = createPodcast(parser);

                        if (listItem != null)
                            result.add(listItem);
                    }
                }

                // Done, get next parsing event
                eventType = parser.next();
            }

            // 4. Sort
            Collections.sort(result);
        } catch (Exception ex) {
            this.exception = ex;

            cancel(true);
        } finally {
            // Make sure we close the file stream
            if (fileStream != null)
                try {
                    fileStream.close();
                } catch (IOException e) {
                    // Nothing we can do here
                }
        }

        return result;
    }

    @Override
    protected void onPostExecute(List<Podcast> result) {
        if (listener != null)
            listener.onPodcastListLoaded(result, importFile);
    }

    @Override
    protected void onCancelled(List<Podcast> result) {
        if (listener != null)
            listener.onPodcastListLoadFailed(importFile, exception);
    }

    /**
     * Read podcast information from the given parser and create a new podcast
     * object for it.
     * 
     * @param parser Parser to read from. Has to be set to the OPML outline
     *            start tag.
     * @return A new Podcast instance with name and URL set. If any error
     *         occurs, <code>null</code> is returned.
     */
    private Podcast createPodcast(XmlPullParser parser) {
        Podcast result = null;

        try {
            // Make sure we start at item tag
            parser.require(XmlPullParser.START_TAG, "", OPML.OUTLINE);
            // Get the podcast name
            String name = parser.getAttributeValue("", OPML.TEXT);
            // Make sure podcast name looks good
            if (name.equals("null"))
                name = null;
            else
                name = Html.fromHtml(name).toString();

            // Create the podcast
            result = new Podcast(name, parser.getAttributeValue("", OPML.XMLURL));

            // Set authorization information
            result.setUsername(parser.getAttributeValue("", OPML.EXTRA_USER));
            result.setPassword(parser.getAttributeValue("", OPML.EXTRA_PASS));
        } catch (XmlPullParserException e) {
            /* Bad outline, skip */
        } catch (IOException e) {
            /* pass */
        }

        return result;
    }
}
