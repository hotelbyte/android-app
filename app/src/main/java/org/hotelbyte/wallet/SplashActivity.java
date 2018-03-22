package org.hotelbyte.wallet;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.hotelbyte.wallet.base.BaseAppCompatActivity;
import org.hotelbyte.wallet.jobs.JobSchedulerService;
import org.hotelbyte.wallet.util.NotificationChannelUtils;

import rx.Single;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

public class SplashActivity extends BaseAppCompatActivity {
    private static final long MIN_SPLASH_TIME = 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long start = System.currentTimeMillis();
        // Start home activity
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelUtils.createChannel(this);
        }
        Single.fromCallable(() -> scheduleJob())
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<Boolean>() {
                    @Override
                    public void onSuccess(Boolean initialized) {
                        if (initialized) {
                            Log.d("JOB", "Job scheduled successfully!");
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed < MIN_SPLASH_TIME) {
            try {
                Thread.sleep(MIN_SPLASH_TIME - elapsed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Close splash activity
        finish();
    }

    private boolean scheduleJob() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ComponentName serviceName = new ComponentName(this, JobSchedulerService.class);
            JobInfo jobInfo = new JobInfo.Builder(1, serviceName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_METERED)
                    .setPeriodic(15L * 60L * 1000L)//Every 15 minutes
                    .build();
            JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int result = scheduler.schedule(jobInfo);
            if (result == JobScheduler.RESULT_SUCCESS) {
                Log.d("JOB", "Job scheduled successfully!");
                return true;
            }
        }
        return false;
    }
}
