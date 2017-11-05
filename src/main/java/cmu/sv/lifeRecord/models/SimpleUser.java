package cmu.sv.lifeRecord.models;

public class SimpleUser {
    String id;
    String firstName;
    String lastName;
    String nickName;
    public SimpleUser(String firstName, String lastName, String nickName){
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickName = nickName;
    }
    public void setId(String id) {
        this.id = id;
    }
}
