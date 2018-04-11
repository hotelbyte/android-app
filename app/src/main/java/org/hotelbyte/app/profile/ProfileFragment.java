package org.hotelbyte.app.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hotelbyte.app.MainActivity;
import org.hotelbyte.app.R;

/**
 * Created by lexfaraday
 * Profile fragment to show rewards, badges and bookings.
 */
public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity.getSupportActionBar() != null) {
            mainActivity.getSupportActionBar().setTitle(getString(R.string.menu_profile_title));
        }
    }
}
