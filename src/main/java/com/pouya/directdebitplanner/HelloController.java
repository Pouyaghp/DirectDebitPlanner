package com.pouya.directdebitplanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextField amountField;

    @FXML
    private TextField dateField;

    @FXML
    private TextField categoryField;

    @FXML
    private TextArea notesField;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<DirectDebit> debitTable;

    @FXML
    private TableColumn<DirectDebit, String> nameColumn;

    @FXML
    private TableColumn<DirectDebit, Double> amountColumn;

    @FXML
    private TableColumn<DirectDebit, String> dateColumn;

    @FXML
    private TableColumn<DirectDebit, String> categoryColumn;

    @FXML
    private TableColumn<DirectDebit, String> notesColumn;

    @FXML
    private Label totalLabel;

    private final ObservableList<DirectDebit> debitList = FXCollections.observableArrayList();
    private FilteredList<DirectDebit> filteredDebits;
    private DirectDebit selectedDebitForEdit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        formatAmountColumn();
        setupRowHighlighting();
        loadDebitsFromDatabase();
        setupSearchFilter();
        setupTableSelection();
    }

    @FXML
    public void addDebit() {
        try {
            String name = nameField.getText().trim();
            String amountText = amountField.getText().trim();
            String dueDate = dateField.getText().trim();
            String category = categoryField.getText().trim();
            String notes = notesField.getText().trim();

            if (name.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Direct debit name is required.");
                nameField.requestFocus();
                return;
            }

            if (amountText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Amount is required.");
                amountField.requestFocus();
                return;
            }

            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Amount must be greater than 0.");
                amountField.requestFocus();
                return;
            }

            if (!dueDate.isEmpty()) {
                try {
                    int day = Integer.parseInt(dueDate);
                    if (day < 1 || day > 31) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Due date must be a number between 1 and 31.");
                        dateField.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Due date must be a number between 1 and 31.");
                    dateField.requestFocus();
                    return;
                }
            }

            DirectDebit directDebit = new DirectDebit(name, amount, dueDate, category, notes);
            DirectDebit savedDebit = DatabaseManager.insertDirectDebit(directDebit);

            debitList.add(savedDebit);
            sortDebitsByDueDate();
            updateMonthlyTotal();
            debitTable.refresh();
            clearFields();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Direct debit added successfully.");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Amount must be a valid number.");
            amountField.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Something went wrong while saving the direct debit.");
        }
    }

    @FXML
    public void updateSelectedDebit() {
        if (selectedDebitForEdit == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a direct debit to update.");
            return;
        }

        try {
            String name = nameField.getText().trim();
            String amountText = amountField.getText().trim();
            String dueDate = dateField.getText().trim();
            String category = categoryField.getText().trim();
            String notes = notesField.getText().trim();

            if (name.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Direct debit name is required.");
                nameField.requestFocus();
                return;
            }

            if (amountText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Amount is required.");
                amountField.requestFocus();
                return;
            }

            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Amount must be greater than 0.");
                amountField.requestFocus();
                return;
            }

            if (!dueDate.isEmpty()) {
                try {
                    int day = Integer.parseInt(dueDate);
                    if (day < 1 || day > 31) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Due date must be a number between 1 and 31.");
                        dateField.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Due date must be a number between 1 and 31.");
                    dateField.requestFocus();
                    return;
                }
            }

            DirectDebit updatedDebit = new DirectDebit(
                    selectedDebitForEdit.getId(),
                    name,
                    amount,
                    dueDate,
                    category,
                    notes
            );

            DatabaseManager.updateDirectDebit(updatedDebit);

            int index = debitList.indexOf(selectedDebitForEdit);
            if (index >= 0) {
                debitList.set(index, updatedDebit);
            }

            sortDebitsByDueDate();
            updateMonthlyTotal();
            debitTable.refresh();
            clearFields();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Direct debit updated successfully.");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Amount must be a valid number.");
            amountField.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Something went wrong while updating the direct debit.");
        }
    }

    @FXML
    public void deleteSelectedDebit() {
        DirectDebit selectedDebit = debitTable.getSelectionModel().getSelectedItem();

        if (selectedDebit == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a direct debit to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete \"" + selectedDebit.getName() + "\"?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            DatabaseManager.deleteDirectDebit(selectedDebit.getId());
            debitList.remove(selectedDebit);
            sortDebitsByDueDate();
            updateMonthlyTotal();
            debitTable.refresh();

            if (selectedDebitForEdit != null && selectedDebitForEdit.getId() == selectedDebit.getId()) {
                clearFields();
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Direct debit deleted successfully.");
        }
    }

    private void loadDebitsFromDatabase() {
        debitList.clear();
        debitList.addAll(DatabaseManager.getAllDirectDebits());
        sortDebitsByDueDate();

        filteredDebits = new FilteredList<>(debitList, p -> true);
        debitTable.setItems(filteredDebits);

        updateMonthlyTotal();
        debitTable.refresh();
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredDebits == null) {
                return;
            }

            String searchText = newValue == null ? "" : newValue.toLowerCase().trim();

            filteredDebits.setPredicate(debit -> {
                if (searchText.isEmpty()) {
                    return true;
                }

                return debit.getName().toLowerCase().contains(searchText)
                        || debit.getCategory().toLowerCase().contains(searchText)
                        || debit.getNotes().toLowerCase().contains(searchText)
                        || debit.getDueDate().toLowerCase().contains(searchText)
                        || String.format("%.2f", debit.getAmount()).contains(searchText);
            });

            debitTable.refresh();
        });
    }

    private void setupTableSelection() {
        debitTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedDebitForEdit = newValue;
                nameField.setText(newValue.getName());
                amountField.setText(String.valueOf(newValue.getAmount()));
                dateField.setText(newValue.getDueDate());
                categoryField.setText(newValue.getCategory());
                notesField.setText(newValue.getNotes());
            }
        });
    }

    private void setupRowHighlighting() {
        debitTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(DirectDebit debit, boolean empty) {
                super.updateItem(debit, empty);

                if (empty || debit == null) {
                    setStyle("");
                    return;
                }

                String style = getRowStyleForDebit(debit);
                setStyle(style);
            }
        });
    }

    private String getRowStyleForDebit(DirectDebit debit) {
        try {
            int dueDay = Integer.parseInt(debit.getDueDate());
            int today = LocalDate.now().getDayOfMonth();

            if (dueDay < today) {
                return "-fx-background-color: #ffe5e5;";
            }

            if (dueDay >= today && dueDay <= today + 3) {
                return "-fx-background-color: #fff4cc;";
            }

        } catch (Exception ignored) {
        }

        return "";
    }

    private void sortDebitsByDueDate() {
        FXCollections.sort(debitList, Comparator.comparingInt(debit -> {
            try {
                return Integer.parseInt(debit.getDueDate());
            } catch (Exception e) {
                return 999;
            }
        }));
    }

    private void formatAmountColumn() {
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);

                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("£%.2f", amount));
                }
            }
        });
    }

    private void updateMonthlyTotal() {
        double total = 0;

        for (DirectDebit debit : debitList) {
            total += debit.getAmount();
        }

        totalLabel.setText(String.format("Monthly Total: £%.2f", total));
    }

    private void clearFields() {
        nameField.clear();
        amountField.clear();
        dateField.clear();
        categoryField.clear();
        notesField.clear();
        selectedDebitForEdit = null;
        debitTable.getSelectionModel().clearSelection();
        nameField.requestFocus();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}