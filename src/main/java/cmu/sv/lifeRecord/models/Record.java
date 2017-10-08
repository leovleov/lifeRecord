package cmu.sv.lifeRecord.models;

import java.util.Date;

public class Record {
    String id, recordName, recordInfo, albumId, targetId, viewId, likeId, editorId;
    public Record(String recordName, String recordInfo, String picture, String albumId, String targetId,
                  String viewId, String likeId, String editorId){
        this.recordName = recordName;
        this.recordInfo = recordInfo;
        this.albumId = albumId;
        this.targetId = targetId;
        this.viewId = viewId;
        this.likeId = likeId;
        this.editorId = editorId;
    }
    public void setId(String id) {
        this.id = id;
    }
}
