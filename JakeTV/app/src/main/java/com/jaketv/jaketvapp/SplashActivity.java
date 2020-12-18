package com.jaketv.jaketvapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.crittercism.app.Crittercism;
import com.jaketv.jaketvapp.util.Constant;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crittercism.initialize(getApplicationContext(), "55ac180a63235a0f00ad8f5e");

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            String userid = extras.getString("jaketvpassword");

            if (userid!=null)
            {
                Intent i = new Intent(this, ChangePasswordActivity.class);
                i.putExtra("userid", userid);
                startActivity(i);
                finish();
            } else {
                goNextScreen();
            }

        } else {
            goNextScreen();

        }

    }

    private void goNextScreen(){
        if (read(Constant.SHRED_PR.KEY_IS_LOGGEDIN).equals("1")) {
            startActivity(HomeActivity.class);
            finish();
        } else {
            setContentView(R.layout.activity_splash);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(LoginActivity.class);
                    overridePendingTransition(R.anim.hold_bottom, R.anim.fade_out);
                    finish();
                }
            }, 2000);
        }
    }
}
