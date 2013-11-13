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

import android.graphics.Bitmap;
import android.text.Html;
import android.util.Base64;
import android.util.Log;

import net.alliknow.podcatcher.model.ParserUtils;
import net.alliknow.podcatcher.model.tags.RSS;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The podcast type. This represents the most important type in the podcatcher
 * application. To create a podcast, give its name and an online location to
 * load its RSS/XML file from. Call {@link #parse(XmlPullParser)} before you
 * expect any additional data to show up.
 */
public class Podcast implements Comparable<Podcast> {

    /** Name of the podcast */
    private String name;
    /** Location of the podcast's RSS file */
    private URL url;

    /** The podcasts list of episodes */
    private List<Episode> episodes = new ArrayList<Episode>();

    /** Podcast's description */
    private String description;
    /** Broadcast language */
    private Language language;
    /** Podcast genre */
    private Genre genre;
    /** Podcast media type */
    private MediaType mediaType;

    /** Whether the podcast contains explicit language */
    private boolean isExplicit = false;

    /** Username for http authorization */
    private String username;
    /** Password for http authorization */
    private String password;

    /** The podcast's image (logo) location */
    private URL logoUrl;
    /** The cached logo bitmap */
    private Bitmap logo;

    /** The point in time when the RSS file as last been set */
    private Date updated;

    /**
     * Create a new podcast by name and RSS file location. The name will not be
     * read from the file, but remains as given (unless you give
     * <code>null</code> as the name). All other data on the podcast will only
     * be available after {@link #parse(XmlPullParser)} was called.
     * 
     * @param name The podcast's name, if you give <code>null</code> the name
     *            will be read from the RSS file (if set afterwards).
     * @param url The location of the podcast's RSS file.
     * @see #parse(XmlPullParser)
     */
    public Podcast(String name, URL url) {
        this.name = name;
        this.url = url;
    }

    /**
     * @return The podcast's name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The podcast's online location.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The language.
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * @param language The language to set.
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * @return The genre.
     */
    public Genre getGenre() {
        return genre;
    }

    /**
     * @param genre The genre to set.
     */
    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    /**
     * @return The mediaType.
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * @param mediaType The mediaType to set.
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * @return Whether the podcast is considered explicit.
     */
    public boolean isExplicit() {
        return isExplicit;
    }

    /**
     * @param isExplicit The flag to set.
     */
    public void setExplicit(boolean isExplicit) {
        this.isExplicit = isExplicit;
    }

    /**
     * @return The user name for this podcast. Maybe <code>null</code> if
     *         unknown or unneeded.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the user name for this podcast.
     * 
     * @param username Name to use. Give <code>null</code> to reset.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return The password for this podcast. Maybe <code>null</code> if unknown
     *         or unneeded.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password for this podcast.
     * 
     * @param password Password to use. Give <code>null</code> to reset.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Authorization string to be used as a HTTP request header or
     *         <code>null</code> if username or password are not set.
     */
    public String getAuthorization() {
        String result = null;

        if (username != null && password != null) {
            final String userpass = username + ":" + password;
            result = "Basic " + Base64.encodeToString(userpass.getBytes(), Base64.NO_WRAP);
        }

        return result;
    }

    /**
     * Find and return all episodes for this podcast. Will never return
     * <code>null</code> but an empty list when encountering problems. Set and
     * parse the RSS file before expecting any results.
     * 
     * @return The list of episodes as listed in the feed.
     * @see #parse(XmlPullParser)
     */
    public List<Episode> getEpisodes() {
        // Need to return a copy, so nobody can change this on us and changes
        // made in the model do not make problems in the UI
        return new ArrayList<Episode>(episodes);
    }

    /**
     * @return The number of episode for this podcast (always >= 0).
     */
    public int getEpisodeNumber() {
        return episodes.size();
    }

    /**
     * Find and return the podcast's image location (logo). Only works after RSS
     * file is set.
     * 
     * @return URL pointing at the logo location.
     * @see #parse(XmlPullParser)
     */
    public URL getLogoUrl() {
        return logoUrl;
    }

    /**
     * Get a cached logo for this podcast.
     * 
     * @return The cached logo if it was previously set using
     *         <code>setLogo()</code>, <code>null</code> otherwise.
     */
    public Bitmap getLogo() {
        return logo;
    }

    /**
     * Cache the podcast given.
     * 
     * @param logo Logo to use for this podcast.
     */
    public void setLogo(Bitmap logo) {
        this.logo = logo;
    }

    /**
     * Set the RSS file parser representing this podcast. This is were the
     * object gets its information from. Many of its methods will not return
     * valid results unless this method was called. Calling this method also
     * resets all information read earlier.
     * 
     * @param parser Parser used to read the RSS/XML file.
     * @throws IOException If we encounter problems read the file.
     * @throws XmlPullParserException On parsing errors.
     */
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        // Reset state
        episodes.clear();

        // Start parsing
        int eventType = parser.next();
        int episodeIndex = 0;

        // Read complete document
        while (eventType != XmlPullParser.END_DOCUMENT) {
            // We only need start tags here
            if (eventType == XmlPullParser.START_TAG) {
                String tagName = parser.getName();

                // Podcast name found
                if (tagName.equalsIgnoreCase(RSS.TITLE))
                    loadName(parser);
                // Image found
                else if (tagName.equalsIgnoreCase(RSS.IMAGE))
                    loadImage(parser);
                // Thumbnail found
                else if (tagName.equalsIgnoreCase(RSS.THUMBNAIL))
                    loadThumbnail(parser);
                // Episode found
                else if (tagName.equalsIgnoreCase(RSS.ITEM))
                    loadEpisode(parser, episodeIndex++);
            }

            // Done, get next parsing event
            eventType = parser.next();
        }

        // Parsing completed without errors, mark as updated
        updated = new Date();
    }

    /**
     * @return The point in time this podcast has last been loaded or
     *         <code>null</code> iff it had not been loaded before.
     */
    public Date getLastLoaded() {
        if (updated == null)
            return null;
        else
            return new Date(updated.getTime());
    }

    @Override
    public String toString() {
        if (name == null)
            return "Unnamed podcast";
        if (url == null)
            return name;
        else
            return name + " at " + url.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        else if (!(o instanceof Podcast))
            return false;

        Podcast other = (Podcast) o;

        if (url == null || other.getUrl() == null)
            return false;
        else
            return url.toString().equals(((Podcast) o).getUrl().toString());
    }

    @Override
    public int hashCode() {
        return url == null ? 0 : url.toString().hashCode();
    }

    @Override
    public int compareTo(Podcast another) {
        if (this.name == null && another.getName() == null)
            return 0;
        else if (this.name == null && another.getName() != null)
            return -1;
        else if (this.name != null && another.getName() == null)
            return 1;
        else
            return getName().compareToIgnoreCase(another.getName());
    }

    private void loadName(XmlPullParser parser) throws XmlPullParserException, IOException {
        // Only update the name if not set
        if (name == null)
            name = Html.fromHtml(parser.nextText().trim()).toString();
    }

    private void loadImage(XmlPullParser parser) throws XmlPullParserException, IOException {
        try {
            // HREF attribute used?
            if (parser.getAttributeValue("", RSS.HREF) != null)
                logoUrl = createLogoUrl(parser.getAttributeValue("", RSS.HREF));
            // URL tag used! We do not override any previous setting, because
            // the HREF is from the <itunes:image> tag which tends to have
            // better pics.
            else if (logoUrl == null) {
                // Make sure we start at image tag
                parser.require(XmlPullParser.START_TAG, "", RSS.IMAGE);

                // Look at all start tags of this image
                while (parser.nextTag() == XmlPullParser.START_TAG) {
                    // URL tag found
                    if (parser.getName().equalsIgnoreCase(RSS.URL))
                        logoUrl = createLogoUrl(parser.nextText());
                    // Unneeded node, skip...
                    else
                        ParserUtils.skipSubTree(parser);
                }

                // Make sure we end at image tag
                parser.require(XmlPullParser.END_TAG, "", RSS.IMAGE);
            }
        } catch (XmlPullParserException e) {
            // The podcast logo information could not be read from the RSS file,
            // skip...
        }
    }

    private void loadThumbnail(XmlPullParser parser) {
        // Some podcasts use thumbnails instead of images
        logoUrl = createLogoUrl(parser.getAttributeValue("", RSS.URL));
    }

    private URL createLogoUrl(String nodeValue) {
        try {
            return new URL(nodeValue);
        } catch (MalformedURLException e) {
            Log.e(getClass().getSimpleName(), "Podcast has invalid logo URL", e);
        }

        return null;
    }

    private void loadEpisode(XmlPullParser parser, int index)
            throws XmlPullParserException, IOException {
        // Create episode and parse the data
        Episode newEpisode = new Episode(this, index);
        newEpisode.parse(parser);

        // Only add if there is some actual content to play
        if (newEpisode.getMediaUrl() != null)
            episodes.add(newEpisode);
    }
}
