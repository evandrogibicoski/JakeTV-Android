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

public class RegisterActivity extends BaseActivity {

    @InjectView(R.id.etFname)
    EditText etFname;
    @InjectView(R.id.etLname)
    EditText etLname;
    @InjectView(R.id.etEmail)
    EditText etEmail;
    @InjectView(R.id.etPassword)
    EditText etPassword;
    @InjectView(R.id.etConfirmPassword)
    EditText etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Typeface mFont = Typeface.createFromAsset(getAssets(), "roboto_regular.ttf");
        ViewGroup root = (ViewGroup) findViewById(R.id.rlMain);
        Util.setFont(root, mFont);
    }

    @OnClick(R.id.rlCancel)
    @SuppressWarnings("unused")
    public void Cancel(View view) {
        finish();
    }

    @OnClick(R.id.rlJoin)
    @SuppressWarnings("unused")
    public void Register(View view) {
        hideKeyboard();
        if (isValidate()) {
            if (Util.isOnline(getApplicationContext()))
                new Register(getText(etFname), getText(etLname), getText(etEmail), getText(etPassword)).execute();
            else toast(Constant.network_error);
        }
    }

    class Register extends AsyncTask<Void, String, String> {

        String fname, lname, email, password;

        public Register(String fname, String lname, String email, String password) {
            this.fname = fname;
            this.lname = lname;
            this.email = email;
            this.password = password;
        }

        ProgressDialog progressDialog;
        String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(RegisterActivity.this, R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog.show();
        }


        @Override
        protected String doInBackground(Void... params) {

            try {
                JSONObject jData = new JSONObject();
                jData.put("method", "Registration");
                jData.put("userid", "0");
                jData.put("googleplusid", "0");
                jData.put("fname", fname);
                jData.put("lname", lname);
                jData.put("email", email);
                jData.put("password", password);
                jData.put("picture", "");

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
                } else toast(jObj.optString("msg"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isValidate() {
        if (isEmpty(getText(etFname))) {
            toast("please enter firstname");
            return false;
        }
        if (isEmpty(getText(etLname))) {
            toast("please enter lastname");
            return false;
        }
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(getText(etEmail)).matches() == false) {
            toast("please enter valid email");
            return false;
        }
        if (isEmpty(getText(etPassword))) {
            toast("please enter password");
            return false;
        }
        if (isEmpty(getText(etConfirmPassword))) {
            toast("please enter confirm password");
            return false;
        }
        if (!getText(etPassword).equals(getText(etConfirmPassword))) {
            toast("password and confirm password must be same");
            return false;
        }
        return true;
    }
}
