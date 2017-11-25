package cmu.sv.lifeRecord.models;

public class Album {
    String id;
    String targetId;
    String albumName;
    String albumDate;;
    public Album(String targetsId, String albumName, String albumDate){
        this.targetId = targetsId;
        this.albumName = albumName;
        this.albumDate = albumDate;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTargetId() { return this.targetId; }
}
