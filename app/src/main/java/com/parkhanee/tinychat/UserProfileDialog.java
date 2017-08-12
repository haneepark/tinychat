package com.parkhanee.tinychat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private Button logout;
    private ImageButton editImage, editName;


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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        if (builder != null) {
            // 사용자 이름
            if (builder.getTextName() != null) {
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


            // 설정한 이미지가 있는 경우
            if (builder.getImageUrl() != null) { // TODO: 2017. 8. 12. 이거 구별하는거 어떻게 ?  얘 지금 안되는거같은데 ?
                // TODO: 2017. 8. 12. 이미지 넣어주기  asyncronously in a sub thread
                // load의 paramater로 url형태의 string 넣어주면 되는건가 ???
                // TODO: 2017. 8. 12. 여기서 이미지를 . . 비트맵으로 넣을까 글라이드로 넣을까 ?
                Glide.with(getActivity()).load(builder.getImageUrl()).into(image);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // 이미지 클릭
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: 2017. 8. 12. go to imageActivity
                        Toast.makeText(getActivity(), "ImageActivity", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // TODO: 2017. 8. 11. default image here
                // TODO: 2017. 8. 12. 이미지 크기, 스케일 타입 조정
                image.setImageResource(R.drawable.ic_profile);
            }


            if (builder.isMine()){ // TODO: 2017. 8. 11. my profile

                    // TODO: 2017. 8. 11. 수정 ?
                    positive.setText("수정");
                    positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getActivity(), "edit", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // 로그아웃
                    logout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // TODO: 2017. 8. 12. 로그아웃 하시겠습니까 같은거 ..
                           MyUtil.logout(getActivity());
                            dismiss();
                        }
                    });

                    // 프로필 이미지 변경하기
                    editImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            builder.getOnEditImageClicked().OnClick(view, getDialog());
                        }
                    });

                    // 이름 변경하기
                    editName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            builder.getOnEditNameClicked().OnClick(view, getDialog());
                        }
                    });

            } else { // not my profile

                    // 1:1 채팅 버튼
                    positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getActivity(), "1:1 대화", Toast.LENGTH_SHORT).show();
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

    private void initViews(View view) {
        image = (AppCompatImageView) view.findViewById(R.id.image);
        name = (TextView) view.findViewById(R.id.name);
        number = (Button) view.findViewById(R.id.number);
        positive = (Button) view.findViewById(R.id.positive);
        negative = (Button) view.findViewById(R.id.negative);
        logout = (Button) view.findViewById(R.id.logout);
        editImage = (ImageButton) view.findViewById(R.id.editImage);
        editName = (ImageButton) view.findViewById(R.id.editName);
    }

    private Dialog show(Activity activity, Builder builder) {
        Log.d(TAG, "show in DialogFragment");
        this.builder = builder;
        Log.d(TAG, "show in DialogFragment : isAdded " + String.valueOf(isAdded()));
        if (!isAdded()){
            show(((AppCompatActivity) activity).getSupportFragmentManager(), SimpleName);
        }
        return getDialog();
    }


    public static class Builder implements Parcelable {

        private String textName;
        private String textNumber;

        private OnPositiveClicked onPositiveClicked;
        private OnEditNameClicked onEditNameClicked;
        private OnEditImageClicked onEditImageClicked;

        private String imageUrl;

        private Context context;

        private boolean isMine; // 내 프로필인지 아닌지


        protected Builder(Parcel in) {
            textName = in.readString();
            textNumber = in.readString();
            isMine = in.readByte() != 0;
            imageUrl = in.readString();
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

        public Builder setMine(boolean isMine){
            this.isMine = isMine;
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
            } else {
                String url = context.getString(R.string.server)+context.getString(R.string.server_img_path)+imageUrl;
                return url;
            }
        }

        public Builder setImageUrl(String imageUrl){
            this.imageUrl = imageUrl;
            return this;
        }

        public String getTextName() {
            return textName;
        }

        public Builder setTextName(String textName) {
            this.textName = textName;
            return this;
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

        public OnEditNameClicked getOnEditNameClicked(){
            return onEditNameClicked;
        }

        public Builder setOnEditNameClicked(OnEditNameClicked onEditNameClicked){
            this.onEditNameClicked = onEditNameClicked;
            return this;
        }

        public OnEditImageClicked getOnEditImageClicked(){
            return onEditImageClicked;
        }

        public Builder setOnEditImageClicked (OnEditImageClicked onEditImageClicked){
            this.onEditImageClicked = onEditImageClicked;
            return this;
        }

        public Builder build() {
            return this;
        }
//
        public Dialog show() {
            Log.d(TAG, "show in Builder");
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
            parcel.writeByte((byte) (isMine ? 1 : 0));
        }
    }

    public interface OnPositiveClicked {
        void OnClick(View view, Dialog dialog);
    }

    public interface OnEditNameClicked {
        void OnClick(View view, Dialog dialog);
    }

    public interface OnEditImageClicked {
        void OnClick(View view, Dialog dialog);
    }

}
