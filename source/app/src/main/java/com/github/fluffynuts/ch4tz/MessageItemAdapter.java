package com.github.fluffynuts.ch4tz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MessageItemAdapter extends ArrayAdapter<MessageWrapper> {
    private final LayoutInflater _layoutInflater;
    private final MainActivity _parent;
    private ArrayList<MessageWrapper> _messages;

    public MessageItemAdapter(LayoutInflater layoutInflater, MainActivity context, int textViewResourceId, ArrayList<MessageWrapper> messages) {
        super(context, textViewResourceId, messages);
        _messages = messages;
        _layoutInflater = layoutInflater;
        _parent = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = _layoutInflater.inflate(R.layout.chat_list, null);
        }
        MessageWrapper message = _messages.get(position);
        if (message == null)
            return v;
        setTextViewText(v, R.id.username, _parent.getSenderFrom(message));
        setTextViewText(v, R.id.message, _parent.getMessageFrom(message));
        return v;
    }

    private void setTextViewText(View view, int id, String value) {
        TextView target = (TextView) (view.findViewById(id));
        if (target == null)
            return;
        target.setText(value);
    }
}
