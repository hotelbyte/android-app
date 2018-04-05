package org.hotelbyte.app;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.hotelbyte.app.base.BaseAppCompatActivity;
import org.hotelbyte.app.onboarding.OnBoardingActivity;
import org.hotelbyte.app.parcels.WalletGenParcel;
import org.hotelbyte.app.util.ImageUtils;
import org.hotelbyte.app.wallet.AccountBean;
import org.hotelbyte.app.wallet.ImportWalletFragment;
import org.hotelbyte.app.wallet.NewWalletFragment;
import org.hotelbyte.app.wallet.WalletCreatorService;
import org.hotelbyte.app.wallet.WalletFragment;

import static org.hotelbyte.app.settings.Constants.PARCEL_PARAM;

/**
 * Main activity with fragment child
 */
public class MainActivity extends BaseAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WalletFragment.OnListFragmentInteractionListener {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureToolBar();

        // By default open wallet fragment
        showMainWallet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Action bar items handler
     *
     * @param item menu item
     * @return item state
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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

    /**
     * Navigation bar items handler
     * @param item nav item
     * @return item state
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_wallet) {
            showMainWallet();
        } else if (id == R.id.nav_profile) {
            // showProfile();
        } else if (id == R.id.nav_share) {
            // showShareDialog();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    public void onListFragmentInteraction(AccountBean item) {
        // TODO
    }

    /**
     * Nav item fragment
     */
    public void showMainWallet() {
        showFragment(WalletFragment.newInstance(false), R.id.fragment_wallet_list_constraint_layout);
    }

    /**
     * This fragment will be child of WalletFragment in terms of the navigation flow.
     */
    public void showNewWallet() {
        showFragment(new NewWalletFragment(), R.id.fragment_new_wallet_container);
    }

    /**
     * This fragment will be child of WalletFragment in terms of the navigation flow.
     */
    public void showImportWallet() {
        showFragment(new ImportWalletFragment(), R.id.fragment_wallet_import_container);
    }

    /**
     * Generic method to show with animations fragments on main container
     * @param fragment any fragment
     * @param target top layout of the fragment
     */
    private void showFragment(Fragment fragment, int target) {
        fragment.setArguments(getIntent().getExtras());
        fragment.setEnterTransition(new Fade().addTarget(target));
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    /**
     * Set tool bar with navigation view
     */
    public void configureToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        configureNavBar();
    }

    private void configureNavBar() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            TextView username = navigationView.getHeaderView(0).findViewById(R.id.nav_username_text);
            username.setText(user.getDisplayName());
            TextView email = navigationView.getHeaderView(0).findViewById(R.id.nav_email_text);
            email.setText(user.getEmail());

            if (user.getPhotoUrl() != null) {
                new ImageUtils.DownloadImageTask((navigationView.getHeaderView(0).findViewById(R.id.nav_image_profile)))
                        .execute(user.getPhotoUrl().toString());
            }
        }
        navigationView.setCheckedItem(R.id.nav_wallet);
    }
}
