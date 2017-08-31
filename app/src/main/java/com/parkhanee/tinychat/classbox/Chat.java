package com.parkhanee.tinychat.classbox;

import com.parkhanee.tinychat.MyPreferences;
import com.parkhanee.tinychat.MyUtil;

/**
 * Created by parkhanee on 2017. 8. 26..
 */

public class Chat {

    // TODO: 2017. 8. 27. friend 객체를 여기서 가지고 있어야, ChatAdapter 에서 친구 정보를 리사이클러뷰의 각 아이템에 넣어줄 수 있음
    // 아니면 ChatAdapter에서 가지고 있어서 거기서 처리 ? . .

    private String mid, rid, from, body, unixTime;
    private String  custom, time, date;    // sqLite 에 저장 안함

    // 시간 표시하는 타입
    public static final int TYPE_CUSTOM=11; // 현재 시간과 비교해서 보기좋게 표기
    public static final int TYPE_TIME=12; // HH:mm 시,분만 표기
    public static final int TYPE_DATE=13; // yy/MM/dd 날짜 표기

    // Chat RecyclerView에 표시하기 위한 날짜 객체
    private boolean dateObject=false;

    public Chat(String mid, String rid, String from, String body, String unixTime){
        this.mid = mid;
        this.rid = rid;
        this.from = from;
        this.body =body;
        this.unixTime = unixTime;
    }

    public Chat(String unixTime){
        dateObject=true;
        this.unixTime = unixTime;

    }

    public String getMid() {
        return mid;
    }

    public String getRid() {
        return rid;
    }

    public String getFrom() {
        return from;
    }

    public String getBody() {
        return body;
    }

    public String getUnitTime() {
        return unixTime;
    }

    /**
     * @param type [TYPE_TIME] : 채팅방에 메세지 옆에 표시되는, 메세지 도착한 시간. e.g. 13:50
     *              [TYPE_DATE] : 년월일 표기. e.g. 2017년 08월 19일
     *              [TYPE_CUSTOM] :  현재 시간과 비교해서 보기좋게 표기.
     * */
    public String getDate(int type){
        switch (type){
            case TYPE_TIME:
                if (time==null){
                    time = MyUtil.UnixTimeToTime(unixTime);
                }
                return time;
            case TYPE_DATE:
                if (date==null){
                    date = MyUtil.UnixTimeToDate(unixTime);
                }
                return date;
            case TYPE_CUSTOM:
                if (custom==null){
                    custom = MyUtil.UnixTimeToCustomDate(unixTime);
                }
                return custom;
            default:
                if (custom==null){
                    custom = MyUtil.UnixTimeToCustomDate(unixTime);
                }
                return custom;
        }
    }

    public boolean isDateObject() {
        return dateObject;
    }

    @Override
    public String toString() {
        return "Chat {" +
            "mid='" + mid + '\'' +
            ", rid='" + rid + '\'' +
            ", from='" + from + '\'' +
            ", body='" + body + '\'' +
            ", unixTime ='" + unixTime + '\'' +
            ", date ='" + date + '\'' +
            '}';
    }
}
