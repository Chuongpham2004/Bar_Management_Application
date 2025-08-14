package com.barmanagement.controller;

import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.model.Order;
import com.barmanagement.model.Table;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import com.barmanagement.util.SceneUtil;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.sql.SQLException;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.scene.control.TableCell;
import javafx.scene.control.Button;

public class TableManagementController {
    @FXML private TableColumn<Table, Void> colPayment;

    @FXML private TableView<Table> tableView;
    @FXML private TableColumn<Table, Number> colId;
    @FXML private TableColumn<Table, String> colName;
    @FXML private TableColumn<Table, String> colStatus;

    @FXML private TextField txtName;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnAdd, btnUpdate, btnDelete, btnEmpty, btnOccupied, btnReserved, btnRefresh;

    private final TableDAO dao = new TableDAO();
    private final ObservableList<Table> data = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        cbStatus.getItems().addAll("empty","occupied","reserved");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTableName()));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        tableView.setItems(data);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> fillForm(b));
        colPayment.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Table, Void> call(final TableColumn<Table, Void> param) {
                final TableCell<Table, Void> cell = new TableCell<>() {
                    private final Button btn = new Button("Thanh toán");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Table table = getTableView().getItems().get(getIndex());
                            handlePayment(table);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        });
        refresh();
    }
    private void handlePayment(Table table) {
        if (table == null) return;
        try {
            Order pendingOrder = new OrderDAO().findPendingByTable(table.getId());
            if (pendingOrder == null) {
                new Alert(Alert.AlertType.INFORMATION, "Bàn này chưa có order cần thanh toán.").showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/payment.fxml"));
            Scene scene = new Scene(loader.load());

            PaymentController pc = loader.getController();
            pc.setOrderId(pendingOrder.getId());
            pc.setTotalLabelText(new OrderDAO().calcTotal(pendingOrder.getId()).toPlainString());

            Stage stage = new Stage();
            stage.setTitle("Thanh toán - Bàn: " + table.getTableName());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể mở giao diện thanh toán:\n" + e.getMessage()).showAndWait();
        }
    }


    @FXML
    private void handlePayment() {
        Table selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Hãy chọn một bàn để thanh toán.").showAndWait();
            return;
        }

        try {
            Order pendingOrder = new OrderDAO().findPendingByTable(selected.getId());
            if (pendingOrder == null) {
                new Alert(Alert.AlertType.INFORMATION, "Bàn này chưa có order cần thanh toán.").showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/payment.fxml"));
            Scene scene = new Scene(loader.load());

            PaymentController pc = loader.getController();
            pc.setOrderId(pendingOrder.getId());
            pc.setTotalLabelText(new OrderDAO().calcTotal(pendingOrder.getId()).toPlainString());

            Stage stage = new Stage();
            stage.setTitle("Thanh toán - Bàn: " + selected.getTableName());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể mở giao diện thanh toán:\n" + e.getMessage()).showAndWait();
        }
    }

    private void fillForm(Table t) {
        if (t == null) return;
        txtName.setText(t.getTableName());
        cbStatus.getSelectionModel().select(t.getStatus());
    }
    @FXML
    private void goBack() {
        // dùng bất kỳ Node trong scene hiện tại; ở đây dùng tableView
        SceneUtil.openScene("/fxml/dashboard.fxml", tableView);
    }

    @FXML
    public void add(ActionEvent e) {
        try {
            Table t = new Table();
            t.setTableName(txtName.getText().trim());
            t.setStatus(cbStatus.getValue() == null ? "empty" : cbStatus.getValue());
            int id = dao.insert(t);
            t.setId(id);
            data.add(t);
            clearForm();
        } catch (Exception ex) { showErr(ex); }
    }

    @FXML
    public void update(ActionEvent e) {
        Table sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            sel.setTableName(txtName.getText().trim());
            sel.setStatus(cbStatus.getValue());
            dao.update(sel);
            tableView.refresh();
        } catch (Exception ex) { showErr(ex); }
    }

    @FXML
    public void delete(ActionEvent e) {
        Table sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try { dao.delete(sel.getId()); data.remove(sel); }
        catch (Exception ex) { showErr(ex); }
    }

    @FXML public void setEmpty()    { setStatus("empty"); }
    @FXML public void setOccupied() { setStatus("occupied"); }
    @FXML public void setReserved() { setStatus("reserved"); }

    private void setStatus(String st) {
        Table sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try { dao.updateStatus(sel.getId(), st); sel.setStatus(st); tableView.refresh(); }
        catch (SQLException e) { showErr(e); }
    }

    @FXML public void refresh() {
        data.clear();
        try { data.addAll(dao.findAll()); }
        catch (Exception e) { showErr(e); }
    }

    private void clearForm() { txtName.clear(); cbStatus.getSelectionModel().clearSelection(); }
    private void showErr(Exception e){ new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); e.printStackTrace(); }
}
