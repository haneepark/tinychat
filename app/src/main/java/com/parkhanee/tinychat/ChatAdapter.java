package com.parkhanee.tinychat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parkhanee.tinychat.classbox.Chat;

import java.util.ArrayList;

/**
 * Created by parkhanee on 2017. 8. 26..
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    ArrayList<Chat> chatArrayList = new ArrayList<>();
    Context context=null;
    private static final String TAG = "ChatAdapter";

    public ChatAdapter(Context context, ArrayList<Chat> chatArrayList) {
        this.context = context;
        this.chatArrayList = chatArrayList;
    }

    public ChatAdapter(Context context){
        this.context = context;
    }

    public void setChatArrayList(ArrayList<Chat> chatArrayList) {
        this.chatArrayList = chatArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_chat,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Chat chat = chatArrayList.get(position);
        String from = chat.getFrom();
        String name;
        if (from.equals( holder.pref.getId())){
            name = holder.pref.getName(); // 내이름
        }else {
            name = holder.sqLite.getFriendName(chat.getFrom()); // 친구이름 찾아서 넣기
        }

        holder.tv_from.setText(name);
        holder.tv_body.setText(chat.getBody());
        holder.tv_date.setText(chat.getDate());
    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv_from, tv_date, tv_body;
        MySQLite sqLite;
        MyPreferences pref;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_from = (TextView) itemView.findViewById(R.id.textView15);
            tv_date = (TextView) itemView.findViewById(R.id.textView16);
            tv_body = (TextView) itemView.findViewById(R.id.textView17);
            sqLite = MySQLite.getInstance(context);
            pref = MyPreferences.getInstance(context);
        }
    }
}
