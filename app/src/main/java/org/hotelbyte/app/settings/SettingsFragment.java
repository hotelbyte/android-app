package org.hotelbyte.app.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import org.hotelbyte.app.MainActivity;
import org.hotelbyte.app.R;

/**
 * Created by lexfaraday
 */

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

     @Override
    public void onStart() {
        super.onStart();
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity.getSupportActionBar() != null) {
            mainActivity.getSupportActionBar().setTitle(getString(R.string.menu_settings_title));
        }
    }
}
