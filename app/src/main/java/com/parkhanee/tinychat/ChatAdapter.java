package com.parkhanee.tinychat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parkhanee.tinychat.classbox.Chat;
import com.parkhanee.tinychat.classbox.Friend;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by parkhanee on 2017. 8. 26..
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private ArrayList<Chat> chatArrayList = new ArrayList<>();
    private HashMap<String, Friend> userHashMap = new HashMap<>();
    private Context context=null;
    private static final String TAG = "ChatAdapter";
    private String id, rid;

    private static final int TYPE_MINE=1;
    private static final int TYPE_CHAT=2;
    private static final int TYPE_DATE=3; // 날짜 알림 선


    public ChatAdapter(Context context,String id,String rid){
        this.context = context;
        this.id = id;
        this.rid = rid;
    }

    public void setChatArrayList(ArrayList<Chat> chatArrayList) {
        this.chatArrayList = chatArrayList;
        notifyDataSetChanged();
    }

    void setSendingMessageToFail(){
        for (Chat chat : chatArrayList){
            if (chat.getType()==1){
                chat.setType(2);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_CHAT:
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_chat,parent,false);
                return new MyViewHolder(v,viewType);
            case TYPE_MINE:
                View v1 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_chat_my,parent,false);
                return new MyViewHolder(v1,viewType);
            case TYPE_DATE:
                View v2 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_chat_date,parent,false);
                return new MyViewHolder(v2,viewType);
            default:
                View v3 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_chat,parent,false);
                return new MyViewHolder(v3,viewType);
        }
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Chat chat = chatArrayList.get(position);
        switch (holder.viewType){
            case TYPE_CHAT:
                holder.tv_body.setText(chat.getBody());
                holder.tv_date.setText(chat.getDate(Chat.TYPE_TIME));
                Friend friend;

                if (userHashMap.containsKey(chat.getFrom())){
                    friend = userHashMap.get(chat.getFrom());
                }else {
                    friend = holder.sqLite.getFriend(chat.getFrom());
                    userHashMap.put(chat.getFrom(),friend);
                }

                if (friend!=null){
                    String name = friend.getName(); // 친구이름 찾아서 넣기
                    holder.tv_from.setText(name);

                    // set blob type image on imageView
                    if (friend.isBlobSet()){
                        byte[] byteArray = friend.getImgBlob();
                        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

                        // get size of imageView
                        holder.imageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        int targetHeight = holder.imageView.getMeasuredHeight();
                        int targetWidth = holder.imageView.getMeasuredWidth();

                        // set image
                        holder.imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, targetWidth, // FIXME: 2017. 9. 1. OutOfMemoryError
                                targetHeight, false));
                    }

                }
                break;
            case TYPE_MINE:
                holder.my_body.setText(chat.getBody());
                if (chat.getType()==0){ // sent
                    holder.my_date.setText(chat.getDate(Chat.TYPE_TIME));
                    holder.cardView.setVisibility(View.GONE);
                    holder.progressBar.setVisibility(View.GONE);

                }else if (chat.getType()==1){ // sending
                    holder.my_date.setText("");
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.cardView.setVisibility(View.GONE);

                } else if (chat.getType()==2){ // failed
                    holder.cardView.setVisibility(View.VISIBLE);
                    holder.my_date.setText("");

                    if (chat.getRid().equals("")){ // 방 처음 만들때 보내는 메세지
                        holder.resend.setVisibility(View.GONE);
                    } else {
                        holder.resend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(context, "resend", Toast.LENGTH_SHORT).show();
                                new TcpAsyncTask(chat,holder.sqLite).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        });
                    }

                    holder.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // 전송 실패한 메세지를 삭제하고 리스트뷰 아이템을 다시 불러온다
                            Toast.makeText(context, "delete", Toast.LENGTH_SHORT).show();
                            holder.sqLite.deleteSendingChat(chat.getMid());

                            ArrayList<Chat> chats = new ArrayList<>();
                            if (holder.sqLite.getAllChatInARoom(rid)!=null){
                                chats.addAll(holder.sqLite.getAllChatInARoom(rid));
                            }
                            if (holder.sqLite.getAllSendingChatInARoom(rid)!=null){
                                chats.addAll(holder.sqLite.getAllSendingChatInARoom(rid));
                            }
                            if (holder.sqLite.getAllFailedChatInARoom(rid)!=null){
                                chats.addAll(holder.sqLite.getAllFailedChatInARoom(rid));
                            }
                            setChatArrayList(chats);
                        }
                    });
                }
                break;
            case TYPE_DATE:
                holder.date.setText(chat.getDate(Chat.TYPE_DATE));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (chatArrayList.get(position).isDateObject()) return TYPE_DATE;
        if (chatArrayList.get(position).getFrom().equals(id)){
            return TYPE_MINE;
        }else {
            return TYPE_CHAT;
        }
    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        int viewType;

        // TYPE_CHAT
        TextView tv_from, tv_date, tv_body;
        ImageView imageView;
        MySQLite sqLite;
//        MyPreferences pref;

        // TYPE_MINE
        TextView my_date,my_body;
        CardView cardView;
        ImageButton resend, delete;
        ProgressBar progressBar;

        // TYPE_DATE
        TextView date;

        public MyViewHolder(View itemView,int viewType) {
            super(itemView);
            this.viewType = viewType;

            switch (viewType){
                case TYPE_CHAT:
                    tv_from = (TextView) itemView.findViewById(R.id.textView15);
                    tv_date = (TextView) itemView.findViewById(R.id.textView16);
                    tv_body = (TextView) itemView.findViewById(R.id.textView17);
                    imageView = (ImageView) itemView.findViewById(R.id.imageView4);
                    sqLite = MySQLite.getInstance(context);
//                    pref = MyPreferences.getInstance(context);
                    break;
                case TYPE_MINE:
                    my_date = (TextView) itemView.findViewById(R.id.textView13);
                    my_body = (TextView) itemView.findViewById(R.id.textView18);
                    progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
                    resend = (ImageButton) itemView.findViewById(R.id.resend);
                    delete = (ImageButton) itemView.findViewById(R.id.delete);
                    cardView = (CardView) itemView.findViewById(R.id.buttonCard);
                    sqLite = MySQLite.getInstance(context);
                    break;
                case TYPE_DATE:
                    date = (TextView) itemView.findViewById(R.id.textView14);
                default:
                    break;
            }

        }
    }

    class TcpAsyncTask extends AsyncTask<String, Void, Void> {
        private static final String TAG = "TcpAsyncTask";
        String msg="empty message";
        boolean isSent=true; // false 이면 onPostExecute 에서 new async 실행
        Chat chat;
        MySQLite sqLite;

        public TcpAsyncTask(Chat chat, MySQLite sqLite) {
            super();
            this.chat = chat;
            this.sqLite = sqLite;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Date date = new Date();
            long time = date.getTime()/1000;
            Log.d(TAG, "sendMessage: unitTime " +time );
            String unixTime = String.valueOf(time);

            chat = new Chat(chat.getMid(),chat.getRid(),chat.getFrom(),chat.getBody(),unixTime,1);
            sqLite.updateChat(chat,1);
            Log.d(TAG, "onPreExecute: "+chat.toString());
        }

        @Override
        protected Void doInBackground(String... strings) {
            MyTCPClient tcpClient = null;

            tcpClient = MyTCPClient.getInstance();

            if (tcpClient!=null){
                if (tcpClient.isRunning()){
                    tcpClient.sendMessage(chat);
                    isSent=true;
                } else {
                    isSent=false;
                }
            } else { // 서비스가 이미 시작되지 않은 경우 또는 서비스가 tcpClient 시작시키지 않은 경우
                isSent=false;
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: "+msg);
            // FIXME: 2017. 9. 2.  async가 끊임없이 다시 시작되는 문제 ..
            if (!isSent){
                sqLite.updateChat(chat.getMid(),2); // 전송 실패
                Log.d(TAG, "onPostExecute: update chat");
            }

            ArrayList<Chat> chats = new ArrayList<>();
            if (sqLite.getAllChatInARoom(rid)!=null){
                chats.addAll(sqLite.getAllChatInARoom(rid));
            }
            if (sqLite.getAllSendingChatInARoom(rid)!=null){
                chats.addAll(sqLite.getAllSendingChatInARoom(rid));
            }
            if (sqLite.getAllFailedChatInARoom(rid)!=null){
                chats.addAll(sqLite.getAllFailedChatInARoom(rid));
            }

            setChatArrayList(chats);

        }
    }

}
