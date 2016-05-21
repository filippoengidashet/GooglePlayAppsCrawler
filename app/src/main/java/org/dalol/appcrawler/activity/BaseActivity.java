package org.dalol.appcrawler.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
/**
 * @author Filippo
 * @version 1.0
 * @date 6/16/2015
 *
 * BaseActivity.java: This class is the base activity for all views.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getViewResource());
        configViews();
    }

    protected abstract void configViews();

    protected abstract int getViewResource();
}
