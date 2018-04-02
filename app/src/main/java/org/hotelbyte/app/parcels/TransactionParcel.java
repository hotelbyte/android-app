package org.hotelbyte.app.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

public class TransactionParcel implements Parcelable {

    private String password;
    private String to;
    private String from;
    private BigDecimal amount;

    public TransactionParcel(String password, String to, String from, BigDecimal amount) {
        this.password = password;
        this.to = to;
        this.from = from;
        this.amount = amount;
    }

    public TransactionParcel(Parcel in) {
        this.password = in.readString();
        this.to = in.readString();
        this.from = in.readString();
        this.amount = new BigDecimal(in.readString());
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(password);
        dest.writeString(to);
        dest.writeString(from);
        dest.writeString(amount.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<TransactionParcel> CREATOR
            = new Parcelable.Creator<TransactionParcel>() {

        public TransactionParcel createFromParcel(Parcel in) {
            return new TransactionParcel(in);
        }

        public TransactionParcel[] newArray(int size) {
            return new TransactionParcel[size];
        }
    };

    public String getPassword() {
        return password;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
