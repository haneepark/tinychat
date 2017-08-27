package com.parkhanee.tinychat.classbox;

import com.parkhanee.tinychat.MyPreferences;
import com.parkhanee.tinychat.MyUtil;

/**
 * Created by parkhanee on 2017. 8. 26..
 */

public class Chat {

    // TODO: 2017. 8. 27. friend 객체를 여기서 가지고 있어야, ChatAdapter 에서 친구 정보를 리사이클러뷰의 각 아이템에 넣어줄 수 있음
    // 아니면 ChatAdapter에서 가지고 있어서 거기서 처리 ? . .

    private String mid, rid, from, body, unitTime,from_name,
            date;  // date와 from_name은 sqLite 에는 저장하지 않지만 Chat 객체를 만들 때 생성해서 각 객체에 가지고 있는다.

    /*public Chat(String mid, String rid, String from, String body, String unitTime,String from_name){
        this.mid = mid;
        this.rid = rid;
        this.from = from;
        this.body =body;
        this.unitTime = unitTime;
        setDate(unitTime);
        from_name = from_name;
    }*/

    public Chat(String mid, String rid, String from, String body, String unitTime){
        this.mid = mid;
        this.rid = rid;
        this.from = from;
        this.body =body;
        this.unitTime = unitTime;
        setDate(unitTime);
    }

    /*public Chat(String rid, String from, String body, String unitTime,String from_name){
        this.rid = rid;
        this.from = from;
        this.body =body;
        this.unitTime = unitTime;
        setDate(unitTime);
        from_name = from_name;
    }*/

    private void setDate(String unitTime){
        date = MyUtil.UnixTimeToDate(unitTime);
    }

    public String getDate() {
        return date;
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
        return unitTime;
    }

    public String getFrom_name() {
        return from_name;
    }

    @Override
    public String toString() {
        return "Chat {" +
            "mid='" + mid + '\'' +
            ", rid='" + rid + '\'' +
            ", from='" + from + '\'' +
            ", body='" + body + '\'' +
            ", unixTime ='" + unitTime + '\'' +
            ", date ='" + date + '\'' +
            '}';
    }
}
