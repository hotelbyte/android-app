package org.hotelbyte.app;

import android.app.Fragment;
import android.support.v7.widget.Toolbar;

/**
 * Created by lexfaraday
 * This is the base for the child fragments that are child of main fragments but not activities
 */

public class BaseChildFragment extends Fragment {


    protected Toolbar prepareToolBar() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity.getSupportActionBar() != null) {
            mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mainActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            mainActivity.getSupportActionBar().setTitle(null);
        }

        return mainActivity.findViewById(R.id.toolbar);
    }
}
