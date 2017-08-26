package com.parkhanee.tinychat;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by parkhanee on 2017. 8. 27..
 * 키보드 보일 때, 리사이클러뷰 스크롤 가장 아래로 내려간 채로 키보드랑 같이 올라오기.
 *      —> 커스텀 recyclerView 클래스 에서 keyboard show/hidden 이벤트 리스너 만들어서 처리.
 */

public class MyRecyclerView extends RecyclerView {
    private static final String TAG = "MyRecyclerView";
    private static boolean shown=false ;
    private OnKeyboardFocusChangeListener listener;

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRecyclerView(Context context) {
        super(context);
    }

    public void setOnKeyboardFocusChangeListener(OnKeyboardFocusChangeListener listener){
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthSpec, int  heightSpec) {

        final int proposedheight = MeasureSpec.getSize(heightSpec);
        final int actualHeight = getHeight();
        // Keyboard is shown when actualHeight > proposedheight
        // Keyboard is hidden when actualHeight <= proposedheight


        if (actualHeight > proposedheight && !shown){ //키보드 이제 보이는데 안보인다고 설정되어 있었던 경우
            Log.d(TAG, "onMeasure: shown");
            shown=true;
            listener.onKeyboardFocusChangeCallback(shown);
        } else if (actualHeight <= proposedheight  && shown){ // 키보드 이제 안보이는데 보인다고 설정되어 있었던 경우
            // Keyboard is hidden
            Log.d(TAG, "onMeasure: hidden");
            shown=false;
            listener.onKeyboardFocusChangeCallback(shown);
        }

        super.onMeasure(widthSpec, heightSpec);
    }


    public interface OnKeyboardFocusChangeListener {
        void onKeyboardFocusChangeCallback(boolean shown);
    }


}
