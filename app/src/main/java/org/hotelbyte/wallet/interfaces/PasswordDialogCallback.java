package org.hotelbyte.wallet.interfaces;


public interface PasswordDialogCallback {

    void success(String password);

    void canceled();
}
