package controller.builder;

import main.Main;
import model.LastSeenStatus;
import model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserBuilder {
    private String userName;
    private String firstName;
    private String lastName;
    private int passwordHash;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;
    private String bio;

    public UserBuilder() {
        this.userName = null;
        this.firstName = null;
        this.lastName = null;
        this.passwordHash = 0;
        this.dateOfBirth = null;
        this.email = null;
        this.phoneNumber = null;
        this.bio = null;
    }

    private long findLastUserID(){
        String query = "select max(\"userID\") from \"User\"";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            long lastUserID = rs.getLong(1);
            rs.close();
            return lastUserID;
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return 0;
    }

    private long generateNewUserID(){
        long lastUserID = findLastUserID();
        return (lastUserID+1);
    }

    public UserBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public UserBuilder setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserBuilder setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserBuilder setPassword(String password) {
        this.passwordHash = password.hashCode();
        return this;
    }

    public UserBuilder setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public UserBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public UserBuilder setBio(String bio) {
        this.bio = bio;
        return this;
    }

    public User build() {
        User user = new User(generateNewUserID(), this.userName, this.firstName,
                this.lastName, this.passwordHash, this.dateOfBirth,
                this.email, this.phoneNumber, this.bio,
                LocalDateTime.now(), LastSeenStatus.EVERYONE,
                false, true, null);

        this.userName = null;
        this.firstName = null;
        this.lastName = null;
        this.passwordHash = 0;
        this.dateOfBirth = null;
        this.email = null;
        this.phoneNumber = null;
        this.bio = null;

        return user;
    }
}
