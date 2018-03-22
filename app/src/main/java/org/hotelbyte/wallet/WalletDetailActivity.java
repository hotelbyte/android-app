package org.hotelbyte.wallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.hotelbyte.wallet.adapter.TransactionAdapter;
import org.hotelbyte.wallet.base.BaseAppCompatActivity;
import org.hotelbyte.wallet.bean.TransactionDisplay;
import org.hotelbyte.wallet.bean.TransactionType;
import org.hotelbyte.wallet.parcels.TransactionDetailParcel;
import org.hotelbyte.wallet.parcels.WalletDetailParcel;
import org.hotelbyte.wallet.service.NetworkApiExplorerService;
import org.hotelbyte.wallet.service.Web3jService;
import org.hotelbyte.wallet.storage.AddressNameStorage;
import org.hotelbyte.wallet.storage.TransactionStorage;
import org.hotelbyte.wallet.util.Blockies;
import org.hotelbyte.wallet.util.ExchangeCalculator;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Single;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

import static org.hotelbyte.wallet.settings.Constants.PARCEL_PARAM;

public class WalletDetailActivity extends BaseAppCompatActivity implements View.OnClickListener {
    @BindView(R.id.layout_detail)
    ConstraintLayout layoutDetail;
    @BindView(R.id.iv_account)
    ImageView ivAccount;
    @BindView(R.id.tv_account_alias)
    TextView tvAccountAlias;
    @BindView(R.id.tv_amount)
    TextView tvAmount;
    @BindView(R.id.tv_address)
    TextView tvAddress;
    @BindView(R.id.tv_trancount)
    TextView tvTrancount;
    @BindView(R.id.ib_copy_address)
    ImageButton ibCopyAddress;
    @BindView(R.id.transaction_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    private WalletDetailParcel walletDetailParcel;
    private Web3jService web3JService;
    private TransactionAdapter transactionAdapter;
    private List<TransactionDisplay> transactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_detail);
        ButterKnife.bind(this);
        this.walletDetailParcel = getIntent().getParcelableExtra(PARCEL_PARAM);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        web3JService = Web3jService.getInstance(this);
        initData();
        initTransactions();
        swipeRefresh.setOnRefreshListener(() -> initTransactions());
    }


    private void initView() {
        ivAccount.setImageBitmap(Blockies.createIcon(walletDetailParcel.getAddress()));
        String alias = AddressNameStorage.getInstance(this).get(walletDetailParcel.getAddress());
        tvAccountAlias.setText(alias);
        tvAddress.setText(walletDetailParcel.getAddress());
        ibCopyAddress.setOnClickListener(v -> {
            String text = tvAddress.getText().toString();
            setClipboard(text);
        });

        //Transaction recyclerView

        transactionAdapter = new TransactionAdapter(transactions, this, this, this);
        LinearLayoutManager mgr = new LinearLayoutManager(WalletDetailActivity.this.getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = mgr;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(transactionAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), mgr.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void setClipboard(String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(getString(R.string.detail_copy_clipboard_action), text);
            clipboard.setPrimaryClip(clip);
        }
        snackMessage(layoutDetail, getString(R.string.detail_copy_clipboard_action));
    }

    private void initData() {
        Single.fromCallable(() -> web3JService.getBalance(walletDetailParcel.getAddress()))
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<BigInteger>() {
                    @Override
                    public void onSuccess(BigInteger bigInteger) {
                        runOnUiThread(() -> {
                            BigDecimal weiBalance = new BigDecimal(bigInteger);
                            BigDecimal hotelCoinBalance = weiBalance.divide(ExchangeCalculator.ONE_ETHER);
                            if (hotelCoinBalance.compareTo(BigDecimal.ZERO) == 0) {
                                tvAmount.setText("0 " + getString(R.string.coin_name));
                            } else {
                                tvAmount.setText(hotelCoinBalance.stripTrailingZeros().toPlainString() + " " + getString(R.string.coin_name));
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });

        Single.fromCallable(() -> web3JService.getTransactionList(walletDetailParcel.getAddress()))
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<BigInteger>() {
                    @Override
                    public void onSuccess(BigInteger bigInteger) {
                        runOnUiThread(() -> tvTrancount.setText(getString(R.string.detail_transaction_count) + bigInteger));
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
    }


    private void initTransactions() {
        Single.fromCallable(() -> web3JService.getCurrentBlockNumber())
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<BigInteger>() {
                    @Override
                    public void onSuccess(BigInteger currentBlock) {
                        Set<String> storageTransactionHashSet = TransactionStorage.getInstance(WalletDetailActivity.this).get(walletDetailParcel.getAddress());
                        if (storageTransactionHashSet != null) {
                            fillTransactionInfo(storageTransactionHashSet, currentBlock);
                        }
                        NetworkApiExplorerService.getInstance(WalletDetailActivity.this).callTransactions(walletDetailParcel.getAddress(), (response) -> {
                            if (response != null) {
                                fillTransactionInfo(response.keySet(), currentBlock);
                            }
                            runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                        }, (e) -> {
                            e.printStackTrace();
                            runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                        });
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                        runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                    }
                });
    }

    private void fillTransactionInfo(Set<String> transactionHashSet, BigInteger currentBlock) {
        for (String transactionHash : transactionHashSet) {
            if (!existTransaction(transactionHash) || hasLowConfirmations(transactionHash)) {
                Single.fromCallable(() -> getTransactionDisplay(transactionHash, currentBlock))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new SingleSubscriber<TransactionDisplay>() {
                            @Override
                            public void onSuccess(TransactionDisplay transactionDisplay) {
                                int index = transactions.indexOf(transactionDisplay);
                                if (index != -1) {
                                    transactions.set(index, transactionDisplay);
                                } else {
                                    transactions.add(transactionDisplay);
                                }
                                Collections.sort(transactions, TransactionDisplay.COMPARATOR);
                                runOnUiThread(() -> transactionAdapter.notifyDataSetChanged());
                            }

                            @Override
                            public void onError(Throwable error) {
                                error.printStackTrace();
                            }
                        });
            }
        }
    }

    private boolean hasLowConfirmations(String transactionHash) {
        boolean found = false;
        for (TransactionDisplay transaction : transactions) {
            if (transactionHash.equalsIgnoreCase(transaction.getHash()) && transaction.getConfirmations() <= 12) {
                found = true;
            }
        }
        return found;
    }

    private boolean existTransaction(String transactionHash) {
        boolean found = false;
        for (TransactionDisplay transaction : transactions) {
            if (transactionHash.equalsIgnoreCase(transaction.getHash())) {
                found = true;
            }
        }
        return found;
    }

    private TransactionDisplay getTransactionDisplay(String transactionHash, BigInteger currentBlock) {
        EthTransaction ethTransaction = web3JService.getTransaction(transactionHash);
        Transaction transaction = ethTransaction.getTransaction();
        EthBlock ethBlock = web3JService.getBlock(transaction.getBlockNumber());
        long confirmations = 0L;
        if (currentBlock != null) {
            confirmations = currentBlock.subtract(transaction.getBlockNumber()).longValue();
            if (confirmations < 0) {
                confirmations = 0L;
            }
        }
        Date creationDate = null;
        if (ethBlock != null && ethBlock.getBlock() != null) {
            creationDate = new Date(ethBlock.getBlock().getTimestamp().longValue() * 1000L);
        }
        TransactionType type = transaction.getFrom().equalsIgnoreCase(walletDetailParcel.getAddress()) ? TransactionType.OUTGOING : TransactionType.INCOMING;
        return new TransactionDisplay(transaction.getHash(), type, transaction.getFrom(), transaction.getTo(), transaction.getValue(), transaction.getBlockNumber(), confirmations, creationDate, transaction.getNonce(), transaction.getGasPrice(), transaction.getGas());
    }

    @Override
    public void onClick(View view) {
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        if (itemPosition >= transactions.size()) {
            return;
        }
        TransactionDisplay transaction = transactions.get(itemPosition);
        TransactionDetailParcel transactionDetailParcel = new TransactionDetailParcel(transaction.getHash(), transaction.getFromAddress(), transaction.getToAddress(), transaction.getAmount().longValue(), transaction.getBlock().longValue(), transaction.getConfirmations(), transaction.getSendDate().getTime(), transaction.getNonce().longValue(), transaction.getGasPrice().longValue(), transaction.getGasUsed().longValue());
        Intent detail = new Intent(this, TransactionDetailActivity.class);
        detail.putExtra(PARCEL_PARAM, transactionDetailParcel);
        startActivity(detail);
    }

}
