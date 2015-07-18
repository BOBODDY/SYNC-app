package com.sync.syncapp.model;

/**
 * Created by nick on 7/17/15.
 */
public class Room {
    
    /*
    {
  "id": "string",
  "RecordType": "string",
  "account_id": "string",
  "person_id": "string",
  "building_id": "string",
  "location_id": "string",
  "RoomType": "string",
  "RoomDescription": "string"
}
     */
    String id;
    Account account;
    Person personId;
    String buildingId;
    String roomType;
    String roomDescription;
    
    
    
    public Room() {
        this(null, null, "", "", "");
    }

    public Room(Account account, Person personId, String buildingId, String roomType, String roomDescription) {
        this.account = account;
        this.personId = personId;
        this.buildingId = buildingId;
        this.roomType = roomType;
        this.roomDescription = roomDescription;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getId() {
        if(id == null) {
            return "";
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Person getPersonId() {
        return personId;
    }

    public void setPersonId(Person personId) {
        this.personId = personId;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    @Override
    public String toString() {
        return roomType;
    }
}
