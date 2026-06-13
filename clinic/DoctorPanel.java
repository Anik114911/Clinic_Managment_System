package clinic;

// ============================================================
//  DoctorPanel.java
//  Purpose : Full Doctor Management module.
//            Features:
//              • Add Doctor
//              • Update Doctor
//              • Delete Doctor
//              • Search Doctor (by name or specialization)
//              • View All Doctors in JTable
//              • Auto-populate form when a table row is clicked
//  Location: src/clinic/DoctorPanel.java
// ============================================================

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DoctorPanel extends JPanel {

    // ── Form input fields ──────────────────────────────────
    private JTextField txtDocId;          // auto-filled, not editable
    private JTextField txtName;
    private JTextField txtSpecialization;
    private JTextField txtPhone;
    private JTextField txtVisitFee;
    private JTextField txtSearch;

    // ── Action buttons ─────────────────────────────────────
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnSearch;
    private JButton btnShowAll;

    // ── Table components ───────────────────────────────────
    private JTable            doctorTable;
    private DefaultTableModel tableModel;

    // ── Colors ─────────────────────────────────────────────
    private static final Color PANEL_BG    = new Color(245, 246, 250);
    private static final Color HEADER_BG   = new Color(44,  62,  80);
    private static final Color BTN_ADD     = new Color(39,  174,  96);
    private static final Color BTN_UPDATE  = new Color(41,  128, 185);
    private static final Color BTN_DELETE  = new Color(192,  57,  43);
    private static final Color BTN_CLEAR   = new Color(127, 140, 141);
    private static final Color BTN_SEARCH  = new Color(142,  68, 173);
    private static final Color BTN_SHOWALL = new Color(52,  152, 219);

    // ── Constructor ────────────────────────────────────────
    public DoctorPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeaderPanel(),  BorderLayout.NORTH);
        add(buildMainContent(),  BorderLayout.CENTER);

        loadAllDoctors();   // populate table on load
    }

    // ═══════════════════════════════════════════════════════
    //  HEADER — page title bar
    // ═══════════════════════════════════════════════════════
    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("👨‍⚕️   Doctor Management");
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
        JLabel lblForm = new JLabel("Doctor Details");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblForm.setForeground(HEADER_BG);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        card.add(lblForm, gc);

        // ── Separator ─────────────────────────────────────
        gc.gridy = 1;
        card.add(new JSeparator(), gc);

        // ── Reset width for label+field rows ──────────────
        gc.gridwidth = 1;

        // ── Doctor ID (read-only, auto-filled) ────────────
        gc.gridx = 0; gc.gridy = 2;
        card.add(makeLabel("Doctor ID:"), gc);
        txtDocId = makeTextField();
        txtDocId.setEditable(false);               // auto-generated by DB
        txtDocId.setBackground(new Color(236, 240, 241));
        txtDocId.setToolTipText("Auto-generated by database");
        gc.gridx = 1;
        card.add(txtDocId, gc);

        // ── Name ──────────────────────────────────────────
        gc.gridx = 0; gc.gridy = 3;
        card.add(makeLabel("Name:"), gc);
        txtName = makeTextField();
        gc.gridx = 1;
        card.add(txtName, gc);

        // ── Specialization ────────────────────────────────
        gc.gridx = 0; gc.gridy = 4;
        card.add(makeLabel("Specialization:"), gc);
        txtSpecialization = makeTextField();
        gc.gridx = 1;
        card.add(txtSpecialization, gc);

        // ── Phone ─────────────────────────────────────────
        gc.gridx = 0; gc.gridy = 5;
        card.add(makeLabel("Phone:"), gc);
        txtPhone = makeTextField();
        gc.gridx = 1;
        card.add(txtPhone, gc);

        // ── Visit Fee ─────────────────────────────────────
        gc.gridx = 0; gc.gridy = 6;
        card.add(makeLabel("Visit Fee (৳):"), gc);
        txtVisitFee = makeTextField();
        gc.gridx = 1;
        card.add(txtVisitFee, gc);

        // ── Button grid (2 × 2 + 1 full-width) ───────────
        JPanel btnGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        btnGrid.setOpaque(false);

        btnAdd     = makeButton("➕  Add",      BTN_ADD);
        btnUpdate  = makeButton("✏  Update",   BTN_UPDATE);
        btnDelete  = makeButton("🗑  Delete",   BTN_DELETE);
        btnClear   = makeButton("✖  Clear",    BTN_CLEAR);

        btnGrid.add(btnAdd);
        btnGrid.add(btnUpdate);
        btnGrid.add(btnDelete);
        btnGrid.add(btnClear);

        gc.gridx = 0; gc.gridy = 7; gc.gridwidth = 2;
        gc.insets = new Insets(14, 4, 4, 4);
        card.add(btnGrid, gc);

        // ── Wire button actions ───────────────────────────
        btnAdd   .addActionListener(e -> addDoctor());
        btnUpdate.addActionListener(e -> updateDoctor());
        btnDelete.addActionListener(e -> deleteDoctor());
        btnClear .addActionListener(e -> clearForm());

        return card;
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
        txtSearch.setToolTipText("Search by name or specialization");
        // Live search: fires on every keystroke
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchDoctor(); }
        });

        btnSearch  = makeButton("🔍  Search",   BTN_SEARCH);
        btnShowAll = makeButton("📋  Show All", BTN_SHOWALL);
        btnSearch .setPreferredSize(new Dimension(115, 32));
        btnShowAll.setPreferredSize(new Dimension(115, 32));

        btnSearch .addActionListener(e -> searchDoctor());
        btnShowAll.addActionListener(e -> {
            txtSearch.setText("");
            loadAllDoctors();
        });

        searchBar.add(lblSearch);
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnShowAll);
        panel.add(searchBar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────
        String[] columns = {"Doc ID", "Name", "Specialization", "Phone", "Visit Fee (৳)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        doctorTable = new JTable(tableModel);
        styleTable(doctorTable);

        // ── Row click → populate form ──────────────────────
        // When the user clicks a row, all form fields fill automatically
        doctorTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                populateFormFromTable();
            }
        });

        JScrollPane scroll = new JScrollPane(doctorTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ═══════════════════════════════════════════════════════
    //  DATABASE OPERATIONS
    // ═══════════════════════════════════════════════════════

    // ── Load all doctors into the table ───────────────────
    private void loadAllDoctors() {
        tableModel.setRowCount(0);   // clear existing rows

        String sql = "SELECT doc_id, name, specialization, phone, visit_fee FROM doctors ORDER BY doc_id";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt   ("doc_id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("phone"),
                    String.format("৳ %.2f", rs.getDouble("visit_fee"))
                });
            }

        } catch (SQLException e) {
            showError("Failed to load doctors: " + e.getMessage());
        }
    }

    // ── ADD a new doctor ──────────────────────────────────
    private void addDoctor() {

        // Validate all fields before touching the DB
        if (!validateForm()) return;

        String sql = "INSERT INTO doctors (name, specialization, phone, visit_fee) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtSpecialization.getText().trim());
            ps.setString(3, txtPhone.getText().trim());
            ps.setDouble(4, Double.parseDouble(txtVisitFee.getText().trim()));

            int rows = ps.executeUpdate();   // returns number of rows affected

            if (rows > 0) {
                showSuccess("Doctor added successfully!");
                clearForm();
                loadAllDoctors();
                refreshDashboard();
            }

        } catch (NumberFormatException e) {
            showError("Visit Fee must be a valid number (e.g. 500 or 750.50).");
        } catch (SQLException e) {
            showError("Failed to add doctor: " + e.getMessage());
        }
    }

    // ── UPDATE an existing doctor ─────────────────────────
    private void updateDoctor() {

        // Doc ID must be filled (user must have selected a row first)
        if (txtDocId.getText().trim().isEmpty()) {
            showError("Please select a doctor from the table first.");
            return;
        }
        if (!validateForm()) return;

        String sql = "UPDATE doctors SET name=?, specialization=?, phone=?, visit_fee=? WHERE doc_id=?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtSpecialization.getText().trim());
            ps.setString(3, txtPhone.getText().trim());
            ps.setDouble(4, Double.parseDouble(txtVisitFee.getText().trim()));
            ps.setInt   (5, Integer.parseInt(txtDocId.getText().trim()));

            int rows = ps.executeUpdate();

            if (rows > 0) {
                showSuccess("Doctor updated successfully!");
                clearForm();
                loadAllDoctors();
                refreshDashboard();
            }

        } catch (NumberFormatException e) {
            showError("Visit Fee must be a valid number.");
        } catch (SQLException e) {
            showError("Failed to update doctor: " + e.getMessage());
        }
    }

    // ── DELETE a doctor ───────────────────────────────────
    private void deleteDoctor() {

        if (txtDocId.getText().trim().isEmpty()) {
            showError("Please select a doctor from the table first.");
            return;
        }

        // Confirm before deleting — good UX practice
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete Dr. " + txtName.getText() + "?\n"
            + "This will also delete all their appointments.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM doctors WHERE doc_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(txtDocId.getText().trim()));
            int rows = ps.executeUpdate();

            if (rows > 0) {
                showSuccess("Doctor deleted successfully.");
                clearForm();
                loadAllDoctors();
                refreshDashboard();
            }

        } catch (SQLException e) {
            showError("Failed to delete doctor: " + e.getMessage());
        }
    }

    // ── SEARCH doctors by name or specialization ──────────
    private void searchDoctor() {
        String keyword = txtSearch.getText().trim();

        // If search box is empty, just show everything
        if (keyword.isEmpty()) {
            loadAllDoctors();
            return;
        }

        // LIKE with wildcards — searches both name and specialization columns
        String sql = "SELECT doc_id, name, specialization, phone, visit_fee " +
                     "FROM doctors " +
                     "WHERE name LIKE ? OR specialization LIKE ? " +
                     "ORDER BY name";

        tableModel.setRowCount(0);

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";  // %keyword% = contains
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt   ("doc_id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("phone"),
                    String.format("৳ %.2f", rs.getDouble("visit_fee"))
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

    // ── Populate form fields from the selected table row ──
    private void populateFormFromTable() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow < 0) return;   // no row selected

        // getValueAt(row, column) — column index matches our table columns
        txtDocId         .setText(tableModel.getValueAt(selectedRow, 0).toString());
        txtName          .setText(tableModel.getValueAt(selectedRow, 1).toString());
        txtSpecialization.setText(tableModel.getValueAt(selectedRow, 2).toString());
        txtPhone         .setText(tableModel.getValueAt(selectedRow, 3).toString());

        // Visit fee is stored as "৳ 800.00" — strip currency symbol before editing
        String fee = tableModel.getValueAt(selectedRow, 4).toString();
        txtVisitFee.setText(fee.replace("৳ ", "").trim());
    }

    // ── Validate form inputs before DB operations ─────────
    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            showError("Doctor name cannot be empty."); return false;
        }
        if (txtSpecialization.getText().trim().isEmpty()) {
            showError("Specialization cannot be empty."); return false;
        }
        if (txtPhone.getText().trim().isEmpty()) {
            showError("Phone number cannot be empty."); return false;
        }
        if (txtVisitFee.getText().trim().isEmpty()) {
            showError("Visit fee cannot be empty."); return false;
        }
        try {
            double fee = Double.parseDouble(txtVisitFee.getText().trim());
            if (fee < 0) { showError("Visit fee cannot be negative."); return false; }
        } catch (NumberFormatException e) {
            showError("Visit Fee must be a number (e.g. 500 or 750.50)."); return false;
        }
        return true;
    }

    // ── Clear all form fields ─────────────────────────────
    private void clearForm() {
        txtDocId         .setText("");
        txtName          .setText("");
        txtSpecialization.setText("");
        txtPhone         .setText("");
        txtVisitFee      .setText("");
        doctorTable.clearSelection();   // deselect any highlighted row
        txtName.requestFocus();
    }

    // ── Tell the Dashboard to refresh its stat cards ──────
    // Walks up the Swing hierarchy to find Dashboard and calls refresh
    private void refreshDashboard() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof Dashboard) {
            ((Dashboard) window).loadDashboardStats();
        }
    }

    // ═══════════════════════════════════════════════════════
    //  UI UTILITY HELPERS
    // ═══════════════════════════════════════════════════════

    // ── Uniform label factory ─────────────────────────────
    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(60, 60, 60));
        return lbl;
    }

    // ── Uniform text field factory ────────────────────────
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

    // ── Uniform button factory ────────────────────────────
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

    // ── Style JTable uniformly ────────────────────────────
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
        table.getColumnModel().getColumn(0).setPreferredWidth(60);   // Doc ID
        table.getColumnModel().getColumn(1).setPreferredWidth(160);  // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(140);  // Specialization
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // Phone
        table.getColumnModel().getColumn(4).setPreferredWidth(110);  // Visit Fee

        // ── Header renderer ───────────────────────────────
        // The system Look-and-Feel can override setBackground/setForeground
        // on JTableHeader, causing text to be invisible until hovered.
        // The fix: install a custom DefaultTableCellRenderer directly on
        // the header so WE control every pixel — L&F cannot interfere.
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        // setOpaque(false) on the header stops the L&F from painting
        // its own background on top of our renderer's background.
        header.setOpaque(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {

                // Start from a plain JLabel (super call resets state cleanly)
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);

                setText(val == null ? "" : val.toString());
                setFont(new Font("Segoe UI", Font.BOLD, 13));

                // These three lines are the core fix:
                setOpaque(true);                             // must be true to paint bg
                setBackground(HEADER_BG);                   // dark navy — always visible
                setForeground(Color.WHITE);                  // white text — always visible

                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createCompoundBorder(
                    // Right-side divider line between columns
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(80, 100, 120)),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)
                ));
                return this;
            }
        });

        // Alternate row shading
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(col == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 246, 250));
                    setForeground(Color.BLACK);
                }
                return this;
            }
        });
    }

    // ── Show success dialog ───────────────────────────────
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Show error dialog ─────────────────────────────────
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}