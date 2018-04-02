package org.hotelbyte.app.service;

import android.content.Context;

import org.hotelbyte.app.settings.Constants;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;

public class Web3jService {
    private static Web3jService instance;

    private Context context;
    private Web3j web3j;

    public static Web3jService getInstance(Context context) {
        if (instance == null)
            instance = new Web3jService(context);
        return instance;
    }

    private Web3jService(Context context) {
        this.context = context;
        web3j = Web3jFactory.build(new HttpService(Constants.WEB3J_URL));
    }

    public TransactionReceipt sendTransactionReceiptRequest(
            String transactionHash) throws IOException {
        EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
        if (transactionReceipt.hasError() && !transactionReceipt.getError().getMessage().equalsIgnoreCase("unknown transaction")) {
            throw new RuntimeException(transactionReceipt.getError().getMessage());
        }
        return transactionReceipt.getTransactionReceipt();
    }

    public BigInteger getNonce(Credentials credentials) throws IOException {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.PENDING).send();
        return ethGetTransactionCount.getTransactionCount();
    }

    public BigInteger getGasPrice() {
        BigInteger gasPrice = BigInteger.ZERO;
        try {
            EthGasPrice ethGetBalance = web3j.ethGasPrice().send();
            gasPrice = ethGetBalance.getGasPrice();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gasPrice;
    }

    public BigInteger getBalance(String address) {
        BigInteger balance = new BigInteger("0");
        try {
            EthGetBalance ethGetBalance = web3j.ethGetBalance(address, () -> "latest").send();
            balance = ethGetBalance.getBalance();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return balance;
    }

    public BigInteger getTransactionList(String address) {
        BigInteger count = new BigInteger("0");
        try {
            EthGetTransactionCount ethCount = web3j.ethGetTransactionCount(address, () -> "latest").send();
            count = ethCount.getTransactionCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    public EthTransaction getTransaction(String transactionHash) {
        try {
            return web3j.ethGetTransactionByHash(transactionHash).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BigInteger getCurrentBlockNumber() {
        try {
            EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();
            return ethBlockNumber.getBlockNumber();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public EthBlock getBlock(BigInteger block) {
        try {
            return web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(block), false).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
