package com.example.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Employee;

public class EmployeeDAO {

	private static final String DB_URL = "jdbc:postgresql://localhost:5432/mt_employee_db";
	private static final String DB_USER = "postgres";
	private static final String DB_PASSWORD = "12345678";

	public List<Employee> getAllEmployees() {
		return getAllEmployees(1, 5, null);
	}

	public List<Employee> getAllEmployees(int pageIndex, int pageSize) {
		return getAllEmployees(pageIndex, pageSize, null);
	}

	public List<Employee> getAllEmployees(int pageIndex, int pageSize, String keyword) {
	    List<Employee> employees = new ArrayList<>();
	    int offset = (pageIndex - 1) * pageSize;
	    
	    StringBuilder sqlBuilder = new StringBuilder();
	    sqlBuilder.append("SELECT employee_code, employee_name, employee_age, date_of_birth FROM Mt_employee");
	    sqlBuilder.append(" WHERE 1=1");
	    
	    if (keyword != null && !keyword.trim().isEmpty()) {
	        sqlBuilder.append(" AND LOWER(employee_name) LIKE ?");
	    }
	    
	    sqlBuilder.append(" ORDER BY employee_code DESC LIMIT ? OFFSET ?");
	    String sql = sqlBuilder.toString();
	    
	    try (Connection connection = getConnection(); 
	         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
	        
	        int paramIndex = 1;
	        if (keyword != null && !keyword.trim().isEmpty()) {
	            preparedStatement.setString(paramIndex++, "%" + keyword.toLowerCase().trim() + "%");
	        }
	        preparedStatement.setInt(paramIndex++, pageSize);
	        preparedStatement.setInt(paramIndex++, offset);
	        
	        try (ResultSet rs = preparedStatement.executeQuery()) {
	            while (rs.next()) {
	                Employee employee = new Employee();
	                employee.setEmployeeCode(rs.getString("employee_code"));
	                employee.setEmployeeName(rs.getString("employee_name"));
	                employee.setEmployeeAge(rs.getInt("employee_age"));
	                employee.setDateOfBirth(rs.getDate("date_of_birth"));
	                employees.add(employee);
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Error getting employee list: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return employees;
	}

	public int getTotalEmployeeCount() {
		return getTotalEmployeeCount(null);
	}

	public int getTotalEmployeeCount(String keyword) {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT COUNT(*) FROM Mt_employee");

		if (keyword != null && !keyword.trim().isEmpty()) {
			sqlBuilder.append(" WHERE employee_name LIKE ?");
		}

		String sql = sqlBuilder.toString();

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			if (keyword != null && !keyword.trim().isEmpty()) {
				stmt.setString(1, "%" + keyword.trim() + "%");
			}

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}

		} catch (SQLException e) {
			System.err.println("Error getting total employee count: " + e.getMessage());
			e.printStackTrace();
		}

		return 0;
	}

	public Employee getEmployeeByCode(String employeeCode) {
		String sql = "SELECT employee_code, employee_name, employee_age, date_of_birth FROM Mt_employee WHERE employee_code = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, employeeCode);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					Employee employee = new Employee();
					employee.setEmployeeCode(rs.getString("employee_code"));
					employee.setEmployeeName(rs.getString("employee_name"));
					employee.setEmployeeAge(rs.getInt("employee_age"));
					employee.setDateOfBirth(rs.getDate("date_of_birth"));
					return employee;
				}
			}

		} catch (SQLException e) {
			System.err.println("Error getting employee by code: " + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	public String getLastEmployeeCode() {
		String sql = "SELECT employee_code FROM Mt_employee ORDER BY employee_code DESC LIMIT 1";

		try (Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet rs = preparedStatement.executeQuery()) {

			if (rs.next()) {
				return rs.getString("employee_code");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	private String generateEmployeeCode() {
		String lastCode = getLastEmployeeCode();

		if (lastCode == null || lastCode.isEmpty()) {
			return "E001";
		}

		String numberPart = lastCode.substring(1);
		int nextNumber = Integer.parseInt(numberPart) + 1;

		return String.format("E%03d", nextNumber);
	}

	private Connection getConnection() throws SQLException {
		try {
			Class.forName("org.postgresql.Driver");
			return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		} catch (ClassNotFoundException e) {
			throw new SQLException("PostgreSQL Driver not found", e);
		}
	}

	public boolean testConnection() {
		try (Connection conn = getConnection()) {
			return conn != null && !conn.isClosed();
		} catch (SQLException e) {
			System.err.println("Database connection error: " + e.getMessage());
			return false;
		}
	}

	public boolean deleteEmployee(String employeeCode) {
		String sql = "DELETE FROM Mt_employee WHERE employee_code = ?";

		try (Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, employeeCode);
			int rowsAffected = preparedStatement.executeUpdate();
			return rowsAffected > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isEmployeeIdExist(String newEmployeeCode) {
		if (newEmployeeCode == null || newEmployeeCode.isEmpty()) {
			return true;
		}
		String sql = "SELECT * FROM Mt_employee WHERE employee_code = ? LIMIT 1";
		try (Connection connection = getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, newEmployeeCode);

			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next())
				return true;
		} catch (SQLException e) {
			System.err.println("Error adding employee: " + e.getMessage());
			e.printStackTrace();
			return true;
		}
		return false;
	}

	public boolean addEmployee(Employee newEmployee) {
		String newEmployeeCode = generateEmployeeCode();
		newEmployee.setEmployeeCode(newEmployeeCode);

		String sql = "INSERT INTO Mt_employee (employee_code, employee_name, employee_age, date_of_birth) VALUES (?,?,?,?)";
		try (Connection connection = getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, newEmployee.getEmployeeCode());
			preparedStatement.setString(2, newEmployee.getEmployeeName());
			preparedStatement.setInt(3, newEmployee.getEmployeeAge());
			if (newEmployee.getDateOfBirth() != null) {
				preparedStatement.setDate(4, new java.sql.Date(newEmployee.getDateOfBirth().getTime()));
			} else {
				preparedStatement.setNull(4, java.sql.Types.DATE);
			}

			int rowsAffected = preparedStatement.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("Error adding employee: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to add employee", e);
		}
	}

	public boolean updateEmployee(Employee updateEmployee) {
		String sql = "UPDATE mt_employee SET employee_name = ?, employee_age = ?, date_of_birth = ? WHERE employee_code = ?";
		try (Connection connection = getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, updateEmployee.getEmployeeName());
			preparedStatement.setInt(2, updateEmployee.getEmployeeAge());
			if (updateEmployee.getDateOfBirth() != null) {
				preparedStatement.setDate(3, new java.sql.Date(updateEmployee.getDateOfBirth().getTime()));
			} else {
				preparedStatement.setNull(3, java.sql.Types.DATE);
			}
			preparedStatement.setString(4, updateEmployee.getEmployeeCode());

			int rowsAffected = preparedStatement.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("Error updating employee: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to update employee", e);
		}
	}

	public static void main(String[] args) throws SQLException {
		EmployeeDAO emp = new EmployeeDAO();
		System.out.print(emp.testConnection());
	}
}