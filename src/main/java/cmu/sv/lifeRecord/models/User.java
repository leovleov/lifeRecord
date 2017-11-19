package cmu.sv.lifeRecord.models;

public class User {
    String id;
    String firstName;
    String lastName;
    String nickName;
    String phoneNumber;
    String emailAddress;
    Boolean isAdmin;
    public User(String firstName, String lastName, String nickName, String phoneNumber, String emailAddress, Boolean isAdmin){
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickName = nickName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.isAdmin = isAdmin;

    }
    public void setId(String id) {
        this.id = id;
    }
}
