package com.barmanagement.controller;

import com.barmanagement.dao.TableDAO;
import com.barmanagement.dao.OrderDAO;
import com.barmanagement.model.Table;
import com.barmanagement.model.Order;
import javafx.collections.*;
import javafx.event.ActionEvent;
import com.barmanagement.util.TimeService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.barmanagement.util.SceneUtil;
import com.barmanagement.util.LogoutUtil;

import java.sql.SQLException;

/**
 * Table Management Controller - MODIFIED VERSION
 * Removed table edit functionality, enhanced delete and card display
 * Updated with consistent table visualization
 */
public class TableManagementController {

    @FXML
    private TableView<Table> tableView;
    @FXML
    private TableColumn<Table, Number> colId;
    @FXML
    private TableColumn<Table, String> colName;
    @FXML
    private TableColumn<Table, String> colStatus;

    @FXML
    private TextField txtName;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private Button btnAdd, btnUpdate, btnDelete, btnEmpty, btnOccupied, btnReserved, btnRefresh;

    // Statistics labels
    @FXML
    private Label lblTotalTables, lblEmptyTables, lblOccupiedTables, lblReservedTables;

    // Card layout container
    @FXML
    private FlowPane tableCardsContainer;

    private final TableDAO tableDAO = new TableDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ObservableList<Table> data = FXCollections.observableArrayList();

    @FXML private Label currentTimeLabel;
    @FXML private Label welcomeTimeLabel;

    @FXML
    public void initialize() {

        if (currentTimeLabel != null) {
            currentTimeLabel.textProperty().bind(TimeService.get().timeTextProperty());
        }
        if (welcomeTimeLabel != null) {
            welcomeTimeLabel.textProperty().bind(TimeService.get().dateTextProperty());
        }
        setupComponents();
        setupTableView();
        refresh();

        // Ẩn nút sửa theo yêu cầu
        if (btnUpdate != null) {
            btnUpdate.setVisible(false);
            btnUpdate.setManaged(false);
        }
    }

    private void setupComponents() {
        // Setup status combo box
        cbStatus.getItems().addAll("empty", "occupied", "reserved", "ordering");
        cbStatus.setValue("empty"); // Default to empty status

        // Setup table columns
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTableName()));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(getStatusDisplayName(c.getValue().getStatus())));

        // Set data to table view
        tableView.setItems(data);

        // Selection listener - only for form filling, not for cards
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> {
            fillForm(b);
            // No need to refresh cards for selection
        });
    }

    private void setupTableView() {
        // Custom cell factory for status column to show colors
        colStatus.setCellFactory(column -> {
            return new TableCell<Table, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);

                        Table table = getTableView().getItems().get(getIndex());
                        String status = table.getStatus();

                        switch (status) {
                            case "empty":
                                setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32;");
                                break;
                            case "occupied":
                                setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828;");
                                break;
                            case "reserved":
                                setStyle("-fx-background-color: #ffe0b2; -fx-text-fill: #ef6c00;");
                                break;
                            case "ordering":
                                setStyle("-fx-background-color: #e1bee7; -fx-text-fill: #7b1fa2;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            };
        });
    }

    private void updateTableCards() {
        if (tableCardsContainer == null) return;

        tableCardsContainer.getChildren().clear();

        for (Table table : data) {
            VBox card = createTableCard(table);
            tableCardsContainer.getChildren().add(card);
        }

        if (data.isEmpty()) {
            Label noTablesLabel = new Label("Chưa có bàn nào được tạo");
            noTablesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 20;");
            tableCardsContainer.getChildren().add(noTablesLabel);
        }
    }

    private VBox createTableCard(Table table) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);
        card.setPrefHeight(150);
        card.setStyle(getCardStyle(table.getStatus()));

        // Set user data for delete functionality
        card.setUserData(table);

        // Create a StackPane for the table with chairs
        StackPane tableWithChairs = new StackPane();

        // Main table rectangle
        String tableColor = getCardColor(table.getStatus());
        String chairColor = getChairColor(table.getStatus());

        Rectangle tableRect = new Rectangle(80, 60);
        tableRect.setArcWidth(10);
        tableRect.setArcHeight(10);
        tableRect.setFill(Color.web(tableColor));
        tableRect.setStroke(Color.WHITE);
        tableRect.setStrokeWidth(2);

        // Top chair
        Rectangle topChair = new Rectangle(25, 15);
        topChair.setArcWidth(5);
        topChair.setArcHeight(5);
        topChair.setFill(Color.web(chairColor));
        topChair.setTranslateY(-37.5);

        // Bottom chair
        Rectangle bottomChair = new Rectangle(25, 15);
        bottomChair.setArcWidth(5);
        bottomChair.setArcHeight(5);
        bottomChair.setFill(Color.web(chairColor));
        bottomChair.setTranslateY(37.5);

        // Left chair
        Rectangle leftChair = new Rectangle(15, 25);
        leftChair.setArcWidth(5);
        leftChair.setArcHeight(5);
        leftChair.setFill(Color.web(chairColor));
        leftChair.setTranslateX(-47.5);

        // Right chair
        Rectangle rightChair = new Rectangle(15, 25);
        rightChair.setArcWidth(5);
        rightChair.setArcHeight(5);
        rightChair.setFill(Color.web(chairColor));
        rightChair.setTranslateX(47.5);

        // Table label
        Label tableLabel = new Label(table.getTableName());
        tableLabel.setTextFill(Color.WHITE);
        tableLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        tableWithChairs.getChildren().addAll(tableRect, topChair, bottomChair, leftChair, rightChair, tableLabel);

        // Table info section
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER);

        Label status = new Label(getStatusDisplayName(table.getStatus()));
        status.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        // Thêm nút xóa vào card
        Button deleteBtn = new Button("❌");
        deleteBtn.setStyle("-fx-background-color: rgba(244,67,54,0.8); -fx-text-fill: white; -fx-background-radius: 50%;");
        deleteBtn.setPrefWidth(30);
        deleteBtn.setPrefHeight(30);
        deleteBtn.setOnAction(e -> {
            // Lấy bàn từ user data của card
            Table t = (Table) card.getUserData();
            if (t != null) {
                deleteTable(t);
            }
        });

        card.getChildren().addAll(tableWithChairs, status, deleteBtn);

        // Add drop shadow
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#0f3460"));
        dropShadow.setRadius(8);
        card.setEffect(dropShadow);

        return card;
    }

    private String getCardColor(String status) {
        switch (status) {
            case "empty": return "#4CAF50";
            case "occupied": return "#f44336";
            case "reserved": return "#FF9800";
            case "ordering": return "#9C27B0";
            default: return "#607D8B";
        }
    }

    private String getChairColor(String status) {
        switch (status) {
            case "empty": return "#2E7D32";
            case "occupied": return "#B71C1C";
            case "reserved": return "#E65100";
            case "ordering": return "#4A148C";
            default: return "#455A64";
        }
    }

    private String getCardStyle(String status) {
        String baseStyle = "-fx-background-radius: 12; -fx-padding: 15; -fx-background-color: ";
        switch (status) {
            case "empty":
                return baseStyle + "#4CAF50;";
            case "occupied":
                return baseStyle + "#f44336;";
            case "reserved":
                return baseStyle + "#FF9800;";
            case "ordering":
                return baseStyle + "#9C27B0;";
            default:
                return baseStyle + "#607D8B;";
        }
    }

    // Thêm hàm xóa bàn riêng cho card
    private void deleteTable(Table table) {
        try {
            // 1) Chặn xóa nếu HÔM NAY có đơn đã thanh toán
            if (orderDAO.existsPaidTodayByTable(table.getId())) {
                showError(new Exception("Không thể xóa bàn vì hôm nay đã có đơn đã thanh toán."));
                return;
            }

            // 2) Chặn xóa nếu còn đơn đang hoạt động trong ngày
            Order activeOrder = orderDAO.findPendingByTable(table.getId());
            if (activeOrder != null) {
                showError(new Exception("Không thể xóa bàn có đơn hàng chưa hoàn thành!\n" +
                        "Order #" + activeOrder.getId() + " (" + activeOrder.getStatusDisplayName() + ")"));
                return;
            }

            // Hiển thị hộp thoại xác nhận
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setHeaderText("Xóa bàn " + table.getTableName() + "?");
            confirmAlert.setContentText("Hành động này không thể hoàn tác!");

            ButtonType result = confirmAlert.showAndWait().orElse(ButtonType.CANCEL);
            if (result == ButtonType.OK) {
                tableDAO.delete(table.getId());
                data.remove(table);
                updateStatistics();
                updateTableCards();
                showInfo("Đã xóa bàn " + table.getTableName() + " thành công!");
            }

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private String getStatusIcon(String status) {
        switch (status) {
            case "empty":
                return "🟢";
            case "occupied":
                return "🔴";
            case "reserved":
                return "🟠";
            case "ordering":
                return "🟣";
            default:
                return "⚪";
        }
    }

    private void fillForm(Table t) {
        if (t == null) {
            clearForm();
            return;
        }

        txtName.setText(t.getTableName());
        cbStatus.getSelectionModel().select(t.getStatus());
    }

    private void clearForm() {
        txtName.clear();
        cbStatus.setValue("empty"); // Default to empty
        tableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void goBack() {
        SceneUtil.openScene("/fxml/dashboard.fxml", tableView);
    }

    // Navigation methods for sidebar
    @FXML
    private void showPayment() {
        SceneUtil.openScene("/fxml/payment.fxml", tableView);
    }

    @FXML
    private void showMenu() {
        SceneUtil.openScene("/fxml/menu_management.fxml", tableView);
    }

    @FXML
    private void showOrder() {
        SceneUtil.openScene("/fxml/order_management.fxml", tableView);
    }

    @FXML
    private void handleLogout() {
        LogoutUtil.confirmLogout(tableView);
    }

    @FXML
    private void exportTables() {
        showInfo("Chức năng xuất danh sách bàn sẽ được phát triển trong phiên bản tới!");
    }

    @FXML
    public void add(ActionEvent e) {
        String name = txtName.getText().trim();
        String status = cbStatus.getValue();

        if (name.isEmpty()) {
            showError(new Exception("Tên bàn không được để trống!"));
            return;
        }

        try {
            // Check if table name already exists
            for (Table existing : data) {
                if (existing.getTableName().equalsIgnoreCase(name)) {
                    showError(new Exception("Tên bàn đã tồn tại!"));
                    return;
                }
            }

            Table t = new Table();
            t.setTableName(name);
            t.setStatus(status == null ? "empty" : status);

            int id = tableDAO.insert(t);
            t.setId(id);
            data.add(t);
            clearForm();
            updateStatistics();
            updateTableCards();
            showInfo("Đã thêm bàn " + name + " thành công!");

        } catch (Exception ex) {
            showError(ex);
        }
    }

    // Phương thức update đã bị ẩn nút nên không cần xóa hoàn toàn
    @FXML
    public void update(ActionEvent e) {
        showInfo("Chức năng sửa bàn đã bị vô hiệu hóa theo yêu cầu!");
    }

    @FXML
    public void delete(ActionEvent e) {
        Table sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showInfo("Vui lòng chọn bàn để xóa!");
            return;
        }

        deleteTable(sel); // Sử dụng lại hàm xóa bàn
    }

    @FXML
    public void setEmpty() {
        setStatus("empty");
    }

    @FXML
    public void setOccupied() {
        setStatus("occupied");
    }

    @FXML
    public void setReserved() {
        setStatus("reserved");
    }

    @FXML
    public void setOrdering() {
        setStatus("ordering");
    }

    private void setStatus(String newStatus) {
        Table sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showInfo("Vui lòng chọn bàn để thay đổi trạng thái!");
            return;
        }

        if (newStatus.equals(sel.getStatus())) {
            showInfo("Bàn đã ở trạng thái " + getStatusDisplayName(newStatus) + " rồi!");
            return;
        }

        try {
            // Check for active orders when setting to empty
            if ("empty".equals(newStatus)) {
                Order activeOrder = orderDAO.findPendingByTable(sel.getId());
                if (activeOrder != null) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Xác nhận");
                    confirmAlert.setHeaderText("Bàn có đơn hàng chưa hoàn thành");
                    confirmAlert.setContentText("Bàn " + sel.getTableName() + " có order #" + activeOrder.getId() +
                            " (" + activeOrder.getStatusDisplayName() + ").\n" +
                            "Bạn có chắc muốn đặt trạng thái Empty không?");

                    ButtonType result = confirmAlert.showAndWait().orElse(ButtonType.CANCEL);
                    if (result != ButtonType.OK) {
                        return;
                    }
                }
            }

            tableDAO.updateStatus(sel.getId(), newStatus);
            sel.setStatus(newStatus);
            tableView.refresh();
            updateStatistics();
            updateTableCards();

            showInfo("Đã cập nhật trạng thái bàn " + sel.getTableName() +
                    " thành: " + getStatusDisplayName(newStatus));

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    @FXML
    public void refresh() {
        data.clear();
        try {
            data.addAll(tableDAO.findAll());
            updateStatistics();
            updateTableCards();
            showInfo("Đã làm mới danh sách bàn!");
        } catch (Exception e) {
            showError(e);
        }
    }

    private void updateStatistics() {
        if (lblTotalTables != null) {
            int total = data.size();
            int empty = (int) data.stream().filter(t -> "empty".equals(t.getStatus())).count();
            int occupied = (int) data.stream().filter(t -> "occupied".equals(t.getStatus())).count();
            int reserved = (int) data.stream().filter(t -> "reserved".equals(t.getStatus())).count();
            int ordering = (int) data.stream().filter(t -> "ordering".equals(t.getStatus())).count();

            lblTotalTables.setText(String.valueOf(total));
            lblEmptyTables.setText(String.valueOf(empty));
            lblOccupiedTables.setText(String.valueOf(occupied));
            lblReservedTables.setText(String.valueOf(reserved));

            // Show ordering count somewhere if you have a label for it
            System.out.println("Table stats - Total: " + total + ", Empty: " + empty +
                    ", Occupied: " + occupied + ", Reserved: " + reserved + ", Ordering: " + ordering);
        }
    }

    private String getStatusDisplayName(String status) {
        switch (status) {
            case "empty": return "Trống";
            case "occupied": return "Có khách";
            case "reserved": return "Đặt trước";
            case "ordering": return "Đang chọn món";
            default: return status;
        }
    }

    private void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText("Có lỗi xảy ra");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
        e.printStackTrace();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}