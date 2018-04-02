package org.hotelbyte.app.wallet;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import org.hotelbyte.app.interfaces.StoredWallet;
import org.hotelbyte.app.service.Web3jService;
import org.hotelbyte.app.storage.AddressNameStorage;
import org.hotelbyte.app.storage.WalletStorage;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lexfaraday
 */
public class WalletManager {

    private Web3jService web3JService;
    private List<AccountBean> accounts = new ArrayList<>();
    private WalletRecyclerViewAdapter walletRecyclerViewAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private Activity activity;

    private static WalletManager instance;
    private BalanceAsyncTask balanceAsyncTask;
    private boolean asyncTaskExecuted;

    public static WalletManager getInstance(Context context, Activity activity) {
        if (instance == null) {
            synchronized (WalletManager.class) {
                if (instance == null) {
                    instance = new WalletManager(context, activity);
                }
            }
        }
        return instance;
    }

    private WalletManager(Context context, Activity activity) {
        this.activity = activity;
        balanceAsyncTask = new BalanceAsyncTask(context);
        web3JService = Web3jService.getInstance(context);

        // Look up local accounts
        final ArrayList<StoredWallet> storedwallets = new ArrayList<>(WalletStorage.getInstance(context).get());
        for (StoredWallet cur : storedwallets) {
            accounts.add(new AccountBean(AddressNameStorage.getInstance(context).get(cur.getPubKey()), cur.getPubKey(), BigInteger.valueOf(1).negate(), AccountBean.CONTACT));
        }
    }


    class BalanceAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;

        public BalanceAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            final ArrayList<StoredWallet> storageWallets = new ArrayList<>(WalletStorage.getInstance(context).get());
            if (storageWallets.size() != accounts.size()) {
                accounts.clear();
                for (StoredWallet cur : storageWallets) {
                    accounts.add(new AccountBean(AddressNameStorage.getInstance(context).get(cur.getPubKey()), cur.getPubKey(), BigInteger.valueOf(1).negate(), AccountBean.CONTACT));
                }
                activity.runOnUiThread(() -> walletRecyclerViewAdapter.notifyDataSetChanged());
            }
            for (int i = 0; i < accounts.size(); i++) {
                Integer walletPosition = i;
                AccountBean wallet = accounts.get(i);
                BigInteger balance = web3JService.getBalance(wallet.getPublicKey());
                //Only update when balance change
                if (wallet.getBalanceNative() == null || !wallet.getBalanceNative().equals(balance)) {
                    wallet.setBalance(balance);
                    activity.runOnUiThread(() -> walletRecyclerViewAdapter.notifyItemChanged(walletPosition));
                }
            }
            activity.runOnUiThread(() -> swipeRefresh.setRefreshing(false));
            return true;
        }
    }

    /**
     *
     * @return List of AccountBean
     */
    public List<AccountBean> getAccounts() {
        return accounts;
    }

    /**
     *
     * @return BalanceAsyncTask to execute async.
     */
    public void executeBalanceAsyncTask() {
        if (!asyncTaskExecuted) {
            balanceAsyncTask.execute();
            asyncTaskExecuted = true;
        }
    }

    public void setWalletRecyclerViewAdapter(WalletRecyclerViewAdapter walletRecyclerViewAdapter) {
        this.walletRecyclerViewAdapter = walletRecyclerViewAdapter;
    }

    public void setSwipeRefresh(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefresh = swipeRefreshLayout;
    }
}
