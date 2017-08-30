package com.parkhanee.tinychat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parkhanee.tinychat.classbox.Chat;
import com.parkhanee.tinychat.classbox.Friend;

import java.util.ArrayList;

/**
 * Created by parkhanee on 2017. 8. 26..
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    ArrayList<Chat> chatArrayList = new ArrayList<>();
    Context context=null;
    private static final String TAG = "ChatAdapter";
    private String id;

    private static final int TYPE_MINE=1;
    private static final int TYPE_CHAT=2;
    private static final int TYPE_DATE=3; // TODO: 2017. 8. 30. 날짜 알림 선 넣기 !!


    public ChatAdapter(Context context, String id, ArrayList<Chat> chatArrayList) {
        this.context = context;
        this.chatArrayList = chatArrayList;
        this.id = id;
    }

    public ChatAdapter(Context context,String id){
        this.context = context;
        this.id = id;
    }

    public void setChatArrayList(ArrayList<Chat> chatArrayList) {
        this.chatArrayList = chatArrayList;
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
                        .inflate(R.layout.recyclerview_my_chat,parent,false);
                return new MyViewHolder(v1,viewType);
//            case TYPE_DATE:
//                break;
            default:
                View v2 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_chat,parent,false);
                return new MyViewHolder(v2,viewType);
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Chat chat = chatArrayList.get(position);
        switch (holder.viewType){
            case TYPE_CHAT:
                holder.tv_body.setText(chat.getBody());
                holder.tv_date.setText(chat.getDate());

                Friend friend = holder.sqLite.getFriend(chat.getFrom());
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
                        holder.imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, targetWidth,
                                targetHeight, false));
                    }

                }
                break;
            case TYPE_MINE:
                holder.my_body.setText(chat.getBody());
                holder.my_date.setText(chat.getDate());
                break;
        }


    }

    @Override
    public int getItemViewType(int position) {
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
                    break;
                default:
                    break;
            }

        }
    }
}
