package com.mymoneymate.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BaseDAO<T> {
    T create(T entity) throws SQLException;
    Optional<T> findById(Integer id) throws SQLException;
    List<T> findAll() throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(Integer id) throws SQLException;
}
