package org.hotelbyte.wallet;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.hotelbyte.wallet.adapter.WalletAdapter;
import org.hotelbyte.wallet.base.BaseAppCompatActivity;
import org.hotelbyte.wallet.bean.WalletDisplay;
import org.hotelbyte.wallet.interfaces.PasswordDialogCallback;
import org.hotelbyte.wallet.interfaces.SimpleStringCallback;
import org.hotelbyte.wallet.interfaces.StoredWallet;
import org.hotelbyte.wallet.parcels.TransactionParcel;
import org.hotelbyte.wallet.parcels.WalletDetailParcel;
import org.hotelbyte.wallet.parcels.WalletGenParcel;
import org.hotelbyte.wallet.service.TransactionService;
import org.hotelbyte.wallet.service.WalletGenService;
import org.hotelbyte.wallet.service.Web3jService;
import org.hotelbyte.wallet.settings.Settings;
import org.hotelbyte.wallet.storage.AddressNameStorage;
import org.hotelbyte.wallet.storage.WalletStorage;
import org.hotelbyte.wallet.util.DialogUtil;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Single;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

import static org.hotelbyte.wallet.settings.Constants.PARCEL_PARAM;

public class MainActivity extends BaseAppCompatActivity implements View.OnClickListener, View.OnLongClickListener, View.OnCreateContextMenuListener {

    @BindView(R.id.layout_main)
    RelativeLayout layout_main;
    @BindView(R.id.gen_wallet_fab)
    FloatingActionButton genWalletFab;
    @BindView(R.id.send_fab)
    FloatingActionButton sendFab;
    @BindView(R.id.fabmenu)
    FloatingActionMenu fabmenu;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    private WalletAdapter walletAdapter;
    private List<WalletDisplay> wallets = new ArrayList<>();
    private Web3jService web3JService;
    private boolean backPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        web3JService = Web3jService.getInstance(this);
        final ArrayList<StoredWallet> storedwallets = new ArrayList<>(WalletStorage.getInstance(this).get());
        for (StoredWallet cur : storedwallets) {
            wallets.add(new WalletDisplay(AddressNameStorage.getInstance(this).get(cur.getPubKey()), cur.getPubKey(), new BigInteger("-1"), WalletDisplay.CONTACT));
        }
        if (wallets.isEmpty()) {
            sendFab.setVisibility(View.GONE);
        }
        walletAdapter.notifyDataSetChanged();
        swipeRefresh.setOnRefreshListener(() -> new BalanceAsyncTask(this).execute());
        new BalanceAsyncTask(this).execute();
    }

    private void initView() {
        walletAdapter = new WalletAdapter(wallets, this, this, this, this);
        LinearLayoutManager mgr = new LinearLayoutManager(MainActivity.this.getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = mgr;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(walletAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), mgr.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }


    @OnClick({R.id.gen_wallet_fab, R.id.send_fab})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.gen_wallet_fab:
                generateDialog();
                fabmenu.close(true);
                break;
            case R.id.send_fab:
                startActivityForResult(new Intent(MainActivity.this, TransactionActivity.class), TransactionActivity.REQUEST_CODE);
                fabmenu.close(true);
                break;
        }
    }

    public void generateDialog() {
        if (!Settings.walletBeingGenerated) {
            Intent genI = new Intent(MainActivity.this, WalletGenActivity.class);
            startActivityForResult(genI, WalletGenActivity.REQUEST_CODE);
        } else {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
                builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
            else
                builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.wallet_one_at_a_time);
            builder.setMessage(R.string.wallet_creation_one_at_a_time_text);
            builder.setNegativeButton(R.string.button_ok, (dialog, which) -> dialog.dismiss());
            builder.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WalletGenActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent generatingService = new Intent(this, WalletGenService.class);
                generatingService.putExtra(PARCEL_PARAM, (WalletGenParcel) data.getParcelableExtra(PARCEL_PARAM));
                startService(generatingService);
            }
        } else if (requestCode == TransactionActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, TransactionService.class);
                intent.putExtra(PARCEL_PARAM, (TransactionParcel) data.getParcelableExtra(PARCEL_PARAM));
                startService(intent);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        if (itemPosition >= wallets.size()) {
            return;
        }
        Intent detail = new Intent(this, WalletDetailActivity.class);
        detail.putExtra(PARCEL_PARAM, new WalletDetailParcel(wallets.get(itemPosition).getPublicKey()));
        startActivity(detail);
    }

    @Override
    public boolean onLongClick(View view) {
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        if (itemPosition >= wallets.size()) {
            return false;
        }
        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        popup.getMenuInflater().inflate(R.menu.wallet_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener((item) -> {
            WalletDisplay wallet = wallets.get(itemPosition);
            if (item.getItemId() == R.id.wallet_popup_edit_name) {
                DialogUtil.askFor(this, R.string.wallet_popup_edit_name, wallet.getName(), new SimpleStringCallback() {
                    @Override
                    public void success(String newName) {
                        if (newName != null && !newName.trim().isEmpty() && (wallet.getName() == null || !wallet.getName().equals(newName))) {
                            AddressNameStorage.getInstance(getApplicationContext()).put(wallet.getPublicKey(), newName, getApplicationContext());
                            wallet.setName(newName);
                            runOnUiThread(() -> walletAdapter.notifyItemChanged(itemPosition));
                        }
                    }

                    @Override
                    public void canceled() {

                    }
                });
            } else if (item.getItemId() == R.id.wallet_popup_backup) {
                WalletStorage walletStorage = WalletStorage.getInstance(getApplicationContext());
                walletStorage.setWalletForExport(wallet.getPublicKey());
                Intent intent = walletStorage.exportWallet(this);
                PackageManager packageManager = getPackageManager();
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent);
                }
            } else if (item.getItemId() == R.id.wallet_popup_delete) {
                if (!Settings.walletBeingDeleted) {
                    Settings.walletBeingDeleted = true;
                    DialogUtil.askForPasswordAndDecode(this, R.string.wallet_popup_delete_prompt, new PasswordDialogCallback() {
                        @Override
                        public void success(String password) {
                            Toast.makeText(MainActivity.this, R.string.wallet_popup_delete_prompt_progress, Toast.LENGTH_LONG).show();
                            Single.fromCallable(() -> WalletStorage.getInstance(getApplicationContext()).getFullWallet(getApplicationContext(), password, wallet.getPublicKey()))
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(new SingleSubscriber<Credentials>() {
                                        @Override
                                        public void onSuccess(Credentials transactionDisplay) {
                                            WalletStorage.getInstance(getApplicationContext()).removeWallet(wallet.getPublicKey(), getApplicationContext());
                                            if (wallets.remove(wallet)) {
                                                runOnUiThread(() -> walletAdapter.notifyDataSetChanged());
                                            }
                                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.wallet_popup_delete_prompt_deleted, Toast.LENGTH_LONG).show());
                                            Settings.walletBeingDeleted = false;
                                        }

                                        @Override
                                        public void onError(Throwable error) {
                                            error.printStackTrace();
                                            runOnUiThread(() -> snackError(layout_main, error.getMessage()));
                                            Settings.walletBeingDeleted = false;
                                        }
                                    });
                        }

                        @Override
                        public void canceled() {
                            Settings.walletBeingDeleted = false;
                        }
                    });
                } else {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= 24) {// Otherwise buttons on 7.0+ are nearly invisible
                        builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
                    } else {
                        builder = new AlertDialog.Builder(MainActivity.this);
                    }
                    builder.setTitle(R.string.wallet_one_at_a_time);
                    builder.setMessage(R.string.wallet_deletion_one_at_a_time_text);
                    builder.setNegativeButton(R.string.button_ok, (dialog, which) -> dialog.dismiss());
                    builder.show();
                }
            } else {
                Toast.makeText(MainActivity.this, "You Clicked : " + item.getTitle() + " on " + wallets.get(itemPosition).getName(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.setGravity(Gravity.RIGHT);
        }
        popup.show();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (backPressed) {
            super.onBackPressed();
        }
        this.backPressed = true;
        Toast.makeText(this, R.string.exit_confirmation, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> backPressed = false, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();
        new BalanceAsyncTask(this).execute();
    }


    class BalanceAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;

        public BalanceAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            final ArrayList<StoredWallet> storageWallets = new ArrayList<>(WalletStorage.getInstance(context).get());
            if (storageWallets.size() != wallets.size()) {
                wallets.clear();
                for (StoredWallet cur : storageWallets) {
                    wallets.add(new WalletDisplay(AddressNameStorage.getInstance(context).get(cur.getPubKey()), cur.getPubKey(), new BigInteger("-1"), WalletDisplay.CONTACT));
                }
                runOnUiThread(() -> walletAdapter.notifyDataSetChanged());
            }
            for (int i = 0; i < wallets.size(); i++) {
                Integer walletPosition = i;
                WalletDisplay wallet = wallets.get(i);
                BigInteger balance = web3JService.getBalance(wallet.getPublicKey());
                //Only update when balance change
                if (wallet.getBalanceNative() == null || !wallet.getBalanceNative().equals(balance)) {
                    wallet.setBalance(balance);
                    runOnUiThread(() -> walletAdapter.notifyItemChanged(walletPosition));
                }
            }
            runOnUiThread(() -> swipeRefresh.setRefreshing(false));
            return true;
        }
    }
}
