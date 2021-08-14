package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private long userID;
    private String userName;
    private String firstName;
    private String lastName;
    private int passwordHash;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;
    private String bio;
    private LocalDateTime lastSeen;
    private LastSeenStatus lastSeenStatus;
    private boolean accountPrivate;
    private boolean accountActive;
    private byte[] userImage;

    public User() {
    }

    public User(long userID, String userName, String firstName,
                String lastName, int passwordHash, LocalDate dateOfBirth,
                String email, String phoneNumber, String bio,
                LocalDateTime lastSeen, LastSeenStatus lastSeenStatus,
                boolean accountPrivate, boolean accountActive, byte[] userImage) {
        this.userID = userID;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.bio = bio;
        this.lastSeen = lastSeen;
        this.lastSeenStatus = lastSeenStatus;
        this.accountPrivate = accountPrivate;
        this.accountActive = accountActive;
        this.userImage = userImage;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(int passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public LastSeenStatus getLastSeenStatus() {
        return lastSeenStatus;
    }

    public void setLastSeenStatus(LastSeenStatus lastSeenStatus) {
        this.lastSeenStatus = lastSeenStatus;
    }

    public boolean isAccountPrivate() {
        return accountPrivate;
    }

    public void setAccountPrivate(boolean accountPrivate) {
        this.accountPrivate = accountPrivate;
    }

    public boolean isAccountActive() {
        return accountActive;
    }

    public void setAccountActive(boolean accountActive) {
        this.accountActive = accountActive;
    }

    public byte[] getUserImage() {
        return userImage;
    }

    public void setUserImage(byte[] userImage) {
        this.userImage = userImage;
    }
}

