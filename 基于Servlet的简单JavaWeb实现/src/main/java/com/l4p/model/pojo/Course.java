package com.l4p.model.pojo;

public class Course {
    private int id;
    private String courseName;
    private String description;
    private int credits;

    // 无参构造方法
    public Course() {
    }

    // 全参构造方法
    public Course(int id, String courseName, String description, int credits) {
        this.id = id;
        this.courseName = courseName;
        this.description = description;
        this.credits = credits;
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", courseName='" + courseName + '\'' +
                ", description='" + description + '\'' +
                ", credits=" + credits +
                '}';
    }
}
