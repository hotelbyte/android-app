package org.hotelbyte.wallet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.hotelbyte.wallet.base.BaseAppCompatActivity;
import org.hotelbyte.wallet.parcels.TransactionDetailParcel;
import org.hotelbyte.wallet.storage.AddressNameStorage;
import org.hotelbyte.wallet.util.Blockies;

import java.math.BigDecimal;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.grantland.widget.AutofitTextView;

import static org.hotelbyte.wallet.settings.Constants.EXPLORER_TRANSACTION_WEB_URL;
import static org.hotelbyte.wallet.settings.Constants.PARCEL_PARAM;
import static org.hotelbyte.wallet.settings.Settings.DATE_FORMATTER;

public class TransactionDetailActivity extends BaseAppCompatActivity implements View.OnClickListener {
    @BindView(R.id.transaction_hash)
    AutofitTextView transaction_hash;

    @BindView(R.id.from_address_icon)
    ImageView from_address_icon;
    @BindView(R.id.from_address_name)
    TextView from_address_name;
    @BindView(R.id.from_address)
    AutofitTextView from_address;

    @BindView(R.id.to_address_icon)
    ImageView to_address_icon;
    @BindView(R.id.to_address_name)
    TextView to_address_name;
    @BindView(R.id.to_address)
    AutofitTextView to_address;

    @BindView(R.id.amount)
    TextView amount;
    @BindView(R.id.amountfiat)
    TextView amountfiat;

    @BindView(R.id.transaction_date)
    TextView transaction_date;
    @BindView(R.id.transaction_block)
    TextView transaction_block;
    @BindView(R.id.nonce)
    TextView nonce;
    @BindView(R.id.gas_price)
    TextView gas_price;
    @BindView(R.id.gas_used)
    TextView gas_used;
    @BindView(R.id.transaction_cost)
    TextView transaction_cost;

    @BindView(R.id.open_in_browser)
    Button open_in_browser;

    private TransactionDetailParcel transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        ButterKnife.bind(this);
        this.transaction = getIntent().getParcelableExtra(PARCEL_PARAM);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
    }

    private void initView() {
        transaction_hash.setText(transaction.getHash());
        //From
        from_address_icon.setImageBitmap(Blockies.createIcon(transaction.getFromAddress()));
        String from_alias = AddressNameStorage.getInstance(this).get(transaction.getFromAddress());
        from_address_name.setText(from_alias);
        from_address.setText(transaction.getFromAddress());
        //To
        to_address_icon.setImageBitmap(Blockies.createIcon(transaction.getToAddress()));
        String to_alias = AddressNameStorage.getInstance(this).get(transaction.getToAddress());
        to_address_name.setText(to_alias);
        to_address.setText(transaction.getToAddress());

        amount.setText(transaction.getAmountEther().toPlainString() + " " + getString(R.string.coin_name));
        //TODO set FIAT in USD
        amountfiat.setVisibility(View.GONE);

        transaction_date.setText(DATE_FORMATTER.format(new Date(transaction.getTimestamp())));
        transaction_block.setText(transaction.getBlock() + " / " + transaction.getConfirmations() + " " + getString(R.string.block_confirmations));
        nonce.setText(String.valueOf(transaction.getNonce()));
        gas_price.setText(transaction.getGasPriceEther().toPlainString() + " " + getString(R.string.coin_alias));
        gas_used.setText(String.valueOf(transaction.getGasUsed()));
        BigDecimal totalGasCost = transaction.getGasPriceEther().multiply(BigDecimal.valueOf(transaction.getGasUsed()));
        transaction_cost.setText(totalGasCost.stripTrailingZeros().toPlainString() + " " + getString(R.string.coin_alias));

        open_in_browser.setOnClickListener((v) -> {
            String url = EXPLORER_TRANSACTION_WEB_URL + transaction.getHash();
            Intent linkIntent = new Intent(Intent.ACTION_VIEW);
            linkIntent.setData(Uri.parse(url));
            startActivity(linkIntent);
        });

    }


    @Override
    public void onClick(View v) {

    }
}
