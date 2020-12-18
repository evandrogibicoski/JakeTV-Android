package com.jaketv.jaketvapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import com.jaketv.jaketvapp.util.Constant;
import com.jaketv.jaketvapp.util.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;

public class DetailActivity extends BaseActivity {

    @InjectView(R.id.btnLike)
    ImageButton btnLike;
    @InjectView(R.id.btnBookmark)
    ImageButton btnBookmark;
    @InjectView(R.id.webview)
    WebView webView;
    ProgressDialog progressDialog;
    HashMap<String, String> hashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        hashMap = (HashMap<String, String>) intent.getSerializableExtra("map");

        if (hashMap.get("isbookmarked").equals("1"))
            btnBookmark.setBackgroundResource(R.drawable.bookmark_green);
        else btnBookmark.setBackgroundResource(R.drawable.bookmark_gray);

        if (hashMap.get("isliked").equals("1"))
            btnLike.setBackgroundResource(R.drawable.like_green);
        else btnLike.setBackgroundResource(R.drawable.like_gray);

        progressDialog = new ProgressDialog(DetailActivity.this, R.style.MyTheme);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);

        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("" + hashMap.get("url"));

    }


    @Override
    protected void onResume() {
        super.onResume();

        MyApplication.getInstance().trackScreenView("Detail Screen");
        String strPostId = hashMap.get("postid");
        MyApplication.getInstance().trackEvent("Post View", "Viewed", strPostId);
    }

    @OnClick(R.id.rlBack)
    @SuppressWarnings("unused")
    public void Back(View view) {

        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                    .invoke(webView, (Object[]) null);

        } catch(ClassNotFoundException cnfe) {

        } catch(NoSuchMethodException nsme) {

        } catch(InvocationTargetException ite) {

        } catch (IllegalAccessException iae) {

        }
        finish();
    }

    @OnClick(R.id.rlShare)
    @SuppressWarnings("unused")
    public void Share(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new post_email()
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new post_email().execute();
    }

    @OnClick(R.id.rlLike)
    @SuppressWarnings("unused")
    public void Like(View view) {
        if (Util.isOnline(getApplicationContext())) new LikeUnlikePost().execute();
        else toast(Constant.network_error);
    }

    @OnClick(R.id.rlBookmark)
    @SuppressWarnings("unused")
    public void Bookmark(View view) {
        if (Util.isOnline(getApplicationContext())) new BookmarkUnbookmarkPost().execute();
        else toast(Constant.network_error);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {

            try {
                Class.forName("android.webkit.WebView")
                        .getMethod("onPause", (Class[]) null)
                        .invoke(webView, (Object[]) null);

            } catch(ClassNotFoundException cnfe) {

            } catch(NoSuchMethodException nsme) {

            } catch(InvocationTargetException ite) {

            } catch (IllegalAccessException iae) {

            }
            finish();
            return true;
        }
        return false;
    }


    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (progressDialog.isShowing()) progressDialog.dismiss();
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            super.onPageStarted(view, url, favicon);
        }
    }

    class LikeUnlikePost extends AsyncTask<Void, String, String> {

        ProgressDialog progressDialog;
        String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DetailActivity.this, R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog.show();
        }


        @Override
        protected String doInBackground(Void... params) {

            try {
                JSONObject jData = new JSONObject();
                jData.put("userid", Util.ReadSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_USERID));
                if (hashMap.get("isliked").equals("1")) jData.put("method", "UnLikePost");
                else jData.put("method", "LikePost");
                jData.put("postid", "" + hashMap.get("postid"));

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
                    Util.WriteSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_RELOAD, "1");
                    if (hashMap.get("isliked").equals("1")) {
                        hashMap.put("isliked", "0");
                        btnLike.setBackgroundResource(R.drawable.like_gray);
                    } else {
                        hashMap.put("isliked", "1");
                        btnLike.setBackgroundResource(R.drawable.like_green);
                    }
                } else toast(jObj.optString("msg"));
            } catch (
                    JSONException e
                    )

            {
                e.printStackTrace();
            }
        }
    }


    class BookmarkUnbookmarkPost extends AsyncTask<Void, String, String> {

        ProgressDialog progressDialog;
        String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DetailActivity.this, R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog.show();
        }


        @Override
        protected String doInBackground(Void... params) {


            try {
                JSONObject jData = new JSONObject();
                jData.put("userid", Util.ReadSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_USERID));
                if (hashMap.get("isbookmarked").equals("1")) jData.put("method", "UnBookmarkPost");
                else jData.put("method", "BookmarkPost");
                jData.put("postid", "" + hashMap.get("postid"));

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
                    Util.WriteSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_RELOAD, "1");
                    if (hashMap.get("isbookmarked").equals("1")) {
                        hashMap.put("isbookmarked", "0");
                        btnBookmark.setBackgroundResource(R.drawable.bookmark_gray);
                    } else {
                        hashMap.put("isbookmarked", "1");
                        btnBookmark.setBackgroundResource(R.drawable.bookmark_green);
                    }
                } else toast(jObj.optString("msg"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class post_email extends AsyncTask<String, String, Uri> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(DetailActivity.this, R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog.show();
        }

        @Override
        protected Uri doInBackground(String... params) {
            Uri uri = null;
            try {
                String imgurl = hashMap.get("image");
                Log.e("imgurl", "" + imgurl);
                Bitmap bmp = Util.getBitmapFromURL(imgurl);
                uri = Util.getImageUri(getApplicationContext(), bmp);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return uri;
        }

        @Override
        protected void onPostExecute(Uri response) {
            super.onPostExecute(response);
            if (progressDialog != null) if (progressDialog.isShowing()) progressDialog.dismiss();

            String body = hashMap.get("title") + "\n\n" + hashMap.get("url") + "\n\n" + "-" + Constant.AppName + "\n" + "https://play.google.com/store/apps/developer?id=" + getPackageName();
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("image/*");

            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    Constant.AppName);
            emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, response);
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
            startActivity(emailIntent);
        }
    }
}
