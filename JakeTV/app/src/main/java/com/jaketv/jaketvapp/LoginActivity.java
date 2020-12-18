package com.jaketv.jaketvapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jaketv.jaketvapp.util.Constant;
import com.jaketv.jaketvapp.util.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private EditText etEmail;
    private EditText etPassword;

    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if ((Util.ReadSharePrefrence(this, Constant.SHRED_PR.KEY_IS_LOGGEDIN)).equals("1")) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        etEmail = (EditText)findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);


        Typeface mFont = Typeface.createFromAsset(getAssets(), "roboto_regular.ttf");
        ViewGroup root = (ViewGroup) findViewById(R.id.rlMain);
        Util.setFont(root, mFont);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        findViewById(R.id.rlGoogle).setOnClickListener(this);
        findViewById(R.id.rlSignIn).setOnClickListener(this);
        findViewById(R.id.rlForgotPassword).setOnClickListener(this);
        findViewById(R.id.rlRegister).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlGoogle:
                hideKeyboard();
                googlesignIn();
                break;
            case R.id.rlSignIn:
                signIn();
                break;
            case R.id.rlForgotPassword:
                ForgotPassword();
                break;
            case R.id.rlRegister:
                Register();
                break;
            default:
                break;
        }
    }

    private void googlesignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    protected void hideKeyboard() {
        // Check if no view has focus:
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    protected String getText(EditText eTxt) {
        return eTxt == null ? "" : eTxt.getText().toString().trim();
    }

    protected void toast(CharSequence text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected void startActivity(Class klass) {
        startActivity(new Intent(this, klass));
    }


    private void signIn() {
        hideKeyboard();
        if (isValidate()) {
            if (Util.isOnline(getApplicationContext()))
                new LogIn(getText(etEmail), getText(etPassword)).execute();
            else Toast.makeText(this, Constant.network_error, Toast.LENGTH_SHORT).show();
        }
    }


    private void ForgotPassword() {
        startActivity(ForgotPasswordActivity.class);
    }

    public void Register() {
        startActivity(RegisterActivity.class);
    }

    private boolean isValidate() {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(getText(etEmail)).matches() == false) {
            toast("please enter valid email");
            return false;
        }
        if (isEmpty(getText(etPassword))) {
            toast("please enter password");
            return false;
        }
        return true;
    }

    protected boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount person = result.getSignInAccount();

            String firstName = person.getGivenName() == null ? "" : person.getGivenName();
            String lastName = person.getFamilyName() == null ? "" : person.getFamilyName();
            String email = person.getEmail();
            String googleid = "" + person.getId();

            if (Util.isOnline(getApplicationContext()))
                new Register(firstName, lastName, email, googleid).execute();
            else toast(Constant.network_error);

        } else {
            // Signed out, show unauthenticated UI.
            toast(Constant.network_error);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        toast("Connection Failed.");
    }

    class LogIn extends AsyncTask<Void, String, String> {

        String email, password;
        ProgressDialog progressDialog;
        String response;

        public LogIn(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this, R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                JSONObject jData = new JSONObject();
                jData.put("method", "Login");
                jData.put("email", email);
                jData.put("password", password);

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
                    write(Constant.SHRED_PR.KEY_USERID, "" + jObj.optString("userid"));
                    write(Constant.SHRED_PR.KEY_IS_LOGGEDIN, "1");
                    write(Constant.SHRED_PR.KEY_PASSWORD, this.password);
                    write(Constant.SHRED_PR.KEY_FNAME, jObj.optString("fname"));
                    write(Constant.SHRED_PR.KEY_LNAME, jObj.optString("lname"));
                    write(Constant.SHRED_PR.KEY_EMAIL, jObj.optString("email"));
                    startActivity(HomeActivity.class);
                    finish();
                } else toast(jObj.optString("msg"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class Register extends AsyncTask<Void, String, String> {

        String fname, lname, email, googleid;

        public Register(String fname, String lname, String email, String googleid) {
            this.fname = fname;
            this.lname = lname;
            this.email = email;
            this.googleid = googleid;
        }

        ProgressDialog progressDialog;
        String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(LoginActivity.this, null,
                    "Loading...	", true, true);
        }


        @Override
        protected String doInBackground(Void... params) {

            try {
                JSONObject jData = new JSONObject();
                jData.put("method", "Registration");
                jData.put("userid", "0");
                jData.put("googleplusid", googleid);
                jData.put("fname", fname);
                jData.put("lname", lname);
                jData.put("email", email);
                jData.put("password", "");
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

                if (status == 1) {
                    write(Constant.SHRED_PR.KEY_USERID, "" + jObj.optString("userid"));
                    write(Constant.SHRED_PR.KEY_IS_LOGGEDIN, "1");
                    startActivity(HomeActivity.class);
                    finish();
                } else toast(jObj.optString("msg"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected void write(String key, String val) {
        Util.WriteSharePrefrence(this, key, val);
    }

}
