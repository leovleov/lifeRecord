package cmu.sv.lifeRecord.models;

import java.util.Date;

public class Picture {
    String id, url, recordId;
    Date date;
    public Picture(String url, String recordId, Date date){
        this.url = url;
        this.recordId = recordId;
        this.date = date;
    }
    public void setId(String id) {
        this.id = id;
    }
}
