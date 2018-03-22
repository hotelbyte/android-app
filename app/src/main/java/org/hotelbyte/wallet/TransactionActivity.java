package org.hotelbyte.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import org.hotelbyte.wallet.base.BaseAppCompatActivity;
import org.hotelbyte.wallet.interfaces.PasswordDialogCallback;
import org.hotelbyte.wallet.parcels.TransactionParcel;
import org.hotelbyte.wallet.service.Web3jService;
import org.hotelbyte.wallet.storage.WalletStorage;
import org.hotelbyte.wallet.util.DialogUtil;
import org.hotelbyte.wallet.util.ExchangeCalculator;
import org.web3j.tx.Transfer;

import java.math.BigDecimal;
import java.math.BigInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Single;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

import static org.hotelbyte.wallet.settings.Constants.PARCEL_PARAM;

public class TransactionActivity extends BaseAppCompatActivity {
    public static final int REQUEST_CODE = 402;

    @BindView(R.id.toAddress)
    EditText toAddress;
    @BindView(R.id.et_amount)
    EditText amount;
    @BindView(R.id.btn_transfer)
    Button btnTransfer;
    @BindView(R.id.layout_transfer)
    LinearLayout layoutTransfer;

    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView(R.id.pBar)
    ProgressBar pgsBar;

    private Web3jService web3JService;
    private BigDecimal balance = BigDecimal.ZERO;
    private BigDecimal gasPrice = BigDecimal.ZERO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        startLoading();
        initView();
        web3JService = Web3jService.getInstance(this);
        initData();
    }

    private void initView() {
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(TransactionActivity.this, R.layout.address_spinner, WalletStorage.getInstance(TransactionActivity.this).getFullOnly()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                startLoading();
                initBalance();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @OnClick(R.id.btn_transfer)
    public void onViewClicked() {
        String to = toAddress.getText().toString().trim();
        String amountTxt = amount.getText().toString().trim();
        String from = spinner.getSelectedItem().toString().toLowerCase();

        if (TextUtils.isEmpty(to) || TextUtils.isEmpty(amountTxt) || TextUtils.isEmpty(from)) {
            snackError(layoutTransfer, getResources().getString(R.string.error_invalid_input));
            return;
        }

        BigDecimal transferAmount;
        try {
            transferAmount = new BigDecimal(amountTxt);
        } catch (NumberFormatException e) {
            snackError(layoutTransfer, getResources().getString(R.string.error_invalid_amount));
            return;
        }


        //If the amount transfer is higher show error
        BigDecimal gasExpended = new BigDecimal(Transfer.GAS_LIMIT).multiply(gasPrice);
        if (transferAmount.add(gasExpended).compareTo(balance) > 0) {
            BigDecimal maxTransfer = balance.subtract(gasExpended);
            if (maxTransfer.compareTo(BigDecimal.ZERO) <= 0) {
                snackError(layoutTransfer, getResources().getString(R.string.error_not_enough_balance));
            } else {
                snackError(layoutTransfer, getResources().getString(R.string.error_not_enough_balance) + " " + getString(R.string.error_not_enough_balance_max) + " " + maxTransfer.stripTrailingZeros().toPlainString());
            }
            return;
        }

        DialogUtil.askForPasswordAndDecode(this, R.string.prompt_password, new PasswordDialogCallback() {
            @Override
            public void success(String password) {
                if (!isPasswordValid(password)) {
                    snackError(layoutTransfer, getResources().getString(R.string.error_invalid_password));
                    return;
                }
                transferBTH(password, to, from, transferAmount);
            }

            @Override
            public void canceled() {

            }
        });
    }

    public void transferBTH(String password, String to, String from, BigDecimal amount) {
        Intent data = new Intent();
        data.putExtra(PARCEL_PARAM, new TransactionParcel(password, to, from, amount));
        setResult(RESULT_OK, data);
        finish();
    }

    private void initData() {
        Single.fromCallable(() -> web3JService.getGasPrice())
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<BigInteger>() {
                    @Override
                    public void onSuccess(BigInteger bigInteger) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BigDecimal weiBalance = new BigDecimal(bigInteger);
                                gasPrice = weiBalance.divide(ExchangeCalculator.ONE_ETHER);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
    }

    private void initBalance() {
        balance = BigDecimal.ZERO;
        Single.fromCallable(() -> web3JService.getBalance(spinner.getSelectedItem().toString().toLowerCase().trim()))
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<BigInteger>() {
                    @Override
                    public void onSuccess(BigInteger bigInteger) {
                        runOnUiThread(() -> {
                            BigDecimal weiBalance = new BigDecimal(bigInteger);
                            balance = weiBalance.divide(ExchangeCalculator.ONE_ETHER);
                            stopLoading();
                        });
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
    }

    private void startLoading() {
        btnTransfer.setVisibility(View.GONE);
        pgsBar.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        btnTransfer.setVisibility(View.VISIBLE);
        pgsBar.setVisibility(View.GONE);
    }
}
