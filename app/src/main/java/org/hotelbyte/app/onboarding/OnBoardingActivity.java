package org.hotelbyte.app.onboarding;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import org.hotelbyte.app.MainActivity;
import org.hotelbyte.app.R;
import org.hotelbyte.app.animations.DepthPageTransformer;
import org.hotelbyte.app.base.BaseAppCompatActivity;
import org.hotelbyte.app.util.Analytics;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author lexfaraday
 *
 * This activity is used for the first usage of the App
 */
public class OnBoardingActivity extends BaseAppCompatActivity implements View.OnClickListener, View.OnLongClickListener, View.OnCreateContextMenuListener {

    private FirebaseAnalytics mFirebaseAnalytics;
    private SharedPreferences sharedPreferences;

    private ViewPager mSlideViewPager;
    private LinearLayout mDotsLayout;
    private TextView[] mDots;
    private SliderAdapter sliderAdapter;

    private Timer timer;
    private int currentPage = 0;

    private static final int RC_SIGN_IN = 123;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        sharedPreferences = getSharedPreferences(MainActivity.class.getPackage().getName(), MODE_PRIVATE);

        // Set up the Slider Adapter
        mSlideViewPager = findViewById(R.id.slideViewPager);
        mDotsLayout = findViewById(R.id.dotsLayout);
        sliderAdapter = new SliderAdapter(this);
        mSlideViewPager.setAdapter(sliderAdapter);
        addDotsIndicator(0);
        mSlideViewPager.addOnPageChangeListener(viewListener);

        // Set up auto animations
        mSlideViewPager.setPageTransformer(true, new DepthPageTransformer());
        autoSwipeSlides();

        findViewById(R.id.sign_in_button).setOnClickListener(onClickListenerSignIn);
        findViewById(R.id.btnGetStarted).setOnClickListener(onClickListenerGetStarted);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
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
                Log.w("ooooo", "Google sign in failed", e);
                Snackbar.make(findViewById(R.id.main_onboarding), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                // [START_EXCLUDE]
               // updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("ooooo", "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("ooooo", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                // Apply activity transition
                                getWindow().setExitTransition(new Explode());
                                startActivity(new Intent(OnBoardingActivity.this, MainActivity.class), ActivityOptions.makeSceneTransitionAnimation(OnBoardingActivity.this).toBundle());
                            } else {
                                // Swap without transition
                                startActivity(new Intent(OnBoardingActivity.this, MainActivity.class));
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("ooooo", "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_onboarding), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop timer
        timer.cancel();
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
                mSlideViewPager.setCurrentItem(++currentPage, true);
                if (currentPage == mSlideViewPager.getAdapter().getCount() - 1) {
                    currentPage = -1;
                }
            }
        };

        timer = new Timer(); // This will create a new Thread
        timer.schedule(new TimerTask() { // task to be scheduled
            @Override
            public void run() {
                handler.post(Update);
            }
        }, swipeDelay, swipePeriod);
    }

    private void addDotsIndicator(int position) {
        mDots = new TextView[3];
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

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
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

            // Avoid to see the ongoing activity when we will close and will run again the App
            sharedPreferences.edit().putBoolean("firstrun", false).apply();

            // Check if we're running on Android 5.0 or higher
         /*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Apply activity transition
                getWindow().setExitTransition(new Explode());
                startActivity(new Intent(OnBoardingActivity.this, MainActivity.class), ActivityOptions.makeSceneTransitionAnimation(OnBoardingActivity.this).toBundle());
            } else {
                // Swap without transition
                startActivity(new Intent(OnBoardingActivity.this, MainActivity.class));
            }*/

        }
    };

    @Override
    public void onClick(View view) {

    }
}
