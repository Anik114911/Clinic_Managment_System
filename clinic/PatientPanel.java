/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clinic;

// ============================================================
//  PatientPanel.java
//  Purpose : Full Patient Management module.
//            Features:
//              • Add Patient
//              • Update Patient
//              • Delete Patient
//              • Search Patient (by name or phone)
//              • View All Patients in JTable
//              • Auto-populate form when a table row is clicked
//  Location: src/clinic/PatientPanel.java
// ============================================================

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class PatientPanel extends JPanel {

    // ── Form input fields ──────────────────────────────────
    private JTextField txtPatientId;      // auto-filled, not editable
    private JTextField txtName;
    private JTextField txtAge;
    private JTextField txtPhone;
    private JTextField txtSearch;

    // ── Action buttons ─────────────────────────────────────
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnSearch;
    private JButton btnShowAll;

    // ── Table components ───────────────────────────────────
    private JTable            patientTable;
    private DefaultTableModel tableModel;

    // ── Colors (same palette as DoctorPanel for consistency)
    private static final Color PANEL_BG    = new Color(245, 246, 250);
    private static final Color HEADER_BG   = new Color(44,  62,  80);
    private static final Color BTN_ADD     = new Color(39,  174,  96);
    private static final Color BTN_UPDATE  = new Color(41,  128, 185);
    private static final Color BTN_DELETE  = new Color(192,  57,  43);
    private static final Color BTN_CLEAR   = new Color(127, 140, 141);
    private static final Color BTN_SEARCH  = new Color(142,  68, 173);
    private static final Color BTN_SHOWALL = new Color(52,  152, 219);

    // ── Constructor ────────────────────────────────────────
    public PatientPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        loadAllPatients();   // populate table on load
    }

    // ═══════════════════════════════════════════════════════
    //  HEADER — page title bar
    // ═══════════════════════════════════════════════════════
    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("🧑   Patient Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        return header;
    }

    // ═══════════════════════════════════════════════════════
    //  MAIN CONTENT — left form + right table
    // ═══════════════════════════════════════════════════════
    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(15, 0));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Left: input form (fixed width)
        JPanel left = buildFormPanel();
        left.setPreferredSize(new Dimension(300, 0));
        main.add(left, BorderLayout.WEST);

        // Right: search bar + table
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
        gc.insets  = new Insets(6, 4, 6, 4);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.anchor  = GridBagConstraints.WEST;

        // ── Section label ─────────────────────────────────
        JLabel lblForm = new JLabel("Patient Details");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblForm.setForeground(HEADER_BG);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        card.add(lblForm, gc);

        // ── Separator ─────────────────────────────────────
        gc.gridy = 1;
        card.add(new JSeparator(), gc);

        gc.gridwidth = 1;

        // ── Patient ID (read-only, auto-filled on row click)
        gc.gridx = 0; gc.gridy = 2;
        card.add(makeLabel("Patient ID:"), gc);
        txtPatientId = makeTextField();
        txtPatientId.setEditable(false);
        txtPatientId.setBackground(new Color(236, 240, 241));
        txtPatientId.setToolTipText("Auto-generated by database");
        gc.gridx = 1;
        card.add(txtPatientId, gc);

        // ── Name ──────────────────────────────────────────
        gc.gridx = 0; gc.gridy = 3;
        card.add(makeLabel("Name:"), gc);
        txtName = makeTextField();
        gc.gridx = 1;
        card.add(txtName, gc);

        // ── Age ───────────────────────────────────────────
        gc.gridx = 0; gc.gridy = 4;
        card.add(makeLabel("Age:"), gc);
        txtAge = makeTextField();
        txtAge.setToolTipText("Enter age as a whole number (e.g. 35)");
        gc.gridx = 1;
        card.add(txtAge, gc);

        // ── Phone ─────────────────────────────────────────
        gc.gridx = 0; gc.gridy = 5;
        card.add(makeLabel("Phone:"), gc);
        txtPhone = makeTextField();
        gc.gridx = 1;
        card.add(txtPhone, gc);

        // ── Patient count badge ───────────────────────────
        // Small live counter showing total patients registered
        gc.gridx = 0; gc.gridy = 6; gc.gridwidth = 2;
        gc.insets = new Insets(10, 4, 2, 4);
        JPanel badgePanel = buildPatientCountBadge();
        card.add(badgePanel, gc);

        // ── Button grid ───────────────────────────────────
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);

        btnAdd    = makeButton("➕  Add",     BTN_ADD);
        btnUpdate = makeButton("✏  Update",  BTN_UPDATE);
        btnDelete = makeButton("🗑  Delete",  BTN_DELETE);
        btnClear  = makeButton("✖  Clear",   BTN_CLEAR);

        btnGrid.add(btnAdd);
        btnGrid.add(btnUpdate);
        btnGrid.add(btnDelete);
        btnGrid.add(btnClear);

        gc.gridx = 0; gc.gridy = 7; gc.gridwidth = 2;
        gc.insets = new Insets(12, 4, 4, 4);
        card.add(btnGrid, gc);

        // ── Wire button actions ───────────────────────────
        btnAdd   .addActionListener(e -> addPatient());
        btnUpdate.addActionListener(e -> updatePatient());
        btnDelete.addActionListener(e -> deletePatient());
        btnClear .addActionListener(e -> clearForm());

        return card;
    }

    // ── Small badge showing total registered patients ──────
    private JPanel buildPatientCountBadge() {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badge.setBackground(new Color(232, 246, 253));   // light blue tint
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(174, 214, 241), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        JLabel icon = new JLabel("🧑");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        JLabel text = new JLabel("Total Registered: ");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        text.setForeground(new Color(44, 62, 80));

        JLabel count = new JLabel("...");
        count.setFont(new Font("Segoe UI", Font.BOLD, 13));
        count.setForeground(new Color(41, 128, 185));

        badge.add(icon);
        badge.add(text);
        badge.add(count);

        // Load count from DB
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM patients");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) count.setText(String.valueOf(rs.getInt(1)));
        } catch (SQLException e) {
            count.setText("?");
        }

        return badge;
    }

    // ═══════════════════════════════════════════════════════
    //  RIGHT — SEARCH BAR + TABLE PANEL
    // ═══════════════════════════════════════════════════════
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // ── Search bar ────────────────────────────────────
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtSearch = new JTextField(22);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(220, 32));
        txtSearch.setToolTipText("Search by patient name or phone number");

        // Live search: fires on every keystroke
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchPatient(); }
        });

        btnSearch  = makeButton("🔍  Search",   BTN_SEARCH);
        btnShowAll = makeButton("📋  Show All", BTN_SHOWALL);
        btnSearch .setPreferredSize(new Dimension(115, 32));
        btnShowAll.setPreferredSize(new Dimension(115, 32));

        btnSearch .addActionListener(e -> searchPatient());
        btnShowAll.addActionListener(e -> {
            txtSearch.setText("");
            loadAllPatients();
        });

        searchBar.add(lblSearch);
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnShowAll);
        panel.add(searchBar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────
        String[] columns = {"Patient ID", "Name", "Age", "Phone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        patientTable = new JTable(tableModel);
        styleTable(patientTable);

        // Row click → populate form automatically
        patientTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                populateFormFromTable();
            }
        });

        JScrollPane scroll = new JScrollPane(patientTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ═══════════════════════════════════════════════════════
    //  DATABASE OPERATIONS
    // ═══════════════════════════════════════════════════════

    // ── Load all patients into the table ──────────────────
    private void loadAllPatients() {
        tableModel.setRowCount(0);   // clear existing rows first

        String sql = "SELECT patient_id, name, age, phone FROM patients ORDER BY patient_id";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt   ("patient_id"),
                    rs.getString("name"),
                    rs.getInt   ("age"),
                    rs.getString("phone")
                });
            }

        } catch (SQLException e) {
            showError("Failed to load patients: " + e.getMessage());
        }
    }

    // ── ADD a new patient ─────────────────────────────────
    private void addPatient() {

        if (!validateForm()) return;

        String sql = "INSERT INTO patients (name, age, phone) VALUES (?, ?, ?)";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtName.getText().trim());
            ps.setInt   (2, Integer.parseInt(txtAge.getText().trim()));
            ps.setString(3, txtPhone.getText().trim());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                showSuccess("Patient registered successfully!");
                clearForm();
                loadAllPatients();
                refreshDashboard();
            }

        } catch (NumberFormatException e) {
            showError("Age must be a whole number (e.g. 25).");
        } catch (SQLException e) {
            showError("Failed to add patient: " + e.getMessage());
        }
    }

    // ── UPDATE an existing patient ────────────────────────
    private void updatePatient() {

        if (txtPatientId.getText().trim().isEmpty()) {
            showError("Please select a patient from the table first.");
            return;
        }
        if (!validateForm()) return;

        String sql = "UPDATE patients SET name=?, age=?, phone=? WHERE patient_id=?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtName.getText().trim());
            ps.setInt   (2, Integer.parseInt(txtAge.getText().trim()));
            ps.setString(3, txtPhone.getText().trim());
            ps.setInt   (4, Integer.parseInt(txtPatientId.getText().trim()));

            int rows = ps.executeUpdate();

            if (rows > 0) {
                showSuccess("Patient record updated successfully!");
                clearForm();
                loadAllPatients();
                refreshDashboard();
            }

        } catch (NumberFormatException e) {
            showError("Age must be a whole number (e.g. 25).");
        } catch (SQLException e) {
            showError("Failed to update patient: " + e.getMessage());
        }
    }

    // ── DELETE a patient ──────────────────────────────────
    private void deletePatient() {

        if (txtPatientId.getText().trim().isEmpty()) {
            showError("Please select a patient from the table first.");
            return;
        }

        // Always confirm destructive operations
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete patient: " + txtName.getText() + "?\n"
            + "This will also delete all their appointments and bills.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(txtPatientId.getText().trim()));
            int rows = ps.executeUpdate();

            if (rows > 0) {
                showSuccess("Patient deleted successfully.");
                clearForm();
                loadAllPatients();
                refreshDashboard();
            }

        } catch (SQLException e) {
            showError("Failed to delete patient: " + e.getMessage());
        }
    }

    // ── SEARCH patients by name or phone ──────────────────
    private void searchPatient() {
        String keyword = txtSearch.getText().trim();

        if (keyword.isEmpty()) {
            loadAllPatients();
            return;
        }

        String sql = "SELECT patient_id, name, age, phone " +
                     "FROM patients " +
                     "WHERE name LIKE ? OR phone LIKE ? " +
                     "ORDER BY name";

        tableModel.setRowCount(0);

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt   ("patient_id"),
                    rs.getString("name"),
                    rs.getInt   ("age"),
                    rs.getString("phone")
                });
            }

            rs.close();

        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  FORM HELPERS
    // ═══════════════════════════════════════════════════════

    // ── Fill form fields from the selected table row ───────
    private void populateFormFromTable() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) return;

        txtPatientId.setText(tableModel.getValueAt(selectedRow, 0).toString());
        txtName     .setText(tableModel.getValueAt(selectedRow, 1).toString());
        txtAge      .setText(tableModel.getValueAt(selectedRow, 2).toString());
        txtPhone    .setText(tableModel.getValueAt(selectedRow, 3).toString());
    }

    // ── Validate form before DB operations ────────────────
    private boolean validateForm() {

        if (txtName.getText().trim().isEmpty()) {
            showError("Patient name cannot be empty."); return false;
        }
        if (txtAge.getText().trim().isEmpty()) {
            showError("Age cannot be empty."); return false;
        }
        try {
            int age = Integer.parseInt(txtAge.getText().trim());
            if (age <= 0 || age > 150) {
                showError("Please enter a realistic age between 1 and 150."); return false;
            }
        } catch (NumberFormatException e) {
            showError("Age must be a whole number (e.g. 35)."); return false;
        }
        if (txtPhone.getText().trim().isEmpty()) {
            showError("Phone number cannot be empty."); return false;
        }
        if (txtPhone.getText().trim().length() < 7) {
            showError("Please enter a valid phone number."); return false;
        }
        return true;
    }

    // ── Clear all form fields and deselect table row ───────
    private void clearForm() {
        txtPatientId.setText("");
        txtName     .setText("");
        txtAge      .setText("");
        txtPhone    .setText("");
        patientTable.clearSelection();
        txtName.requestFocus();
    }

    // ── Refresh Dashboard summary cards ───────────────────
    private void refreshDashboard() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof Dashboard) {
            ((Dashboard) window).loadDashboardStats();
        }
    }

    // ═══════════════════════════════════════════════════════
    //  TABLE STYLING  (with L&F-proof header renderer)
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

        // ── Column widths ─────────────────────────────────
        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // Patient ID
        table.getColumnModel().getColumn(1).setPreferredWidth(200);  // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(60);   // Age
        table.getColumnModel().getColumn(3).setPreferredWidth(140);  // Phone

        // ── L&F-proof header renderer ─────────────────────
        // Uses setOpaque(false) on the header + a custom cell renderer
        // so the system L&F cannot override our colors (fixes the
        // "text invisible until hover" bug from Stage 4).
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
        header.setOpaque(false);   // stops L&F painting over our renderer

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {

                super.getTableCellRendererComponent(t, val, sel, foc, row, col);

                setText(val == null ? "" : val.toString());
                setFont(new Font("Segoe UI", Font.BOLD, 13));

                // Force our colors — L&F cannot override inside this renderer
                setOpaque(true);
                setBackground(HEADER_BG);   // dark navy
                setForeground(Color.WHITE); // always visible

                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(80, 100, 120)),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)
                ));
                return this;
            }
        });

        // ── Alternate row shading renderer ────────────────
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {

                super.getTableCellRendererComponent(t, val, sel, foc, row, col);

                // Center ID and Age columns; left-align text columns
                setHorizontalAlignment(
                    (col == 0 || col == 2) ? SwingConstants.CENTER : SwingConstants.LEFT
                );
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 246, 250));
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
        tf.setPreferredSize(new Dimension(150, 30));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        return tf;
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
