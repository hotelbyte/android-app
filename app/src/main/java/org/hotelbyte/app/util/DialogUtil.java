package org.hotelbyte.app.util;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.hotelbyte.app.R;
import org.hotelbyte.app.interfaces.PasswordDialogCallback;
import org.hotelbyte.app.interfaces.SimpleStringCallback;
import org.hotelbyte.app.wallet.NewWalletFragment;
import org.hotelbyte.app.wallet.WalletFragment;

public class DialogUtil {

   public static void askForPasswordAndDecode(Activity ac, int title, final PasswordDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ac, R.style.AlertDialogTheme);
        builder.setTitle(title);

        final EditText input = new EditText(ac);
        final CheckBox showpw = new CheckBox(ac);
        showpw.setText(R.string.password_in_clear_text);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());

        LinearLayout container = new LinearLayout(ac);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.topMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.bottomMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.leftMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params2.rightMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);
        showpw.setLayoutParams(params2);

        container.addView(input);
        container.addView(showpw);
        builder.setView(container);

        showpw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            input.setSelection(input.getText().length());
        });

        builder.setView(container);
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager inputMgr = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
        builder.setPositiveButton("Ok", (dialog, which) -> {
            InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
            callback.success(input.getText().toString());
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
            callback.canceled();
            dialog.cancel();
        });

        builder.show();
    }

    public static void askFor(Activity ac, int title, String defaultValue, final SimpleStringCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ac, R.style.AlertDialogTheme);
        builder.setTitle(title);

        final EditText input = new EditText(ac);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(defaultValue);

        LinearLayout container = new LinearLayout(ac);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.topMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.bottomMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);

        input.setLayoutParams(params);


        container.addView(input);
        builder.setView(container);


        builder.setView(container);
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager inputMgr = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
        builder.setPositiveButton("Ok", (dialog, which) -> {
            InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
            callback.success(input.getText().toString());
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
            callback.canceled();
            dialog.cancel();
        });

        builder.show();
    }
}
