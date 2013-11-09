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

package net.alliknow.podcatcher.view.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

import net.alliknow.podcatcher.AddPodcastActivity;
import net.alliknow.podcatcher.R;
import net.alliknow.podcatcher.adapters.PodcastListAdapter;
import net.alliknow.podcatcher.listeners.OnSelectPodcastListener;
import net.alliknow.podcatcher.listeners.PodcastListContextListener;
import net.alliknow.podcatcher.model.types.Podcast;
import net.alliknow.podcatcher.model.types.Progress;
import net.alliknow.podcatcher.view.PodcastListItemView;

import java.util.Collections;
import java.util.List;

/**
 * List fragment to display the list of podcasts.
 */
public class PodcastListFragment extends PodcatcherListFragment {

    /** The listener call-back to alert on podcast selection */
    private OnSelectPodcastListener podcastSelectionListener;

    /** The list of podcasts currently shown */
    private List<Podcast> currentPodcastList;

    /** The logo view */
    private ImageView logoView;
    /** The logo view height used to make it square */
    private int logoViewHeight;
    /** The current logo view mode */
    private LogoViewMode logoViewMode = LogoViewMode.SMALL;

    /** The options available for the logo view */
    public static enum LogoViewMode {
        /**
         * Do not show the podcast logo
         */
        NONE,

        /**
         * Show small podcast logo in list item
         */
        SMALL,

        /**
         * Show large podcast logo at the bottom
         */
        LARGE
    };

    /** Flag for animation currently running */
    private boolean animating = false;
    /** The podcast add and remove animation duration */
    private int addRemoveDuration;
    /** The logo view slide animation duration */
    private int slideDuration;

    /** Status flag indicating that our view is created */
    private boolean viewCreated = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure our listener is present
        try {
            this.podcastSelectionListener = (OnSelectPodcastListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSelectPodcastListener");
        }

        this.addRemoveDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        this.slideDuration = addRemoveDuration;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        // Make the UI show to be working once it is up
        showProgress = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.podcast_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find logo view member handle
        logoView = (ImageView) view.findViewById(R.id.podcast_image);
        // ... and make sure the logo view is square
        logoView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        // We only need this once
                        logoView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        // Store value for future use in animations
                        logoViewHeight = getView().getWidth();
                        // Update new layout params
                        final LayoutParams layoutParams = logoView.getLayoutParams();
                        layoutParams.height = logoViewHeight;
                        logoView.setLayoutParams(layoutParams);
                    }
                });

        // Set list choice listener (context action mode)
        getListView().setMultiChoiceModeListener(new PodcastListContextListener(this));

        // Consider the view created successfully beyond this point
        viewCreated = true;

        // This will make sure we show the right information once the view
        // controls are established (the list might have been set earlier)
        if (currentPodcastList != null)
            setPodcastList(currentPodcastList);

        // Make sure logo view mode is set
        updateLogo(logoViewMode);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.podcast_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.podcast_add_menuitem:
                startActivity(new Intent(getActivity(), AddPodcastActivity.class));

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        Podcast selectedPodcast = (Podcast) adapter.getItem(position);

        // Alert parent activity
        podcastSelectionListener.onPodcastSelected(selectedPodcast);
    }

    @Override
    public void onDestroyView() {
        viewCreated = false;

        super.onDestroyView();
    }

    /**
     * Set the list of podcasts to show in this fragment. You can call this any
     * time and the view will catch up as soon as it is created. This will also
     * reset any selection.
     * 
     * @param podcastList List of podcasts to show.
     */
    public void setPodcastList(List<Podcast> podcastList) {
        this.currentPodcastList = podcastList;

        showProgress = false;
        showLoadFailed = false;

        // Reset selection since it might not work with the new list
        selectNone();

        // Maps the podcast list items to the list UI
        // Only update the UI if it has been inflated
        if (viewCreated) {
            if (adapter == null)
                // This also set the member
                setListAdapter(new PodcastListAdapter(getActivity(), podcastList));
            else
                ((PodcastListAdapter) adapter).updateList(podcastList);

            updateUiElementVisibility();
        }
    }

    /**
     * Add a podcast to the list shown. Use this instead of
     * {@link #setPodcastList(List)} if you want a nice, animated addition.
     * 
     * @param podcast Podcast to add.
     */
    public void addPodcast(Podcast podcast) {
        currentPodcastList.add(podcast);
        Collections.sort(currentPodcastList);
        ((PodcastListAdapter) adapter).updateList(currentPodcastList);

        final int index = currentPodcastList.indexOf(podcast);

        if (viewCreated) {
            final PodcastListItemView listItemView = (PodcastListItemView) findListItemViewForIndex(index);

            // Is the position visible?
            if (listItemView != null) {
                listItemView.setAlpha(0f);
                listItemView.animate().alpha(1f).setDuration(addRemoveDuration).setListener(null);
            }
        }
    }

    /**
     * Remove a podcast from the list shown. Use this instead of
     * {@link #setPodcastList(List)} if you want a nice, animated removal.
     * 
     * @param podcast Podcast to remove.
     */
    public void removePodcast(final Podcast podcast) {
        final int index = currentPodcastList.indexOf(podcast);

        if (viewCreated) {
            final PodcastListItemView listItemView = (PodcastListItemView) findListItemViewForIndex(index);

            // Is the position visible?
            if (listItemView != null)
                listItemView.animate().alpha(0f).setDuration(addRemoveDuration)
                        .setListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (currentPodcastList.remove(podcast))
                                    ((PodcastListAdapter) adapter).updateList(currentPodcastList);
                                // Set it back to opaque because the view might
                                // be recycled and we need it to show
                                listItemView.setAlpha(1f);
                            }
                        });
            // Not visible, simply remove the podcast
            else if (currentPodcastList.remove(podcast))
                ((PodcastListAdapter) adapter).updateList(currentPodcastList);
        }
    }

    /**
     * Show progress for a certain position in the podcast list. Progress will
     * ignored if the item is not visible.
     * 
     * @param position Position in list to show progress for.
     * @param progress Progress information to show.
     */
    public void showProgress(int position, Progress progress) {
        // To prevent this if we are not ready to handle progress update
        // e.g. on app termination
        if (viewCreated) {
            final PodcastListItemView listItemView = (PodcastListItemView) findListItemViewForIndex(position);

            // Is the position visible?
            if (listItemView != null)
                listItemView.updateProgress(progress);
        }
    }

    /**
     * Set the logo view mode. This will also update the logo(s) showing if
     * possible or needed.
     * 
     * @param mode The logo view mode to use.
     */
    public void updateLogo(LogoViewMode mode) {
        final boolean needsSlide =
                // Mode changes from something to LARGE
                LogoViewMode.LARGE.equals(mode) && !LogoViewMode.LARGE.equals(logoViewMode) ||
                        // Mode changes from LARGE to something else
                        !LogoViewMode.LARGE.equals(mode) && LogoViewMode.LARGE.equals(logoViewMode);

        this.logoViewMode = mode;

        // Only act if the view is actually created
        if (viewCreated) {
            // SMALL means that the adapter needs to make the individual podcast
            // list item view show the podcast icon, see there
            if (adapter != null)
                ((PodcastListAdapter) adapter).setShowLogo(LogoViewMode.SMALL.equals(logoViewMode));

            // LARGE shows the big image view below the list
            if (LogoViewMode.LARGE.equals(logoViewMode)) {
                updatePodcastLogoView();
                logoView.setVisibility(View.VISIBLE);

                if (needsSlide && logoViewHeight > 0)
                    slideInLogoView();
            }
            // NONE hides the logo view
            else {
                if (needsSlide)
                    slideOutLogoView();
                else if (!animating)
                    logoView.setVisibility(View.GONE);
            }
        }
    }

    private void slideInLogoView() {
        final LayoutParams layoutParams = logoView.getLayoutParams();

        ValueAnimator animator = ValueAnimator.ofInt(0, logoViewHeight);
        animator.setDuration(slideDuration);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // Animate to new layout params
                layoutParams.height = (Integer) animation.getAnimatedValue();
                logoView.setLayoutParams(layoutParams);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                animating = false;
            }
        });

        animating = true;
        animator.start();
    }

    private void slideOutLogoView() {
        final LayoutParams layoutParams = logoView.getLayoutParams();

        ValueAnimator animator = ValueAnimator.ofInt(logoViewHeight, 0);
        animator.setDuration(slideDuration);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // Animate to new layout params
                layoutParams.height = (Integer) animation.getAnimatedValue();
                logoView.setLayoutParams(layoutParams);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                logoView.setVisibility(View.GONE);
                // Reset to old layout params
                layoutParams.height = logoViewHeight;
                logoView.setLayoutParams(layoutParams);

                animating = false;
            }
        });

        animating = true;
        animator.start();
    }

    private void updatePodcastLogoView() {
        if (currentPodcastList != null && selectedPosition >= 0) {
            final Podcast selectedPodcast = currentPodcastList.get(selectedPosition);

            // Check for logo and show it if available
            if (selectedPodcast.getLogo() != null) {
                logoView.setImageBitmap(selectedPodcast.getLogo());
                logoView.setScaleType(ScaleType.FIT_XY);
            } else
                showGenericPodcastLogo();
        } else
            showGenericPodcastLogo();
    }

    private void showGenericPodcastLogo() {
        logoView.setImageResource(R.drawable.default_podcast_logo);
        logoView.setScaleType(ScaleType.CENTER_INSIDE);
    }
}
