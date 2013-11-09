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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.alliknow.podcatcher.R;
import net.alliknow.podcatcher.listeners.OnEnterAuthorizationListener;

/**
 * A podcast authorization dialog. The activity hosting this fragment needs to
 * implement {@link OnEnterAuthorizationListener}. Once the listener is called,
 * the fragment will auto-dismiss itself.
 * <p>
 * You might also want to use {@link #setArguments(Bundle)} with a string value
 * to pre-set the user name using the key {@link #USERNAME_PRESET_KEY}. (This
 * needs to be done before showing the dialog.)
 * </p>
 */
public class AuthorizationFragment extends DialogFragment {

    /** Argument key for the user name to pre-set */
    public static final String USERNAME_PRESET_KEY = "username_preset";
    /** The tag we identify our authorization dialog fragment with */
    public static final String TAG = "authorization";

    /** The callback we are working with */
    private OnEnterAuthorizationListener listener;

    /** The user name to display onShow() */
    private String usernamePreset = null;

    /** The username text view */
    private EditText usernameTextView;
    /** The password text view */
    private EditText passwordTextView;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        this.usernamePreset = args.getString(USERNAME_PRESET_KEY);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure our listener is present
        try {
            this.listener = (OnEnterAuthorizationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnEnterAuthorizationListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Define context to use (parent activity might have no theme)
        final ContextThemeWrapper context = new ContextThemeWrapper(getActivity(),
                android.R.style.Theme_Holo_Light_Dialog);

        // Inflate our custom view
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View content = inflater.inflate(R.layout.authorization, null);

        this.usernameTextView = (EditText) content.findViewById(R.id.username);
        usernameTextView.setText(usernamePreset);
        this.passwordTextView = (EditText) content.findViewById(R.id.password);
        passwordTextView.setTypeface(Typeface.SANS_SERIF);

        // Add click listeners
        final Button submitButton = (Button) content.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final CharSequence username = usernameTextView.getText();
                final CharSequence password = passwordTextView.getText();

                listener.onSubmitAuthorization(username.toString(), password.toString());
                dismiss();
            }
        });
        final Button cancelButton = (Button) content.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onCancel(AuthorizationFragment.this.getDialog());
                dismiss();
            }
        });

        // Build the dialog
        final AlertDialog.Builder abuilder = new AlertDialog.Builder(context);
        abuilder.setTitle(R.string.auth_required)
                .setView(content);

        return abuilder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        listener.onCancelAuthorization();
    }
}
