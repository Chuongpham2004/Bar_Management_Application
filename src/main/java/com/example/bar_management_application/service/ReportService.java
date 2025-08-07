package com.example.bar_management_application.service;

import com.example.bar_management_application.dao.ReportDAO;
import com.example.bar_management_application.model.Order;
import com.example.bar_management_application.model.Payment;

import java.time.LocalDate;
import java.util.List;

public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public double getTotalRevenue(LocalDate start, LocalDate end) {
        return reportDAO.getTotalRevenue(start, end);
    }

    public List<Order> getOrdersInDateRange(LocalDate start, LocalDate end) {
        return reportDAO.getOrdersInDateRange(start, end);
    }

    public List<Payment> getPaymentsInDateRange(LocalDate start, LocalDate end) {
        return reportDAO.getPaymentsInDateRange(start, end);
    }
}

