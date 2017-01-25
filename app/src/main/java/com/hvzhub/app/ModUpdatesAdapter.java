package com.hvzhub.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hvzhub.app.API.API;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.model.Games.News.ModUpdateContainer;
import com.hvzhub.app.API.model.Games.News.ModUpdateShort;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.Prefs.GamePrefs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModUpdatesAdapter extends BaseAdapter {
    private List<ModUpdateShort> updateList;
    private Map<Integer, String> newsCache;
    private Context context;

    // A cache for looking up Views
    private static class ViewHolder {
        TextView title;
        TextView text;
        ProgressBar progressBar;
        TextView loadingMsg;
        long id;
    }

    public ModUpdatesAdapter(Context context, List<ModUpdateShort> updateList) {
        this.updateList = updateList;
        this.context = context;
        this.newsCache = new HashMap<>();
    }

    @Override
    public int getCount() {
        return updateList.size();
    }

    @Override
    public ModUpdateShort getItem(int position) {
        return updateList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.mod_update_list_item, parent, false);

            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Get the data item for this position
        final ModUpdateShort modUpdateShort = getItem(position);

        // Populate the data into the template view using the data object
        viewHolder.title.setText(modUpdateShort.title);
        viewHolder.loadingMsg = (TextView) convertView.findViewById(R.id.loading_msg);
        if (viewHolder.id != getItemId(position)) {
            loadModUpdate(viewHolder, modUpdateShort.id);
        }
        viewHolder.id = getItemId(position);



        // Return the completed view to render on screen
        return convertView;
    }

    private void loadModUpdate(final ViewHolder viewHolder, final int modUpdateId) {
        if (newsCache.containsKey(modUpdateId)) {
            viewHolder.text.setText(newsCache.get(modUpdateId));
            return;
        }

        showProgress(viewHolder, true);
        HvZHubClient client = API.getInstance(context).getHvZHubClient();
        String uuid = context.getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE)
                .getString(GamePrefs.PREFS_SESSION_ID, null);
        Call<ModUpdateContainer> call = client.getModUpdate(
                new Uuid(uuid),
                modUpdateId
        );
        call.enqueue(new Callback<ModUpdateContainer>() {
            @Override
            public void onResponse(Call<ModUpdateContainer> call, Response<ModUpdateContainer> response) {
                showProgress(viewHolder, false);
                if (response.isSuccessful()) {
                    viewHolder.text.setText(response.body().update.text);
                    newsCache.put(modUpdateId, response.body().update.text);
                } else {
                    viewHolder.text.setText(R.string.couldnt_load_update);
                }
            }

            @Override
            public void onFailure(Call<ModUpdateContainer> call, Throwable t) {
                showProgress(viewHolder, false);
                viewHolder.text.setText(R.string.couldnt_load_update);
            }
        });
    }

    private void showProgress(ViewHolder viewHolder, boolean show) {
        viewHolder.loadingMsg.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        viewHolder.progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        viewHolder.text.setVisibility(show ? View.INVISIBLE: View.VISIBLE);
    }



}
