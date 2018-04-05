package org.hotelbyte.app.wallet;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.hotelbyte.app.MainActivity;
import org.hotelbyte.app.R;
import org.hotelbyte.app.parcels.WalletGenParcel;
import org.hotelbyte.app.settings.Settings;

import moe.feng.common.stepperview.VerticalStepperItemView;

import static android.app.Activity.RESULT_OK;
import static org.hotelbyte.app.settings.Constants.PARCEL_PARAM;

public class NewWalletFragment extends Fragment {

    public static final int REQUEST_CODE = 401;

    private VerticalStepperItemView mSteppers[] = new VerticalStepperItemView[3];
    private EditText mPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_new, parent, false);

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
                mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                mainActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);
                mainActivity.configureToolBar();
            }
        });

        view.findViewById(R.id.btn_wallet_new).setOnClickListener(onClickListenerNewWallet);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mSteppers[0] = view.findViewById(R.id.stepper_0);
        mSteppers[1] = view.findViewById(R.id.stepper_1);

        VerticalStepperItemView.bindSteppers(mSteppers);

        Button mNextBtn0 = view.findViewById(R.id.button_next_0);
        mNextBtn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                EditText walletName = view.getRootView().findViewById(R.id.wallet_name_text);
                if (walletName != null && walletName.getText() != null) {
                    if (walletName.getText().length() > 3) {
                        mSteppers[0].nextStep();
                        mSteppers[0].setSummaryFinished(walletName.getText().toString() + getString(R.string.step_done));
                    } else {
                        mSteppers[0].setErrorText(getString(R.string.step_0_warn_01));
                    }
                } else {
                    // Never will occur
                    Snackbar.make(getActivity().findViewById(R.id.content_layout), getString(R.string.step_0_warn_00), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        Button mPrevBtn1 = view.findViewById(R.id.button_prev_1);
        mPrevBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSteppers[1].prevStep();
            }
        });

        Button mNextBtn1 = view.findViewById(R.id.button_next_1);
        mNextBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                mPassword = view.getRootView().findViewById(R.id.password);
                EditText passwordConfirm = view.getRootView().findViewById(R.id.passwordConfirm);

                if (mPassword != null && passwordConfirm != null
                        && mPassword.getText() != null && passwordConfirm.getText() != null) {

                    String passwordString = mPassword.getText().toString().trim();
                    String passwordConfirmString = passwordConfirm.getText().toString().trim();

                    String errorText = null;
                    if (passwordString.length() < 9) {
                        errorText = getString(R.string.step_1_warn_00);
                    } else if (!passwordString.equals(passwordConfirmString)) {
                        errorText = getString(R.string.step_1_warn_01);
                    } else if (!isPasswordSecure(passwordString)) {
                        errorText = getString(R.string.step_1_warn_02);
                    }

                    if (errorText != null) {
                        mSteppers[1].setErrorText(errorText);
                    } else {
                        // End final step
                        mSteppers[1].setErrorText(null);
                        mSteppers[1].setSummaryFinished(getString(R.string.step_1_done));
                        mSteppers[1].setState(2);
                        view.getRootView().findViewById(R.id.btn_wallet_new).setVisibility(View.VISIBLE);
                    }
                } else {
                    // Never will occur
                    Snackbar.make(getActivity().findViewById(R.id.content_layout), getString(R.string.step_1_warn_00), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    View.OnClickListener onClickListenerNewWallet = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            writeDownPassword();
        }
    };

    private boolean isPasswordSecure(String password) {
        boolean hasNumber = false;
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        for (int i = 0; i < password.length() && (!hasNumber || !hasUpperCase || !hasLowerCase); i++) {
            if (Character.isDigit((password.charAt(i)))) {
                hasNumber = true;
            } else if (Character.isUpperCase(password.charAt(i))) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(password.charAt(i))) {
                hasLowerCase = true;
            }
        }
        return hasNumber && hasUpperCase && hasLowerCase;
    }

    private void genWalletFile() {
        Settings.walletBeingGenerated = true; // Lock so a user can only generate one wallet at a time
        Intent data = new Intent();
        data.putExtra(PARCEL_PARAM, new WalletGenParcel(mPassword.getText().toString().trim()));
        Intent generatingService = new Intent(getActivity(), WalletCreatorService.class);
        generatingService.putExtra(PARCEL_PARAM, (WalletGenParcel) data.getParcelableExtra(PARCEL_PARAM));
        getActivity().startService(generatingService);
    }

    private void writeDownPassword() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) { // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        builder.setTitle(R.string.dialog_write_down_pw_title);
        builder.setMessage(getString(R.string.dialog_write_down_pw_text));
        builder.setPositiveButton(R.string.action_sign_in, (dialog, which) -> {
            genWalletFile();
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.dialog_back_button, (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
