package com.barmanagement.controller;

import com.barmanagement.dao.MenuItemDAO;
import com.barmanagement.model.MenuItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.barmanagement.util.SceneUtil;
/**
 * Quản lý thực đơn (menu_items)
 * Model MenuItem: price = double
 */
public class MenuManagementController {

    @FXML private TableView<MenuItem> tableView;
    @FXML private TableColumn<MenuItem, Number> colId;
    @FXML private TableColumn<MenuItem, String> colName;
    @FXML private TableColumn<MenuItem, String> colCategory;
    @FXML private TableColumn<MenuItem, Number> colPrice;

    @FXML private TextField txtName, txtPrice;
    @FXML private ComboBox<String> cbCategory;
    @FXML private Button btnAdd, btnUpdate, btnDelete, btnRefresh;

    private final MenuItemDAO dao = new MenuItemDAO();
    private final ObservableList<MenuItem> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // schema: category ENUM('food','drink') DEFAULT 'drink'
        cbCategory.getItems().addAll("drink", "food");

        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colCategory.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));
        // price là double -> dùng SimpleDoubleProperty
        colPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrice()));

        tableView.setItems(data);
        tableView.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> fillForm(b));
        refresh();
    }

    private void fillForm(MenuItem m){
        if (m == null) return;
        txtName.setText(m.getName());
        // format số cho đẹp, tránh "1.0E4"
        txtPrice.setText(String.format(java.util.Locale.US, "%.2f", m.getPrice()));
        cbCategory.getSelectionModel().select(m.getCategory());
    }

    @FXML
    public void add() {
        try {
            MenuItem m = new MenuItem();
            m.setName(txtName.getText().trim());
            m.setCategory(cbCategory.getValue() == null ? "drink" : cbCategory.getValue());
            m.setPrice(Double.parseDouble(txtPrice.getText().trim())); // <- double
            m.setId(dao.insert(m));
            data.add(m);
            clear();
        } catch (Exception e) { err(e); }
    }
    @FXML
    private void goBack() {
        SceneUtil.openScene("/fxml/dashboard.fxml", tableView);
    }
    @FXML
    public void update() {
        MenuItem m = tableView.getSelectionModel().getSelectedItem();
        if (m == null) return;
        try {
            m.setName(txtName.getText().trim());
            m.setCategory(cbCategory.getValue() == null ? "drink" : cbCategory.getValue());
            m.setPrice(Double.parseDouble(txtPrice.getText().trim())); // <- double
            dao.update(m);
            tableView.refresh();
        } catch (Exception e) { err(e); }
    }

    @FXML
    public void delete() {
        MenuItem m = tableView.getSelectionModel().getSelectedItem();
        if (m == null) return;
        try {
            dao.delete(m.getId());
            data.remove(m);
        } catch (Exception e) { err(e); }
    }

    @FXML
    public void refresh() {
        data.clear();
        try { data.addAll(dao.findAll()); }
        catch (Exception e) { err(e); }
    }

    private void clear() {
        txtName.clear();
        txtPrice.clear();
        cbCategory.getSelectionModel().clearSelection();
    }

    private void err(Exception e) {
        new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        e.printStackTrace();
    }
}
