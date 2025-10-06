package ru.astant.service1.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username")
    @Size(min = 2, max = 150, message = "Имя пользователя должно содержать от 2 до 50 символов")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "yandex_id")
    private String yandexId;

    @Column(name = "registration_type")
    private String registrationType;

    public User() {
    }


    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setYandexId(String yandexId) {
        this.yandexId = yandexId;
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }
}
