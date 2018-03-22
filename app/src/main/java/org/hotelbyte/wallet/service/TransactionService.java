package org.hotelbyte.wallet.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.hotelbyte.wallet.MainActivity;
import org.hotelbyte.wallet.R;
import org.hotelbyte.wallet.parcels.TransactionParcel;
import org.hotelbyte.wallet.settings.Constants;
import org.hotelbyte.wallet.storage.TransactionStorage;
import org.hotelbyte.wallet.storage.WalletStorage;
import org.hotelbyte.wallet.util.ExchangeCalculator;
import org.hotelbyte.wallet.util.NotificationChannelUtils;
import org.json.JSONException;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.request.RawTransaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;

import static org.hotelbyte.wallet.settings.Constants.PARCEL_PARAM;

public class TransactionService extends IntentService {
    private static final int SLEEP_DURATION = 15000;
    private static final int ATTEMPTS = 40;
    private static final int mNotificationId = 153;

    private Web3jService web3JService;

    public TransactionService() {
        super("Transaction Service");
        web3JService = Web3jService.getInstance(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        sendNotification();
        TransactionParcel transactionParcel = intent.getParcelableExtra(PARCEL_PARAM);
        try {
            Web3j web3j = Web3jFactory.build(new HttpService(Constants.WEB3J_URL));
            WalletStorage instance = WalletStorage.getInstance(getApplicationContext());
            Credentials credentials = instance.getFullWallet(getApplicationContext(), transactionParcel.getPassword(), transactionParcel.getFrom());

            BigInteger nonce = web3JService.getNonce(credentials);
            BigInteger gasPrice = web3JService.getGasPrice();
            RawTransaction tx = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    Transfer.GAS_LIMIT,
                    transactionParcel.getTo(),
                    transactionParcel.getAmount().multiply(ExchangeCalculator.ONE_ETHER).toBigInteger(),
                    ""
            );
            byte[] signedMessage = TransactionEncoder.signMessage(tx, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
            if (ethSendTransaction.hasError()) {
                throw new RuntimeException(ethSendTransaction.getError().getMessage());
            }
            String transactionHash = ethSendTransaction.getTransactionHash();
            TransactionStorage.getInstance(this).add(transactionHash, transactionParcel.getFrom(), transactionParcel.getTo(), this);
            boolean found = false;
            for (int i = 0; i < ATTEMPTS && !found; i++) {
                TransactionReceipt receiptOptional = web3JService.sendTransactionReceiptRequest(transactionHash);
                if (receiptOptional == null) {
                    Thread.sleep(SLEEP_DURATION);
                } else {
                    found = true;
                }
            }
            if (!found) {
                error("Transaction failed or too slow");
            } else {
                suc(ethSendTransaction.getTransactionHash());
            }
        } catch (IOException e) {
            e.printStackTrace();
            error(e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
            error(e.getMessage());
        } catch (CipherException e) {
            e.printStackTrace();
            error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            error(e.getMessage());
        }
    }


    private void suc(String hash) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannelUtils.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(0x2d435c)
                .setTicker(getString(R.string.notification_transfersuc))
                .setContentTitle(getString(R.string.notification_transfersuc))
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText(hash);

        Intent main = new Intent(this, MainActivity.class);
        main.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }

    private void error(String err) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannelUtils.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(0x2d435c)
                .setTicker(getString(R.string.notification_transferfail))
                .setContentTitle(getString(R.string.notification_transferfail))
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText(err);

        Intent main = new Intent(this, MainActivity.class);
        main.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannelUtils.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(0x2d435c)
                .setTicker(getString(R.string.notification_transferingticker))
                .setContentTitle(getString(R.string.notification_transfering_title))
                .setContentText(getString(R.string.notification_might_take_a_minute))
                .setOngoing(true)
                .setProgress(0, 0, true);
        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }
}
