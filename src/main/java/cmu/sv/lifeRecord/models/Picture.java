package cmu.sv.lifeRecord.models;

public class Picture {
    String id, url, recordId, targetId;
    public Picture(String url, String recordId, String targetId){
        this.url = url;
        this.recordId = recordId;
        this.targetId = targetId;
    }
    public void setId(String id) {
        this.id = id;
    }
}
