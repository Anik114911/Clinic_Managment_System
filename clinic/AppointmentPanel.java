/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clinic;

// ============================================================
//  AppointmentPanel.java
//  Purpose : Full Appointment Management module.
//            Features:
//              • Book Appointment
//              • Update Appointment
//              • Cancel Appointment
//              • Search Appointment (by patient or doctor name)
//              • View All Appointments in JTable
//              • Patient & Doctor selection via JComboBox
//              • Double-booking prevention (same doctor/date/time)
//              • Auto-populate form when a table row is clicked
//  Location: src/clinic/AppointmentPanel.java
// ============================================================

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;

public class AppointmentPanel extends JPanel {

    // ── Form input components ──────────────────────────────
    private JTextField  txtApptId;        // read-only, auto-filled
    private JComboBox<String> cmbPatient; // populated from DB
    private JComboBox<String> cmbDoctor;  // populated from DB
    private JTextField  txtDate;          // format: YYYY-MM-DD
    private JTextField  txtTime;          // format: HH:MM
    private JComboBox<String> cmbStatus;  // Pending / Completed / Cancelled
    private JTextField  txtSearch;

    // ── Action buttons ─────────────────────────────────────
    private JButton btnBook;
    private JButton btnUpdate;
    private JButton btnCancel;
    private JButton btnClear;
    private JButton btnSearch;
    private JButton btnShowAll;

    // ── Table ──────────────────────────────────────────────
    private JTable            apptTable;
    private DefaultTableModel tableModel;

    // ── Colors ─────────────────────────────────────────────
    private static final Color PANEL_BG    = new Color(245, 246, 250);
    private static final Color HEADER_BG   = new Color(44,  62,  80);
    private static final Color BTN_BOOK    = new Color(39,  174,  96);
    private static final Color BTN_UPDATE  = new Color(41,  128, 185);
    private static final Color BTN_CANCEL  = new Color(192,  57,  43);
    private static final Color BTN_CLEAR   = new Color(127, 140, 141);
    private static final Color BTN_SEARCH  = new Color(142,  68, 173);
    private static final Color BTN_SHOWALL = new Color(52,  152, 219);

    // ── Constructor ────────────────────────────────────────
    public AppointmentPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        loadAllAppointments();
    }

    // ═══════════════════════════════════════════════════════
    //  HEADER
    // ═══════════════════════════════════════════════════════
    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("📅   Appointment Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        // Today's date shown in the header as a handy reference
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"));
        JLabel lblDate = new JLabel("Today:  " + today);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDate.setForeground(new Color(174, 214, 241));
        header.add(lblDate, BorderLayout.EAST);

        return header;
    }

    // ═══════════════════════════════════════════════════════
    //  MAIN CONTENT — left form + right table
    // ═══════════════════════════════════════════════════════
    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(15, 0));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel left = buildFormPanel();
        left.setPreferredSize(new Dimension(320, 0));
        main.add(left, BorderLayout.WEST);

        main.add(buildTablePanel(), BorderLayout.CENTER);
        return main;
    }

    // ═══════════════════════════════════════════════════════
    //  LEFT — FORM PANEL
    // ═══════════════════════════════════════════════════════
    private JPanel buildFormPanel() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(5, 4, 5, 4);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.anchor  = GridBagConstraints.WEST;

        // ── Section title ─────────────────────────────────
        JLabel lblForm = new JLabel("Appointment Details");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblForm.setForeground(HEADER_BG);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        card.add(lblForm, gc);

        gc.gridy = 1;
        card.add(new JSeparator(), gc);

        gc.gridwidth = 1;

        // ── Appointment ID (read-only) ────────────────────
        gc.gridx = 0; gc.gridy = 2;
        card.add(makeLabel("Appt ID:"), gc);
        txtApptId = makeTextField();
        txtApptId.setEditable(false);
        txtApptId.setBackground(new Color(236, 240, 241));
        txtApptId.setToolTipText("Auto-generated by database");
        gc.gridx = 1;
        card.add(txtApptId, gc);

        // ── Patient ComboBox ──────────────────────────────
        gc.gridx = 0; gc.gridy = 3;
        card.add(makeLabel("Patient:"), gc);
        cmbPatient = new JComboBox<>();
        styleComboBox(cmbPatient);
        gc.gridx = 1;
        card.add(cmbPatient, gc);

        // ── Doctor ComboBox ───────────────────────────────
        gc.gridx = 0; gc.gridy = 4;
        card.add(makeLabel("Doctor:"), gc);
        cmbDoctor = new JComboBox<>();
        styleComboBox(cmbDoctor);
        gc.gridx = 1;
        card.add(cmbDoctor, gc);

        // ── Date field ────────────────────────────────────
        gc.gridx = 0; gc.gridy = 5;
        card.add(makeLabel("Date:"), gc);
        txtDate = makeTextField();
        txtDate.setToolTipText("Format: YYYY-MM-DD  (e.g. 2024-12-25)");

        // Pre-fill with today's date as a convenience
        txtDate.setText(LocalDate.now().toString());
        gc.gridx = 1;
        card.add(txtDate, gc);

        // ── Time field ────────────────────────────────────
        gc.gridx = 0; gc.gridy = 6;
        card.add(makeLabel("Time:"), gc);
        txtTime = makeTextField();
        txtTime.setToolTipText("Format: HH:MM  (e.g. 09:30 or 14:00)");
        gc.gridx = 1;
        card.add(txtTime, gc);

        // ── Status ComboBox ───────────────────────────────
        gc.gridx = 0; gc.gridy = 7;
        card.add(makeLabel("Status:"), gc);
        cmbStatus = new JComboBox<>(new String[]{"Pending", "Completed", "Cancelled"});
        styleComboBox(cmbStatus);
        gc.gridx = 1;
        card.add(cmbStatus, gc);

        // ── Format hint labels ────────────────────────────
        gc.gridx = 0; gc.gridy = 8; gc.gridwidth = 2;
        gc.insets = new Insets(2, 4, 2, 4);
        JLabel hint = new JLabel("  📌  Date: YYYY-MM-DD  |  Time: HH:MM");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(127, 140, 141));
        card.add(hint, gc);

        // ── Buttons ───────────────────────────────────────
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);

        btnBook   = makeButton("📅  Book",    BTN_BOOK);
        btnUpdate = makeButton("✏  Update",  BTN_UPDATE);
        btnCancel = makeButton("✖  Cancel",  BTN_CANCEL);
        btnClear  = makeButton("🔄  Clear",   BTN_CLEAR);

        btnGrid.add(btnBook);
        btnGrid.add(btnUpdate);
        btnGrid.add(btnCancel);
        btnGrid.add(btnClear);

        gc.gridx = 0; gc.gridy = 9; gc.gridwidth = 2;
        gc.insets = new Insets(12, 4, 4, 4);
        card.add(btnGrid, gc);

        // ── Wire actions ──────────────────────────────────
        btnBook  .addActionListener(e -> bookAppointment());
        btnUpdate.addActionListener(e -> updateAppointment());
        btnCancel.addActionListener(e -> cancelAppointment());
        btnClear .addActionListener(e -> clearForm());

        // Load combobox data from DB
        loadPatientsIntoCombo();
        loadDoctorsIntoCombo();

        return card;
    }

    // ═══════════════════════════════════════════════════════
    //  RIGHT — SEARCH BAR + TABLE
    // ═══════════════════════════════════════════════════════
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // ── Search bar ────────────────────────────────────
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(200, 32));
        txtSearch.setToolTipText("Search by patient name or doctor name");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchAppointment(); }
        });

        btnSearch  = makeButton("🔍  Search",   BTN_SEARCH);
        btnShowAll = makeButton("📋  Show All", BTN_SHOWALL);
        btnSearch .setPreferredSize(new Dimension(115, 32));
        btnShowAll.setPreferredSize(new Dimension(115, 32));

        btnSearch .addActionListener(e -> searchAppointment());
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); loadAllAppointments(); });

        searchBar.add(lblSearch);
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnShowAll);
        panel.add(searchBar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────
        String[] cols = {
            "Appt ID", "Patient", "Doctor",
            "Date", "Time", "Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        apptTable = new JTable(tableModel);
        styleTable(apptTable);

        // Row click → populate form
        apptTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                populateFormFromTable();
            }
        });

        JScrollPane scroll = new JScrollPane(apptTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ═══════════════════════════════════════════════════════
    //  LOAD COMBO BOXES FROM DATABASE
    // ═══════════════════════════════════════════════════════

    // ── Populate patient combo: "ID - Name" ───────────────
    private void loadPatientsIntoCombo() {
        cmbPatient.removeAllItems();
        cmbPatient.addItem("-- Select Patient --");

        String sql = "SELECT patient_id, name FROM patients ORDER BY name";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Format: "1 - Karim Hossain"
                cmbPatient.addItem(rs.getInt("patient_id") + " - " + rs.getString("name"));
            }

        } catch (SQLException e) {
            showError("Could not load patients: " + e.getMessage());
        }
    }

    // ── Populate doctor combo: "ID - Name (Spec)" ─────────
    private void loadDoctorsIntoCombo() {
        cmbDoctor.removeAllItems();
        cmbDoctor.addItem("-- Select Doctor --");

        String sql = "SELECT doc_id, name, specialization FROM doctors ORDER BY name";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Format: "1 - Dr. Ahmed Khan (Cardiology)"
                cmbDoctor.addItem(
                    rs.getInt("doc_id") + " - " +
                    rs.getString("name") + " (" +
                    rs.getString("specialization") + ")"
                );
            }

        } catch (SQLException e) {
            showError("Could not load doctors: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  DATABASE OPERATIONS
    // ═══════════════════════════════════════════════════════

    // ── Load all appointments ─────────────────────────────
    private void loadAllAppointments() {
        tableModel.setRowCount(0);

        // JOIN patients and doctors to show names instead of IDs
        String sql =
            "SELECT a.appt_id, p.name AS patient, d.name AS doctor, " +
            "       a.appt_date, a.appt_time, a.status " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.patient_id " +
            "JOIN doctors  d ON a.doc_id     = d.doc_id " +
            "ORDER BY a.appt_date DESC, a.appt_time DESC";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt   ("appt_id"),
                    rs.getString("patient"),
                    rs.getString("doctor"),
                    rs.getString("appt_date"),
                    rs.getString("appt_time"),
                    rs.getString("status")
                });
            }

        } catch (SQLException e) {
            showError("Failed to load appointments: " + e.getMessage());
        }
    }

    // ── BOOK a new appointment ────────────────────────────
    private void bookAppointment() {

        if (!validateForm()) return;

        int patientId = extractId(cmbPatient);
        int doctorId  = extractId(cmbDoctor);
        String date   = txtDate.getText().trim();
        String time   = normalizeTime(txtTime.getText().trim());

        // ── Double-booking check ──────────────────────────
        // The DB has a UNIQUE constraint too, but we check here first
        // to show a friendly message instead of a raw SQL error.
        if (isDoubleBooked(doctorId, date, time, -1)) {
            showError(
                "⚠ Double-booking detected!\n\n" +
                "This doctor already has an appointment\n" +
                "on " + date + " at " + time + ".\n\n" +
                "Please choose a different time slot."
            );
            return;
        }

        String sql =
            "INSERT INTO appointments (patient_id, doc_id, appt_date, appt_time, status) " +
            "VALUES (?, ?, ?, ?, 'Pending')";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, patientId);
            ps.setInt   (2, doctorId);
            ps.setString(3, date);
            ps.setString(4, time);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                showSuccess("Appointment booked successfully!");
                clearForm();
                loadAllAppointments();
                refreshDashboard();
            }

        } catch (SQLException e) {
            // Catches the DB-level UNIQUE constraint as a safety net
            if (e.getErrorCode() == 1062) {
                showError("Double-booking error: that time slot is already taken.");
            } else {
                showError("Failed to book appointment: " + e.getMessage());
            }
        }
    }

    // ── UPDATE an existing appointment ────────────────────
    private void updateAppointment() {

        if (txtApptId.getText().trim().isEmpty()) {
            showError("Please select an appointment from the table first.");
            return;
        }
        if (!validateForm()) return;

        int apptId    = Integer.parseInt(txtApptId.getText().trim());
        int patientId = extractId(cmbPatient);
        int doctorId  = extractId(cmbDoctor);
        String date   = txtDate.getText().trim();
        String time   = normalizeTime(txtTime.getText().trim());
        String status = cmbStatus.getSelectedItem().toString();

        // Double-booking check — exclude the current appointment's own slot
        if (isDoubleBooked(doctorId, date, time, apptId)) {
            showError(
                "⚠ Double-booking detected!\n\n" +
                "This doctor already has a different appointment\n" +
                "on " + date + " at " + time + ".\n\n" +
                "Please choose a different time slot."
            );
            return;
        }

        String sql =
            "UPDATE appointments " +
            "SET patient_id=?, doc_id=?, appt_date=?, appt_time=?, status=? " +
            "WHERE appt_id=?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, patientId);
            ps.setInt   (2, doctorId);
            ps.setString(3, date);
            ps.setString(4, time);
            ps.setString(5, status);
            ps.setInt   (6, apptId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                showSuccess("Appointment updated successfully!");
                clearForm();
                loadAllAppointments();
                refreshDashboard();
            }

        } catch (SQLException e) {
            showError("Failed to update appointment: " + e.getMessage());
        }
    }

    // ── CANCEL an appointment (sets status = 'Cancelled') ─
    // We do NOT delete — cancellation is a status change so
    // the record is preserved for billing history.
    private void cancelAppointment() {

        if (txtApptId.getText().trim().isEmpty()) {
            showError("Please select an appointment from the table first.");
            return;
        }

        String currentStatus = cmbStatus.getSelectedItem().toString();
        if ("Cancelled".equals(currentStatus)) {
            showError("This appointment is already cancelled.");
            return;
        }
        if ("Completed".equals(currentStatus)) {
            showError("A completed appointment cannot be cancelled.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Cancel appointment #" + txtApptId.getText() + "?\n" +
            "The record will be kept for history.",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE appointments SET status = 'Cancelled' WHERE appt_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(txtApptId.getText().trim()));
            int rows = ps.executeUpdate();

            if (rows > 0) {
                showSuccess("Appointment #" + txtApptId.getText() + " has been cancelled.");
                clearForm();
                loadAllAppointments();
                refreshDashboard();
            }

        } catch (SQLException e) {
            showError("Failed to cancel appointment: " + e.getMessage());
        }
    }

    // ── SEARCH appointments ───────────────────────────────
    private void searchAppointment() {
        String keyword = txtSearch.getText().trim();

        if (keyword.isEmpty()) {
            loadAllAppointments();
            return;
        }

        String sql =
            "SELECT a.appt_id, p.name AS patient, d.name AS doctor, " +
            "       a.appt_date, a.appt_time, a.status " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.patient_id " +
            "JOIN doctors  d ON a.doc_id     = d.doc_id " +
            "WHERE p.name LIKE ? OR d.name LIKE ? " +
            "ORDER BY a.appt_date DESC";

        tableModel.setRowCount(0);

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt   ("appt_id"),
                    rs.getString("patient"),
                    rs.getString("doctor"),
                    rs.getString("appt_date"),
                    rs.getString("appt_time"),
                    rs.getString("status")
                });
            }
            rs.close();

        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  DOUBLE-BOOKING CHECK
    // ═══════════════════════════════════════════════════════
    // Returns true if another appointment exists for the same
    // doctor, date, and time.
    // excludeApptId = -1 when booking new (no exclusion needed)
    // excludeApptId = current appt ID when updating (exclude self)
    private boolean isDoubleBooked(int doctorId, String date,
                                   String time, int excludeApptId) {
        String sql =
            "SELECT COUNT(*) FROM appointments " +
            "WHERE doc_id = ? AND appt_date = ? AND appt_time = ? " +
            "AND appt_id != ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, doctorId);
            ps.setString(2, date);
            ps.setString(3, time);
            ps.setInt   (4, excludeApptId);   // -1 never matches a real ID

            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════
    //  FORM HELPERS
    // ═══════════════════════════════════════════════════════

    // ── Fill form from selected table row ─────────────────
    private void populateFormFromTable() {
        int row = apptTable.getSelectedRow();
        if (row < 0) return;

        int    apptId  = (int)    tableModel.getValueAt(row, 0);
        String patient = tableModel.getValueAt(row, 1).toString();
        String doctor  = tableModel.getValueAt(row, 2).toString();
        String date    = tableModel.getValueAt(row, 3).toString();
        String time    = tableModel.getValueAt(row, 4).toString();
        String status  = tableModel.getValueAt(row, 5).toString();

        txtApptId.setText(String.valueOf(apptId));
        txtDate  .setText(date);
        txtTime  .setText(time);
        cmbStatus.setSelectedItem(status);

        // Select the right patient in the combobox
        // The combo items are formatted as "ID - Name"
        // So we fetch the patient_id from the DB to find the right item
        selectComboByApptId(apptId);
    }

    // ── Match combobox selections to the appointment's IDs ─
    // Queries the DB for the actual patient_id and doc_id,
    // then scans the combobox items for the matching "ID - ..." prefix
    private void selectComboByApptId(int apptId) {
        String sql = "SELECT patient_id, doc_id FROM appointments WHERE appt_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, apptId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int patientId = rs.getInt("patient_id");
                int docId     = rs.getInt("doc_id");

                // Find and select the matching combo item
                selectComboById(cmbPatient, patientId);
                selectComboById(cmbDoctor,  docId);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Scan a combobox for an item starting with "ID - " ─
    private void selectComboById(JComboBox<String> combo, int id) {
        String prefix = id + " - ";
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).startsWith(prefix)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    // ── Extract just the numeric ID from "1 - Name..." ────
    private int extractId(JComboBox<String> combo) {
        String item = combo.getSelectedItem().toString();
        return Integer.parseInt(item.split(" - ")[0].trim());
    }

    // ── Validate all form fields ──────────────────────────
    private boolean validateForm() {

        if (cmbPatient.getSelectedIndex() == 0) {
            showError("Please select a patient."); return false;
        }
        if (cmbDoctor.getSelectedIndex() == 0) {
            showError("Please select a doctor."); return false;
        }
        if (txtDate.getText().trim().isEmpty()) {
            showError("Please enter a date."); return false;
        }

        // Validate date format YYYY-MM-DD
        try {
            LocalDate.parse(txtDate.getText().trim(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            showError("Invalid date format.\nPlease use YYYY-MM-DD  (e.g. 2024-12-25).");
            return false;
        }

        if (txtTime.getText().trim().isEmpty()) {
            showError("Please enter a time."); return false;
        }

        // Validate time format HH:MM
        String timeInput = normalizeTime(txtTime.getText().trim());
        try {
            LocalTime.parse(timeInput, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showError("Invalid time format.\nPlease use HH:MM  (e.g. 09:30 or 14:00).");
            return false;
        }

        return true;
    }

    // ── Normalize time: "9:5" → "09:05", "9" → invalid ───
    // Ensures the time string is always stored as HH:MM
    private String normalizeTime(String raw) {
        if (raw.contains(":")) {
            String[] parts = raw.split(":");
            if (parts.length == 2) {
                String h = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
                String m = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
                return h + ":" + m;
            }
        }
        return raw;   // return as-is; validation will catch bad formats
    }

    // ── Clear form and reset all inputs ───────────────────
    private void clearForm() {
        txtApptId.setText("");
        txtDate  .setText(LocalDate.now().toString());  // reset to today
        txtTime  .setText("");
        cmbPatient.setSelectedIndex(0);
        cmbDoctor .setSelectedIndex(0);
        cmbStatus .setSelectedIndex(0);   // back to "Pending"
        apptTable.clearSelection();
    }

    // ── Refresh Dashboard summary cards ───────────────────
    private void refreshDashboard() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof Dashboard) {
            ((Dashboard) window).loadDashboardStats();
        }
    }

    // ═══════════════════════════════════════════════════════
    //  TABLE STYLING  (L&F-proof header renderer)
    // ═══════════════════════════════════════════════════════
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(41, 128, 185, 80));
        table.setSelectionForeground(Color.BLACK);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(65);   // Appt ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150);  // Patient
        table.getColumnModel().getColumn(2).setPreferredWidth(160);  // Doctor
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Date
        table.getColumnModel().getColumn(4).setPreferredWidth(70);   // Time
        table.getColumnModel().getColumn(5).setPreferredWidth(90);   // Status

        // ── L&F-proof header renderer (same fix as Stage 4) ─
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
        header.setOpaque(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setText(val == null ? "" : val.toString());
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setOpaque(true);
                setBackground(HEADER_BG);
                setForeground(Color.WHITE);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(80, 100, 120)),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)
                ));
                return this;
            }
        });

        // ── Row renderer with status colour-coding ─────────
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);

                setHorizontalAlignment(
                    (col == 0) ? SwingConstants.CENTER : SwingConstants.LEFT
                );
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 246, 250));
                }

                // Colour-code the Status column (index 5)
                if (col == 5 && val != null) {
                    switch (val.toString()) {
                        case "Pending":
                            setForeground(new Color(230, 126, 34));  break; // orange
                        case "Completed":
                            setForeground(new Color(39,  174, 96));  break; // green
                        case "Cancelled":
                            setForeground(new Color(192,  57, 43));  break; // red
                        default:
                            setForeground(Color.BLACK);
                    }
                } else {
                    setForeground(Color.BLACK);
                }
                return this;
            }
        });
    }

    // ═══════════════════════════════════════════════════════
    //  UI UTILITY HELPERS
    // ═══════════════════════════════════════════════════════

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(60, 60, 60));
        return lbl;
    }

    private JTextField makeTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(160, 30));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        return tf;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(160, 30));
        combo.setBackground(Color.WHITE);
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(130, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}
