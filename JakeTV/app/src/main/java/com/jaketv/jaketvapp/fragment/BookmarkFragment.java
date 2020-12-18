package com.jaketv.jaketvapp.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Kevin on 7/19/2015.
 */
public class BookmarkFragment extends Fragment {

    public static BookmarkFragment newInstance() {
        BookmarkFragment bookmarkFragment = new BookmarkFragment();
        return bookmarkFragment;
    }

    @InjectView(R.id.img)
    ImageButton img;
    @InjectView(R.id.pull_refresh_list)
    PullToRefreshView mPullRefreshListView;
    ListView lvNews;
    int currentpage = 0, totalpage = 0;
    PostListAdapter postListAdapter;

    ArrayList<HashMap<String, String>> listPost = new ArrayList<HashMap<String, String>>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
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

                // Do work to refresh the list here.
                if (Util.isOnline(getActivity()) == false) {
                    Toast.makeText(getActivity(), Constant.network_error, Toast.LENGTH_SHORT).show();
                    mPullRefreshListView.onRefreshComplete();
                } else if (Util.isOnline(getActivity()) == true) {
                    new getPost(false, Util.ReadSharePrefrence(getActivity(), Constant.SHRED_PR.KEY_USERID)).execute();
                }
            }
        });

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
                jData.put("method", "GetBookmarkByUserid");
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
                    if (listPost.size() == 0) img.setVisibility(View.VISIBLE);
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
                        currentpage++;
                        new getPost(false, Util.ReadSharePrefrence(getActivity(), Constant.SHRED_PR.KEY_USERID)).execute();
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

}