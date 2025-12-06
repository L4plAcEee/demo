package com.l4p.model.pojo;

import java.util.Date;

public class TodoItem {

    // Fields
    private Long id;              // todo_item_id
    private String description;   // todo_item_desc
    private Integer priority;     // todo_item_priority
    private Boolean isCompleted;  // todo_item_completed
    private Date createdAt;       // todo_item_created_at
    private Date completedAt;     // todo_item_completed_at
    private String creator;       // todo_item_creator

    // Default constructor
    public TodoItem() {
    }

    // Parameterized constructor
    public TodoItem(Long id, String description, Integer priority, Boolean isCompleted,
                    Date createdAt, Date completedAt, String creator) {
        this.id = id;
        this.description = description;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.creator = creator;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
