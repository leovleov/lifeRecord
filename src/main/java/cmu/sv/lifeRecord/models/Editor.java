package cmu.sv.lifeRecord.models;

public class Editor {
    String id;
    String userId;
    String targetId;
    boolean isCreator;
    public Editor(String userId, String targetsId, boolean isCreator){
        this.userId = userId;
        this.targetId = targetsId;
        this.isCreator = isCreator;
    }
    public void setId(String id) {
        this.id = id;
    }
}
