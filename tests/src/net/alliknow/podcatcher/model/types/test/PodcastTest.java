
package net.alliknow.podcatcher.model.types.test;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Base64;

import net.alliknow.podcatcher.model.test.Utils;
import net.alliknow.podcatcher.model.types.Podcast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

@SuppressWarnings("javadoc")
public class PodcastTest extends InstrumentationTestCase {

    public final void testEquals() {
        assertFalse(new Podcast(null, null).equals(new Podcast(null, null)));

        Podcast tal = new Podcast(null, "http://feeds.thisamericanlife.org/talpodcast");
        assertTrue(tal.equals(tal));
        assertFalse(tal.equals(getInstrumentation()));

        Podcast tal2 = new Podcast(null, "http://feeds.thisamericanlife.org/talpodcast");
        assertTrue(tal.equals(tal2));
    }

    public final void testHashCode() {
        assertTrue(new Podcast(null, null).hashCode() != 0);
    }

    public final void testCompareTo() {
        assertTrue(new Podcast(null, null).compareTo(new Podcast(null, null)) == 0);
        assertEquals("Bla".compareToIgnoreCase("ABZ"),
                new Podcast("Bla", null).compareTo(new Podcast("ABZ", null)));
        assertEquals("Bla".compareToIgnoreCase("bla"),
                new Podcast("Bla", null).compareTo(new Podcast("bla", null)));
        assertEquals("ABC".compareToIgnoreCase("ABZ"),
                new Podcast("ABC", null).compareTo(new Podcast("ABZ", null)));
        assertEquals("ABC".compareToIgnoreCase("ABC"),
                new Podcast("ABC", null).compareTo(new Podcast("ABC", null)));
    }

    public final void testGetName() throws XmlPullParserException, IOException {
        String name = null;
        Podcast podcast = new Podcast(name, null);
        assertEquals(name, podcast.getName());

        name = "";
        podcast = new Podcast(name, null);
        assertEquals(name, podcast.getName());

        name = "Test";
        podcast = new Podcast(name, null);
        assertEquals(name, podcast.getName());

        Podcast tal = new Podcast(null, "http://feeds.thisamericanlife.org/talpodcast");
        assertNull(tal.getName());
        Utils.loadAndWait(tal);
        assertNotNull(tal.getName());
    }

    public final void testToString() {
        String name = null;
        Podcast podcast = new Podcast(name, null);
        assertNotNull(podcast.toString());
    }

    public final void testGetEpisodeNumber() {
        assertEquals(0, new Podcast(null, null).getEpisodeCount());

        Podcast tal = new Podcast("TAL", "http://feeds.thisamericanlife.org/talpodcast");
        assertEquals(0, tal.getEpisodeCount());
        Utils.loadAndWait(tal);
        assertEquals(1, tal.getEpisodeCount());
    }

    public final void testGetEpisodes() {
        assertNotNull(new Podcast(null, null).getEpisodes());

        Podcast tal = new Podcast("TAL", "http://feeds.thisamericanlife.org/talpodcast");
        assertTrue(tal.getEpisodes().isEmpty());
        Utils.loadAndWait(tal);
        assertFalse(tal.getEpisodes().isEmpty());

        Podcast merkel = new Podcast("Merkel",
                "http://www.bundeskanzlerin.de/SiteGlobals/Functions/Webs/BKin/RSSFeed/rssVideoAbo.xml");
        assertTrue(merkel.getEpisodes().isEmpty());
        Utils.loadAndWait(merkel);
        assertFalse(merkel.getEpisodes().isEmpty());
    }

    public final void testGetLogoUrl() {
        assertNull(new Podcast(null, null).getLogoUrl());

        Podcast tal = new Podcast("TAL", "http://feeds.thisamericanlife.org/talpodcast");
        assertNull(tal.getLogoUrl());
        Utils.loadAndWait(tal);
        assertNotNull(tal.getLogoUrl());
    }

    public final void testGetLogo() {
        assertNull(new Podcast(null, null).getLogo());

        Podcast tal = new Podcast("TAL", "http://feeds.thisamericanlife.org/talpodcast");
        assertNull(tal.getLogo());
        assertFalse(tal.isLogoCached());
        Utils.loadAndWait(tal);
        assertNull(tal.getLogo());
        assertFalse(tal.isLogoCached());

        tal.setLogo(Bitmap.createBitmap(10, 10, Config.ALPHA_8));
        assertNotNull(tal.getLogo());
        assertTrue(tal.isLogoCached());
    }

    public final void testLastLoaded() {
        assertNull(new Podcast(null, null).getLastLoaded());

        Podcast tal = new Podcast("TAL", "http://feeds.thisamericanlife.org/talpodcast");
        assertNull(tal.getLastLoaded());
        Utils.loadAndWait(tal);
        assertNotNull(tal.getLastLoaded());
    }

    @MediumTest
    public final void testIsExplicit() {
        assertFalse(new Podcast(null, null).isExplicit());

        Podcast colt = new Podcast("Colt", "http://tsmradio.com/coltcabana/feed");
        assertFalse(colt.isExplicit());
        Utils.loadAndWait(colt);
        assertTrue(colt.isExplicit());

        Podcast tal = new Podcast("TAL", "http://feeds.thisamericanlife.org/talpodcast");
        assertFalse(tal.isExplicit());
        Utils.loadAndWait(tal);
        assertFalse(tal.isExplicit());
    }

    public final void testGetAuth() {
        Podcast podcast = new Podcast(null, null);
        assertNull(podcast.getAuthorization());
        podcast.setUsername("kevin");
        assertNull(podcast.getAuthorization());

        podcast.setUsername(null);
        podcast.setPassword("monkey");
        assertNull(podcast.getAuthorization());

        podcast.setUsername("kevin");
        assertNotNull(podcast.getAuthorization());
        assertEquals(podcast.getAuthorization(),
                "Basic " + Base64.encodeToString("kevin:monkey".getBytes(), Base64.NO_WRAP));
    }

    public final void testToAbsoluteUrl() {
        String url = "http://some-server.com/feeds/podcast.xml";
        Podcast dummy = new Podcast(null, url);

        assertEquals(null, dummy.toAbsoluteUrl(null));
        assertEquals("", dummy.toAbsoluteUrl(""));
        assertEquals(url, dummy.toAbsoluteUrl(url));
        assertEquals("http://some-server.com/feeds/blödsinn", dummy.toAbsoluteUrl("blödsinn"));
        assertEquals("http://some-server.com/bla/image.png", dummy.toAbsoluteUrl("/bla/image.png"));
        assertEquals("http://some-server.com/feeds/bla/image.png",
                dummy.toAbsoluteUrl("bla/image.png"));
    }
}
