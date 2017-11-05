package cmu.sv.lifeRecord.models;

import java.util.ArrayList;

public class WatcherList {
    String id;
    ArrayList<String> usersId;
    String targetId;
    public WatcherList(ArrayList<String> usersId, String targetsId){
        this.usersId = usersId;
        this.targetId = targetsId;
    }
    public void setId(String id) {
        this.id = id;
    }
}
