package cmu.sv.lifeRecord.models;

public class Target {
    String id, targetName, targetInfo, creatorId;
    public Target(String targetName, String targetInfo, String creatorId){
        this.targetName = targetName;
        this.targetInfo = targetInfo;
        this.creatorId = creatorId;
    }
    public void setId(String id) {
        this.id = id;
    }
}
