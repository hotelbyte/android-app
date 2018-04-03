package org.hotelbyte.app.wallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.hotelbyte.app.MainActivity;
import org.hotelbyte.app.R;
import org.hotelbyte.app.settings.Settings;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class WalletFragment extends Fragment implements View.OnClickListener {

    private FloatingActionButton genWalletFab;
    private FloatingActionButton sendFab;
    private FloatingActionMenu fabmenu;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private WalletManager walletManager;
    private WalletRecyclerViewAdapter walletRecyclerViewAdapter;
    private MaterialTapTargetPrompt mFabPrompt;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WalletFragment() {
    }

    // TODO: Customize parameter initialization
    public static WalletFragment newInstance(int columnCount) {
        WalletFragment fragment = new WalletFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        // Instantiate manager and adapter
        walletManager = WalletManager.getInstance(getContext(), getActivity());
        walletRecyclerViewAdapter = new WalletRecyclerViewAdapter(walletManager.getAccounts(), mListener, getContext());
        walletManager.setWalletRecyclerViewAdapter(walletRecyclerViewAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set up view
        View view = inflater.inflate(R.layout.fragment_wallet_list, container, false);

        fabmenu = view.findViewById(R.id.fab_wallet_menu);
        genWalletFab = view.findViewById(R.id.gen_wallet_fab);
        sendFab = view.findViewById(R.id.send_fab);
        genWalletFab.setOnClickListener(this);
        sendFab.setOnClickListener(this);

        // Pass UI elements to wallet manager
        walletManager.setSwipeRefresh(view.findViewById(R.id.swipeRefresh));
        setupRecyclerView(view);
        // Async balance task before set up the recycler view
        walletManager.executeBalanceAsyncTask();

        return view;
    }

    private void setupRecyclerView(View view) {
        // Set the adapter
        RecyclerView recyclerView = view.findViewById(R.id.list);
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        recyclerView.setAdapter(new WalletRecyclerViewAdapter(walletManager.getAccounts(), mListener, context));
        walletRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        // We have already the view let's go to customize view with Wallet Manager data
        if (walletManager.getAccounts().isEmpty()) {
            // We don't have accounts on local
            mFabPrompt = new MaterialTapTargetPrompt.Builder(WalletFragment.this)
                    .setTarget(fabmenu.getMenuIconView())
                    .setBackgroundColour(ContextCompat.getColor(getContext(), R.color.colorSecondary))
                    .setIconDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_add_white_24px))
                    .setAnimationInterpolator(new FastOutSlowInInterpolator())
                    .setAutoDismiss(false)
                    .setAutoFinish(false)
                    .setCaptureTouchEventOutsidePrompt(true)
                    .setPrimaryText(R.string.wallet_home_welcome_title)
                    .setSecondaryText(R.string.wallet_home_welcome_body)
                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                        @Override
                        public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                prompt.finish();
                                mFabPrompt = null;
                            } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                mFabPrompt = null;
                            }
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onClick(View view) {
        Log.d("tag1", "aaa");

        MainActivity mainActivity = (MainActivity) getActivity();
        switch (view.getId()) {
            case R.id.gen_wallet_fab:
                fabmenu.close(true);
                mainActivity.showCreateAccount();
                break;
            case R.id.send_fab:
                //startActivityForResult(new Intent(MainActivity.this, TransactionActivity.class), TransactionActivity.REQUEST_CODE);
                fabmenu.close(true);
                break;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(AccountBean item);
    }

    public void generateDialog() {
        if (!Settings.walletBeingGenerated) {
            //Intent genI = new Intent(getContext(), WalletGenActivity.class);
            //startActivityForResult(genI, WalletGenActivity.REQUEST_CODE);
        } else {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
                builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
            else
                builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.wallet_one_at_a_time);
            builder.setMessage(R.string.wallet_creation_one_at_a_time_text);
            builder.setNegativeButton(R.string.button_ok, (dialog, which) -> dialog.dismiss());
            builder.show();
        }
    }
}
