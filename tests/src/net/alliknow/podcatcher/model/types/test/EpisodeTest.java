
package net.alliknow.podcatcher.model.types.test;

import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;

import net.alliknow.podcatcher.model.types.Episode;
import net.alliknow.podcatcher.model.types.Podcast;

import java.util.Date;

@SuppressWarnings("javadoc")
public class EpisodeTest extends TypeTest {

    class Episode2 extends Episode {

        public Episode2(Podcast podcast, int index) {
            super(podcast, index);
        }

        public void setPubDate(Date date) {
            this.pubDate = date;
        }
    }

    @Override
    protected void setUp() throws Exception {
        sampleSize = 20;

        super.setUp();

        // if (examplePodcasts == null) {
        // examplePodcasts = new ArrayList<Podcast>();
        //
        // Podcast extra = new Podcast("Skip Heinzig",
        // new URL("http://skipheitzig.com/podcast/tv"));
        // Utils.loadAndWait(extra);
        // examplePodcasts.add(extra);
        // }
    }

    @SmallTest
    public final void testEquals() {
        for (Podcast podcast : examplePodcasts) {
            Episode first = null;
            for (Episode episode : podcast.getEpisodes()) {
                assertFalse(episode.equals(null));
                assertTrue(episode.equals(episode));
                assertFalse(episode.equals(new Object()));
                assertFalse(episode.equals(new Podcast(null, null)));
                assertFalse(episode.equals(podcast));

                if (podcast.getEpisodes().indexOf(episode) == 0)
                    first = episode;
                else if (first != null)
                    assertFalse(first.equals(episode));
            }
        }
    }

    @SmallTest
    public final void testHashCode() {
        for (Podcast podcast : examplePodcasts) {

            Episode first = null;
            for (Episode episode : podcast.getEpisodes()) {
                assertTrue(episode.hashCode() != 0);

                if (podcast.getEpisodes().indexOf(episode) == 0)
                    first = episode;
                else if (first != null)
                    assertFalse(first.hashCode() == episode.hashCode());
            }
        }
    }

    @LargeTest
    public final void testCompareTo() {
        for (Podcast podcast : examplePodcasts)
            for (Episode episode : podcast.getEpisodes())
                for (Podcast otherPodcast : examplePodcasts)
                    for (Episode otherEpisode : otherPodcast.getEpisodes())
                        assertEquals(
                                "LHS: " + episode.getName() + " RHS: " + otherEpisode.getName(),
                                episode.equals(otherEpisode),
                                episode.compareTo(otherEpisode) == 0);

        // "LHS: " + episode.getName() + "/"
        // + episode.getPodcast().getName() + "/"
        // + episode.getPubDate()
        // + " RHS: " + otherEpisode.getName() + "/"
        // + otherEpisode.getPodcast().getName() + "/"
        // + otherEpisode.getPubDate(),
    }

    @SmallTest
    public final void testCompareTo2() {
        Date one = new Date(100);
        Date same = new Date(100);
        Date other = new Date(101);

        Podcast dummy = new Podcast(null, null);
        Episode2 first = new Episode2(dummy, 1);
        first.setPubDate(one);
        Episode2 second = new Episode2(dummy, 2);

        assertTrue(first.compareTo(second) > 0);
        assertTrue(second.compareTo(first) < 0);

        second.setPubDate(same);
        Episode2 third = new Episode2(dummy, 1);
        third.setPubDate(other);

        assertTrue(first.compareTo(second) == 0);
        assertTrue(second.compareTo(first) == 0);
        assertEquals(one.compareTo(other), -first.compareTo(third));
        assertEquals(other.compareTo(one), -third.compareTo(first));
    }

    @SmallTest
    public final void testGetName() {
        for (Podcast podcast : examplePodcasts) {
            for (Episode episode : podcast.getEpisodes()) {
                assertNotNull(episode.getName());
                assertTrue(episode.getName().length() > 0);
                assertFalse(episode.getName().contains("\n"));
                assertFalse(episode.getName().contains("\r"));
                assertFalse(episode.getName().contains("\r\n"));
            }
        }
    }

    @SmallTest
    public final void testGetMediaUrl() {
        for (Podcast podcast : examplePodcasts) {
            for (Episode episode : podcast.getEpisodes()) {
                assertNotNull(episode.getMediaUrl());
            }
        }
    }

    @SmallTest
    public final void testGetPodcastName() {
        for (Podcast podcast : examplePodcasts) {
            for (Episode episode : podcast.getEpisodes()) {
                assertEquals(episode.getPodcast().getName(), podcast.getName());
            }
        }
    }

    @SmallTest
    public final void testGetPubDate() {
        for (Podcast podcast : examplePodcasts) {
            for (Episode episode : podcast.getEpisodes()) {
                final String episodeName = "Episode " + episode.getName() + " in Podcast "
                        + episode.getPodcast().getName();

                assertNotNull(episodeName, episode.getPubDate());
                assertTrue(episodeName, episode.getPubDate().after(new Date(0)));
                // No more then one week into the future
                assertTrue(episodeName, episode.getPubDate().before(
                        new Date(new Date().getTime() + 1000 * 60 * 60 * 24 * 7)));
            }
        }
    }

    @SmallTest
    public final void testGetDuration() {
        for (Podcast podcast : examplePodcasts)
            for (Episode episode : podcast.getEpisodes()) {
                final String episodeName = "Episode " + episode.getName() + " in Podcast "
                        + episode.getPodcast().getName();

                assertEquals(episodeName, episode.getDuration() > 0,
                        episode.getDurationString() != null);
                assertFalse(episodeName, episode.getDuration() == 0);
            }
    }
}
