package com.example.instagram;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.instagram.Fragment.HomeFragment;
import com.example.instagram.Fragment.NotificationFragment;
import com.example.instagram.Fragment.ProfileFragment;
import com.example.instagram.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;
    String tag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation_bar);

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        // open commenter profile if clicked (sent from comment adapter)
        Bundle intent = getIntent().getExtras();
        if (intent != null) {   // opening other user profile from comments
            String publisherId = intent.getString("publisherId");

            SharedPreferences.Editor e = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            e.putString("profileId", publisherId);
            e.apply();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment(), "profileFragment").commit();
        } else {
            // open my profile
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment(), "homeFragment").commit();
        }



    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.home_nav:
                            selectedFragment = new HomeFragment();
                            tag = "homeFragment";
                            break;
                        case R.id.search_nav:
                            selectedFragment = new SearchFragment();
                            tag = "searchFragment";
                            break;
                        case R.id.add_nav:
                            selectedFragment = null;
                            tag = "";
                            startActivity(new Intent(MainActivity.this, PostActivity.class));
                            finish();
                            break;
                        case R.id.favorite_nav:
                            selectedFragment = new NotificationFragment();
                            tag = "notificationFragment";
                            break;
                        case R.id.profile_nav:
                            SharedPreferences.Editor e = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                            e.putString("profileId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            e.apply();
                            selectedFragment = new ProfileFragment();
                            tag = "profileFragment";
                            break;
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment, tag).commit();
                    }

                    return true;
                }
            };

    @Override
    public void onBackPressed() {
        Fragment homeFragment = getSupportFragmentManager().findFragmentByTag("homeFragment");
        Fragment postDetailsFragment = getSupportFragmentManager().findFragmentByTag("PostDetailsFragment");

        if (homeFragment != null && homeFragment.isVisible()) {
            Log.v("Main Activity", "homeFragment status : "+homeFragment);
            super.onBackPressed();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment(), "homeFragment").commit();
            bottomNavigationView.setSelectedItemId(R.id.home_nav);
        }
    }
}
