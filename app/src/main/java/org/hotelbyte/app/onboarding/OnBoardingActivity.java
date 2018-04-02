package org.hotelbyte.app.onboarding;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import org.hotelbyte.app.MainActivity;
import org.hotelbyte.app.R;
import org.hotelbyte.app.animations.DepthPageTransformer;
import org.hotelbyte.app.base.BaseAppCompatActivity;
import org.hotelbyte.app.util.Analytics;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author lexfaraday
 *
 * This activity is used for the first usage of the App and login
 */
public class OnBoardingActivity extends BaseAppCompatActivity implements View.OnClickListener, View.OnLongClickListener, View.OnCreateContextMenuListener {

    private static final String TAG = "OnBoarding";
    private static final int RC_SIGN_IN = 123;

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private ViewPager mSlideViewPager;
    private LinearLayout mDotsLayout;
    private Timer mTimer;
    private int mCurrentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing);

        // Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Set up the Slider Adapter
        mSlideViewPager = findViewById(R.id.slideViewPager);
        mDotsLayout = findViewById(R.id.dotsLayout);
        SliderAdapter sliderAdapter = new SliderAdapter(this);
        mSlideViewPager.setAdapter(sliderAdapter);
        mSlideViewPager.addOnPageChangeListener(onPageChangeListenerSlider);
        addDotsIndicator(0);

        // Buttons
        findViewById(R.id.sign_in_button).setOnClickListener(onClickListenerSignIn);
        findViewById(R.id.btnGetStarted).setOnClickListener(onClickListenerGetStarted);

        // Prepare google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        // Set up auto animations
        mSlideViewPager.setPageTransformer(true, new DepthPageTransformer());
        autoSwipeSlides();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Snackbar.make(findViewById(R.id.main_onboarding), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop mTimer
        mTimer.cancel();
    }

    @Override
    public void onClick(View view) {

    }

    View.OnClickListener onClickListenerSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Analytics.logEvent(mFirebaseAnalytics, "goolge_signin", "btnSignIn", "btnSignIn", "button");
            signIn();
        }
    };

    View.OnClickListener onClickListenerGetStarted = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Analytics.logEvent(mFirebaseAnalytics, "get_started", "btnGetStarted", "getStarted", "button");
            // phone login activity
        }
    };

    ViewPager.OnPageChangeListener onPageChangeListenerSlider = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, show main activity
                            Log.d(TAG, "signInWithCredential:success");

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                // Apply activity transition
                                getWindow().setExitTransition(new Explode());
                                startActivity(new Intent(OnBoardingActivity.this, MainActivity.class), ActivityOptions.makeSceneTransitionAnimation(OnBoardingActivity.this).toBundle());
                            } else {
                                // Swap without transition
                                startActivity(new Intent(OnBoardingActivity.this, MainActivity.class));
                            }
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_onboarding), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    /**
     * Auto swipe between slide views
     */
    private void autoSwipeSlides() {
        final long swipeDelay = 0;
        final long swipePeriod = 6000; // Time between executions.

        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                mSlideViewPager.setCurrentItem(++mCurrentPage, true);
                if (mCurrentPage == mSlideViewPager.getAdapter().getCount() - 1) {
                    mCurrentPage = -1;
                }
            }
        };

        mTimer = new Timer(); // This will create a new Thread
        mTimer.schedule(new TimerTask() { // task to be scheduled
            @Override
            public void run() {
                handler.post(Update);
            }
        }, swipeDelay, swipePeriod);
    }

    private void addDotsIndicator(int position) {
        TextView[] mDots = new TextView[3];
        mDotsLayout.removeAllViews();

        for (int i = 0; i<mDots.length; i++) {
            TextView mDot = new TextView(this);
            mDot.setText(Html.fromHtml("&#8226;"));
            mDot.setTextSize(35);
            mDot.setTextColor(getResources().getColor(R.color.transparentWhite));
            mDots[i] = mDot;
            mDotsLayout.addView(mDot);
        }

        mDots[position].setTextColor(getResources().getColor(R.color.white));
    }
}
