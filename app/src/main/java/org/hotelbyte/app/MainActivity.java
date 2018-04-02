package org.hotelbyte.app;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.hotelbyte.app.base.BaseAppCompatActivity;
import org.hotelbyte.app.onboarding.OnBoardingActivity;
import org.hotelbyte.app.wallet.AccountBean;
import org.hotelbyte.app.wallet.VerticalStepperAdapterDemoFragment;
import org.hotelbyte.app.wallet.VerticalStepperDemoFragment;
import org.hotelbyte.app.wallet.WalletFragment;

import java.io.InputStream;

public class MainActivity extends BaseAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WalletFragment.OnListFragmentInteractionListener {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            TextView username = navigationView.getHeaderView(0).findViewById(R.id.nav_username_text);
            username.setText(user.getDisplayName());
            TextView email = navigationView.getHeaderView(0).findViewById(R.id.nav_email_text);
            email.setText(user.getEmail());

            if (user.getPhotoUrl() != null) {
                new DownloadImageTask((navigationView.getHeaderView(0).findViewById(R.id.nav_image_profile)))
                        .execute(user.getPhotoUrl().toString());
            }
        }

        // By default open wallet view
        showWallet();
        navigationView.setCheckedItem(R.id.nav_wallet);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent ongoingIntent = new Intent(MainActivity.this, OnBoardingActivity.class);
            startActivity(ongoingIntent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_wallet) {
            showWallet();
        } else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_share) {

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showWallet() {
        WalletFragment walletFragment = WalletFragment.newInstance(2);
        walletFragment.setArguments(getIntent().getExtras());
        walletFragment.setEnterTransition(new Fade()
                .addTarget(R.id.fragment_wallet_list_constraint_layout));
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, walletFragment).commit();
    }

    @Override
    public void onListFragmentInteraction(AccountBean item) {

    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mImage = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mImage = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mImage;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
