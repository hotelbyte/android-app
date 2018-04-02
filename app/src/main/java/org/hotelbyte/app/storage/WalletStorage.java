package org.hotelbyte.app.storage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.hotelbyte.app.bean.FullWallet;
import org.hotelbyte.app.bean.WatchWallet;
import org.hotelbyte.app.interfaces.StoredWallet;
import org.hotelbyte.app.util.ExternalStorageHandler;
import org.json.JSONException;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Map;


public class WalletStorage {

    private ArrayList<StoredWallet> mapdb;
    private static WalletStorage instance;
    private String walletToExport; // Used as temp if users wants to export but still needs to grant write permission

    public static WalletStorage getInstance(Context context) {
        if (instance == null) {
            synchronized (WalletStorage.class) {
                if (instance == null) {
                    instance = new WalletStorage(context);
                }
            }
        }
        return instance;
    }

    private WalletStorage(Context context) {
        try {
            load(context);
        } catch (Exception e) {
            e.printStackTrace();
            mapdb = new ArrayList<>();
        }
        if (mapdb.size() == 0) // Try to find local wallets
            checkForWallets(context);
    }

    public synchronized boolean add(StoredWallet address, Context context) {
        for (int i = 0; i < mapdb.size(); i++) {
            if (mapdb.get(i).getPubKey().equalsIgnoreCase(address.getPubKey())) {
                return false;
            }
        }
        mapdb.add(address);
        save(context);
        return true;
    }

    public synchronized ArrayList<StoredWallet> get() {
        return mapdb;
    }

    public synchronized ArrayList<String> getFullOnly() {
        ArrayList<String> erg = new ArrayList<String>();
        if (mapdb.size() == 0) return erg;
        for (int i = 0; i < mapdb.size(); i++) {
            StoredWallet cur = mapdb.get(i);
            if (cur instanceof FullWallet)
                erg.add(cur.getPubKey());
        }
        return erg;
    }

    public synchronized boolean isFullWallet(String addr) {
        if (mapdb.size() == 0) return false;
        for (int i = 0; i < mapdb.size(); i++) {
            StoredWallet cur = mapdb.get(i);
            if (cur instanceof FullWallet && cur.getPubKey().equalsIgnoreCase(addr))
                return true;
        }
        return false;
    }

    public void removeWallet(String address, Context context) {
        int position = -1;
        for (int i = 0; i < mapdb.size(); i++) {
            if (mapdb.get(i).getPubKey().equalsIgnoreCase(address)) {
                position = i;
                break;
            }
        }
        if (position >= 0) {
            if (mapdb.get(position) instanceof FullWallet) { // IF full wallet delete private key too
                new File(context.getFilesDir(), address.substring(2, address.length())).delete();
            }
            mapdb.remove(position);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(address);
        editor.apply();
        save(context);
    }

    public void checkForWallets(Context c) {
        // Full wallets
        File[] wallets = c.getFilesDir().listFiles();
        if (wallets == null) {
            return;
        }
        for (int i = 0; i < wallets.length; i++) {
            if (wallets[i].isFile()) {
                if (wallets[i].getName().length() == 40) {
                    add(new FullWallet("0x" + wallets[i].getName(), wallets[i].getName()), c);
                }
            }
        }

        // Watch only
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().length() == 42 && !mapdb.contains(entry.getKey()))
                add(new WatchWallet(entry.getKey()), c);
        }
        if (mapdb.size() > 0)
            save(c);
    }


    public void setWalletForExport(String wallet) {
        walletToExport = wallet;
    }

    public Intent exportWallet(Activity c) {
        return exportWallet(c, false);
    }


    public static String stripWalletName(String s) {
        if (s.lastIndexOf("--") > 0)
            s = s.substring(s.lastIndexOf("--") + 2);
        if (s.endsWith(".json"))
            s = s.substring(0, s.indexOf(".json"));
        return s;
    }

    private Intent exportWallet(Activity c, boolean already) {
        if (walletToExport == null) {
            return null;
        }
        if (walletToExport.startsWith("0x")) {
            walletToExport = walletToExport.substring(2);
        }

        if (ExternalStorageHandler.hasPermission(c)) {
            File folder = new File(Environment.getExternalStorageDirectory(), "HotelByte");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File storeFile = new File(folder, walletToExport + ".json");
            try {
                copyFile(new File(c.getFilesDir(), walletToExport), storeFile);
            } catch (IOException e) {
                return null;
            }


            Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() + "/HotelByte/");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(selectedUri, "resource/folder");
            return intent;
        } else if (!already) {
            ExternalStorageHandler.askForPermission(c);
            return exportWallet(c, true);
        } else {
            return null;
        }
    }


    private void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    public Credentials getFullWallet(Context context, String password, String wallet) throws IOException, JSONException, CipherException {
        if (wallet.startsWith("0x")) {
            wallet = wallet.substring(2, wallet.length());
        }
        return WalletUtils.loadCredentials(password, new File(context.getFilesDir(), wallet));
    }


    public synchronized void save(Context context) {
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(new File(context.getFilesDir(), "wallets.dat"));
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(mapdb);
            oos.close();
            fout.close();
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void load(Context context) throws IOException, ClassNotFoundException {
        FileInputStream fout = new FileInputStream(new File(context.getFilesDir(), "wallets.dat"));
        ObjectInputStream oos = new ObjectInputStream(new BufferedInputStream(fout));
        mapdb = (ArrayList<StoredWallet>) oos.readObject();
        oos.close();
        fout.close();
    }

}
