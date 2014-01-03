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

package net.alliknow.podcatcher.view;

import android.content.Context;
import android.util.AttributeSet;

import net.alliknow.podcatcher.R;
import net.alliknow.podcatcher.model.types.Progress;

/**
 * A sophisticated horizontal progress view.
 */
public class HorizontalProgressView extends ProgressView {

    /**
     * The layout id to inflate for this view.
     */
    protected static int LAYOUT = R.layout.progress_horizontal;

    /**
     * Create progress view.
     * 
     * @param context Context view lives in.
     * @param attrs View attributes.
     */
    public HorizontalProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayout() {
        return R.layout.progress_horizontal;
    }

    @Override
    public void publishProgress(Progress progress) {
        super.publishProgress(progress);

        // Show progress in progress bar
        if (progress.getPercentDone() >= 0 && progress.getPercentDone() <= 100) {
            progressBar.setIndeterminate(false);
            progressBar.setProgress(progress.getPercentDone());
        } else
            progressBar.setIndeterminate(true);
    }
}
