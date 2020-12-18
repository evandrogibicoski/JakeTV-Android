package com.jaketv.jaketvapp;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.jaketv.jaketvapp.util.Constant;
import com.jaketv.jaketvapp.util.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by beauty on 6/6/16.
 */
public class ChangePasswordActivity extends BaseActivity {

    @InjectView(R.id.etOldPassword)
    EditText etOldPassword;
    @InjectView(R.id.etNewPassword)
    EditText etNewPassword;
    @InjectView(R.id.etConfirmPassword)
    EditText etConfirmPassword;

    private String openUrlID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Typeface mFont = Typeface.createFromAsset(getAssets(), "roboto_regular.ttf");
        ViewGroup root = (ViewGroup) findViewById(R.id.rlMain);
        Util.setFont(root, mFont);

        Bundle b = getIntent().getExtras();
        openUrlID = "";
        if(b != null)
            openUrlID = b.getString("userid");

        if (!isEmpty(openUrlID)) {
            View oldPasswordView = findViewById(R.id.rlOldPassword);
            oldPasswordView.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.rlChange)
    public void onChange(View view){
        hideKeyboard();
        if (isValidate()) {
            if (Util.isOnline(getApplicationContext())) {
                String userid = "";

                if (isEmpty(openUrlID)){
                    userid = Util.ReadSharePrefrence(this, Constant.SHRED_PR.KEY_USERID);
                } else {
                    userid = openUrlID;
                }

                new ChangePassword(getText(etOldPassword), getText(etNewPassword), userid).execute();
            } else {
                toast(Constant.network_error);
            }
        }
    }

    @OnClick(R.id.rlCancel)
    public void onCancel(View view){
        finish();
    }

    private boolean isValidate() {

        if (isEmpty(openUrlID) && isEmpty(getText(etOldPassword))) {
            toast("please enter old password");
            return false;
        }
        if (isEmpty(getText(etNewPassword))) {
            toast("please enter new password");
            return false;
        }
        String strNewPassword = getText(etNewPassword);
        if (isEmpty(getText(etConfirmPassword)) || !(strNewPassword.equals(getText(etConfirmPassword)))) {
            toast("password and confirm password does not match");
            return false;
        }

        if (isEmpty(openUrlID) && !(getText(etOldPassword).equals(Util.ReadSharePrefrence(this, Constant.SHRED_PR.KEY_PASSWORD)))) {
            toast("Old password is incorrect.");
            return false;
        }


        return true;
    }


    class ChangePassword extends AsyncTask<Void, String, String> {

        String oldPassword, newPassword, userid;
        ProgressDialog progressDialog;
        String response;

        public ChangePassword(String oldPassword, String newPassword, String userid) {
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
            this.userid = userid;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ChangePasswordActivity.this, R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                JSONObject jData = new JSONObject();
                jData.put("method", "ChangePassword");
                jData.put("userid", this.userid);
                jData.put("password", newPassword);

                List<NameValuePair> params1 = new ArrayList<NameValuePair>(2);
                params1.add(new BasicNameValuePair("data", jData.toString()));
                response = Util.makeServiceCall(Constant.URL, 1, params1);
                Log.e("params1", ">>" + params1);

                Log.e("** response is:- ", ">>" + response);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return response;
        }

        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            if (progressDialog != null) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            try {
                JSONObject jObj = new JSONObject(result);
                int status = jObj.optInt("success");

                if (status == 1) {
                    write(Constant.SHRED_PR.KEY_PASSWORD, newPassword);
                    toast(jObj.getString("msg"));

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(LoginActivity.class);
                            overridePendingTransition(R.anim.hold_bottom, R.anim.fade_out);
                            finish();
                        }
                    }, 1000);

                } else {
                    toast(jObj.optString("msg"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
