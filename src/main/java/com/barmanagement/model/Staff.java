package com.barmanagement.model;

import java.time.LocalDateTime;

/**
 * Model class for Staff/Employee
 * Represents staff members who can login to the system
 */
public class Staff {
    private int id;
    private String employeeId;
    private String username;
    private String password;
    private String fullName;
    private String position;
    private String role; // ADMIN, MANAGER, BARTENDER, WAITER
    private double salary;
    private String phone;
    private String email;
    private String address;
    private boolean status; // true = active, false = inactive
    private LocalDateTime hireDate;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Constructors
    public Staff() {}

    public Staff(String username, String password, String fullName, String position, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.position = position;
        this.role = role;
        this.status = true;
        this.createdAt = LocalDateTime.now();
    }

    public Staff(int id, String employeeId, String username, String fullName,
                 String position, String role, double salary, String phone,
                 String email, boolean status, LocalDateTime hireDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.username = username;
        this.fullName = fullName;
        this.position = position;
        this.role = role;
        this.salary = salary;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.hireDate = hireDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    // Utility methods
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public boolean isManager() {
        return "MANAGER".equalsIgnoreCase(this.role);
    }

    public boolean isBartender() {
        return "BARTENDER".equalsIgnoreCase(this.role);
    }

    public boolean isWaiter() {
        return "WAITER".equalsIgnoreCase(this.role);
    }

    public String getDisplayRole() {
        switch (role.toUpperCase()) {
            case "ADMIN": return "Quản trị viên";
            case "MANAGER": return "Quản lý";
            case "BARTENDER": return "Pha chế";
            case "WAITER": return "Phục vụ";
            default: return role;
        }
    }

    public String getStatusText() {
        return status ? "Hoạt động" : "Ngừng hoạt động";
    }

    @Override
    public String toString() {
        return "Staff{" +
                "id=" + id +
                ", employeeId='" + employeeId + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", position='" + position + '\'' +
                ", role='" + role + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Staff staff = (Staff) obj;
        return id == staff.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}