package com.hvzhub.app;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hvzhub.app.API.model.Games.News.GameNewsItem;
import com.hvzhub.app.API.model.Games.News.Player;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
public class GameNewsAdapter extends BaseAdapter {
    private List<GameNewsItem> newsList;
    private Context context;

    // A cache for looking up Views
    private static class ViewHolder {
        TextView date;
        TextView message;
    }

    public GameNewsAdapter(Context context, List<GameNewsItem> newsList) {
        this.newsList = newsList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public GameNewsItem getItem(int position) {
        return newsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.news_list_item, parent, false);

            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.message = (TextView) convertView.findViewById(R.id.message);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Get the data item for this position
        final GameNewsItem newsItem = getItem(position);
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

        // Populate the data into the template view using the data object
        viewHolder.date.setText(dateFormat.format(newsItem.timestamp));
        viewHolder.message.setText(newsItemToHtmlString(newsItem));

        // Return the completed view to render on screen
        return convertView;
    }

    private CharSequence newsItemToHtmlString(GameNewsItem newsItem) {
        int nameColorInt = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        String nameColorString = String.format("#%06X", (0xFFFFFF & nameColorInt));
        String messageRaw;
        switch(newsItem.type) {
            case GameNewsItem.ROUND_END:
                messageRaw = String.format(context.getString(R.string.game_end_format_string), newsItem.player0Name);
                return Html.fromHtml(messageRaw);
            case GameNewsItem.ROUND_START:
                messageRaw = String.format(context.getString(R.string.game_start_format_string), newsItem.player0Name);
                return Html.fromHtml(messageRaw);
            case GameNewsItem.JOIN:
                //noinspection ResourceType
                messageRaw = String.format(
                        "<font color='%s'>%s</font> %s",
                        nameColorString,
                        newsItem.player0Name,
                        context.getString(R.string.joined_the_game)
                );
                return Html.fromHtml(messageRaw);
            case GameNewsItem.TAG_WITH_ASSISTANTS:
                messageRaw = String.format(
                        "<font color='%1$s'>%2$s</font> %3$s <font color='%1$s'>%4$s</font> %5$s %6$s",
                        nameColorString,
                        newsItem.player0Name,
                        context.getString(R.string.was_tagged_by),
                        newsItem.player1Name,
                        context.getString(R.string.with_help_from),
                        getAssistantsString(newsItem.assistants, nameColorString)
                );
                return Html.fromHtml(messageRaw);
            case GameNewsItem.TAG:
                //noinspection ResourceType
                messageRaw = String.format(
                        "<font color='%1$s'>%2$s</font> %3$s <font color='%1$s'>%4$s</font>",
                        nameColorString,
                        newsItem.player0Name,
                        context.getString(R.string.was_tagged_by),
                        newsItem.player1Name
                );
                return Html.fromHtml(messageRaw);
            case GameNewsItem.TAG_OZ_WITH_ASSISTANTS:
                //noinspection ResourceType
                messageRaw = String.format(
                        "<font color='%s'>%s</font> %s %s %s %s",
                        nameColorString,
                        newsItem.player0Name,
                        context.getString(R.string.was_tagged_by),
                        context.getString(R.string.an_oz),
                        context.getString(R.string.with_help_from),
                        getAssistantsString(newsItem.assistants, nameColorString)
                );
                return Html.fromHtml(messageRaw);
            case GameNewsItem.TAG_OZ:
                //noinspection ResourceType
                messageRaw = String.format(
                        "<font color='%s'>%s</font> %s %s",
                        nameColorString,
                        newsItem.player0Name,
                        context.getString(R.string.was_tagged_by),
                        context.getString(R.string.an_oz)
                );
                return Html.fromHtml(messageRaw);
            case GameNewsItem.OZ_REVEAL:
                //noinspection ResourceType
                messageRaw = String.format(
                        "<font color='%s'>%s</font> %s",
                        nameColorString,
                        newsItem.player0Name,
                        context.getString(R.string.was_revealed_as_an_oz)
                );
                return Html.fromHtml(messageRaw);
            default:
                return context.getString(R.string.error_loading_item);
        }
    }

    private String getAssistantsString(List<Player> assistants, String nameColorString) {
        StringBuilder asstStrBuilder = new StringBuilder();
        for (int i = 0; i < assistants.size(); i++) {
            Player asst = assistants.get(i);
            asstStrBuilder.append(
                    String.format(
                            "<font color='%s'>%s</font>",
                            nameColorString,
                            asst.name
                    )
            );

            if (i == assistants.size() - 2) {
                asstStrBuilder.append(' ');
                asstStrBuilder.append(context.getString(R.string.and));
                asstStrBuilder.append(' ');
            } else if (i < assistants.size() - 2) {
                asstStrBuilder.append(", ");
            }
        }
        return asstStrBuilder.toString();
    }


}


