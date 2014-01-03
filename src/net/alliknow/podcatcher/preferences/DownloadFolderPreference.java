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

package net.alliknow.podcatcher.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.preference.Preference;
import android.util.AttributeSet;

import net.alliknow.podcatcher.SelectFileActivity;
import net.alliknow.podcatcher.SelectFileActivity.SelectionMode;

import java.io.File;

/**
 * The custom download folder preference. Shows folder selection dialog when
 * clicked.
 */
public class DownloadFolderPreference extends Preference {

    /** Our request code for the folder selection dialog */
    public static final int REQUEST_CODE = 99;

    /** Currently set download folder */
    private File downloadFolder;

    /**
     * Create new preference.
     * 
     * @param context Context the preference lives in.
     * @param attrs Values from the XML.
     */
    public DownloadFolderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // We want to init this before any other method is called to avoid a
        // situation where downloadFolder == null
        onSetInitialValue(false, null);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // The default is the public podcast directory
        downloadFolder = new File(getPersistedString(getDefaultDownloadFolder().getAbsolutePath()));
    }

    @Override
    protected void onClick() {
        // Create select folder intent
        Intent selectFolderIntent = new Intent(getContext(), SelectFileActivity.class);
        selectFolderIntent
                .putExtra(SelectFileActivity.SELECTION_MODE_KEY, SelectionMode.FOLDER);
        selectFolderIntent
                .putExtra(SelectFileActivity.INITIAL_PATH_KEY, downloadFolder.getAbsolutePath());

        // Start activity. Result will be caught by the SettingsActivity.
        ((Activity) getContext()).startActivityForResult(selectFolderIntent, REQUEST_CODE);
    }

    @Override
    public CharSequence getSummary() {
        return downloadFolder.getAbsolutePath();
    }

    /**
     * Set new value for the preference.
     * 
     * @param newFolder Updated folder to use.
     */
    public void update(File newFolder) {
        this.downloadFolder = newFolder;

        if (newFolder != null)
            persistString(newFolder.getAbsolutePath());
    }

    /**
     * @return The default podcast episode download folder.
     */
    public static File getDefaultDownloadFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
    }
}
