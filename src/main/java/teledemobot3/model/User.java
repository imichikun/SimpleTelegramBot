package teledemobot3.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "tele_demobot1")
public class User {

    @Id
    @Column(name = "chatid")
    private Long chatId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "username")
    private String username;

    @Column(name = "registered_time")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime registeredTime;

    public User(Long chatId, String firstName, String lastName, String username, LocalDateTime registeredTime) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.registeredTime = registeredTime;
    }

    public User() {
    }

    public Long getChatId() {
        return chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getRegisteredTime() {
        return registeredTime;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRegisteredTime(LocalDateTime registeredTime) {
        this.registeredTime = registeredTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", registeredTime=" + registeredTime +
                '}';
    }
}