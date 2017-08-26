package com.parkhanee.tinychat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parkhanee.tinychat.classbox.Friend;
import com.parkhanee.tinychat.classbox.Room;

import java.util.ArrayList;

/**
 * Created by parkhanee on 2017. 8. 4..
 */

public class RoomTabAdapter extends BaseAdapter {
    final String TAG = "RoomTabAdapter";
    private ArrayList<Room> roomArrayList = new ArrayList<>();
    private Context context=null;

    public RoomTabAdapter(Context context){
        this.context = context;
//        setDummy();
    }

    private void setDummy(){
//        roomArrayList.add(new Room("123345",1));
//        roomArrayList.add(new Room("384935",4));
//        roomArrayList.add(new Room("123334",3));
//        roomArrayList.add(new Room("123346",1));
//        roomArrayList.add(new Room("123347",2));
//        roomArrayList.add(new Room("123348",1));
//        roomArrayList.add(new Room("123349",1));
//        roomArrayList.add(new Room("123350",1));
//        roomArrayList.add(new Room("123351",1));
//        roomArrayList.add(new Room("123352",1));
//        roomArrayList.add(new Room("123353",1));
//        roomArrayList.add(new Room("123354",1));
//        roomArrayList.add(new Room("123355",1));
//        roomArrayList.add(new Room("123356",1));
    }

    @Override
    public int getCount() {
        return roomArrayList.size();
    }

    @Override
    public Room getItem(int i) {
        return roomArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void addItem(Room room){
        roomArrayList.add(room);
    }

    public void clearItem(){
        roomArrayList.clear();
    }

    public void addItem(Room room, int position){
        roomArrayList.add(position,room);
    }

    public void setRoomArrayList(ArrayList<Room> rooms){
        // TODO: 2017. 8. 25. null 인 room 있으면 제외해야 ...
        roomArrayList = rooms;
    }

    @Override
    public View getView(int position, View v, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder;
        if (v == null) {
            holder = new ViewHolder();
            v = inflater.inflate(R.layout.listview_room, null);
            holder.title = (TextView) v.findViewById(R.id.room_title);
            holder.msg = (TextView) v.findViewById(R.id.room_recent_msg);
            holder.time = (TextView) v.findViewById(R.id.room_recent_time);
            holder.img = (ImageView) v.findViewById(R.id.imageView3);
//            holder.pref = MyPreferences.getInstance(context);
//            holder.db = MySQLite.getInstance(context);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag(); // we call the view created before to not create a view in each time
        }

        if (roomArrayList.size()>0){
            Room room = roomArrayList.get(position);

            String title="";
            if (room.isPrivate()){ // 일대일방
                title = room.getParticipant().getName();
            }else { //여러명방
                for (Friend friend : room.getParticipants()){
                    String name = friend.getName();
                    title += name+" ";
                }
            }


            holder.title.setText(title);
            holder.msg.setText(room.getPpl()+" "+room.isPrivate().toString());

            // 이미지 설정
            if (!room.isPrivate()){
                holder.img.setImageResource(R.drawable.tab_friends);
            } else { // 일대일방

                Friend friend = room.getParticipant();
                if (friend.isBlobSet()){
                    byte[] byteArray = friend.getImgBlob();
                    Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

                    // get size of imageView
                    holder.img.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    int targetHeight = holder.img.getMeasuredHeight();
                    int targetWidth = holder.img.getMeasuredWidth();

                    // set image
                    holder.img.setImageBitmap(Bitmap.createScaledBitmap(bmp, targetWidth,
                            targetHeight, false));
                } else {
                    holder.img.setImageResource(R.drawable.ic_profile);
                }
                 

            }
        }

        return v;
    }

    private static class ViewHolder {
        TextView title = null;
        TextView msg = null;
        TextView time = null;
        ImageView img = null;
//        MyPreferences pref = null;
//        MySQLite db=null;
    }
}
