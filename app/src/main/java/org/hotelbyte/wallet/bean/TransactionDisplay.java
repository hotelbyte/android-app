package org.hotelbyte.wallet.bean;

import org.hotelbyte.wallet.util.ExchangeCalculator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;

public class TransactionDisplay {
    private String hash;
    private TransactionType type;
    private String fromAddress;
    private String toAddress;
    private BigInteger amount;
    private BigInteger block;
    private long confirmations;
    private Date sendDate;
    private BigInteger nonce;
    private BigInteger gasPrice;
    private BigInteger gasUsed;

    public TransactionDisplay(String hash, TransactionType type, String fromAddress, String toAddress, BigInteger amount, BigInteger block, long confirmations, Date sendDate, BigInteger nonce, BigInteger gasPrice, BigInteger gasUsed) {
        this.hash = hash;
        this.type = type;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amount = amount;
        this.block = block;
        this.confirmations = confirmations;
        this.sendDate = sendDate;
        this.nonce = nonce;
        this.gasPrice = gasPrice;
        this.gasUsed = gasUsed;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public double getAmountEther() {
        return new BigDecimal(amount).divide(ExchangeCalculator.ONE_ETHER, 8, BigDecimal.ROUND_UP).stripTrailingZeros().doubleValue();
    }

    public long getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(long confirmations) {
        this.confirmations = confirmations;
    }

    public BigInteger getBlock() {
        return block;
    }

    public void setBlock(BigInteger block) {
        this.block = block;
    }


    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionDisplay that = (TransactionDisplay) o;

        return hash != null ? hash.equals(that.hash) : that.hash == null;
    }

    @Override
    public int hashCode() {
        return hash != null ? hash.hashCode() : 0;
    }

    public static final Comparator<TransactionDisplay> COMPARATOR = (o1, o2) -> {
        Date date1 = o1 != null ? o1.getSendDate() : null;
        Date date2 = o2 != null ? o2.getSendDate() : null;
        if (date1 == null && date2 != null) {
            return -1;
        } else if (date1 != null && date2 == null) {
            return 1;
        } else if (date1 != null && date2 != null) {
            return -date1.compareTo(date2);
        } else {
            return 0;
        }
    };

}
