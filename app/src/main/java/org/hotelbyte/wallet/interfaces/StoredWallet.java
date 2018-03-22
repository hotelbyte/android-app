package org.hotelbyte.wallet.interfaces;


public interface StoredWallet {

    String getPubKey();

    long getDateAdded();

    void setPubKey(String pubKey);

    void setDateAdded(long dateAdded);
}
