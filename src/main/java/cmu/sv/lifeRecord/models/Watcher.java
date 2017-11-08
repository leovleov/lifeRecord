package cmu.sv.lifeRecord.models;

public class Watcher {
    String id;
    String userId;
    String targetId;
    public Watcher(String userId, String targetsId){
        this.userId = userId;
        this.targetId = targetsId;
    }
    public void setId(String id) {
        this.id = id;
    }
}
