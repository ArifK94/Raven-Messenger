package com.example.arifk.ravenmessenger;

public class User {

    private String name, avatar, username;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String background_image) {
        this.username = background_image;
    }

    public User() {

    }

    public User(String name, String avatar, String username) {

        this.name = name;
        this.avatar = avatar;
        this.username = username;
    }


}
