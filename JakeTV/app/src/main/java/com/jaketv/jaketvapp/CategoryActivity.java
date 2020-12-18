package com.jaketv.jaketvapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaketv.jaketvapp.pulltorefresh.mad.PullToRefreshBase;
import com.jaketv.jaketvapp.pulltorefresh.mad.PullToRefreshView;
import com.jaketv.jaketvapp.util.Constant;
import com.jaketv.jaketvapp.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CategoryActivity extends BaseActivity {

    @InjectView(R.id.pull_refresh_list)
    PullToRefreshView mPullRefreshListView;
    ListView lvCategory;
    int currentpage = 0, totalpage = 0;
    CategoryAdapter CategoryAdapter;

    ArrayList<HashMap<String, String>> listCategory = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Typeface mFont = Typeface.createFromAsset(getAssets(), "roboto_regular.ttf");
        ViewGroup root = (ViewGroup) findViewById(R.id.rlMain);
        Util.setFont(root, mFont);

        lvCategory = mPullRefreshListView.getRefreshableView();
        //lvNews.setDivider(null);
        //lvNews.setDividerHeight(0);

        if (Util.isOnline(getApplicationContext())) {
            new getPost(true).execute();
        } else {
            Toast.makeText(getApplicationContext(), Constant.network_error, Toast.LENGTH_SHORT).show();
        }

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullRefreshListView.setLastUpdatedLabel(DateUtils.formatDateTime(getApplicationContext(),
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL));

                // Do work to refresh the list here.
                if (Util.isOnline(getApplicationContext()) == false) {
                    Toast.makeText(getApplicationContext(), Constant.network_error, Toast.LENGTH_SHORT).show();
                    mPullRefreshListView.onRefreshComplete();
                } else if (Util.isOnline(getApplicationContext()) == true) {
                    new getPost(false).execute();
                }
            }
        });

    }

    @OnClick(R.id.rlDone)
    @SuppressWarnings("unused")
    public void Done(View view) {
        finish();
    }

    class getPost extends AsyncTask<Void, String, String> {

        ProgressDialog progressDialog;
        String response;
        boolean flag;

        private getPost(boolean flag) {
            this.flag = flag;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (flag) {
                progressDialog = new ProgressDialog(CategoryActivity.this, R.style.MyTheme);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                JSONObject jData = new JSONObject();
                Log.e("userid",""+Util.ReadSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_USERID));
                jData.put("userid", Util.ReadSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_USERID));
                jData.put("method", "GetCategory");
                jData.put("Page", "" + currentpage);

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

            mPullRefreshListView.onRefreshComplete();
            if (progressDialog != null) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            listCategory.clear();
            try {
                JSONObject jObj = new JSONObject(result);
                int status = jObj.optInt("success");
                if (status == 1) {
                    totalpage = jObj.optInt("totalpage");
                    JSONArray jData = jObj.getJSONArray("data");
                    for (int i = 0; i < jData.length(); i++) {
                        JSONObject jsonObject = jData.getJSONObject(i);
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put("catid", "" + jsonObject.optString("catid"));
                        hashMap.put("catuniqueid", "" + jsonObject.optString("catuniqueid"));
                        hashMap.put("category", "" + jsonObject.optString("category"));
                        hashMap.put("isselected", "" + jsonObject.optString("isselected"));
                        hashMap.put("subcatdata", "" + jsonObject.optString("subcatdata"));

                        if (currentpage == 0)
                            listCategory.add(hashMap);
                        else CategoryAdapter.add(hashMap);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (currentpage == 0) {
                CategoryAdapter = new CategoryAdapter(getApplicationContext(), listCategory);
                lvCategory.setAdapter(CategoryAdapter);
            }
        }
    }

    public class CategoryAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater = null;
        ArrayList<HashMap<String, String>> locallist;

        DisplayImageOptions options;
        ImageLoader imageLoader = ImageLoader.getInstance();

        public CategoryAdapter(Context mContext, ArrayList<HashMap<String, String>> locallist) {
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

            final ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.listrow_category, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.tvTitle.setText(locallist.get(position).get("category"));

            if (locallist.get(position).get("isselected").equals("1")) {
                holder.btnIcon.setBackgroundResource(R.drawable.icon_tick);
            }else{
//                (locallist.get(position).get("subcatdata").length() == 0) {
                holder.btnIcon.setBackgroundResource(R.drawable.icon_plus);
            }
//            else
//                holder.btnIcon.setBackgroundResource(R.drawable.icon_arrow);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (locallist.get(position).get("isselected").equals("1")) {
                        new SelectUnselectCategory("UnSelectCategoryByUser", "" + locallist.get(position).get("catuniqueid"), position, holder.btnIcon).execute();
                    } else {
                        new SelectUnselectCategory("SelectCategoryByUser", "" + locallist.get(position).get("catuniqueid"), position, holder.btnIcon).execute();
                    }
//                    Intent intent = new Intent(CategoryActivity.this, SubCategoryActivity.class);
//                    intent.putExtra("map", locallist.get(position));
//                    startActivity(intent);
                }
            });

            if (position == locallist.size() - 1) {
                if (totalpage > (currentpage + 1)) {
                    if (Util.isOnline(getApplicationContext()) == true) {
                        currentpage++;
                        new getPost(false).execute();
                    }
                }
            }

            return view;
        }

        class SelectUnselectCategory extends AsyncTask<Void, String, String> {

            ProgressDialog progressDialog;
            String response;
            String method, catid;
            int pos;
            ImageButton btnIcon;

            private SelectUnselectCategory(String method, String catid, int pos, ImageButton btnIcon) {
                this.method = method;
                this.catid = catid;
                this.pos = pos;
                this.btnIcon = btnIcon;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(CategoryActivity.this, R.style.MyTheme);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {


                try {
                    JSONObject jData = new JSONObject();
                    jData.put("userid", Util.ReadSharePrefrence(getApplicationContext(), Constant.SHRED_PR.KEY_USERID));
                    jData.put("method", method);
                    jData.put("catid", catid);

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

                mPullRefreshListView.onRefreshComplete();
                if (progressDialog != null) {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                }

                try {
                    JSONObject jObj = new JSONObject(result);
                    int status = jObj.optInt("success");

                    if (status == 1) {
                        if (locallist.get(pos).get("isselected").equals("1")) {
                            locallist.get(pos).put("isselected", "0");
                            btnIcon.setBackgroundResource(R.drawable.icon_plus);
                        } else {
                            locallist.get(pos).put("isselected", "1");
                            btnIcon.setBackgroundResource(R.drawable.icon_tick);
                        }
                    } else toast("" + jObj.optString("msg"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class ViewHolder {
        @InjectView(R.id.tv_title)
        TextView tvTitle;
        @InjectView(R.id.btn_icon)
        ImageButton btnIcon;
        @InjectView(R.id.rlIcon)
        RelativeLayout rlIcon;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
