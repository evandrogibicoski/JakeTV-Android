package com.jaketv.jaketvapp.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jaketv.jaketvapp.ChangePasswordActivity;
import com.jaketv.jaketvapp.R;
import com.jaketv.jaketvapp.util.Constant;
import com.jaketv.jaketvapp.util.Util;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kevin on 7/20/2015.
 */
public class SettingsFragment extends Fragment {

    public static SettingsFragment newInstance() {
        SettingsFragment settingsFragment = new SettingsFragment();
        return settingsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Typeface mFont = Typeface.createFromAsset(getActivity().getAssets(), "roboto_regular.ttf");
        ViewGroup root = (ViewGroup) getView().findViewById(R.id.rlMain);
        Util.setFont(root, mFont);

    }

    @OnClick(R.id.rlProfile)
    public void ProfileSettings(View v){

    }

    @OnClick(R.id.rlChangePassword)
    public void ResetPassword(View v){
        startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
    }

    @OnClick(R.id.rlAbout)
    public void About(View v) {
        String url = "http://jaketv.org/about-jake/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        getActivity().startActivity(i);
    }

    @OnClick(R.id.rlShareApp)
    public void ShareApp(View v) {
        String shareBody = "Hey I'm using JakeTV\nDownload App: https://play.google.com/store/apps/developer?id=" + getActivity().getPackageName();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, Constant.AppName);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share"));
    }

    @OnClick(R.id.rlReviewApp)
    public void ReviewApp(View v) {
        final String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

}
