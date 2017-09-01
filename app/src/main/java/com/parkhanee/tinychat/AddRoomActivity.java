package com.parkhanee.tinychat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parkhanee.tinychat.classbox.Friend;

import java.util.ArrayList;

public class AddRoomActivity extends AppCompatActivity {
    EditText et_search;
    ImageButton btn_clear, btn_search;
    MySQLite db=null;
    AddRoomAdapter adapter;
    Button btn_confirm;
    boolean active=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        setToolbar();

        // set listView
        adapter = new AddRoomAdapter();
        ListView listView = (ListView) findViewById(R.id.listview_add_room);
        listView.setAdapter(adapter);

        if (db==null){
            db = MySQLite.getInstance(AddRoomActivity.this);
        }
        adapter.setAllFriends(db.getAllFriends());
        adapter.notifyDataSetChanged();

        initViews();
    }

    private void initViews(){
        btn_confirm = (Button) findViewById(R.id.btn_addRoom);
        btn_confirm.setText("0"+getText(R.string.add_room_btn));
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active){
                    Intent intent = new Intent(AddRoomActivity.this,ChatActivity.class);
                    // 참여자 정보 가져오기 : 단체방이고 empty room 인 경우 에만 참여자정보를 pref 가 아니라 인텐트에서 가져온다
                    // ppl  -  2:68620823,11111111
                    //   intent.putExtra
                    startActivity(intent);
                }
            }
        });

        // 검색과 관련된 View 들 init
        et_search = (EditText) findViewById(R.id.et_search2);
        btn_search = (ImageButton) findViewById(R.id.btn_search2);
        btn_clear = (ImageButton) findViewById(R.id.cleanable_button_clear2);
        btn_clear.setVisibility(View.INVISIBLE);

        // edit text 에서 키보드 enter 누른 경우
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    search();
                }
                return true;
            }
        });

        // search image button 누른 경우
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

        // cleanable edittext
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
            }
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {
                    btn_clear.setVisibility(View.VISIBLE);
                    // TODO: 2017. 9. 1. search() ?  글자 입력할 때 마다 검색결과 보이도록!!
                    search();
                } else {
                    // 입력했던 글자 모두 지우면
                    // cleanable button 안보이기 && 이전 검색 결과 clear
                    btn_clear.setVisibility(View.INVISIBLE);
                    adapter.clearItem();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void setToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // do not show default name text and instead, show the textView i included
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // do not show back button
    }

    public void search(){
        String searchString =  et_search.getText().toString();
        adapter.getFilter().filter(searchString);
    }

    public class AddRoomAdapter extends BaseAdapter {
        private ArrayList<Friend> friends = new ArrayList<>();
        private ArrayList<Friend> allFriends;
        private static final String TAG = "AddRoomAdapter";
        private SparseBooleanArray booleans; // key-friend_id : value-checked

        @Override
        public int getCount() {
            return friends.size();
        }

        @Override
        public Object getItem(int i) {
            return friends.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public void setAllFriends(ArrayList<Friend> allFriends){
            this.allFriends = allFriends;
            friends = allFriends;
            booleans = new SparseBooleanArray(allFriends.size());
            for (int i =0; i< allFriends.size();i++){
                booleans.put(Integer.parseInt(allFriends.get(i).getId()),false);
            }
//            Log.d(TAG, "setAllFriends: "+booleans.toString()); //setAllFriends: {11111111=false, 22222222=false, 42424242=false, 91433734=false}
        }

        public void clearItem(){
            // search 결과를 clear.
            friends = allFriends;
            this.notifyDataSetChanged();
        }

        /**
         * SparseBooleanArray 에서 선택된 항목의 개수를 세어서 btn_confirm의  UI update
         * */
        private void countBoolean(){
            int count=0;
            Log.d(TAG, "countBoolean: "+booleans.toString());
            for (int i =0; i< allFriends.size();i++){
                if (booleans.get(Integer.parseInt(allFriends.get(i).getId()))){
                    count++;
                }
            }
            String text_count = String.valueOf(count)+getString(R.string.add_room_btn);
            btn_confirm.setText(text_count);
            if (count>1){
                // button active
                active =true;
                btn_confirm.setBackgroundResource(R.color.colorAccent);
            }else {
                // button inactive
                active=false;
                btn_confirm.setBackgroundResource(R.color.textGrey);
            }
        }

        @Override
        public View getView(final int i, View v, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) AddRoomActivity.this.getSystemService(AddRoomActivity.LAYOUT_INFLATER_SERVICE);
            final ViewHolder holder;
            if (v == null) {
                holder = new ViewHolder();
                v = inflater.inflate(R.layout.listview_add_room, null);
                holder.img = (ImageView) v.findViewById(R.id.add_room_img);
                holder.tv = (CheckedTextView) v.findViewById(R.id.tv_check);
//                holder.db = MySQLite.getInstance(AddRoomActivity.this);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag(); // we call the view created before to not create a view in each time
            }

            if (friends.size() > 0) {
                Friend friend = friends.get(i);
                final int friend_id = Integer.parseInt(friend.getId());

                holder.tv.setText(friend.getName());
                holder.tv.setChecked(booleans.get(friend_id));
                if (booleans.get(friend_id)){
                    holder.tv.setCheckMarkDrawable(R.drawable.check3);
                }else {
                    holder.tv.setCheckMarkDrawable(null);
                }
                Log.d(TAG, "getView: "+friend_id+" "+booleans.get(friend_id));

                holder.tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.tv.isChecked()){ // 체크 --> 해제
                            holder.tv.setChecked(false);
                            booleans.put(friend_id,false);
                            holder.tv.setCheckMarkDrawable(null);

                            countBoolean();

                        } else { // 해제 --> 체크
                            holder.tv.setChecked(true);
                            booleans.put(friend_id,true);
                            holder.tv.setCheckMarkDrawable(R.drawable.check3);

                            countBoolean();
                        }
                    }
                });

                // set blob type image on imageView
                Log.d(TAG, "getView: "+i+" "+friend.toString());
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
            return v;
        }

        private class ViewHolder {
            ImageView img = null;
            CheckedTextView tv = null;
        }

        public Filter getFilter(){
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults results = new FilterResults();
                    ArrayList<Friend> filteredArrayList = new ArrayList<>();

                    if (charSequence == null || charSequence.length() == 0) {
                        // set the Original result to return
                        results.count = allFriends.size();
                        results.values = allFriends;
                    } else {
                        charSequence = charSequence.toString().toLowerCase();
//                        Log.d(TAG, "performFiltering: constraint " + charSequence);
//                        Log.d(TAG, "performFiltering: allFriend.size" + String.valueOf(allFriends.size()));
                        for (int i = 0; i < allFriends.size(); i++) {
                            Friend f = allFriends.get(i);
                            String data = f.getName();
//                            Log.d(TAG, "performFiltering: data " + data);
                            if (data.toLowerCase().contains(charSequence.toString())) {
                                filteredArrayList.add(f);
                            }
                        }
                        // set the Filtered result to return
                        results.count = filteredArrayList.size();
                        results.values = filteredArrayList;
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    friends = (ArrayList<Friend>) filterResults.values;
//                    Log.d(TAG, "publishResults: "+friends.toString());
                    notifyDataSetChanged();
                }
            };
            return filter;
        }
    }
}
