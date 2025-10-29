package com.example.model;

import java.util.Date;

public class Employee {
    private String employeeCode;
    private String employeeName;
    private Integer employeeAge;
    private Date dateOfBirth;
    
    public Employee() {
    }
    
    public Employee(String employeeCode, String employeeName, Integer employeeAge, Date dateOfBirth) {
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.employeeAge = employeeAge;
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getEmployeeCode() {
        return employeeCode;
    }
    
    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }
    
    public String getEmployeeName() {
        return employeeName;
    }
    
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
    
    public Integer getEmployeeAge() {
        return employeeAge;
    }
    
    public void setEmployeeAge(Integer employeeAge) {
        this.employeeAge = employeeAge;
    }
    
    public Date getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    @Override
    public String toString() {
        return "Employee{" +
                "employeeCode='" + employeeCode + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", employeeAge=" + employeeAge +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}