package com.parkhanee.tinychat.classbox;

/**
 * Created by parkhanee on 2017. 8. 2..
 */

public class Friend {
    private String id, nid, name, img_url, rid;
    private byte[] img_blob;
    private int  created;

    public Friend (String id, String nid, String name,String rid, String img_url,byte[] img_blob,  int created ){
        this.id = id;
        this.nid = nid;
        this.name = name;
        this.rid = rid;
        this.img_url = img_url; // 서버 이미지 url
        this.img_blob = img_blob; // 서버 이미지 url
        this.created = created; // 친구 된 시간 unixtime
    }

    // 친구 추가하기 전에 썸네일 이미지 가지고 있을 필요 없을 때 쓰는 생성자 !
    public Friend (String id, String nid, String name,String img_url,  int created ){
        this.id = id;
        this.nid = nid;
        this.name = name;
        this.img_url = img_url; // 서버 이미지 url
        this.created = created; // 친구 된 시간 unixtime
    }

    // 친구 목록 새로고침해서 받아왔을 때 썸네일 blob받아오기 전 상태
    public Friend (String id, String nid, String name,String rid,String img_url,  int created ){
        this.id = id;
        this.nid = nid;
        this.name = name;
        this.rid = rid;
        this.img_url = img_url; // 서버 이미지 url
        this.created = created; // 친구 된 시간 unixtime
    }

/*    *//**
     *
     * 근데 이 클래스의 인스턴스는 애초에 일회용인데.
     * 디비도 아니고 이걸 업데이트할 일이 있을까?
     *
     * update the friend instance information.
     * caution : id cannot be updated !
     * if id from paramater is differ from id from the instance, it fails update and return false.
     * *//*
    public boolean updateFriend (String id, String nid, String name, String img_url, byte[] img_blob, int created ){
        if (!this.id.equals(id)){
            return false;
        }
        this.id = id;
        this.nid = nid;
        this.name = name;
        this.img_url = img_url;
        this.img_blob = img_blob;
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
        this.img_url = friend.getImgUrl();
        this.img_blob = friend.getImgBlob();
        this.created = friend.getCreated();
        return true;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateImg(String img_url, byte[] img_blob) {
        this.img_url = img_url;
        this.img_blob = img_blob;
    }*/

    public void setImgBlob(byte[] img_blob) {
        this.img_blob = img_blob;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public boolean isBlobSet(){
        // true when it exists
        // false when it is not
        return img_blob!=null ;
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

    public String getRid() {
        return rid;
    }

    public String getImgUrl() {
        return img_url;
    }

    public int getCreated() {
        return created;
    }

    public byte[] getImgBlob() {
        return img_blob;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "id='" + id + '\'' +
                ", nid='" + nid + '\'' +
                ", name='" + name + '\'' +
                ", rid='" + rid + '\'' +
                ", img_url='" + img_url + '\'' +
                ", img_blob=" + (img_blob!=null)  +
                ", created=" + created +
                '}';
    }
}
