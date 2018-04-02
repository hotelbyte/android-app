package org.hotelbyte.app.parcels;


import android.os.Parcel;
import android.os.Parcelable;

public class WalletDetailParcel implements Parcelable {
    private String address;

    public WalletDetailParcel(String address) {
        this.address = address;
    }

    public WalletDetailParcel(Parcel in) {
        this.address = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getAddress());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<WalletDetailParcel> CREATOR
            = new Parcelable.Creator<WalletDetailParcel>() {

        public WalletDetailParcel createFromParcel(Parcel in) {
            return new WalletDetailParcel(in);
        }

        public WalletDetailParcel[] newArray(int size) {
            return new WalletDetailParcel[size];
        }
    };

    public String getAddress() {
        return address;
    }

}
