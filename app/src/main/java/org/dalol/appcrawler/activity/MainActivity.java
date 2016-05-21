package org.dalol.appcrawler.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.ResponseContext;

import org.dalol.appcrawler.R;
import org.dalol.appcrawler.adapter.AppLayoutManager;
import org.dalol.appcrawler.adapter.AppListAdapter;
import org.dalol.appcrawler.model.App;
import org.dalol.appcrawler.model.Constant;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private AppListAdapter mAdapter;
    private List<App> mApps;
    private TextView mTextView;
    private EditText mSearchText;
    private Button mButton;
    private ProgressDialog mProgressDialog;

    @Override
    protected int getViewResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void configViews() {

        mSearchText = (EditText) findViewById(R.id.searchTitle);
        mButton = (Button) findViewById(R.id.okayBtn);
        mTextView = (TextView) findViewById(R.id.seeAllApp);
        mRecyclerView = (RecyclerView) findViewById(R.id.appListRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new AppLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.DEVELOPER_URL)));
            }
        });

        mApps = getApps();
        mAdapter = new AppListAdapter(MainActivity.this, mApps);
        mRecyclerView.setAdapter(mAdapter);


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeveloperApps(mSearchText.getText().toString());
            }
        });
    }

    public ArrayList<App> getApps() {
        ArrayList<App> apps = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            App app = new App();
            app.mAppTile = "Hey -- " + i;
            apps.add(app);
        }
        return apps;
    }

    public class GetDeveloperAppsTask extends AsyncTask<String, App, Void> {

        private MarketSession mSession;
        private String mEmail = "test", mPass = "test";
        private int mProgress;
        private String mAuthToken;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage("Getting App List...");
            mProgressDialog.setTitle("Please wait");
            mProgressDialog.setMax(100);
            mProgressDialog.show();
            mButton.setEnabled(false);
            mProgress = 0;
        }

        @Override
        protected Void doInBackground(String... args) throws RuntimeException {

            mAuthToken = updateToken(true);

            if (mAuthToken.isEmpty()) {
                Log.d("Token", "Token is empty :: " + mAuthToken);
                return null;
            }

            mSession = new MarketSession();
            mSession.setAuthSubToken(mAuthToken);
            // mSession.login(mEmail, mPass);

            String query = args[0];

            AppsRequest appsRequest = AppsRequest.newBuilder()
                    .setQuery(query)
                    .setStartIndex(0)
                    .setEntriesCount(10)
                    .setWithExtendedInfo(true)
                    .build();

            mSession.append(appsRequest, new Callback<AppsResponse>() {
                @Override
                public void onResult(ResponseContext context, AppsResponse response) {

                    List<Market.App> apps = response.getAppList();
                    for (final Market.App app : apps) {

                        final String app_id = app.getId();
                        Market.GetImageRequest imgReq = Market.GetImageRequest.newBuilder()
                                .setAppId(app_id).setImageUsage(Market.GetImageRequest.AppImageUsage.ICON)
                                .setImageId("1").build();

                        MarketSession session = new MarketSession();
                        //session.login(mEmail, mPass);
                        session.setAuthSubToken(mAuthToken);

                        session.append(imgReq, new Callback<Market.GetImageResponse>() {
                            @Override
                            public void onResult(ResponseContext context,
                                                 Market.GetImageResponse response) {

                                Log.d("App Detail", "App Title :: " + app.getTitle() + " - App Package :: " + app.getPackageName());

                                Bitmap bitmap = BitmapFactory.decodeByteArray(response.getImageData().toByteArray(), 0, response.getImageData().toByteArray().length);
                                App appCraweled = new App();
                                appCraweled.mAppTile = app.getTitle();
                                appCraweled.mIcon = bitmap;
                                appCraweled.mRating = Float.parseFloat(app.getRating());
                                appCraweled.mLink = app.getPackageName();
                                publishProgress(appCraweled);
                            }
                        });
                        session.flush();

                    }
                }
            });

            mSession.flush();

            return null;
        }

        @Override
        protected void onProgressUpdate(App... values) {
            super.onProgressUpdate(values);
            mProgress = mProgress + 10;
            mProgressDialog.setProgress(mProgress);
            mApps.add(values[0]);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
            mButton.setEnabled(true);
        }
    }

    private void getDeveloperApps(String text) {
        mApps.clear();
        mAdapter.notifyDataSetChanged();
        new GetDeveloperAppsTask().execute(text);
    }

    private String updateToken(boolean invalidateToken) {
        String authToken = "null";
        try {
            AccountManager am = AccountManager.get(getApplicationContext());
            Account[] accounts = am.getAccountsByType("com.google");
            AccountManagerFuture<Bundle> accountManagerFuture = am.getAuthToken(accounts[0], "android", null, MainActivity.this, null, null);

            Bundle authTokenBundle = accountManagerFuture.getResult();
            authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN).toString();
            if (invalidateToken) {
                am.invalidateAuthToken("com.google", authToken);
                authToken = updateToken(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authToken;
    }

}