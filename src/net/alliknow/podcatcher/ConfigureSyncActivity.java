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

import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import net.alliknow.podcatcher.listeners.OnSyncListener;
import net.alliknow.podcatcher.model.sync.ControllerImpl;
import net.alliknow.podcatcher.model.sync.SyncController.SyncMode;
import net.alliknow.podcatcher.view.fragments.ConfigureSyncFragment;
import net.alliknow.podcatcher.view.fragments.ConfigureSyncFragment.ConfigureSyncDialogListener;

/**
 * Non-UI activity to configure the synchronization settings. Will use a
 * {@link ConfigureSyncFragment} to show the corresponding dialog.
 */
public class ConfigureSyncActivity extends BaseActivity implements ConfigureSyncDialogListener,
        OnSyncListener {

    /** The podcatcher help web site URL (sync anchor) */
    private static final String PODCATCHER_HELPSITE_SYNC = "http://www.podcatcher-deluxe.com/help#sync";

    /** Tag to find the sync config dialog fragment under */
    private static final String SYNC_CONFIG_DIALOG_TAG = "sync_config_dialog";
    /** The fragment containing the settings UI */
    private ConfigureSyncFragment configureSyncFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register with the sync manager
        syncManager.addSyncListener(this);

        // Create and show the dialog fragment
        if (savedInstanceState == null) {
            this.configureSyncFragment = new ConfigureSyncFragment();
            // Need to set style, because this activity has no UI
            configureSyncFragment.setStyle(DialogFragment.STYLE_NORMAL,
                    android.R.style.Theme_Holo_Light_Dialog);

            configureSyncFragment.show(getFragmentManager(), SYNC_CONFIG_DIALOG_TAG);
        } else
            this.configureSyncFragment = (ConfigureSyncFragment)
                    getFragmentManager().findFragmentByTag(SYNC_CONFIG_DIALOG_TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        syncManager.removeSyncListener(this);
    }

    @Override
    public void onUpdateSettings(ControllerImpl impl) {
        final Intent configureIntent = new Intent();

        switch (impl) {
            case GPODDER:
                configureIntent.setClass(this, ConfigureGpodderSyncActivity.class);
                break;
            case DROPBOX:
                configureIntent.setClass(this, ConfigureDropboxSyncActivity.class);
                break;
        }

        startActivityForResult(configureIntent, 42);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        configureSyncFragment.refresh();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);

        if (key.equals(SettingsActivity.KEY_LAST_SYNC) && configureSyncFragment != null)
            configureSyncFragment.refresh();
    }

    @Override
    public void onUpdateMode(ControllerImpl impl, SyncMode mode) {
        syncManager.setSyncMode(impl, mode);

        configureSyncFragment.refresh();
    }

    @Override
    public void onSyncNow() {
        syncManager.syncAll();
    }

    @Override
    public void onSyncStarted() {
        configureSyncFragment.refresh();
    }

    @Override
    public void onSyncCompleted() {
        configureSyncFragment.refresh();
    }

    @Override
    public void onShowHelp() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PODCATCHER_HELPSITE_SYNC)));
        } catch (ActivityNotFoundException e) {
            // We are in a restricted profile without a browser
            showToast(getString(R.string.no_browser));
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
