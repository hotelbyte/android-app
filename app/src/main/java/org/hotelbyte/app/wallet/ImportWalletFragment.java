package org.hotelbyte.app.wallet;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hotelbyte.app.MainActivity;
import org.hotelbyte.app.R;

/**
 * Import a wallet file
 */
public class ImportWalletFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_import, parent, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity.getSupportActionBar() != null) {
            mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mainActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            mainActivity.getSupportActionBar().setTitle(null);
        }

        Toolbar toolbar = mainActivity.findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.showMainWallet();
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity.getSupportActionBar() != null) {
                    mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    mainActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);
                }
                mainActivity.configureToolBar();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }
}
