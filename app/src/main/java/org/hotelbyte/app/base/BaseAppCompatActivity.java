package org.hotelbyte.app.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class BaseAppCompatActivity extends AppCompatActivity {

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean isPasswordValid(String password) {
        return password.length() >= 9;
    }

    public boolean isPasswordSecure(String password) {
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

    public void snackError(View layout, String s) {
        if (layout == null) {
            return;
        }
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
        //Show snack
        Snackbar.make(layout, s, Snackbar.LENGTH_LONG).show();
    }

    public void snackMessage(View layout, String s) {
        //Show snack
        Snackbar.make(layout, s, Snackbar.LENGTH_SHORT).show();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}