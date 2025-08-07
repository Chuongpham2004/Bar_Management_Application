package com.example.bar_management_application.service;

import com.example.bar_management_application.dao.PaymentDAO;
import com.example.bar_management_application.model.Payment;

import java.util.List;

public class PaymentService {

    private final PaymentDAO paymentDAO;

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
    }

    public List<Payment> getAllPayments() {
        return paymentDAO.getAllPayments();
    }

    public boolean createPayment(Payment payment) {
        return paymentDAO.insertPayment(payment);
    }

    public boolean deletePayment(int paymentId) {
        return paymentDAO.deletePayment(paymentId);
    }
}

