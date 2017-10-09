package cmu.sv.lifeRecord.models;

public class Picture {
    String id, url, recordId;
    public Picture(String url, String recordId){
        this.url = url;
        this.recordId = recordId;
    }
    public void setId(String id) {
        this.id = id;
    }
}
