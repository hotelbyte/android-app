package org.hotelbyte.app.parcels;


import android.os.Parcel;
import android.os.Parcelable;


public class WalletGenParcel implements Parcelable {
    private String password;

    public WalletGenParcel(String password) {
        this.password = password;
    }

    public WalletGenParcel(Parcel in) {
        this.password = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getPassword());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WalletGenParcel> CREATOR
            = new Creator<WalletGenParcel>() {

        public WalletGenParcel createFromParcel(Parcel in) {
            return new WalletGenParcel(in);
        }

        public WalletGenParcel[] newArray(int size) {
            return new WalletGenParcel[size];
        }
    };


    public String getPassword() {
        return password;
    }
}
