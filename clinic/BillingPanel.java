package clinic;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class BillingPanel extends JPanel {

    private JTextField  txtSearchAppt;
    private JButton     btnFetchAppt;
    private JTextField  txtBillId;
    private JTextField  txtPatientName;
    private JTextField  txtDoctorName;
    private JTextField  txtApptDate;
    private JTextField  txtDoctorFee;
    private JTextField  txtTotalAmount;
    private JComboBox<String> cmbPaymentStatus;

    private int currentCheckoutApptId = 0;

    private JButton btnGenerateBill;
    private JButton btnMarkPaid;
    private JButton btnClearForm;
    private JButton btnRefresh;
    private JButton btnSearch;
    private JButton btnShowAll;

    private JTextField txtSearch;

    private JTable            billTable;
    private DefaultTableModel tableModel;

    private static final Color PANEL_BG      = new Color(245, 246, 250);
    private static final Color HEADER_BG     = new Color(44,  62,  80);
    private static final Color BTN_GENERATE  = new Color(39,  174,  96);
    private static final Color BTN_PAID      = new Color(41,  128, 185);
    private static final Color BTN_CLEAR     = new Color(127, 140, 141);
    private static final Color BTN_REFRESH   = new Color(52,  152, 219);
    private static final Color BTN_SEARCH    = new Color(142,  68, 173);
    private static final Color BTN_SHOWALL   = new Color(52,  152, 219);

    public BillingPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        loadAllBills();
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("💳   Billing Module");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        JLabel lblRevenue = buildRevenueBadge();
        header.add(lblRevenue, BorderLayout.EAST);

        return header;
    }

    private JLabel buildRevenueBadge() {
        JLabel lbl = new JLabel("Loading...");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(174, 214, 241));

        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bills WHERE payment_status='Paid'";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lbl.setText("💰  Total Revenue:  ৳ " +
                    String.format("%,.2f", rs.getDouble(1)));
            }
        } catch (SQLException e) {
            lbl.setText("Revenue: N/A");
        }
        return lbl;
    }

    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(15, 0));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel left = buildCheckoutForm();
        left.setPreferredSize(new Dimension(400, 0)); 
        main.add(left, BorderLayout.WEST);

        main.add(buildTablePanel(), BorderLayout.CENTER);
        return main;
    }

    private JPanel buildCheckoutForm() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(8, 5, 8, 5); 
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.anchor  = GridBagConstraints.WEST;

        JLabel lblSection = new JLabel("Checkout — Generate Bill");
        lblSection.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSection.setForeground(HEADER_BG);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        card.add(lblSection, gc);

        gc.gridy = 1;
        card.add(new JSeparator(), gc);

        gc.gridwidth = 1;

        gc.gridx = 0; gc.gridy = 2;
        card.add(makeLabel("Appt ID / Phone:"), gc);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);

        txtSearchAppt = new JTextField();
        txtSearchAppt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchAppt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        txtSearchAppt.setToolTipText("Enter Appt ID or Phone Number and press Fetch");

        btnFetchAppt = makeButton("Fetch", BTN_REFRESH);
        btnFetchAppt.setPreferredSize(new Dimension(70, 30));

        searchPanel.add(txtSearchAppt, BorderLayout.CENTER);
        searchPanel.add(btnFetchAppt, BorderLayout.EAST);

        gc.gridx = 1;
        card.add(searchPanel, gc);

        btnFetchAppt.addActionListener(e -> autoFillFromAppointment());
        txtSearchAppt.addActionListener(e -> autoFillFromAppointment());

        gc.gridx = 0; gc.gridy = 3;
        card.add(makeLabel("Bill ID:"), gc);
        txtBillId = makeTextField();
        txtBillId.setEditable(false);
        txtBillId.setBackground(new Color(236, 240, 241));
        txtBillId.setToolTipText("Auto-generated by database");
        gc.gridx = 1;
        card.add(txtBillId, gc);

        gc.gridx = 0; gc.gridy = 4;
        card.add(makeLabel("Patient:"), gc);
        txtPatientName = makeTextField();
        txtPatientName.setEditable(false);
        txtPatientName.setBackground(new Color(236, 240, 241));
        gc.gridx = 1;
        card.add(txtPatientName, gc);

        gc.gridx = 0; gc.gridy = 5;
        card.add(makeLabel("Doctor:"), gc);
        txtDoctorName = makeTextField();
        txtDoctorName.setEditable(false);
        txtDoctorName.setBackground(new Color(236, 240, 241));
        gc.gridx = 1;
        card.add(txtDoctorName, gc);

        gc.gridx = 0; gc.gridy = 6;
        card.add(makeLabel("Appt Date:"), gc);
        txtApptDate = makeTextField();
        txtApptDate.setEditable(false);
        txtApptDate.setBackground(new Color(236, 240, 241));
        gc.gridx = 1;
        card.add(txtApptDate, gc);

        gc.gridx = 0; gc.gridy = 7;
        card.add(makeLabel("Doctor Fee (৳):"), gc);
        txtDoctorFee = makeTextField();
        txtDoctorFee.setEditable(false);
        txtDoctorFee.setBackground(new Color(236, 240, 241));
        txtDoctorFee.setToolTipText("Auto-fetched from doctor's profile");
        gc.gridx = 1;
        card.add(txtDoctorFee, gc);

        gc.gridx = 0; gc.gridy = 8;
        card.add(makeLabel("Total Amount (৳):"), gc);
        txtTotalAmount = makeTextField();
        txtTotalAmount.setToolTipText("Editable — adjust if needed");
        txtTotalAmount.setForeground(new Color(39, 174, 96));
        txtTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gc.gridx = 1;
        card.add(txtTotalAmount, gc);

        gc.gridx = 0; gc.gridy = 9;
        card.add(makeLabel("Payment:"), gc);
        cmbPaymentStatus = new JComboBox<>(new String[]{"Unpaid", "Paid"});
        styleComboBox(cmbPaymentStatus);
        gc.gridx = 1;
        card.add(cmbPaymentStatus, gc);

        gc.gridx = 0; gc.gridy = 10; gc.gridwidth = 2;
        JLabel lblBillDate = new JLabel(
            "  📅  Billing Date: " + LocalDate.now().toString());
        lblBillDate.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblBillDate.setForeground(new Color(127, 140, 141));
        card.add(lblBillDate, gc);

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 10, 10)); 
        btnGrid.setOpaque(false);

        btnGenerateBill = makeButton("💳  Generate Bill", BTN_GENERATE);
        btnMarkPaid     = makeButton("✔  Mark Paid",     BTN_PAID);
        btnClearForm    = makeButton("✖  Clear",         BTN_CLEAR);
        btnRefresh      = makeButton("⟳  Refresh",       BTN_REFRESH);

        btnGrid.add(btnGenerateBill);
        btnGrid.add(btnMarkPaid);
        btnGrid.add(btnClearForm);
        btnGrid.add(btnRefresh);

        gc.gridx = 0; gc.gridy = 11; gc.gridwidth = 2;
        gc.insets = new Insets(15, 4, 4, 4); 
        card.add(btnGrid, gc);

        btnGenerateBill.addActionListener(e -> generateBill());
        btnMarkPaid    .addActionListener(e -> markAsPaid());
        btnClearForm   .addActionListener(e -> clearForm());
        btnRefresh     .addActionListener(e -> {
            loadAllBills();
            refreshDashboard();
        });

        return card;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(180, 32));
        txtSearch.setToolTipText("Search by patient name or bill ID");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchBills(); }
        });

        btnSearch  = makeButton("🔍  Search",   BTN_SEARCH);
        btnShowAll = makeButton("📋  Show All", BTN_SHOWALL);
        btnSearch .setPreferredSize(new Dimension(115, 32));
        btnShowAll.setPreferredSize(new Dimension(115, 32));

        btnSearch .addActionListener(e -> searchBills());
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); loadAllBills(); });

        searchBar.add(lblSearch);
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnShowAll);
        panel.add(searchBar, BorderLayout.NORTH);

        String[] cols = {
            "Bill ID", "Appt ID", "Patient",
            "Doctor", "Doctor Fee", "Total (৳)",
            "Payment", "Date"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        billTable = new JTable(tableModel);
        styleTable(billTable);

        billTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                populateBillIdFromTable();
            }
        });

        JScrollPane scroll = new JScrollPane(billTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void autoFillFromAppointment() {
        String keyword = txtSearchAppt.getText().trim();

        if (keyword.isEmpty()) {
            clearReadOnlyFields();
            currentCheckoutApptId = 0;
            return;
        }

        int searchId = -1;
        try {
            searchId = Integer.parseInt(keyword);
        } catch (NumberFormatException e) {
        }

        String sql =
            "SELECT a.appt_id, p.name AS patient, d.name AS doctor, " +
            "       a.appt_date, d.visit_fee " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.patient_id " +
            "JOIN doctors  d ON a.doc_id     = d.doc_id " +
            "LEFT JOIN bills b ON a.appt_id  = b.appt_id " +
            "WHERE (a.appt_id = ? OR p.phone = ?) " +
            "  AND a.status = 'Pending' " +
            "  AND b.bill_id IS NULL " +
            "ORDER BY a.appt_date ASC LIMIT 1";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, searchId);
            ps.setString(2, keyword);
            
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentCheckoutApptId = rs.getInt("appt_id");
                
                double fee = rs.getDouble("visit_fee");
                txtPatientName.setText(rs.getString("patient"));
                txtDoctorName .setText(rs.getString("doctor"));
                txtApptDate   .setText(rs.getString("appt_date"));
                txtDoctorFee  .setText(String.format("%.2f", fee));
                txtTotalAmount.setText(String.format("%.2f", fee));
                
                txtSearchAppt.setText(String.valueOf(currentCheckoutApptId));
            } else {
                showError("No pending unbilled appointment found for this ID or Phone.");
                clearReadOnlyFields();
                currentCheckoutApptId = 0;
            }
            rs.close();

        } catch (SQLException e) {
            showError("Could not fetch appointment details: " + e.getMessage());
            clearReadOnlyFields();
            currentCheckoutApptId = 0;
        }
    }

    private void generateBill() {

        if (currentCheckoutApptId == 0 || txtPatientName.getText().trim().isEmpty()) {
            showError("Please fetch a valid pending appointment to bill.");
            return;
        }
        
        if (txtTotalAmount.getText().trim().isEmpty()) {
            showError("Total amount cannot be empty.");
            return;
        }

        double doctorFee;
        double totalAmount;

        try {
            doctorFee   = Double.parseDouble(txtDoctorFee  .getText().trim());
            totalAmount = Double.parseDouble(txtTotalAmount.getText().trim());
        } catch (NumberFormatException e) {
            showError("Doctor fee and Total amount must be valid numbers.");
            return;
        }

        if (totalAmount < 0) {
            showError("Total amount cannot be negative.");
            return;
        }

        String payStatus = cmbPaymentStatus.getSelectedItem().toString();

        Connection conn = DBConnect.getConnection();

        try {
            conn.setAutoCommit(false);

            String insertSql =
                "INSERT INTO bills " +
                "(appt_id, doctor_fee, total_amount, payment_status, billing_date) " +
                "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt   (1, currentCheckoutApptId);
                ps.setDouble(2, doctorFee);
                ps.setDouble(3, totalAmount);
                ps.setString(4, payStatus);
                ps.setString(5, LocalDate.now().toString());
                ps.executeUpdate();
            }

            String updateSql =
                "UPDATE appointments SET status = 'Completed' WHERE appt_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, currentCheckoutApptId);
                ps.executeUpdate();
            }

            conn.commit();

            showSuccess(
                "Bill generated successfully!\n\n" +
                "Appointment #" + currentCheckoutApptId + " marked as Completed.\n" +
                "Total Charged:  ৳ " + String.format("%,.2f", totalAmount)
            );

            clearForm();
            loadAllBills();
            refreshDashboard();

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            showError("Failed to generate bill: " + e.getMessage());

        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void markAsPaid() {

        int row = billTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a bill from the table first.");
            return;
        }

        int    billId    = (int) tableModel.getValueAt(row, 0);
        String curStatus = tableModel.getValueAt(row, 6).toString();

        if ("Paid".equals(curStatus)) {
            showError("This bill is already marked as Paid.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Mark Bill #" + billId + " as Paid?",
            "Confirm Payment",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE bills SET payment_status = 'Paid' WHERE bill_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, billId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                showSuccess("Bill #" + billId + " has been marked as Paid.");
                loadAllBills();
                refreshDashboard();
            }

        } catch (SQLException e) {
            showError("Failed to update payment status: " + e.getMessage());
        }
    }

    private void loadAllBills() {
        tableModel.setRowCount(0);

        String sql =
            "SELECT b.bill_id, b.appt_id, p.name AS patient, d.name AS doctor, " +
            "       b.doctor_fee, b.total_amount, b.payment_status, b.billing_date " +
            "FROM bills b " +
            "JOIN appointments a ON b.appt_id     = a.appt_id " +
            "JOIN patients     p ON a.patient_id  = p.patient_id " +
            "JOIN doctors      d ON a.doc_id      = d.doc_id " +
            "ORDER BY b.bill_id DESC";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt   ("bill_id"),
                    rs.getInt   ("appt_id"),
                    rs.getString("patient"),
                    rs.getString("doctor"),
                    String.format("৳ %.2f", rs.getDouble("doctor_fee")),
                    String.format("৳ %.2f", rs.getDouble("total_amount")),
                    rs.getString("payment_status"),
                    rs.getString("billing_date")
                });
            }

        } catch (SQLException e) {
            showError("Failed to load bills: " + e.getMessage());
        }
    }

    private void searchBills() {
        String keyword = txtSearch.getText().trim();

        if (keyword.isEmpty()) {
            loadAllBills();
            return;
        }

        String sql =
            "SELECT b.bill_id, b.appt_id, p.name AS patient, d.name AS doctor, " +
            "       b.doctor_fee, b.total_amount, b.payment_status, b.billing_date " +
            "FROM bills b " +
            "JOIN appointments a ON b.appt_id    = a.appt_id " +
            "JOIN patients     p ON a.patient_id = p.patient_id " +
            "JOIN doctors      d ON a.doc_id     = d.doc_id " +
            "WHERE p.name LIKE ? OR CAST(b.bill_id AS CHAR) LIKE ? " +
            "ORDER BY b.bill_id DESC";

        tableModel.setRowCount(0);

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt   ("bill_id"),
                    rs.getInt   ("appt_id"),
                    rs.getString("patient"),
                    rs.getString("doctor"),
                    String.format("৳ %.2f", rs.getDouble("doctor_fee")),
                    String.format("৳ %.2f", rs.getDouble("total_amount")),
                    rs.getString("payment_status"),
                    rs.getString("billing_date")
                });
            }
            rs.close();

        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private void populateBillIdFromTable() {
        int row = billTable.getSelectedRow();
        if (row < 0) return;
        txtBillId.setText(tableModel.getValueAt(row, 0).toString());
    }

    private void clearForm() {
        txtSearchAppt   .setText("");
        txtBillId       .setText("");
        txtPaymentStatus();
        clearReadOnlyFields();
        currentCheckoutApptId = 0;
        billTable.clearSelection();
    }

    private void txtPaymentStatus() {
        cmbPaymentStatus.setSelectedIndex(0);
    }

    private void clearReadOnlyFields() {
        txtPatientName.setText("");
        txtDoctorName .setText("");
        txtApptDate   .setText("");
        txtDoctorFee  .setText("");
        txtTotalAmount.setText("");
    }

    private void refreshDashboard() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof Dashboard) {
            ((Dashboard) window).loadDashboardStats();
        }
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(41, 128, 185, 80));
        table.setSelectionForeground(Color.BLACK);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);

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

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);

                setHorizontalAlignment(
                    (col == 0 || col == 1) ? SwingConstants.CENTER : SwingConstants.LEFT
                );
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 246, 250));
                }

                if (col == 6 && val != null) {
                    switch (val.toString()) {
                        case "Paid":
                            setForeground(new Color(39,  174,  96)); break;
                        case "Unpaid":
                            setForeground(new Color(192,  57,  43)); break;
                        default:
                            setForeground(Color.BLACK);
                    }
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setForeground(Color.BLACK);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                return this;
            }
        });
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(60, 60, 60));
        return lbl;
    }

    private JTextField makeTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(200, 30));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        return tf;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(200, 30));
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