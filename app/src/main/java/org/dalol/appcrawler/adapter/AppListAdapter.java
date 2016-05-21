package org.dalol.appcrawler.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import org.dalol.appcrawler.R;
import org.dalol.appcrawler.model.App;


public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppHolder> {

    private List<App> mApps;
    private Context mContext;

    public AppListAdapter(Context context, List<App> apps) {
        mContext = context;
        mApps = apps;
    }

    @Override
    public AppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View appView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item, null);
        return new AppHolder(appView);
    }

    @Override
    public void onBindViewHolder(AppHolder appHolder, int position) {

        App currentApp = mApps.get(position);

        TextView title = (TextView) appHolder.itemView.findViewById(R.id.appTitle);
        ImageView icon = (ImageView) appHolder.itemView.findViewById(R.id.appIcon);
        RatingBar ratingBar = (RatingBar) appHolder.itemView.findViewById(R.id.appRating);
        title.setText(currentApp.mAppTile);
        Bitmap currIcon = currentApp.mIcon;
        if (currIcon != null) {
            icon.setImageBitmap(currIcon);
        }
        ratingBar.setRating(currentApp.mRating);

        final String packageName = currentApp.mLink;

        appHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
            }
        });

    }

    @Override
    public int getItemCount() {
        return mApps.size();
    }

    public class AppHolder extends RecyclerView.ViewHolder {

        public AppHolder(View itemView) {
            super(itemView);
        }
    }
}
