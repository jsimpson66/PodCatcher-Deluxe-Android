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

package net.alliknow.podcatcher;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import net.alliknow.podcatcher.view.fragments.SelectFileFragment;
import net.alliknow.podcatcher.view.fragments.SelectFileFragment.SelectFileDialogListener;

import java.io.File;

/**
 * Non-UI activity to select files and folders. Will use a fragment to show the
 * selection dialog.
 * <p>
 * <b>Configuration:</b> Use the intent and constants defined to configure its
 * behavior. In particular, use {@link #INITIAL_PATH_KEY} and
 * {@link #SELECTION_MODE_KEY} to set the initial directory shown and the
 * file/folder mode respectively. The defaults are file selection and root
 * external directory.
 * </p>
 * <p>
 * <b>Get result:</b> Start the activity with
 * {@link Activity#startActivityForResult(Intent, int)} to be alerted on
 * selection. The URI of the selected element (file or folder) is available from
 * {@link Intent#getData()}.
 * </p>
 */
public class SelectFileActivity extends BaseActivity implements SelectFileDialogListener {

    /** The key to store initial path under in intent */
    public static final String INITIAL_PATH_KEY = "initial_path";
    /** The key to store wanted selection mode under in intent */
    public static final String SELECTION_MODE_KEY = "file_selection_mode";

    /** The selection mode options */
    public static enum SelectionMode {
        /** File selection */
        FILE,

        /** Folder selection */
        FOLDER
    }

    /** Tag to find the add suggestion dialog fragment under */
    private static final String SELECT_FILE_DIALOG_TAG = "select_file_dialog";
    /** The fragment containing the select file UI */
    private SelectFileFragment selectFileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the dialog fragment
        if (savedInstanceState == null) {
            this.selectFileFragment = new SelectFileFragment();
            selectFileFragment.setStyle(DialogFragment.STYLE_NORMAL,
                    android.R.style.Theme_Holo_Light_Dialog);

            // Show the fragment
            selectFileFragment.show(getFragmentManager(), SELECT_FILE_DIALOG_TAG);
        } else
            this.selectFileFragment = (SelectFileFragment)
                    getFragmentManager().findFragmentByTag(SELECT_FILE_DIALOG_TAG);

        // Use getIntent() to configure selection mode
        final SelectionMode modeFromIntent =
                (SelectionMode) getIntent().getSerializableExtra(SELECTION_MODE_KEY);
        // Set the selection mode for the dialog fragment
        selectFileFragment.setSelectionMode(
                modeFromIntent == null ? SelectionMode.FILE : modeFromIntent);

        // Use getIntent() to configure initial path
        final String initialPathString = getIntent().getStringExtra(INITIAL_PATH_KEY);
        // Set the initial path
        if (initialPathString != null && new File(initialPathString).exists())
            selectFileFragment.setPath(new File(initialPathString));
        else
            selectFileFragment.setPath(Environment.getExternalStorageDirectory());

        // Apply theme color
        selectFileFragment.setThemeColors(themeColor, lightThemeColor);
    }

    @Override
    public void onFileSelected(File selectedFile) {
        selectFileFragment.dismiss();

        Intent result = new Intent();
        result.setData(Uri.fromFile(selectedFile));

        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onDirectoryChanged(File path) {
        // This will make sure the path is kept when the activity is re-created,
        // on configuration changes
        getIntent().putExtra(INITIAL_PATH_KEY, path.getAbsolutePath());
    }

    @Override
    public void onAccessDenied(File path) {
        showToast(getString(R.string.file_select_access_denied));
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
