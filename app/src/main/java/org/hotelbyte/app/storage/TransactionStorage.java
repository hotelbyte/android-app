package org.hotelbyte.app.storage;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class TransactionStorage {

    private Map<String, Set<String>> transactions;
    private static TransactionStorage instance;

    public static TransactionStorage getInstance(Context context) {
        if (instance == null) {
            synchronized (TransactionStorage.class) {
                if (instance == null) {
                    instance = new TransactionStorage(context);
                }
            }
        }
        return instance;
    }

    private TransactionStorage(Context context) {
        try {
            load(context);
        } catch (Exception e) {
            e.printStackTrace();
            transactions = new HashMap<>();
        }
    }

    public synchronized boolean add(String hash, String fromAddress, String toAddress, Context context) {
        String hashLowerCase = hash.toLowerCase();
        String fromAddressLowerCase = fromAddress.toLowerCase();
        String toAddressLowerCase = toAddress.toLowerCase();
        if (!transactions.containsKey(fromAddressLowerCase)) {
            transactions.put(fromAddressLowerCase, new HashSet<>());
        }
        if (!transactions.containsKey(toAddressLowerCase)) {
            transactions.put(toAddressLowerCase, new HashSet<>());
        }
        boolean isNew = transactions.get(fromAddressLowerCase).add(hashLowerCase);
        isNew = isNew | transactions.get(toAddressLowerCase).add(hashLowerCase);
        if (isNew) {
            save(context);
            return true;
        } else {
            return false;
        }
    }

    public synchronized Set<String> get(String address) {
        return transactions.get(address.toLowerCase());
    }

    public boolean contains(String hash) {
        for (Set<String> transactionList : transactions.values()) {
            if (transactionList.contains(hash.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public void removeTransaction(String hash, Context context) {

        boolean isDeleted = false;
        for (Set<String> transactionList : transactions.values()) {
            for (Iterator<String> transactionIt = transactionList.iterator(); transactionIt.hasNext(); ) {
                String transactionHash = transactionIt.next();
                if (transactionHash.equalsIgnoreCase(hash)) {
                    transactionIt.remove();
                    isDeleted = true;
                }
            }
        }
        if (isDeleted) {
            save(context);
        }
    }


    public synchronized void save(Context context) {
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(new File(context.getFilesDir(), "transaction_notify.dat"));
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(transactions);
            oos.close();
            fout.close();
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void load(Context context) throws IOException, ClassNotFoundException {
        FileInputStream fout = new FileInputStream(new File(context.getFilesDir(), "transaction_notify.dat"));
        ObjectInputStream oos = new ObjectInputStream(new BufferedInputStream(fout));
        transactions = (Map<String, Set<String>>) oos.readObject();
        oos.close();
        fout.close();
    }

}
