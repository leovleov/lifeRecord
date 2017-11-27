package cmu.sv.lifeRecord.models;

public class Like {
    String id, userId, recordId, createDate;
    public Like(String userId, String recordId, String createDate){
        this.userId = userId;
        this.recordId = recordId;
        this.createDate = createDate;
    }
    public void setId(String id) {
        this.id = id;
    }
}
