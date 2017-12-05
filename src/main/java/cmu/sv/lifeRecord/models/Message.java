package cmu.sv.lifeRecord.models;

public class Message {
    String id, messageInfo, recordId, userId, createDate, userName;
    public Message(String userId, String messageInfo, String recordId, String createDate, String userName){
        this.messageInfo = messageInfo;
        this.recordId = recordId;
        this.userId = userId;
        this.createDate = createDate;
        this.userName = userName;
    }
    public void setId(String id) {
        this.id = id;
    }
}
