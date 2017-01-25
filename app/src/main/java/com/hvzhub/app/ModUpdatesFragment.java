package com.hvzhub.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hvzhub.app.API.API;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.model.Games.News.ModUpdate;
import com.hvzhub.app.API.model.Games.News.ModUpdatesContainer;
import com.hvzhub.app.API.model.Games.News.ModUpdateShort;
import com.hvzhub.app.API.model.Uuid;

import java.util.List;

import retrofit2.Call;


public class ModUpdatesFragment extends NewsFragment<ModUpdateShort, ModUpdatesContainer> {

    private Snackbar sb;

    public ModUpdatesFragment() {
        super(false);
    }

    public static ModUpdatesFragment newInstance() {
        return new ModUpdatesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        getActivity().setTitle(getString(R.string.mod_updates));
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ModUpdateShort modUpdateShort = (ModUpdateShort) parent.getItemAtPosition(position);
                Intent i = new Intent(getActivity(), ModUpdateViewActivity.class);
                i.putExtra(ModUpdateViewActivity.ARG_TITLE, modUpdateShort.title);
                i.putExtra(ModUpdateViewActivity.ARG_ID, modUpdateShort.id);
                startActivity(i);
            }
        });

    }

    @Override
    protected BaseAdapter createAdapter(List<ModUpdateShort> list) {
        return new ModUpdatesAdapter(getActivity(), list);
    }

    @Override
    protected Call<ModUpdatesContainer> createLoadNewsCall(
            Uuid uuid,
            int gameId,
            int initialNum,
            boolean fetchEntireList,
            int numItemsToFetch
    ) {
        if (!fetchEntireList) {
            throw new RuntimeException(
                    "Unexpected request to fetch partial list.\n" +
                    "Make sure NewsFragment was constructed with infiniteScrollMode = false");
        }
        HvZHubClient client = API.getInstance(getActivity()).getHvZHubClient();
        return client.getModUpdates(
            uuid,
            gameId
        );
    }


}
