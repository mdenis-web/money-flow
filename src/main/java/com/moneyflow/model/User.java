package com.moneyflow.model;

public class User {
    private String login;
    private String passwordHash;
    private Wallet wallet;

    public User() {
    }

    public User(String login, String passwordHash, Wallet wallet) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.wallet = wallet;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}
