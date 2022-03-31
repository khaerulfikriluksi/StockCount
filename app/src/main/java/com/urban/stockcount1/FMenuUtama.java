package com.urban.stockcount1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FMenuUtama extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fmenu_utama);

        BottomNavigationView hm_navbar = (BottomNavigationView) findViewById(R.id.hm_navbar);
        hm_navbar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.hm_menu:
                        startActivity(new Intent(getApplicationContext(),FMenuList.class));
                        finish();
                        overridePendingTransition(1000,1000);
                        return true;
                    case R.id.hm_user:
                        startActivity(new Intent(getApplicationContext(),Fuser.class));
                        finish();
                        overridePendingTransition(1000,1000);
                        return true;
                }
                return false;
            }
        });
        hm_navbar.setSelectedItemId(R.id.hm_menu);
    }
}