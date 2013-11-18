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

package net.alliknow.podcatcher.model.types;

import android.text.Html;
import android.util.Log;

import net.alliknow.podcatcher.model.ParserUtils;
import net.alliknow.podcatcher.model.tags.RSS;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The episode type. Each episode represents an item from a podcast's RSS/XML
 * feed. Episodes are created when the podcast is loaded (parsed), you should
 * have no need to create instances yourself.
 */
public class Episode implements Comparable<Episode> {

    /** The podcast this episode is part of */
    private Podcast podcast;
    /**
     * The index (starting with zero at the top of the feed) this episode is in
     * its podcast. -1 means that we do not have this information.
     */
    private final int index;

    /** This episode title */
    private String name;
    /** The episode's online location */
    private URL mediaUrl;
    /** The episode's release date */
    private Date pubDate;
    /** The episode duration */
    private int duration = -1;
    /** The episode's description */
    private String description;
    /** The episode's long content description */
    private String content;

    /**
     * Create a new episode.
     * 
     * @param podcast Podcast this episode belongs to. Cannot be
     *            <code>null</code>.
     * @param index The index of the episode created in the podcast's feed (used
     *            for sorting if the publication date is not available).
     */
    public Episode(Podcast podcast, int index) {
        if (podcast == null)
            throw new NullPointerException("Episode can not have null as the podcast instance!");

        this.podcast = podcast;
        this.index = index;
    }

    /**
     * Create a new episode and set all fields manually.
     * 
     * @param podcast Podcast this episode belongs to. Cannot be
     *            <code>null</code>.
     * @param name Episode name.
     * @param mediaUrl The remote URL of this episode.
     * @param pubDate The publication date.
     * @param description The episode's description.
     */
    public Episode(Podcast podcast, String name, URL mediaUrl, Date pubDate, String description) {
        this(podcast, -1);

        this.name = name;
        this.mediaUrl = mediaUrl;
        this.description = description;
        // Publication date might not be present
        if (pubDate != null)
            this.pubDate = new Date(pubDate.getTime());
    }

    /**
     * @return The owning podcast. This will not be <code>null</code>.
     */
    public Podcast getPodcast() {
        return podcast;
    }

    /**
     * @return The index for this episode object in the podcast's feed. -1 means
     *         that this information is not available.
     */
    public int getPositionInPodcast() {
        return index;
    }

    /**
     * @return The episode's title.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The media content online location.
     */
    public URL getMediaUrl() {
        return mediaUrl;
    }

    /**
     * @return The publication date for this episode.
     */
    public Date getPubDate() {
        if (pubDate == null)
            return null;
        else
            return new Date(pubDate.getTime());
    }

    /**
     * @return The episode's duration as given by the podcast feed converted
     *         into a string 00:00:00. This might not be available and therefore
     *         <code>null</code>.
     */
    public String getDurationString() {
        return duration > 0 ? ParserUtils.formatTime(duration) : null;
    }

    /**
     * @return The episode's duration as given by the podcast feed or -1 if not
     *         available.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @return The description for this episode (if any). Might be
     *         <code>null</code>.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The long content description for this episode from the
     *         content:encoded tag (if any). Might be <code>null</code>.
     */
    public String getLongDescription() {
        return content;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        else if (!(o instanceof Episode))
            return false;

        Episode other = (Episode) o;

        if (mediaUrl == null || other.getMediaUrl() == null)
            return false;
        else
            return mediaUrl.toString().equals(((Episode) o).getMediaUrl().toString());
    }

    @Override
    public int hashCode() {
        return 42 + (mediaUrl == null ? 0 : mediaUrl.hashCode());
    }

    @Override
    public int compareTo(Episode another) {
        // We need to be "consistent with equals": only return 0 (zero) for
        // equal episodes. Failing to do so will cause episodes with equal
        // pubDates to mysteriously disappear when put in a SortedSet.
        int result = 0;
        // We mainly compare by the publication date of the episodes. If these
        // are not available or are equal, we check for their position in the
        // podcast. As a last resort we simply return something <> 0.
        if (this.pubDate == null && another.getPubDate() == null)
            ; // pass, this is handled below
        else if (this.pubDate == null && another.getPubDate() != null)
            result = -1;
        else if (this.pubDate != null && another.getPubDate() == null)
            result = 1;
        else
            result = -pubDate.compareTo(another.getPubDate());

        // This should never be zero unless the episodes are equal, since a
        // podcast might publish two episodes at the same pubDate. If it is
        // (and the episodes are not equal) we use the original order from
        // the feed instead. If all that is not available we simply return
        // a consistent, non-zero integer since failing the do so would remove
        // the episode from sets.
        if (result == 0 && !this.equals(another)) {
            // The pubDates are equal, but episode are not, try index
            if (index >= 0 && another.getPositionInPodcast() >= 0
                    && index != another.getPositionInPodcast())
                result = index - another.getPositionInPodcast();
            // As a last resort return a consistent, non-zero int
            else {
                final int lastResort = this.hashCode() - another.hashCode();
                result = lastResort == 0 ? -1 : lastResort;
            }
        }

        return result;
    }

    /**
     * Read data from an item node in the RSS/XML podcast file and use it to set
     * this episode's fields.
     * 
     * @param parser Podcast file parser, set to the start tag of the item to
     *            read.
     * @throws XmlPullParserException On parsing problems.
     * @throws IOException On I/O problems.
     */
    void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        // Make sure we start at item tag
        parser.require(XmlPullParser.START_TAG, "", RSS.ITEM);

        // Look at all start tags of this item
        while (parser.nextTag() == XmlPullParser.START_TAG) {
            final String tagName = parser.getName();

            // Episode title
            if (tagName.equalsIgnoreCase(RSS.TITLE))
                name = Html.fromHtml(parser.nextText().trim()).toString();
            // Episode media URL
            else if (tagName.equalsIgnoreCase(RSS.ENCLOSURE)) {
                mediaUrl = createMediaUrl(parser.getAttributeValue("", RSS.URL));
                parser.nextText();
            }
            // Episode publication date (2 options)
            else if (tagName.equalsIgnoreCase(RSS.DATE) && pubDate == null)
                pubDate = parsePubDate(parser.nextText());
            else if (tagName.equalsIgnoreCase(RSS.PUBDATE) && pubDate == null)
                pubDate = parsePubDate(parser.nextText());
            // Episode duration
            else if (tagName.equalsIgnoreCase(RSS.DURATION))
                duration = parseDuration(parser.nextText());
            // Episode description
            else if (tagName.equalsIgnoreCase(RSS.DESCRIPTION))
                description = parser.nextText();
            else if (isContentEncodedTag(parser))
                content = parser.nextText();
            // Unneeded node, skip...
            else
                ParserUtils.skipSubTree(parser);
        }

        // Make sure we end at item tag
        parser.require(XmlPullParser.END_TAG, "", RSS.ITEM);
    }

    private boolean isContentEncodedTag(XmlPullParser parser) {
        return RSS.CONTENT_ENCODED.equals(parser.getName()) &&
                RSS.CONTENT_NAMESPACE.equals(parser.getNamespace(parser.getPrefix()));
    }

    private URL createMediaUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            Log.e(getClass().getSimpleName(), "Episode has invalid URL", e);
        }

        return null;
    }

    private Date parsePubDate(String value) {
        try {
            // RSS/XML files use this format for dates
            DateFormat formatter =
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

            return formatter.parse(value);
        } catch (ParseException e) {
            Log.w(getClass().getSimpleName(), "Episode has invalid publication date", e);
        }

        return null;
    }

    private int parseDuration(String durationString) {
        try {
            // Duration simply given as number of seconds
            return Integer.parseInt(durationString);
        } catch (NumberFormatException e) {
            // The duration is given as something like "1:12:34" instead
            try {
                final String[] split = durationString.split(":");

                if (split.length == 2)
                    return Integer.parseInt(split[1]) + Integer.parseInt(split[0]) * 60;
                else if (split.length == 3)
                    return Integer.parseInt(split[2]) + Integer.parseInt(split[1]) * 60
                            + Integer.parseInt(split[0]) * 3600;
                else
                    return -1;
            } catch (NumberFormatException ex) {
                return -1;
            }
        }
    }
}
