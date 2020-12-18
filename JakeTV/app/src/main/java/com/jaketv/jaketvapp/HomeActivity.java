package com.jaketv.jaketvapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaketv.jaketvapp.fragment.BookmarkFragment;
import com.jaketv.jaketvapp.fragment.CategoryFragment;
import com.jaketv.jaketvapp.fragment.HomeFragment;
import com.jaketv.jaketvapp.fragment.LikeFragment;
import com.jaketv.jaketvapp.fragment.SettingsFragment;
import com.jaketv.jaketvapp.util.Constant;
import com.jaketv.jaketvapp.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class HomeActivity extends FragmentActivity {

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.list_slidermenu)
    ListView mDrawerList;
    @InjectView(R.id.imgLogo)
    ImageButton imgLogo;
    @InjectView(R.id.tvTitle)
    TextView tvTitle;
    @InjectView(R.id.rlSignOut)
    RelativeLayout rlSignOut;
    @InjectView(R.id.rlCancel)
    RelativeLayout rlCancel;
    @InjectView(R.id.rlSearch)
    RelativeLayout rlSearch;
    @InjectView(R.id.rlMessage)
    RelativeLayout rlMessage;
    @InjectView(R.id.rlSearchBox)
    RelativeLayout rlSearchBox;
    @InjectView(R.id.rlClear)
    RelativeLayout rlClear;
    @InjectView(R.id.etSearch)
    EditText etSearch;
    @InjectView(R.id.lvSearchNews)
    ListView lvSearchNews;
    @InjectView(R.id.view_popupwindow)
    RelativeLayout view_popupWindow;
    @InjectView(R.id.view_popupSignupMailChimp)
    LinearLayout view_popupSignupMailChimp;
    @InjectView(R.id.view_popupSubscribed)
    LinearLayout view_popupSubscribed;

    FragmentTransaction fragmentTransaction;
    Fragment fragment;

    int[] icons = {0, R.drawable.home, R.drawable.liked, R.drawable.bookmarked, R.drawable.settings, R.drawable.talktous, 0};
    ArrayList<HashMap<String, String>> listMenu = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> listPost = new ArrayList<HashMap<String, String>>();
    public static int selectedPosition = 1;
    int currentpage = 0, totalpage = 0;
    String searchString;
    PostListAdapter postListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);

        selectedPosition = 1;
        init();
        setMenuList();
        displayView(selectedPosition);

        if ((Util.ReadSharePrefrence(this, Constant.SHRED_PR.KEY_ENABLE_MAILCHIMP)).equals("later"))
        {
            String registeredDate = Util.ReadSharePrefrence(this, Constant.SHRED_PR.KEY_REGISTEREDDATE);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String currentDate = dateFormat.format(Calendar.getInstance().getTime());
            int count = this.get_count_of_days(registeredDate, currentDate);
            if (count > 30) {
                view_popupWindow.setVisibility(View.VISIBLE);
                view_popupSignupMailChimp.setVisibility(View.VISIBLE);
                view_popupSubscribed.setVisibility(View.GONE);

                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        } else if (!((Util.ReadSharePrefrence(this, Constant.SHRED_PR.KEY_ENABLE_MAILCHIMP)).equals("yes")) &&
                !((Util.ReadSharePrefrence(this, Constant.SHRED_PR.KEY_ENABLE_MAILCHIMP)).equals("no"))) {
            view_popupWindow.setVisibility(View.VISIBLE);
            view_popupSignupMailChimp.setVisibility(View.VISIBLE);
            view_popupSubscribed.setVisibility(View.GONE);

            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

    }

    private void init() {

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN) {

                    hideKeyboard();
                    searchString = etSearch.getText().toString().trim();
                    if (searchString.length() > 0) {
                        if (Util.isOnline(getApplicationContext())) {
                            new getSearchPost(true).execute();
                        } else {
                            Toast.makeText(getApplicationContext(), Constant.network_error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    return true;
                }
                return false;
            }
        });


        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() == 0)
                    rlClear.setVisibility(View.GONE);
                else rlClear.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Util.isOnline(getApplicationContext())) {
            new GetSelectedCategory().execute();
        }

        if (Util.ReadSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_RELOAD).equals("1")) {
            Util.WriteSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_RELOAD, "0");
            displayView(selectedPosition);
        }
        MyApplication.getInstance().trackScreenView("Home Screen");
    }

    private void setMenuList() {
        listMenu.clear();

        HashMap<String, String> hashMap1 = new HashMap<String, String>();
        hashMap1.put("catid", "");
        hashMap1.put("category", "JAKE TV");

        HashMap<String, String> hashMap2 = new HashMap<String, String>();
        hashMap2.put("catid", "");
        hashMap2.put("category", "Home");

        HashMap<String, String> hashMap3 = new HashMap<String, String>();
        hashMap3.put("catid", "");
        hashMap3.put("category", "Likes");

        HashMap<String, String> hashMap4 = new HashMap<String, String>();
        hashMap4.put("catid", "");
        hashMap4.put("category", "Bookmarks");

        HashMap<String, String> hashMap5 = new HashMap<String, String>();
        hashMap5.put("catid", "");
        hashMap5.put("category", "Settings");

        HashMap<String, String> hashMap6 = new HashMap<String, String>();
        hashMap6.put("catid", "");
        hashMap6.put("category", "Talk to us");

        HashMap<String, String> hashMap7 = new HashMap<String, String>();
        hashMap7.put("catid", "");
        hashMap7.put("category", "Search by Categories");

        listMenu.add(hashMap1);
        listMenu.add(hashMap2);
        listMenu.add(hashMap3);
        listMenu.add(hashMap4);
        listMenu.add(hashMap5);
        listMenu.add(hashMap6);
        listMenu.add(hashMap7);
    }


    public int get_count_of_days(String Created_date_String, String Expire_date_String) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Date Created_convertedDate = null, Expire_CovertedDate = null, todayWithZeroTime = null;
        try {
            Created_convertedDate = dateFormat.parse(Created_date_String);
            Expire_CovertedDate = dateFormat.parse(Expire_date_String);

            Date today = new Date();

            todayWithZeroTime = dateFormat.parse(dateFormat.format(today));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int c_year = 0, c_month = 0, c_day = 0;

        if (Created_convertedDate.after(todayWithZeroTime)) {
            Calendar c_cal = Calendar.getInstance();
            c_cal.setTime(Created_convertedDate);
            c_year = c_cal.get(Calendar.YEAR);
            c_month = c_cal.get(Calendar.MONTH);
            c_day = c_cal.get(Calendar.DAY_OF_MONTH);

        } else {
            Calendar c_cal = Calendar.getInstance();
            c_cal.setTime(todayWithZeroTime);
            c_year = c_cal.get(Calendar.YEAR);
            c_month = c_cal.get(Calendar.MONTH);
            c_day = c_cal.get(Calendar.DAY_OF_MONTH);
        }

        Calendar e_cal = Calendar.getInstance();
        e_cal.setTime(Expire_CovertedDate);

        int e_year = e_cal.get(Calendar.YEAR);
        int e_month = e_cal.get(Calendar.MONTH);
        int e_day = e_cal.get(Calendar.DAY_OF_MONTH);

        Calendar date1 = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();

        date1.clear();
        date1.set(c_year, c_month, c_day);
        date2.clear();
        date2.set(e_year, e_month, e_day);

        long diff = date2.getTimeInMillis() - date1.getTimeInMillis();

        float dayCount = (float) diff / (24 * 60 * 60 * 1000);

        return (int)dayCount;
    }

    protected void write(String key, String val) {
        Util.WriteSharePrefrence(this, key, val);
    }

    @OnClick(R.id.btn_signmeup)
    @SuppressWarnings("unused")
    public void Signmeup(View view) {
        (new SendMailChimpRequest()).execute();

        view_popupSignupMailChimp.setVisibility(View.GONE);
        view_popupSubscribed.setVisibility(View.VISIBLE);

    }

    @OnClick(R.id.btn_dismiss)
    @SuppressWarnings("unused")
    public void onDismiss(View view) {
        view_popupSubscribed.setVisibility(View.GONE);
        view_popupWindow.setVisibility(View.GONE);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }


    @OnClick(R.id.btn_nothanks)
    @SuppressWarnings("unused")
    public void Nothanks(View view) {
        write(Constant.SHRED_PR.KEY_ENABLE_MAILCHIMP, "no");
        view_popupWindow.setVisibility(View.GONE);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @OnClick(R.id.btn_askmelater)
    @SuppressWarnings("unused")
    public void Askmelater(View view) {
        write(Constant.SHRED_PR.KEY_ENABLE_MAILCHIMP, "later");
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
        write(Constant.SHRED_PR.KEY_REGISTEREDDATE, formattedDate);

        view_popupWindow.setVisibility(View.GONE);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @OnClick(R.id.view_popupwindow)
    @SuppressWarnings("unused")
    public void OnPopup(View view) {

    }

    @OnClick(R.id.rlSearch)
    @SuppressWarnings("unused")
    public void Search(View view) {
        rlSearch.setVisibility(View.GONE);
        rlMessage.setVisibility(View.GONE);
        imgLogo.setVisibility(View.GONE);
        rlCancel.setVisibility(View.VISIBLE);
        rlSearchBox.setVisibility(View.VISIBLE);
        etSearch.setText("");
    }

    @OnClick(R.id.rlMessage)
    @SuppressWarnings("unused")
    public void Message(View view) {
        try{
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ "jaketvmanager@gmail.com" });
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback: JakeTV Android");
            emailIntent.setType("message/rfc822");
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e){
            Toast toast = Toast.makeText(HomeActivity.this, "Sorry, no email client found :(", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @OnClick(R.id.rlCancel)
    @SuppressWarnings("unused")
    public void Cancel(View view) {
        rlSearch.setVisibility(View.VISIBLE);
        rlMessage.setVisibility(View.VISIBLE);
        imgLogo.setVisibility(View.VISIBLE);
        rlCancel.setVisibility(View.GONE);
        rlSearchBox.setVisibility(View.GONE);
        etSearch.setText("");
        displayView(selectedPosition);
    }

    @OnClick(R.id.rlClear)
    @SuppressWarnings("unused")
    public void Clear(View view) {
        etSearch.setText("");
    }

    @OnClick(R.id.rlMenu)
    @SuppressWarnings("unused")
    public void Menu(View view) {
        hideKeyboard();

        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            mDrawerLayout.openDrawer(mDrawerList);
        }
    }

    @OnClick(R.id.rlSignOut)
    @SuppressWarnings("unused")
    public void SignOut(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
        alert.setTitle("" + Constant.AppName);
        alert.setMessage("Are you sure do you want to SignOut?");
        alert.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @SuppressLint("InlinedApi")
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        Util.WriteSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_IS_LOGGEDIN, "false");
                        Intent i = new Intent(HomeActivity.this,
                                LoginActivity.class);
                        if (android.os.Build.VERSION.SDK_INT >= 11) {
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        } else {
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        }
                        startActivity(i);
                    }
                });
        alert.setNegativeButton("No",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        // TODO Auto-generated method stub
                        dialog.cancel();
                    }
                });
        alert.create();
        alert.show();
    }

    private void displayView(int position) {
        // update the main content by replacing fragments
        selectedPosition = position;
        switch (position) {
            case 1:
                fragment = HomeFragment.newInstance();
                break;
            case 2:
                fragment = LikeFragment.newInstance();
                break;
            case 3:
                fragment = BookmarkFragment.newInstance();
                break;
            case 4:
                fragment = SettingsFragment.newInstance();
                break;
            default:
                if (position > 6)
                    fragment = CategoryFragment.newInstance("" + listMenu.get(position).get("catid"));
                else fragment = new HomeFragment();
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();

            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment).commit();
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
            frameLayout.setVisibility(View.VISIBLE);
            lvSearchNews.setVisibility(View.GONE);
            rlCancel.performClick();

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerLayout.closeDrawer(mDrawerList);
            mDrawerList.setAdapter(new MenuListAdapter(getApplicationContext(), listMenu, icons, position));
        }

        if (position == 4) {
            imgLogo.setVisibility(View.GONE);
            tvTitle.setVisibility(View.VISIBLE);
            rlSignOut.setVisibility(View.VISIBLE);
            rlSearch.setVisibility(View.GONE);
            rlMessage.setVisibility(View.GONE);
        } else {
            imgLogo.setVisibility(View.VISIBLE);
            tvTitle.setVisibility(View.GONE);
            rlSignOut.setVisibility(View.GONE);
            rlSearch.setVisibility(View.VISIBLE);
            rlMessage.setVisibility(View.VISIBLE);
        }
    }

    public class MenuListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater = null;
        ArrayList<HashMap<String, String>> locallist;
        int[] icons;
        int selectedPosition;

        public MenuListAdapter(Context mContext, ArrayList<HashMap<String, String>> locallist, int[] icons, int selectedPosition) {
            this.mContext = mContext;
            this.locallist = locallist;
            this.icons = icons;
            this.selectedPosition = selectedPosition;
            inflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return locallist.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {

            ViewHolder holder;
            if (position == 0 || position == 6) {
                view = inflater.inflate(R.layout.menulist_row_blank, null);
                holder = new ViewHolder(view);
                view.setTag(holder);

                holder.tvTitle.setText(locallist.get(position).get("category"));
                if (position == 0) holder.btnIcon.setVisibility(View.GONE);
                else holder.btnIcon.setVisibility(View.VISIBLE);

            } else {
                view = inflater.inflate(R.layout.menulist_row, null);
                holder = new ViewHolder(view);
                view.setTag(holder);

                if (position == selectedPosition) {
                    holder.rlMain.setBackgroundColor(getResources().getColor(R.color.gray_selected));
                } else {
                    holder.rlMain.setBackgroundColor(getResources().getColor(R.color.transparent));
                }

                holder.tvTitle.setText(locallist.get(position).get("category"));
                if (position > 6) holder.btnIcon.setBackgroundResource(R.drawable.tempcat);
                else holder.btnIcon.setBackgroundResource(icons[position]);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                if (position != 0) {
                    selectedPosition = position;
                    switch (position) {
                        case 5:
                            try{
                                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ "jaketvmanager@gmail.com" });
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback: JakeTV Android");
                                emailIntent.setType("message/rfc822");
                                startActivity(emailIntent);
                            } catch (ActivityNotFoundException e){
                                Toast toast = Toast.makeText(HomeActivity.this, "Sorry, no email client found :(", Toast.LENGTH_LONG);
                                toast.show();
                            }
                            break;
                        case 6:
                            startActivity(CategoryActivity.class);
                            mDrawerLayout.closeDrawer(mDrawerList);
                            break;
                        default:
                            displayView(selectedPosition);
                            break;
                    }
                }
                }
            });

            return view;
        }
    }

    static class ViewHolder {
        @InjectView(R.id.rlMain)
        RelativeLayout rlMain;
        @InjectView(R.id.tv_title)
        TextView tvTitle;
        @InjectView(R.id.btn_icon)
        ImageButton btnIcon;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    class GetSelectedCategory extends AsyncTask<Void, String, String> {

        String response;

        @Override
        protected String doInBackground(Void... params) {


            try {
                JSONObject jData = new JSONObject();
                jData.put("userid", Util.ReadSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_USERID));
                jData.put("method", "GetSelectedCategory");

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

            setMenuList();
            try {
                if (result != null) {
                    JSONObject jObj = new JSONObject(result);
                    int status = jObj.optInt("success");
                    if (status == 1) {
                        JSONArray jData = jObj.getJSONArray("data");
                        for (int i = 0; i < jData.length(); i++) {
                            JSONObject jsonObject = jData.getJSONObject(i);
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            hashMap.put("catid", "" + jsonObject.optString("catid"));
                            hashMap.put("category", "" + jsonObject.optString("category"));

                            listMenu.add(hashMap);
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            mDrawerList.setAdapter(new MenuListAdapter(getApplicationContext(), listMenu, icons, selectedPosition));
        }
    }

    class getSearchPost extends AsyncTask<Void, String, String> {

        ProgressDialog progressDialog;
        String response;
        boolean flag;

        private getSearchPost(boolean flag) {
            this.flag = flag;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (flag) {

                progressDialog = new ProgressDialog(HomeActivity.this, R.style.MyTheme);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                progressDialog.show();

            }
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                JSONObject jData = new JSONObject();
                jData.put("userid", Util.ReadSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_USERID));
                jData.put("method", "GetSearch");
                jData.put("Page", "" + currentpage);
                jData.put("search", "" + searchString);

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

            //mPullRefreshListView.onRefreshComplete();
            if (progressDialog != null) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            listPost.clear();
            try {
                JSONObject jObj = new JSONObject(result);
                int status = jObj.optInt("success");
                if (status == 1) {
                    totalpage = jObj.optInt("totalpage");
                    JSONArray jData = jObj.getJSONArray("data");
                    for (int i = 0; i < jData.length(); i++) {
                        JSONObject jsonObject = jData.getJSONObject(i);
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put("postid", "" + jsonObject.optString("postid"));
                        hashMap.put("title", "" + jsonObject.optString("title"));
                        hashMap.put("catid", "" + jsonObject.optString("catid"));
                        hashMap.put("category", "" + jsonObject.optString("category"));
                        hashMap.put("subcatid", "" + jsonObject.optString("subcatid"));
                        hashMap.put("subcategory", "" + jsonObject.optString("subcategory"));
                        hashMap.put("image", "" + jsonObject.optString("image"));
                        hashMap.put("url", "" + jsonObject.optString("url"));
                        hashMap.put("description", "" + jsonObject.optString("description"));
                        hashMap.put("totalpostlikes", "" + jsonObject.optString("totalpostlikes"));
                        hashMap.put("isbookmarked", "" + jsonObject.optString("isbookmarked"));
                        hashMap.put("isliked", "" + jsonObject.optString("isliked"));
                        hashMap.put("cr_date", "" + jsonObject.optString("cr_date"));

                        if (currentpage == 0)
                            listPost.add(hashMap);
                        else postListAdapter.add(hashMap);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (currentpage == 0) {
                postListAdapter = new PostListAdapter(getApplicationContext(), listPost);
                lvSearchNews.setAdapter(postListAdapter);
                lvSearchNews.setVisibility(View.VISIBLE);

                FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
                frameLayout.setVisibility(View.GONE);
            }
        }
    }

    public class PostListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater = null;
        ArrayList<HashMap<String, String>> locallist;

        DisplayImageOptions options;
        ImageLoader imageLoader = ImageLoader.getInstance();

        public PostListAdapter(Context mContext, ArrayList<HashMap<String, String>> locallist) {
            this.mContext = mContext;
            this.locallist = locallist;
            inflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.temp).resetViewBeforeLoading(true)
                    .showImageForEmptyUri(R.drawable.temp).showImageOnFail(R.drawable.temp).cacheInMemory(true)
                    .cacheOnDisk(true).considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
            imageLoader.init(ImageLoaderConfiguration
                    .createDefault(mContext));
        }

        private void add(HashMap<String, String> hashMap) {
            locallist.add(hashMap);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return locallist.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {

            ViewHolder1 holder;
            if (view == null) {
                view = inflater.inflate(R.layout.list_row, null);
                holder = new ViewHolder1(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder1) view.getTag();
            }

            holder.tvTitle.setText(locallist.get(position).get("title"));
            holder.tvCategory.setText(locallist.get(position).get("category"));
            holder.tvLikeCount.setText(locallist.get(position).get("totalpostlikes"));
            imageLoader.displayImage(locallist.get(position).get("image"), holder.img, options);

            if (locallist.get(position).get("isbookmarked").equals("1")) {
                holder.imgBookmark.setVisibility(View.VISIBLE);
            } else {
                holder.imgBookmark.setVisibility(View.GONE);
            }

            if (locallist.get(position).get("isliked").equals("1")) {
                holder.imgLike.setBackgroundResource(R.drawable.like);
            } else {
                holder.imgLike.setBackgroundResource(R.drawable.like);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.putExtra("map", locallist.get(position));
                    startActivity(intent);
                }
            });

            if (position == locallist.size() - 1) {
                if (totalpage > (currentpage + 1)) {
                    if (Util.isOnline(getApplicationContext()) == true) {
                        currentpage++;
                        new getSearchPost(false).execute();
                    }
                }
            }

            return view;
        }
    }

    class ViewHolder1 {
        @InjectView(R.id.tvTitle)
        TextView tvTitle;
        @InjectView(R.id.tvCategory)
        TextView tvCategory;
        @InjectView(R.id.tvLikeCount)
        TextView tvLikeCount;
        @InjectView(R.id.img)
        ImageView img;
        @InjectView(R.id.imgBookmark)
        ImageView imgBookmark;
        @InjectView(R.id.imgLike)
        ImageView imgLike;

        public ViewHolder1(View view) {
            ButterKnife.inject(this, view);
        }
    }


    protected void startActivity(Class klass) {
        startActivity(new Intent(this, klass));
    }

    protected void hideKeyboard() {
        // Check if no view has focus:
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    private class SendMailChimpRequest extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {
            String result = "";
            try{
                HttpClient httpclient = new DefaultHttpClient();

                HttpGet request = new HttpGet();
                String firstName = Util.ReadSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_FNAME);
                String lastName = Util.ReadSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_LNAME);
                String email = Util.ReadSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_EMAIL);

                URI website = new URI("http://jaketv.tv/emails.php?email=" + email + "&fname=" + firstName + "&lname=" + lastName);
                request.setURI(website);
                HttpResponse response = httpclient.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));

                // NEW CODE
                result = in.readLine();
                Log.e("log_tag", "####################" + result);

                write(Constant.SHRED_PR.KEY_ENABLE_MAILCHIMP, "yes");

            }catch(Exception e){
                Log.e("log_tag", "Error in http connection "+e.toString());
            }
            return result;
        }

        protected void onPostExecute(String result) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }

}


