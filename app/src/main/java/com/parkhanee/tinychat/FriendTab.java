package com.parkhanee.tinychat;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parkhanee.tinychat.classbox.Friend;

import org.w3c.dom.Text;


public class FriendTab extends Fragment implements View.OnClickListener {
    final String TAG = "FriendTab";
    private FriendTabAdapter adapter;
    private ViewGroup header;
    MySQLite db = null;
    private View myprofile;
    MyPreferences pref=null;
    UserProfileDialog.Builder dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_fragment_friend,container,false);
        header = (ViewGroup)inflater.inflate(R.layout.listview_friend_header, container, false);
        myprofile = header.findViewById(R.id.myprofile);
        myprofile.setOnClickListener(this); // header안에 있는 애니까 header에서 찾아줌 !!
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db==null){
            db = MySQLite.getInstance(getActivity().getApplicationContext());
        }
        if (pref==null){
            pref = MyPreferences.getInstance(getActivity());
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((TextView)myprofile.findViewById(R.id.header_name)).setText(pref.getString("name"));
        // TODO: 2017. 8. 4. 내프로필사진 보이기
        // ((ImageView)myprofile.findViewById(R.id.header_img)) <-- (pref.getString("img"));


        adapter = new FriendTabAdapter(getActivity());
        ListView listView = (ListView) view.findViewById(R.id.friend_list_view);
        listView.setAdapter(adapter);
        listView.addHeaderView(header, null, false);
        adapter.setFriendArrayList(db.getAllFriends());
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // TODO: 2017. 8. 4. show friend's profile dialog
                Friend friend = (Friend) adapterView.getItemAtPosition(i);
                Toast.makeText(getActivity(), "clicked "+friend.getName()+" id:"+friend.getId(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.myprofile :
                // TODO: 2017. 8. 4. init and show my profile dialog
                if (dialog == null){
                    dialog = new UserProfileDialog.Builder(getActivity())
                            .setMine(false)
                            .setTextName("Hanee Park")
                            .setTextNumber("010-6862-0823")
                            .setOnLogoutClicked(new UserProfileDialog.OnLogoutClicked() {
                                @Override
                                public void OnClick(View view, Dialog dialog) {
                                    Toast.makeText(getActivity(), "logout", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .build();
                }
                dialog.show();
                /*if (alert==null){
                    alert = new FancyAlertDialog.Builder(getActivity())
                            .setImageRecourse(R.drawable.plus_circle)
                            .setTextTitle("박하늬")
//                            .setTextSubTitle("박하늬")
                            .setBody("010 6862 0823")
                            .setNegativeColor(R.color.colorWarning)
                            .setNegativeButtonText("닫기")
                            .setOnNegativeClicked(new FancyAlertDialog.OnNegativeClicked() {
                                @Override
                                public void OnClick(View view, Dialog dialog) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButtonText("메세지 보내기")
                            .setPositiveColor(R.color.colorAccent)
                            .setOnPositiveClicked(new FancyAlertDialog.OnPositiveClicked() {
                                @Override
                                public void OnClick(View view, Dialog dialog) {
                                    Toast.makeText(getActivity(), "메세지 보내기", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setButtonsGravity(FancyAlertDialog.PanelGravity.RIGHT)
                            .build();

                }
                alert.show();
        */
                break;
        }
    }


}
