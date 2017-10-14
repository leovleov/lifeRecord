package cmu.sv.lifeRecord.models;

public class User {
    String id;
    String firstName;
    String lastName;
    String nickName;
    String phoneNumber;
    String emailAddress;
    public User(String firstName, String lastName, String nickName, String phoneNumber, String emailAddress){
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickName = nickName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }
    public void setId(String id) {
        this.id = id;
    }
}
