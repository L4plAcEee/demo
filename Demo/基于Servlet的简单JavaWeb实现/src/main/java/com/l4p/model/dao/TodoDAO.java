package com.l4p.model.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.l4p.model.pojo.TodoItem; // 引入英文POJO类

public class TodoDAO extends DBO {

    @Override
    public int add(Object obj) throws Exception {
        TodoItem todo = (TodoItem) obj;
        open();
        String sql = "INSERT INTO todo_items "
                + "(todo_item_desc, todo_item_priority, todo_item_completed, "
                + " todo_item_created_at, todo_item_completed_at, todo_item_creator) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        // 1. 指定返回生成键
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, todo.getDescription());
        ps.setInt(2, todo.getPriority());
        ps.setBoolean(3, todo.getIsCompleted());
        ps.setTimestamp(4, todo.getCreatedAt() != null ? new Timestamp(todo.getCreatedAt().getTime()) : null);
        ps.setTimestamp(5, todo.getCompletedAt() != null ? new Timestamp(todo.getCompletedAt().getTime()) : null);
        ps.setString(6, todo.getCreator());

        int result = ps.executeUpdate();

        // 2. 回填新 ID
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                todo.setId(keys.getLong(1));
            }
        }

        close();
        return result;
    }


    @Override
    public int update(Object obj) throws Exception {
        TodoItem todo = (TodoItem) obj;
        open();
        String sql = "UPDATE todo_items SET todo_item_desc = ?, todo_item_priority = ?, todo_item_completed = ?, todo_item_created_at = ?, todo_item_completed_at = ?, todo_item_creator = ? " +
                "WHERE todo_item_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, todo.getDescription());
        ps.setInt(2, todo.getPriority());
        ps.setBoolean(3, todo.getIsCompleted());
        ps.setTimestamp(4, todo.getCreatedAt() != null ? new Timestamp(todo.getCreatedAt().getTime()) : null);
        ps.setTimestamp(5, todo.getCompletedAt() != null ? new Timestamp(todo.getCompletedAt().getTime()) : null);
        ps.setString(6, todo.getCreator());
        ps.setLong(7, todo.getId());
        int result = ps.executeUpdate();
        close();
        return result;
    }

    @Override
    public int delete(int id) throws Exception {
        open();
        String sql = "DELETE FROM todo_items WHERE todo_item_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        int result = ps.executeUpdate();
        close();
        return result;
    }

    @Override
    public TodoItem find(int id) throws Exception {
        open();
        String sql = "SELECT * FROM todo_items WHERE todo_item_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        TodoItem todo = null;
        if (rs.next()) {
            todo = mapResultSetToTodoItem(rs);
        }
        close();
        return todo;
    }

    @Override
    public List<TodoItem> findAll() throws Exception {
        open();
        String sql = "SELECT * FROM todo_items ORDER BY todo_item_id DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        List<TodoItem> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSetToTodoItem(rs));
        }
        close();
        return list;
    }

    private TodoItem mapResultSetToTodoItem(ResultSet rs) throws Exception {
        TodoItem todo = new TodoItem();
        todo.setId(rs.getLong("todo_item_id"));
        todo.setDescription(rs.getString("todo_item_desc"));
        todo.setPriority(rs.getInt("todo_item_priority"));
        todo.setIsCompleted(rs.getBoolean("todo_item_completed"));
        todo.setCreatedAt(rs.getTimestamp("todo_item_created_at"));
        todo.setCompletedAt(rs.getTimestamp("todo_item_completed_at"));
        todo.setCreator(rs.getString("todo_item_creator"));
        return todo;
    }
}
