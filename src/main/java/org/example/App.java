package org.example;

import org.pool.datasource.CustomDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) throws SQLException {
        CustomDataSource customDataSource = new CustomDataSource("jdbc:postgresql://localhost:5432/postgres", "USER", "PASSWORD");

        try (Connection connection = customDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM employees");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                System.out.printf("Employee{id = [%d], fist_name = [%s], last_name = [%s]}\n",
                        id, firstName, lastName);
            }
        }
        customDataSource.close();
    }
}