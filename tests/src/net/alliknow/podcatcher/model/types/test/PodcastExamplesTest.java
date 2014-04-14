
package net.alliknow.podcatcher.model.types.test;

import net.alliknow.podcatcher.model.types.Podcast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

@SuppressWarnings("javadoc")
public class PodcastExamplesTest extends SuggestionsAsExamplesTest {

    @Override
    protected void setUp() throws Exception {
        sampleSize = 25;

        super.setUp();
    }

    public final void testEquals() {
        for (Podcast podcast : examplePodcasts) {
            assertFalse(podcast.equals(null));
            assertTrue(podcast.equals(podcast));
            assertFalse(podcast.equals(new Object()));
            assertFalse(podcast.equals(new Podcast(null, null)));
            assertTrue(podcast.equals(new Podcast(null, podcast.getUrl())));

            for (Podcast other : examplePodcasts) {
                if (podcast.getUrl().equals(other.getUrl()))
                    assertTrue(podcast.equals(other));
                else
                    assertFalse(podcast.equals(other));
            }
        }
    }

    public final void testHashCode() {
        for (Podcast podcast : examplePodcasts) {
            assertTrue(podcast.hashCode() != 0);

            for (Podcast other : examplePodcasts) {
                if (podcast.equals(other))
                    assertTrue(podcast.hashCode() == other.hashCode());
                else
                    assertTrue(podcast.hashCode() != other.hashCode());
            }
        }
    }

    public final void testCompareTo() {
        for (Podcast podcast : examplePodcasts) {
            for (Podcast other : examplePodcasts) {
                if (podcast.equals(other))
                    assertEquals(0, podcast.compareTo(other));
                else
                    assertTrue(podcast.compareTo(other) != 0);
            }
        }
    }

    public final void testGetName() throws XmlPullParserException, IOException {
        for (Podcast podcast : examplePodcasts) {
            assertNotNull("Podcast " + podcast.getName() + " has no name!", podcast.getName());
        }
    }

    public final void testGetEpisodeNumber() {
        for (Podcast podcast : examplePodcasts)
            assertTrue("Podcast " + podcast.getName() + " has no episodes!",
                    podcast.getEpisodeCount() > 0);
    }

    public final void testGetEpisodes() {
        for (Podcast podcast : examplePodcasts)
            assertNotNull("Podcast " + podcast.getName() + " has no episode list!",
                    podcast.getEpisodes());
    }

    public final void testGetLogoUrl() {
        for (Podcast podcast : examplePodcasts)
            assertNotNull("Podcast " + podcast.getName() + " has no logo!", podcast.getLogoUrl());
    }

    public final void testLastLoaded() {
        for (Podcast podcast : examplePodcasts)
            assertNotNull("Podcast " + podcast.getName() + " has no last loaded date!",
                    podcast.getLastLoaded());
    }
}
