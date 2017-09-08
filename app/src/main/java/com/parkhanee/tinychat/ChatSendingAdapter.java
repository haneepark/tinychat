package com.parkhanee.tinychat;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parkhanee.tinychat.classbox.Chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by parkhanee on 2017. 9. 8..
 */

public class ChatSendingAdapter extends RecyclerView.Adapter<ChatSendingAdapter.MyViewHolder>{
    private static final String TAG = "ChatSendingAdapter";
    private static final int TYPE_SENDING=1;
    private static final int TYPE_FAIL=2;

    // boolean true:sending, false:failed.
    // mid - chat, boolean
//    private HashMap<String, Integer> sendingChatBoolean = new HashMap<>();    // mid - boolean

//    private ArrayList<Chat> chatArrayList = new ArrayList<>();
//    ArrayList<Boolean> booleen = new ArrayList<>();
    private LinkedHashMap<String, List<Object>> sendingChatHashMap = new LinkedHashMap<>();

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_chat_my,parent,false);
        return new MyViewHolder(v,viewType);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Chat chat  =(Chat)sendingChatHashMap.get(getKeyByIndex(sendingChatHashMap,position)).get(0);
        Log.d(TAG, "onBindViewHolder: "+chat.toString());
        holder.body.setText(chat.getBody());
        holder.date.setText(chat.getDate(Chat.TYPE_TIME));
        if (holder.viewType==TYPE_SENDING){
            holder.progressBar.setVisibility(View.VISIBLE);
        }else {
            holder.resend.setVisibility(View.VISIBLE);
            holder.resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: 2017. 9. 8. 메세지 재전송
                }
            });

            holder.delete.setVisibility(View.VISIBLE);
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteSendingChat(chat.getMid());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return sendingChatHashMap.size();
    }

    @Override
    public int getItemViewType(int position) {
        if ((boolean)sendingChatHashMap.get(getKeyByIndex(sendingChatHashMap,position)).get(1)){
            return TYPE_SENDING ;
        }else {
            return TYPE_FAIL;
        }
    }

    public String getKeyByIndex(LinkedHashMap map,int index){
        return  (String)(map.keySet().toArray())[ index ] ;
    }

    public void sendingChat(Chat chat){
        List<Object> list = new ArrayList<>();
        list.add(0,chat);
        list.add(1,true);
        sendingChatHashMap.put(chat.getMid(),list);
        notifyDataSetChanged();
    }

    public boolean sendChatFailed(String mid){
        try {
            // true --> false
            sendingChatHashMap.get(mid).add(1,false);
            notifyDataSetChanged();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // user가 delete 버튼 눌러서 지우는 경우
    // 전송 성공해서 지우는 경우
    public boolean deleteSendingChat(String mid){
        if (sendingChatHashMap.containsKey(mid)){
            sendingChatHashMap.remove(mid);
            notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        int viewType;
        TextView body, date;
        ImageButton resend, delete;
        ProgressBar progressBar;

        public MyViewHolder(View itemView, int viewType){
            super(itemView);
            this.viewType = viewType;
            body = (TextView) itemView.findViewById(R.id.textView18);
            date = (TextView) itemView.findViewById(R.id.textView13);
            if (viewType== TYPE_SENDING){
                progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
            }else {
                resend = (ImageButton) itemView.findViewById(R.id.resend);
                delete = (ImageButton) itemView.findViewById(R.id.delete);
            }
        }
    }
}
