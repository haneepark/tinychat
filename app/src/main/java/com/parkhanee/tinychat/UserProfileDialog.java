package com.parkhanee.tinychat;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

    private CardView cardView;
    private AppCompatImageView image;
    private TextView name;
    private Button positive, negative, logout, number;
    private ImageButton addImage, editName;
    private LinearLayout buttonsPanel;

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

    }

    private void initViews(View view) {
        cardView = (CardView) view.findViewById(R.id.card_view);
        image = (AppCompatImageView) view.findViewById(R.id.image);
        cardView = (CardView) view.findViewById(R.id.card_view);
        title = (TextView) view.findViewById(R.id.title);
        subTitle = (TextView) view.findViewById(R.id.sub_title);
        body = (TextView) view.findViewById(R.id.body);
        positive = (Button) view.findViewById(R.id.position);
        negative = (Button) view.findViewById(R.id.negative);
        buttonsPanel = (LinearLayout) view.findViewById(R.id.buttons_panel);
    }

    private Dialog show(Activity activity, Builder builder) {
        this.builder = builder;
        if (!isAdded())
            show(((AppCompatActivity) activity).getSupportFragmentManager(), TAG);
        return getDialog();
    }


    public static class Builder implements Parcelable {

        private String positiveButtonText;
        private String negativeButtonText;

        private String textTitle;
        private String textSubTitle;
        private String body;

        private OnPositiveClicked onPositiveClicked;
        private OnNegativeClicked onNegativeClicked;


        private boolean autoHide;

        private int timeToHide;

        private int positiveTextColor;
        private int backgroundColor;
        private int negativeColor;
        private int titleColor;
        private int subtitleColor;
        private int bodyColor;

        private int imageRecourse;
        private Drawable imageDrawable;

        private Typeface titleFont;
        private Typeface subTitleFont;
        private Typeface bodyFont;
        private Typeface positiveButtonFont;
        private Typeface negativeButtonFont;

        private Typeface alertFont;

        private Context context;

        private PanelGravity buttonsGravity;

        protected Builder(Parcel in) {
            positiveButtonText = in.readString();
            negativeButtonText = in.readString();
            textTitle = in.readString();
            textSubTitle = in.readString();
            body = in.readString();
            autoHide = in.readByte() != 0;
            timeToHide = in.readInt();
            positiveTextColor = in.readInt();
            backgroundColor = in.readInt();
            negativeColor = in.readInt();
            titleColor = in.readInt();
            subtitleColor = in.readInt();
            bodyColor = in.readInt();
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



        public Typeface getAlertFont() {
            return alertFont;
        }


        public Typeface getTitleFont() {
            return titleFont;
        }


        public Builder setTitleFont(String titleFontPath) {
            this.titleFont = Typeface.createFromAsset(context.getAssets(), titleFontPath);
            return this;
        }

        public Typeface getSubTitleFont() {
            return subTitleFont;
        }

        public Builder setSubTitleFont(String subTitleFontPath) {
            this.subTitleFont = Typeface.createFromAsset(context.getAssets(), subTitleFontPath);
            return this;
        }



        public Builder setBodyFont(String bodyFontPath) {
            this.bodyFont = Typeface.createFromAsset(context.getAssets(), bodyFontPath);
            return this;
        }


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

        public Builder setAutoHide(boolean autoHide) {
            this.autoHide = autoHide;
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

        public int getPositiveTextColor() {
            return positiveTextColor;
        }

        public Builder setPositiveColor(int positiveTextColor) {
            this.positiveTextColor = positiveTextColor;
            return this;
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

        public String getPositiveButtonText() {
            return positiveButtonText;
        }


        public Builder setPositiveButtonText(int positiveButtonText) {
            this.positiveButtonText = context.getString(positiveButtonText);
            return this;
        }

        public Builder setPositiveButtonText(String positiveButtonText) {
            this.positiveButtonText = positiveButtonText;
            return this;
        }

        public String getNegativeButtonText() {
            return negativeButtonText;
        }

        public Builder setNegativeButtonText(String negativeButtonText) {
            this.negativeButtonText = negativeButtonText;
            return this;
        }

        public Builder setNegativeButtonText(int negativeButtonText) {
            this.negativeButtonText = context.getString(negativeButtonText);
            return this;
        }

        public String getTextTitle() {
            return textTitle;
        }

        public Builder setTextTitle(String textTitle) {
            this.textTitle = textTitle;
            return this;
        }

        public Builder setTextTitle(int textTitle) {
            this.textTitle = context.getString(textTitle);
            return this;
        }

        public String getTextSubTitle() {
            return textSubTitle;
        }

        public Builder setTextSubTitle(String textSubTitle) {
            this.textSubTitle = textSubTitle;
            return this;
        }

        public Builder setTextSubTitle(int textSubTitle) {
            this.textSubTitle = context.getString(textSubTitle);
            return this;
        }

        public String getBody() {
            return body;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder setBody(int body) {
            this.body = context.getString(body);
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

        public Builder build() {
            return this;
        }

        public Dialog show() {
            return getInstance().show(((Activity) context), this);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(positiveButtonText);
            parcel.writeString(negativeButtonText);
            parcel.writeString(textTitle);
            parcel.writeString(textSubTitle);
            parcel.writeString(body);
            parcel.writeByte((byte) (autoHide ? 1 : 0));
            parcel.writeInt(timeToHide);
            parcel.writeInt(positiveTextColor);
            parcel.writeInt(backgroundColor);
            parcel.writeInt(negativeColor);
            parcel.writeInt(titleColor);
            parcel.writeInt(subtitleColor);
            parcel.writeInt(bodyColor);
            parcel.writeInt(imageRecourse);
        }
    }

    public interface OnPositiveClicked {
        void OnClick(View view, Dialog dialog);
    }

    public interface OnNegativeClicked {
        void OnClick(View view, Dialog dialog);
    }

    public enum PanelGravity {
        LEFT,
        RIGHT,
        CENTER
    }

}
