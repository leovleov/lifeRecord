package cmu.sv.lifeRecord.models;


import org.bson.BsonTimestamp;

import java.util.Date;

public class Record {
    String id, recordName, recordInfo, albumId, targetId, userId, createDate, updateDate;
    public Record(String recordName, String recordInfo, String albumId, String targetId, String userId, String createDate, String updateDate){
        this.recordName = recordName;
        this.recordInfo = recordInfo;
        this.albumId = albumId;
        this.targetId = targetId;
        this.userId = userId;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }
    public void setId(String id) {
        this.id = id;
    }
}
