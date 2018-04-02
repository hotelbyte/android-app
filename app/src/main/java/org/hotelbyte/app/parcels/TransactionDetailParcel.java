package org.hotelbyte.app.parcels;


import android.os.Parcel;
import android.os.Parcelable;

import org.hotelbyte.app.util.ExchangeCalculator;

import java.math.BigDecimal;

public class TransactionDetailParcel implements Parcelable {
    private String hash;
    private String fromAddress;
    private String toAddress;
    private long amount;
    private long block;
    private long confirmations;
    private long timestamp;
    private long nonce;
    private long gasPrice;
    private long gasUsed;

    public TransactionDetailParcel(String hash, String fromAddress, String toAddress, long amount, long block, long confirmations, long timestamp, long nonce, long gasPrice, long gasUsed) {
        this.hash = hash;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amount = amount;
        this.block = block;
        this.confirmations = confirmations;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.gasPrice = gasPrice;
        this.gasUsed = gasUsed;
    }

    public TransactionDetailParcel(Parcel in) {
        this.hash = in.readString();
        this.fromAddress = in.readString();
        this.toAddress = in.readString();
        this.amount = in.readLong();
        this.block = in.readLong();
        this.confirmations = in.readLong();
        this.timestamp = in.readLong();
        this.nonce = in.readLong();
        this.gasPrice = in.readLong();
        this.gasUsed = in.readLong();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hash);
        dest.writeString(fromAddress);
        dest.writeString(toAddress);
        dest.writeLong(amount);
        dest.writeLong(block);
        dest.writeLong(confirmations);
        dest.writeLong(timestamp);
        dest.writeLong(nonce);
        dest.writeLong(gasPrice);
        dest.writeLong(gasUsed);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TransactionDetailParcel> CREATOR
            = new Creator<TransactionDetailParcel>() {

        public TransactionDetailParcel createFromParcel(Parcel in) {
            return new TransactionDetailParcel(in);
        }

        public TransactionDetailParcel[] newArray(int size) {
            return new TransactionDetailParcel[size];
        }
    };

    public String getHash() {
        return hash;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public long getAmount() {
        return amount;
    }

    public BigDecimal getAmountEther() {
        return new BigDecimal(amount).divide(ExchangeCalculator.ONE_ETHER).stripTrailingZeros();
    }

    public long getBlock() {
        return block;
    }

    public long getConfirmations() {
        return confirmations;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getNonce() {
        return nonce;
    }

    public long getGasPrice() {
        return gasPrice;
    }

    public BigDecimal getGasPriceEther() {
        return new BigDecimal(gasPrice).divide(ExchangeCalculator.ONE_ETHER).stripTrailingZeros();
    }

    public long getGasUsed() {
        return gasUsed;
    }
}
