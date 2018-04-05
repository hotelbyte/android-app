package org.hotelbyte.app.wallet;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.hotelbyte.app.MainActivity;
import org.hotelbyte.app.R;
import org.hotelbyte.app.bean.FullWallet;
import org.hotelbyte.app.parcels.WalletGenParcel;
import org.hotelbyte.app.settings.Settings;
import org.hotelbyte.app.storage.AddressNameStorage;
import org.hotelbyte.app.storage.WalletStorage;
import org.hotelbyte.app.util.Blockies;
import org.hotelbyte.app.util.NotificationChannelUtils;
import org.hotelbyte.app.util.OwnWalletUtils;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.hotelbyte.app.settings.Constants.PARCEL_PARAM;

public class WalletCreatorService extends IntentService {
    final int mNotificationId = 152;

    public WalletCreatorService() {
        super("WalletGen Service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        WalletGenParcel walletGenParcel = intent.getParcelableExtra(PARCEL_PARAM);

        sendNotification();

        try {
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();

            String walletAddress = OwnWalletUtils.generateWalletFile(walletGenParcel.getPassword(), ecKeyPair, new File(this.getFilesDir(), ""), true);
            //KeyStore keyStore = new KeyStore(new File(this.getFilesDir(), "").getAbsolutePath(), Geth.StandardScryptN, Geth.StandardScryptP);
            //String walletAddress = keyStore.newAccount(password).getAddress().getHex().toLowerCase();
            WalletStorage.getInstance(this).add(new FullWallet("0x" + walletAddress, walletAddress), this);
            AddressNameStorage.getInstance(this).put("0x" + walletAddress, "Wallet " + ("0x" + walletAddress).substring(0, 6), this);
            Settings.walletBeingGenerated = false;

            finished("0x" + walletAddress);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannelUtils.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(0x2d435c)
                .setTicker(getString(R.string.notification_wallgen_title))
                .setContentTitle(this.getResources().getString(R.string.wallet_gen_service_title))
                .setOngoing(true)
                .setProgress(0, 0, true)
                .setContentText(getString(R.string.notification_wallgen_maytake));
        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }

    private void finished(String address) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannelUtils.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(0x2d435c)
                .setTicker(getString(R.string.notification_wallgen_title))
                .setContentTitle(getString(R.string.notification_wallgen_finished))
                .setLargeIcon(Blockies.createIcon(address.toLowerCase()))
                .setAutoCancel(true)
                .setLights(Color.CYAN, 3000, 3000)
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText(getString(R.string.notification_click_to_view));

        if (android.os.Build.VERSION.SDK_INT >= 18) { // Android bug in 4.2, just disable it for everyone then...
            builder.setVibrate(new long[]{1000, 1000});
        }
        Intent main = new Intent(this, MainActivity.class);
        main.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }
}
