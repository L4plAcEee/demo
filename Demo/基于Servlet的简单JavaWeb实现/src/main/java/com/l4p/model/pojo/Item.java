package com.l4p.model.pojo;

import java.util.Objects;

public class Item {
    private long id;
    private String name;

    // Jackson 需要一個無參構造函數
    public Item() {
    }

    public Item(long id, String name) {
        this.id = id;
        this.name = name;
    }

    // --- Getters and Setters ---
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --- equals() and hashCode() for potential use in collections ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id == item.id && Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}