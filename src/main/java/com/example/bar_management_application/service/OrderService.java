package com.example.bar_management_application.service;

import com.example.bar_management_application.dao.OrderDAO;
import com.example.bar_management_application.model.Order;

import java.util.List;

public class OrderService {

    private final OrderDAO orderDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
    }

    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }

    public boolean createOrder(Order order) {
        return orderDAO.insertOrder(order);
    }

    public boolean deleteOrder(int orderId) {
        return orderDAO.deleteOrder(orderId);
    }

    public boolean updateOrderStatus(int orderId, String status) {
        return orderDAO.updateOrderStatus(orderId, status);
    }
}
