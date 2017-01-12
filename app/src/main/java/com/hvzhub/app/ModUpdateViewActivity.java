package com.hvzhub.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hvzhub.app.API.API;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.model.Games.News.ModUpdateContainer;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.Prefs.GamePrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModUpdateViewActivity extends AppCompatActivity {
    public static final String ARG_TITLE = "title";
    public static final String ARG_ID = "id";

    ProgressBar progressBar;
    TextView titleView;
    TextView newsContent;
    ScrollView newsContentWrapper;

    private int updateId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() == null) {
            throw new RuntimeException("ModUpdateViewActivity must be called with extras");
        }
        Bundle extras = getIntent().getExtras();
        if (!extras.containsKey(ARG_TITLE) || !extras.containsKey(ARG_ID)) {
            throw new RuntimeException("ModUpdateViewActivity must be called with ARG_TITLE and ARG_ID");
        }
        String title = extras.getString(ARG_TITLE);
        updateId = extras.getInt(ARG_ID);

        setContentView(R.layout.activity_mod_update_view);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(title);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        titleView = (TextView) findViewById(R.id.news_title);
        newsContent = (TextView) findViewById(R.id.news_content);
        newsContentWrapper = (ScrollView) findViewById(R.id.news_container);

        titleView.setText(title);
        load();
    }

    private void load() {
        showProgress(true);
        HvZHubClient client = API.getInstance(this).getHvZHubClient();
        Call<ModUpdateContainer> call = client.getModUpdate(
                SessionManager.getInstance().getSessionUUID(),
                updateId
        );
        call.enqueue(new Callback<ModUpdateContainer>() {
            @Override
            public void onResponse(Call<ModUpdateContainer> call, Response<ModUpdateContainer> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    newsContent.setText(response.body().update.text);
                } else {
                    API.displayUnexpectedResponseError(ModUpdateViewActivity.this, new API.OnRetryListener() {
                        @Override
                        public void OnRetry() {
                            load();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ModUpdateContainer> call, Throwable t) {
                showProgress(false);
                API.displayGenericConnectionError(ModUpdateViewActivity.this, new API.OnRetryListener() {
                    @Override
                    public void OnRetry() {
                        load();
                    }
                });
            }
        });

    }

    private void showProgress(final boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        newsContentWrapper.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}