package org.hotelbyte.app.wallet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.hotelbyte.app.base.BaseChildFragment;
import org.hotelbyte.app.MainActivity;
import org.hotelbyte.app.R;

/**
 * Import a wallet file
 */
public class ImportWalletFragment extends BaseChildFragment {

    private static final String TAG = "import-wallet-fragment";
    private static final int READ_REQUEST_CODE = 42;


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

        Button importBtn = view.findViewById(R.id.btn_wallet_import);
        importBtn.setOnClickListener(onClickListenerImport);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }


    View.OnClickListener onClickListenerImport = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setType("*/*");
            startActivityForResult(intent, READ_REQUEST_CODE);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



            if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.
                // Pull that URI using resultData.getData().
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());
                }
            }

    }
}
