package com.parkhanee.tinychat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

/**
 * Created by parkhanee on 2017. 8. 11..
 */

public class UserProfileDialog extends DialogFragment {
    public static final String SimpleName = UserProfileDialog.class.getSimpleName();
    public static final String TAG = "UserProfileDialog";

    private Builder builder;
    private static UserProfileDialog instance = new UserProfileDialog(); // why static ?

    public static UserProfileDialog getInstance(){
        return instance;
    }

    private AppCompatImageView image;
    private TextView name;
    private Button number, positive, negative;

    // my profile
    private AppCompatImageView empty;
    private Button logout;
    private ImageButton editImage;
    private EditText editName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.setCancelable(true);

        if (savedInstanceState != null) {
            if (builder != null) {
                builder = savedInstanceState.getParcelable(Builder.class.getSimpleName());
            }
        }
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_userprofile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        if (builder != null) {
            // 사용자 이름
            if (builder.getTextName() != null) {
                if (name == null){
                    Log.d(TAG, "onViewCreated: name is null ? ");
                }
                name.setText(builder.getTextName());
            } else {
                // TODO: 2017. 8. 11. 이름이 없을때 처리 ?
                name.setText(" ");
            }

            // 사용자 전화번호
            if (builder.getTextNumber() != null) {
                number.setText(builder.getTextNumber());
            } else {
                number.setText(" ");
            }

            // 닫기 버튼
            negative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            if (builder.isEditing()){ // 프사 바꾸려고 방금 갤러리/카메라에서 선택한 사진이 있는 경우. (아직 서버에 저장 안함)
                image.setImageBitmap(builder.getImageBitmap());
            } else if (builder.getImageUrl() != null) {// 설정한 이미지가 있는 경우
                // 이미지 넣어주기  : Glide
                Glide.with(getActivity()).load(builder.getImageUrl()).into(image);
                // 이미지 클릭
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: 2017. 8. 12. go to imageActivity
                        Toast.makeText(getActivity(), "ImageActivity", Toast.LENGTH_SHORT).show();
                    }
                });
            } else { // 디폴트 이미지 보이는 경우
                empty.setVisibility(View.VISIBLE);
            }


            if (builder.isMine()){ // my profile

                    if (builder.isEditing()){ // 프로필 수정 중 일 때

                        setViewEditingState(builder);

                    } else { // 프로필 수정 중 아닐 때
                        builder.setImageBitmapNull();

                        positive.setText("수정");
                        positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setViewEditingState(builder);
                            }
                        });

//                        // 닫기 버튼을 parent END로 정렬 <-- positive 버튼이 없기 때문에 .
//                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)negative.getLayoutParams();
//                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                        params.addRule(RelativeLayout.ALIGN_PARENT_END);
//                        negative.setLayoutParams(params); //causes layout update
                    }


                    // 로그아웃
                    logout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // TODO: 2017. 8. 12. 로그아웃 하시겠습니까
                            MyUtil.logout(getActivity());
                            dismiss();
                        }
                    });

                    // 프로필 이미지 변경하기
                    editImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // TODO: 2017. 8. 12.
                            builder.getOnEditImageClicked().OnClick(view, getDialog());
                            dismiss();
                        }
                    });

                    editName.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            builder.setEditedName(editable.toString());
                        }
                    });

            } else { // not my profile

                    // 1:1 채팅 버튼
                    positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (builder.getRid()!=null){
                                Intent i =  new Intent(getActivity(),ChatActivity.class);
                                // 채팅방 정보 번들로 넘겨주기 !
                                i.putExtra("rid",builder.getRid());
                                // TODO: 2017. 8. 22.   builder.getFriendId() ?
                                // TODO: 2017. 8. 22. chatActivity 에서 intent로 friend-id받아야. .. 하나 ?
                                startActivity(i);
                                dismiss();
                            } else {
                                Toast.makeText(getActivity(), "방 아이디 가져올 수 없음?", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    // 전화번호 누르면 통화
                    number.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", builder.getTextNumber(), null));
                            startActivity(intent);
                        }
                    });

                    // 본인 프로필일때만 보이는 버튼들 가리기
                    logout.setVisibility(View.GONE);
                    editImage.setVisibility(View.GONE);
                    editName.setVisibility(View.GONE);

            }
        }
    }

    private void setViewEditingState(Builder builder){

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        editImage.setVisibility(View.VISIBLE);
        builder.setEditing(true);
        // 이름 수정하는 edittext
        editName.setVisibility(View.VISIBLE);
        name.setVisibility(View.INVISIBLE);
        editName.setText(builder.getTextName());

        // 수정사항 저장 버튼 텍스트 설정
        positive.setText("수정 사항 저장");

        // 수정 사항 저장 버튼 크기 늘리기 !!
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)positive.getLayoutParams();
        params.width = Math.round(130 * getResources().getDisplayMetrics().density); // 300 dp -->  900.0 pixel(float) --> 900 pixel(int)
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        positive.setLayoutParams(params); //causes layout update

        final OnUpdateClicked updateClicked = builder.getOnUpdateClicked();

        // 수정사항 저장 버튼 onClick
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateClicked.OnClick(view, getDialog());
                dismiss();
            }
        });
        logout.setVisibility(View.GONE);
    }

    private void initViews(View view) {
        image = (AppCompatImageView) view.findViewById(R.id.image);
        name = (TextView) view.findViewById(R.id.name);
        number = (Button) view.findViewById(R.id.number);
        positive = (Button) view.findViewById(R.id.positive);
        negative = (Button) view.findViewById(R.id.negative);
        logout = (Button) view.findViewById(R.id.logout);
        editImage = (ImageButton) view.findViewById(R.id.editImage);
        editName = (EditText) view.findViewById(R.id.editName);
        empty = (AppCompatImageView) view.findViewById(R.id.empty_image);
    }

    private Dialog show(Activity activity, Builder builder) {
        this.builder = builder;
        if (!isAdded()){
            show(((AppCompatActivity) activity).getSupportFragmentManager(), SimpleName);
        }
        return getDialog();
    }


    public static class Builder implements Parcelable {

        private String textName;
        private String textNumber;

        // 친구프로필에서 1:1 대화방으로 넘어갈때 필요
        private String rid;

        // 내 프로필에서 이름 수정 했을 때
        private String editedName;

        private OnPositiveClicked onPositiveClicked;
        private OnEditImageClicked onEditImageClicked;
        private OnUpdateClicked onUpdateClicked;

        private String imageUrl;
        private Bitmap imageBitmap;

        private Context context;

        private boolean isMine; // 내 프로필인지 아닌지
        private boolean editing;  // 프로필 사진 수정 중 일때 true


        protected Builder(Parcel in) {
            textName = in.readString();
            textNumber = in.readString();
            rid = in.readString();
            editedName = in.readString();
            isMine = in.readByte() != 0;
            imageUrl = in.readString();
            imageBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel in) {
                return new Builder(in);
            }

            @Override
            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };

        public boolean isMine(){
            return isMine;
        }

        Builder setMine(boolean isMine){
            this.isMine = isMine;
            return this;
        }

        public boolean isEditing(){
            return editing;
        }

        public Builder setEditing(boolean editing) {
            this.editing = editing;
            return this;
        }

        //        public Context getContext() {
//            return context;
//        }
//
//        public Builder setActivity(Context context) {
//            this.context = context;
//            return this;
//        }

        public Builder(Context context) {
            this.context = context;
        }

//        public Builder (){}

        @Nullable
        public String getImageUrl(){
            if (imageUrl.trim().length() <= 0){ // 이미지 설정 안 된 경우
                return null;
            }

            // 원래는 클라 db에 파일이름만 저장하다가 전체 url 저장하는걸로 바뀌어서 if문 두개로 처리했음

            else if(imageUrl.toLowerCase().contains("http")){ // imageUrl 에 http 포함 한 경우
                return imageUrl;
            } else { // 안한 경우
                String url = context.getString(R.string.server_url)+context.getString(R.string.server_img_path)+imageUrl;
                return url;
            }
        }

        public Builder setImageUrl(String imageUrl){
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder setImageBitmap(Bitmap bitmap){
            this.imageBitmap = bitmap;
            return this;
        }

        public Builder setImageBitmapNull(){
            this.imageBitmap = null;
            return this;
        }

        public Bitmap getImageBitmap() {
            return imageBitmap;
        }

        public String getTextName() {
            return textName;
        }

        public Builder setTextName(String textName) {
            this.textName = textName;
            return this;
        }

        public Builder setRid(String rid) {
            this.rid = rid;
            return this;
        }

        public String getRid() {
            return rid;
        }

        public Builder setEditedName(String editedName) {
            this.editedName = editedName;
            return this;
        }

        public String getEditedName() {
            return editedName;
        }


        public String getTextNumber() {
            return textNumber;
        }

        public Builder setTextNumber(String textNumber) {
            if (MyUtil.nidFormChecker(textNumber)){
                // 휴대폰번호 형식이 맞으면 하이픈 넣기
                textNumber = new StringBuilder(textNumber).insert(7, "-").toString(); // 0101234-5678
                textNumber = new StringBuilder(textNumber).insert(3, "-").toString(); // 010-1234-5678
            }
            this.textNumber = textNumber;
            return this;
        }

        public OnPositiveClicked getOnPositiveClicked() {
            return onPositiveClicked;
        }

        public Builder setOnPositiveClicked(OnPositiveClicked onPositiveClicked) {
            this.onPositiveClicked = onPositiveClicked;
            return this;
        }

        public OnEditImageClicked getOnEditImageClicked(){
            return onEditImageClicked;
        }

        public Builder setOnEditImageClicked (OnEditImageClicked onEditImageClicked){
            this.onEditImageClicked = onEditImageClicked;
            return this;
        }


        public OnUpdateClicked getOnUpdateClicked() {
            return onUpdateClicked;
        }

        public Builder setOnUpdateClicked(OnUpdateClicked onUpdateClicked) {
            this.onUpdateClicked = onUpdateClicked;
            return this;
        }

        public Builder build() {
            return this;
        }
//
        public Dialog show() {
            return UserProfileDialog.getInstance().show(((Activity) context), this);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(textName);
            parcel.writeString(textNumber);
            parcel.writeString(rid);
            parcel.writeString(editedName);
            parcel.writeByte((byte) (isMine ? 1 : 0));
            parcel.writeValue(imageBitmap);
            parcel.writeString(imageUrl);
        }
    }

    public interface OnPositiveClicked {
        void OnClick(View view, Dialog dialog);
    }

    public interface OnEditImageClicked {
        void OnClick(View view, Dialog dialog);
    }

    public interface OnUpdateClicked {
        void OnClick(View view, Dialog dialog);
    }

}
