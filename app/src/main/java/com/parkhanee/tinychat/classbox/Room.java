package com.parkhanee.tinychat.classbox;

/**
 * Created by parkhanee on 2017. 8. 2..
 */

public class Room {
    private String rid;
    private int ppl;
    private Boolean isPrivateRoom=null;

    // TODO: 2017. 8. 2. 최근 대화 sharedPreferences는 어디서 뽑지?

    public Room(String rid, int ppl){
        this.rid = rid;
        this.ppl = ppl;
        this.isPrivateRoom = isPrivateRoom();
    }

    public Boolean isPrivateRoom(){
        if (isPrivateRoom==null){
            isPrivateRoom = ppl == 1;
        }
        return isPrivateRoom;
    }

    public String getRid() {
        return rid;
    }

    public int getPpl() {
        return ppl;
    }

    @Override
    public String toString() {
        return "Room{" +
                "rid='" + rid + '\'' +
                ", ppl=" + ppl +
                ", isPrivateRoom=" + isPrivateRoom +
                '}';
    }
}
