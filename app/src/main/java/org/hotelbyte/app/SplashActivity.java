package org.hotelbyte.app;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.hotelbyte.app.base.BaseAppCompatActivity;
import org.hotelbyte.app.jobs.JobSchedulerService;
import org.hotelbyte.app.onboarding.OnBoardingActivity;
import org.hotelbyte.app.util.NotificationChannelUtils;

import rx.Single;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

/**
 * Splash activity
 */
public class SplashActivity extends BaseAppCompatActivity {

    private static final long MIN_SPLASH_TIME = 1000L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        long start = System.currentTimeMillis();

        // Prepare main intents
        Intent ongoingIntent = new Intent(SplashActivity.this, OnBoardingActivity.class);
        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);

        // Find user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(ongoingIntent);
        } else {
            startActivity(mainIntent);
        }

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
