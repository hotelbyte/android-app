package org.hotelbyte.app.interfaces;


public interface StoredWallet {

    String getPubKey();

    long getDateAdded();

    void setPubKey(String pubKey);

    void setDateAdded(long dateAdded);
}
