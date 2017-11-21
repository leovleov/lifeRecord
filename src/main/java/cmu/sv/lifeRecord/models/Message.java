package cmu.sv.lifeRecord.models;

public class Message {
    String id, messageInfo, recordId, userId, createDate;
    public Message(String userId, String messageInfo, String recordId, String createDate){
        this.messageInfo = messageInfo;
        this.recordId = recordId;
        this.userId = userId;
        this.createDate = createDate;
    }
    public void setId(String id) {
        this.id = id;
    }
}
