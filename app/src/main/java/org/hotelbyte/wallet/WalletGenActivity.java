package org.hotelbyte.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.hotelbyte.wallet.base.BaseAppCompatActivity;
import org.hotelbyte.wallet.parcels.WalletGenParcel;
import org.hotelbyte.wallet.settings.Settings;
import org.hotelbyte.wallet.util.DialogUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.hotelbyte.wallet.settings.Constants.PARCEL_PARAM;

public class WalletGenActivity extends BaseAppCompatActivity {

    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.passwordConfirm)
    EditText pwdConfirm;
    @BindView(R.id.email_sign_in_button)
    Button btnGenerate;
    @BindView(R.id.layout_gen)
    LinearLayout layoutGen;
    public static final int REQUEST_CODE = 401;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_gen);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.email_sign_in_button)
    public void onViewClicked() {
        String pwdtxt = password.getText().toString().trim();
        String pwdAgain = pwdConfirm.getText().toString().trim();

        if (!pwdAgain.equals(pwdtxt)) {
            snackError(layoutGen, getResources().getString(R.string.error_incorrect_password));
            return;
        }

        if (!isPasswordValid(pwdtxt)) {
            snackError(layoutGen, getResources().getString(R.string.error_invalid_password));
            return;
        }

        if (!isPasswordSecure(pwdtxt)) {
            snackError(layoutGen, getResources().getString(R.string.error_secure_password));
            return;
        }

        DialogUtil.writeDownPassword(this);
    }


    public void genWalletFile() {
        Settings.walletBeingGenerated = true; // Lock so a user can only generate one wallet at a time
        Intent data = new Intent();
        data.putExtra(PARCEL_PARAM, new WalletGenParcel(password.getText().toString().trim()));
        setResult(RESULT_OK, data);
        finish();
    }
}
