package com.parkhanee.tinychat;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;

/**
 * Created by parkhanee on 2017. 8. 11..
 */

public class UserProfileDialog extends DialogFragment {
    public static final String TAG = UserProfileDialog.class.getSimpleName();
    private Builder builder;
    private static UserProfileDialog instance = new UserProfileDialog(); // why static ?

    public static UserProfileDialog getInstance(){
        return instance;
    }

    private AppCompatImageView image;
    private TextView name;
    private Button number, positive, negative;

    // my profile
    private ImageButton logout,editImage, editName;


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
            if (builder.getTextName() != null) {
                name.setText(builder.getTextName());
            } else {
                // TODO: 2017. 8. 11. 이름이 없을때 처리 ?
                name.setVisibility(View.GONE);
            }

            if (builder.getTextNumber() != null) {
                number.setText(builder.getTextNumber());
            } else {
                number.setVisibility(View.GONE);
            }

            if (builder.getOnPositiveClicked() != null) {
                Log.d("OnPositive", "Clicked");
                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.getOnPositiveClicked().OnClick(v, getDialog());
                    }
                });
            }

            if (builder.getOnNegativeClicked() != null) {
                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.getOnNegativeClicked().OnClick(v, getDialog());
                    }
                });
            }

            if (builder.getImageRecourse() != 0) {
                Drawable imageRes = VectorDrawableCompat.create(getResources(), builder.getImageRecourse(), getActivity().getTheme());
                image.setImageDrawable(imageRes);
            } else if (builder.getImageDrawable() != null) {
                image.setImageDrawable(builder.getImageDrawable());
            } else {
                // TODO: 2017. 8. 11. default image here
                image.setVisibility(View.GONE);
            }

            if (builder.isAutoHide()) {
                int time = builder.getTimeToHide() != 0 ? builder.getTimeToHide() : 10000;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isAdded() && getActivity() != null)
                            dismiss();
                    }
                }, time);
            }

            if (builder.isMine()){
                // TODO: 2017. 8. 11. my profile
                logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        builder.getOnLogoutClicked().OnClick(view, getDialog());
                    }
                });
                editImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        builder.getOnEditImageClicked().OnClick(view, getDialog());
                    }
                });
                editName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        builder.getOnEditNameClicked().OnClick(view, getDialog());
                    }
                });
            } else {
                // not my profile
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
        logout = (ImageButton) view.findViewById(R.id.logout);
        editImage = (ImageButton) view.findViewById(R.id.editImage);
        editName = (ImageButton) view.findViewById(R.id.editName);
    }

    private Dialog show(Activity activity, Builder builder) {
        this.builder = builder;
        if (!isAdded())
            show(((AppCompatActivity) activity).getSupportFragmentManager(), TAG);
        return getDialog();
    }


    public static class Builder implements Parcelable {

        private String textName;
        private String textNumber;

        private OnPositiveClicked onPositiveClicked;
        private OnNegativeClicked onNegativeClicked;
        private OnLogoutClicked onLogoutClicked;
        private OnEditNameClicked onEditNameClicked;
        private OnEditImageClicked onEditImageClicked;

        private boolean autoHide;
        private int timeToHide;

        private int imageRecourse;
        private Drawable imageDrawable;

        private Context context;

        private boolean isMine; // 내 프로필인지 아닌지


        protected Builder(Parcel in) {
            textName = in.readString();
            textNumber = in.readString();
            autoHide = in.readByte() != 0;
            isMine = in.readByte() != 0;
            timeToHide = in.readInt();
            imageRecourse = in.readInt();
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

        public int getTimeToHide() {
            return timeToHide;
        }

        public Builder setTimeToHide(int timeToHide) {
            this.timeToHide = timeToHide;
            return this;
        }

        public boolean isAutoHide() {
            return autoHide;
        }

        public boolean isMine(){
            return isMine;
        }

        public Builder setAutoHide(boolean autoHide) {
            this.autoHide = autoHide;
            return this;
        }

        public Builder setMine(boolean isMine){
            this.isMine = isMine;
            return this;
        }

        public Context getContext() {
            return context;
        }

        public Builder setActivity(Context context) {
            this.context = context;
            return this;
        }

        public Builder(Context context) {
            this.context = context;
        }


        public int getImageRecourse() {
            return imageRecourse;
        }

        public Builder setImageRecourse(int imageRecourse) {
            this.imageRecourse = imageRecourse;
            return this;
        }

        public Drawable getImageDrawable() {
            return imageDrawable;
        }

        public Builder setImageDrawable(Drawable imageDrawable) {
            this.imageDrawable = imageDrawable;
            return this;
        }


        public String getTextName() {
            return textName;
        }

        public Builder setTextName(String textName) {
            this.textName = textName;
            return this;
        }

        public Builder setTextName(int textName) {
            this.textName = context.getString(textName);
            return this;
        }

        public String getTextNumber() {
            return textNumber;
        }

        public Builder setTextNumber(String textNumber) {
            this.textNumber = textNumber;
            return this;
        }

        public Builder setTextNumber(int textNumber) {
            this.textNumber = context.getString(textNumber);
            return this;
        }


        public OnPositiveClicked getOnPositiveClicked() {
            return onPositiveClicked;
        }

        public Builder setOnPositiveClicked(OnPositiveClicked onPositiveClicked) {
            this.onPositiveClicked = onPositiveClicked;
            return this;
        }

        public OnNegativeClicked getOnNegativeClicked() {
            return onNegativeClicked;
        }

        public Builder setOnNegativeClicked(OnNegativeClicked onNegativeClicked) {
            this.onNegativeClicked = onNegativeClicked;
            return this;
        }

        public OnLogoutClicked getOnLogoutClicked(){
            return onLogoutClicked;
        }

        public Builder setOnLogoutClicked(OnLogoutClicked onLogoutClicked){
            this.onLogoutClicked = onLogoutClicked;
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
            parcel.writeByte((byte) (autoHide ? 1 : 0));
            parcel.writeByte((byte) (isMine ? 1 : 0));
            parcel.writeInt(timeToHide);
            parcel.writeInt(imageRecourse);
        }
    }

    public interface OnPositiveClicked {
        void OnClick(View view, Dialog dialog);
    }

    public interface OnNegativeClicked {
        void OnClick(View view, Dialog dialog);
    }

    public interface OnLogoutClicked {
        void OnClick(View view, Dialog dialog);
    }

    public interface OnEditNameClicked {
        void OnClick(View view, Dialog dialog);
    }

    public interface OnEditImageClicked {
        void OnClick(View view, Dialog dialog);
    }

}
