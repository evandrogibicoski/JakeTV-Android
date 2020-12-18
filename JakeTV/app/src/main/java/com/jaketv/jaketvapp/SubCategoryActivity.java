package com.jaketv.jaketvapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

public class SubCategoryActivity extends BaseActivity {

    @InjectView(R.id.pull_refresh_list)
    PullToRefreshView mPullRefreshListView;
    @InjectView(R.id.tvTitle)
    TextView tvTitle;
    ListView lvCategory;
    CategoryAdapter CategoryAdapter;

    ArrayList<HashMap<String, String>> listCategory = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> hashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);

        Typeface mFont = Typeface.createFromAsset(getAssets(), "roboto_regular.ttf");
        ViewGroup root = (ViewGroup) findViewById(R.id.rlMain);
        Util.setFont(root, mFont);

        lvCategory = mPullRefreshListView.getRefreshableView();

        Intent intent = getIntent();
        hashMap = (HashMap<String, String>) intent.getSerializableExtra("map");

        tvTitle.setText("" + hashMap.get("category"));
        setSubCategory();

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullRefreshListView.setLastUpdatedLabel(DateUtils.formatDateTime(getApplicationContext(),
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL));

                // Do work to refresh the list here.
                mPullRefreshListView.onRefreshComplete();

            }
        });

    }

    @OnClick(R.id.rlBack)
    @SuppressWarnings("unused")
    public void Back(View view) {
        finish();
    }

    @OnClick(R.id.rlDone)
    @SuppressWarnings("unused")
    public void Done(View view) {
        finish();
    }

    private void setSubCategory() {
        try {
            JSONArray jData = new JSONArray(hashMap.get("subcatdata"));
            for (int i = 0; i < jData.length(); i++) {
                JSONObject jsonObject = jData.getJSONObject(i);
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("subcatid", "" + jsonObject.optString("subcatid"));
                hashMap.put("subcatuniqueid", "" + jsonObject.optString("subcatuniqueid"));
                hashMap.put("subcategory", "" + jsonObject.optString("subcategory"));
                hashMap.put("isselected", "" + jsonObject.optString("isselected"));

                listCategory.add(hashMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        lvCategory.setAdapter(new CategoryAdapter(getApplicationContext(), listCategory));
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

            holder.tvTitle.setText(locallist.get(position).get("subcategory"));

            if (locallist.get(position).get("isselected").equals("1")) {
                holder.btnIcon.setBackgroundResource(R.drawable.icon_tick);
            } else
                holder.btnIcon.setBackgroundResource(R.drawable.icon_plus);


            holder.rlIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (locallist.get(position).get("isselected").equals("1")) {
                        new SelectUnselectCategory("UnSelectCategoryByUser", "" + locallist.get(position).get("subcatuniqueid"), position, holder.btnIcon).execute();
                    } else {
                        new SelectUnselectCategory("SelectCategoryByUser", "" + locallist.get(position).get("subcatuniqueid"), position, holder.btnIcon).execute();
                    }
                }
            });

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
                progressDialog = new ProgressDialog(SubCategoryActivity.this, R.style.MyTheme);
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
