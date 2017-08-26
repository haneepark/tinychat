package com.parkhanee.tinychat.classbox;

import com.parkhanee.tinychat.MyUtil;

/**
 * Created by parkhanee on 2017. 8. 26..
 */

public class Chat {
    private String mid, rid, id, body, unitTime,
            date;  // date는 sqLite 에는 저장하지 않지만 Chat 객체를 만들 때 생성해서 각 객체에 가지고 있는다.

    public Chat(String mid, String rid, String id, String body, String unitTime){
        this.mid = mid;
        this.rid = rid;
        this.id = id;
        this.body =body;
        this.unitTime = unitTime;
        setDate(unitTime);
    }

    /*public Chat(String rid, String id, String body, String unitTime){
        this.rid = rid;
        this.id = id;
        this.body =body;
        this.unitTime = unitTime;
        setDate(unitTime);
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

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public String getUnitTime() {
        return unitTime;
    }

    @Override
    public String toString() {
        return "Chat {" +
            "mid='" + mid + '\'' +
            ", rid='" + rid + '\'' +
            ", id='" + id + '\'' +
            ", body='" + body + '\'' +
            ", unixTime ='" + unitTime + '\'' +
            ", date ='" + date + '\'' +
            '}';
    }
}
