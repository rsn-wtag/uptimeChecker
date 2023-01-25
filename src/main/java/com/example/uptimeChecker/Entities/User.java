package com.example.uptimeChecker.Entities;

import lombok.*;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Integer userId;

    @Column(name = "username")
    private String userName;

    @Column(name="password")
    private String password;
    @Column(name="enabled")
    private Boolean enabled;

    @Column(name="email")
    private String email;
    @Column(name="slack_id")
    private String slackId;

  /*  @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "website_user_mapping",
            joinColumns=@JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "wb_id")
    )*/
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<WebsiteUserMetaData> mappedWebsiteDetails;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(	name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(String userName, String password, Boolean enabled, String email, String slackId) {
        this.userName = userName;
        this.password = password;
        this.enabled = enabled;
        this.email = email;
        this.slackId = slackId;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + enabled +
                ", email='" + email + '\'' +
                ", slackId='" + slackId + '\'' +
                '}';
    }
}
