package com.hvzhub.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hvzhub.app.API.model.Chat.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ChatAdapter extends BaseAdapter {
    // If two chat messages are more than CHAT_MESSAGE_SEPARATION_THRESHOLD minutes apart,
    // don't group them
    public static final int CHAT_MESSAGE_SEPARATION_THRESHOLD = 2;
    private List<Message> chats;
    private Context context;

    // A cache for looking up Views
    private static class ViewHolder {
        TextView name;
        TextView date;
        TextView message;
        LinearLayout chatInfoContainer;
    }

    public ChatAdapter(Context context, List<Message> chats) {
        this.chats = chats;
        this.context = context;
    }

    @Override
    public int getCount() {
        return chats.size();
    }

    @Override
    public Message getItem(int position) {
        return chats.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Message msgObject = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);

            convertView = inflater.inflate(R.layout.chat_list_item, parent, false);

            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.message = (TextView) convertView.findViewById(R.id.message);
            viewHolder.chatInfoContainer = (LinearLayout) convertView.findViewById(R.id.chat_info_container);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        if (position > 0 && getItem(position - 1).userId == msgObject.userId) {
            Message lastItem = getItem(position - 1);
            if (lastItem.userId == msgObject.userId) {
                Calendar someTimeAgo = Calendar.getInstance();
                someTimeAgo.setTime(msgObject.timestamp);
                someTimeAgo.add(Calendar.MINUTE, -CHAT_MESSAGE_SEPARATION_THRESHOLD);
                if (lastItem.timestamp.before(someTimeAgo.getTime())) {
                    viewHolder.chatInfoContainer.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.chatInfoContainer.setVisibility(View.GONE);
                }
            }
        } else {
            viewHolder.chatInfoContainer.setVisibility(View.VISIBLE);
        }

        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        viewHolder.name.setText(msgObject.name);
        viewHolder.date.setText(dateFormat.format(msgObject.timestamp));
        viewHolder.message.setText(msgObject.message);

        // Return the completed view to render on screen
        return convertView;
    }


}

