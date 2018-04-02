package org.hotelbyte.app.jobs;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import org.hotelbyte.app.MainActivity;
import org.hotelbyte.app.R;
import org.hotelbyte.app.interfaces.StoredWallet;
import org.hotelbyte.app.parcels.WalletDetailParcel;
import org.hotelbyte.app.service.NetworkApiExplorerService;
import org.hotelbyte.app.service.Web3jService;
import org.hotelbyte.app.storage.TransactionStorage;
import org.hotelbyte.app.storage.WalletStorage;
import org.hotelbyte.app.util.NotificationChannelUtils;
import org.web3j.protocol.core.methods.response.EthTransaction;

import java.util.ArrayList;
import java.util.Map;

import rx.Single;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

import static org.hotelbyte.app.settings.Constants.PARCEL_PARAM;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {

    public JobSchedulerService() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        final ArrayList<StoredWallet> storedWallets = new ArrayList<>(WalletStorage.getInstance(this).get());
        for (StoredWallet cur : storedWallets) {
            String address = cur.getPubKey();
            NetworkApiExplorerService networkApiExplorerService = NetworkApiExplorerService.getInstance(this);
            networkApiExplorerService.callTransactions(address, (response) -> processResponse(address, response));
        }
        return false;
    }

    private void processResponse(String address, Map<String, Boolean> transactionMap) {
        for (Map.Entry<String, Boolean> transaction : transactionMap.entrySet()) {
            //Is not the sender
            if (transaction.getValue() != null && !transaction.getValue()) {
                String transactionHash = transaction.getKey();
                if (!TransactionStorage.getInstance(this).contains(transactionHash)) {
                    sendTransactionNotification(address, transactionHash);
                }
            }
        }
    }

    private void sendTransactionNotification(String address, String transactionHash) {
        Single.fromCallable(() -> Web3jService.getInstance(this).getTransaction(transactionHash))
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<EthTransaction>() {
                    @Override
                    public void onSuccess(EthTransaction ethTransaction) {
                        if (ethTransaction != null && ethTransaction.getTransaction() != null) {
                            boolean sendNotification = !TransactionStorage.getInstance(JobSchedulerService.this).contains(transactionHash);
                            if (sendNotification && TransactionStorage.getInstance(JobSchedulerService.this).add(transactionHash, ethTransaction.getTransaction().getFrom(), ethTransaction.getTransaction().getTo(), JobSchedulerService.this)) {
                                sendNotification(address, transactionHash);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });

    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void sendNotification(String address, String transactionHash) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannelUtils.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(0x2d435c)
                .setTicker(getString(R.string.notification_transfer_received))
                .setContentTitle(getString(R.string.notification_transfer_received))
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText(transactionHash);

        Intent detail = new Intent(this, MainActivity.class);
        detail.putExtra(PARCEL_PARAM, new WalletDetailParcel(address));

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                detail, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(transactionHash.hashCode(), builder.build());
    }
}