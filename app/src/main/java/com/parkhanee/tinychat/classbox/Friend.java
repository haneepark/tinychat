package com.parkhanee.tinychat.classbox;

/**
 * Created by parkhanee on 2017. 8. 2..
 */

public class Friend {
    private String id, nid, name, img;
    private int  created;

    // TODO: 2017. 8. 2. 일대일 RID ??

    public Friend (String id, String nid, String name, String img, int created ){
        this.id = id;
        this.nid = nid;
        this.name = name;
        this.img = img; // 서버 이미지 url
        this.created = created; // 친구 된 시간 unixtime
    }

    /**
     * update the friend instance information.
     * caution : id cannot be updated !
     * if id from paramater is differ from id from the instance, it fails update and return false.
     * */
    public boolean updateFriend (String id, String nid, String name, String img, int created ){
        if (!this.id.equals(id)){
            return false;
        }
        this.id = id;
        this.nid = nid;
        this.name = name;
        this.img = img;
        this.created = created;
        return true;
    }

    public boolean updateFriend (Friend friend){
        if (!this.id.equals(friend.getId())){
            return false;
        }
        this.id = friend.getId();
        this.nid = friend.getNid();
        this.name = friend.getName();
        this.img = friend.getImg();
        this.created = friend.getCreated();
        return true;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateImg(String img) {
        this.img = img;
    }

    public String getId() {
        return id;
    }

    public String getNid() {
        return nid;
    }

    public String getName() {
        return name;
    }

    public String getImg() {
        return img;
    }

    public int getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "id='" + id + '\'' +
                ", nid='" + nid + '\'' +
                ", name='" + name + '\'' +
                ", img='" + img + '\'' +
                ", created=" + created +
                '}';
    }
}
