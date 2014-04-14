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

package net.alliknow.podcatcher.listeners;

import android.content.DialogInterface.OnCancelListener;

import java.io.File;

/**
 * Interface definition for a callback to be invoked when an file or folder is
 * selected.
 */
public interface OnSelectFileListener extends OnCancelListener {

    /**
     * A file/folder was selected by the user in the dialog.
     * 
     * @param selectedFile The file/folder selected.
     */
    public void onFileSelected(File selectedFile);

    /**
     * The current folder set in the file dialog changed.
     * 
     * @param path The new path.
     */
    public void onDirectoryChanged(File path);

    /**
     * The user tried to navigate to an unavailable path.
     * 
     * @param path The path.
     */
    public void onAccessDenied(File path);
}
