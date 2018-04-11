package org.hotelbyte.app.wallet;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hotelbyte.app.base.BaseChildFragment;
import org.hotelbyte.app.MainActivity;
import org.hotelbyte.app.R;

/**
 * Import a wallet file
 */
public class ImportWalletFragment extends BaseChildFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_import, parent, false);

        Toolbar toolbar = prepareToolBar();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.showMainWallet();
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
