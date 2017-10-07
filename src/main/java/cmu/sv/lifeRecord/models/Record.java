package cmu.sv.lifeRecord.models;

import java.util.Date;

public class Record {
    String id, recordName, recordInfo, picture, albumId, targetId, viewId, likeId, editorId;
    Date date;
    public Record(String recordName, String recordInfo, String picture, String albumId, String targetId,
                  String viewId, String likeId, String editorId, Date date){
        this.recordName = recordName;
        this.recordInfo = recordInfo;
        this.picture = picture;
        this.albumId = albumId;
        this.targetId = targetId;
        this.viewId = viewId;
        this.likeId = likeId;
        this.editorId = editorId;
        this.date = date;
    }
    public void setId(String id) {
        this.id = id;
    }
}
