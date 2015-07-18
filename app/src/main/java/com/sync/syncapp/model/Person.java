package com.sync.syncapp.model;

/**
 * Created by nick on 7/17/15.
 */
public class Person {
    
    /*
    Associated JSON for POST:
    {
        "id": "string",
        "RecordType": "string",
        "user_id": "string",
        "account_id": "string",
        "nickname": "string"
    }
     */
    
    Account account;
    String name;
//    String userId; //TODO: determine if the user owns the account


    public Person(Account account, String name) {
        this.account = account;
        this.name = name;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
