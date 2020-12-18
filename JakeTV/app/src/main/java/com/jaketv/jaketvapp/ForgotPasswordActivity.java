package com.jaketv.jaketvapp;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
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


public class ForgotPasswordActivity extends BaseActivity {

    @InjectView(R.id.etEmail)
    EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Typeface mFont = Typeface.createFromAsset(getAssets(), "roboto_regular.ttf");
        ViewGroup root = (ViewGroup) findViewById(R.id.rlMain);
        Util.setFont(root, mFont);

    }

    @OnClick(R.id.rlSend)
    @SuppressWarnings("unused")
    public void Send(View view) {
        hideKeyboard();
        String email = getText(etEmail);
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(getText(etEmail)).matches() == false) {
            toast("please enter valid email");
        } else {
            if (Util.isOnline(getApplicationContext()))
                new ForgotPassword(email).execute();
            else toast(Constant.network_error);
        }
    }

    class ForgotPassword extends AsyncTask<Void, String, String> {

        String email;
        ProgressDialog progressDialog;
        String response;

        public ForgotPassword(String email) {
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ForgotPasswordActivity.this, R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog.show();
        }


        @Override
        protected String doInBackground(Void... params) {

            try {
                JSONObject jData = new JSONObject();
                jData.put("method", "ForgotPassword");
                jData.put("email", email);
                jData.put("device", "android");

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

                toast(jObj.optString("msg"));
                if (status == 1) {
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
