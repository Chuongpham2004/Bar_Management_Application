package com.barmanagement.model;

public class Table {
    private int id;
    private String name;
    private String status; // "AVAILABLE", "OCCUPIED", "RESERVED"...

    public Table(int id, String name) {
        this.id = id;
        this.name = name;
        this.status = "AVAILABLE";
    }

    public Table(int id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name + " (" + status + ")";
    }
}
