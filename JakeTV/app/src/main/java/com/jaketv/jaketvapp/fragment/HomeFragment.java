package com.jaketv.jaketvapp.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaketv.jaketvapp.DetailActivity;
import com.jaketv.jaketvapp.R;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class HomeFragment extends Fragment {

    @InjectView(R.id.pull_refresh_list)
    PullToRefreshView mPullRefreshListView;
    @InjectView(R.id.ad_contatiner)
    View rlAdContainer;
    @InjectView(R.id.ad_img)
    ImageView imgAd;

    ListView lvNews;
    int currentpage = 0, totalpage = 0;
    PostListAdapter postListAdapter;

    ArrayList<HashMap<String, String>> listPost = new ArrayList<HashMap<String, String>>();

    public static HomeFragment newInstance() {
        HomeFragment homeFragment = new HomeFragment();
        return homeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lvNews = mPullRefreshListView.getRefreshableView();

        if (Util.isOnline(getActivity())) {
            new getPost(true, Util.ReadSharePrefrence(getActivity(), Constant.SHRED_PR.KEY_USERID)).execute();
        } else {
            Toast.makeText(getActivity(), Constant.network_error, Toast.LENGTH_SHORT).show();
        }

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullRefreshListView.setLastUpdatedLabel(DateUtils.formatDateTime(getActivity(),
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL));

                mPullRefreshListView.onRefreshComplete();

            }
        });

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 100 * width / 640);
        rel_btn.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rlAdContainer.setLayoutParams(rel_btn);

        rlAdContainer.setVisibility(View.GONE);
        (new GetAdTask()).execute();

    }

    class getPost extends AsyncTask<Void, String, String> {

        ProgressDialog progressDialog;
        String response, UserID;
        boolean flag;

        private getPost(boolean flag, String UserID) {
            this.flag = flag;
            this.UserID = UserID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (flag) {
                if (getActivity() != null) {
                    progressDialog = new ProgressDialog(getActivity(), R.style.MyTheme);
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                    progressDialog.show();
                }
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                JSONObject jData = new JSONObject();
                jData.put("userid", "" + UserID);
                jData.put("method", "GetPost");
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
                        hashMap.put("kickerline", "" + jsonObject.optString("kickerline"));
                        hashMap.put("source", "" + jsonObject.optString("source"));

                        if (currentpage == 0)
                            listPost.add(hashMap);
                        else postListAdapter.add(hashMap);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (getActivity() != null) {
                if (currentpage == 0) {
                    postListAdapter = new PostListAdapter(getActivity(), listPost);
                    lvNews.setAdapter(postListAdapter);
                }
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
                    .showImageOnLoading(0).resetViewBeforeLoading(true)
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

            ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.list_row, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.tvKickerline.setText(locallist.get(position).get("kickerline"));
            holder.tvTitle.setText(locallist.get(position).get("title"));
            holder.tvCategory.setText(locallist.get(position).get("source"));
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
                    mContext.startActivity(intent);
                }
            });

            if (position == locallist.size() - 1) {
                if (totalpage > (currentpage + 1)) {
                    if (Util.isOnline(getActivity()) == true) {
                        currentpage += 1;
                        new getPost(true, Util.ReadSharePrefrence(getActivity(), Constant.SHRED_PR.KEY_USERID)).execute();
                    }
                }
            }

            return view;
        }
    }

    static class ViewHolder {
        @InjectView(R.id.tvTitle)
        TextView tvTitle;
        @InjectView(R.id.tvKickerline)
        TextView tvKickerline;
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

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    class GetAdTask extends AsyncTask<String, String, String> {


        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL("http://ads.jaketv.tv/ads");
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONArray jArray = new JSONArray(result);
                if(jArray.length() > 0) {
                    JSONObject jObject = jArray.getJSONObject(0);
                    String portraitImgUrl = jObject.getString("portrait");
                    int duration = jObject.getInt("duration");
                    final String adVideoUrl = jObject.getString("video") == null ? "" : jObject.getString("video");

                    DisplayImageOptions options;
                    ImageLoader imageLoader = ImageLoader.getInstance();

                    options = new DisplayImageOptions.Builder()
                            .showImageOnLoading(0).resetViewBeforeLoading(true)
                            .showImageForEmptyUri(R.drawable.temp).showImageOnFail(R.drawable.temp).cacheInMemory(true)
                            .cacheOnDisk(true).considerExifParams(true)
                            .bitmapConfig(Bitmap.Config.RGB_565).build();
                    imageLoader.init(ImageLoaderConfiguration
                            .createDefault(imgAd.getContext()));

                    imageLoader.displayImage(portraitImgUrl, imgAd, options);

                    rlAdContainer.setTranslationY(rlAdContainer.getHeight());
                    rlAdContainer.setVisibility(View.VISIBLE);
                    Animation animShow = AnimationUtils.loadAnimation(rlAdContainer.getContext(), R.anim.slide_in_bottom);
                    rlAdContainer.startAnimation(animShow);

                    // click listener on Ad view
                    rlAdContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!adVideoUrl.equals("")) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(adVideoUrl));
                                startActivity(browserIntent);
                            }
                        }
                    });

                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Animation aminationFade = AnimationUtils.loadAnimation(rlAdContainer.getContext(), R.anim.fade_out);
                            rlAdContainer.startAnimation(aminationFade);
                            rlAdContainer.setVisibility(View.GONE);
                        }
                    }, duration * 1000);

                }

            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
