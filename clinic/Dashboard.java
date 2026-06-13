package clinic;

// ============================================================
//  Dashboard.java
//  Purpose : Main application window shown after login.
//            Contains:
//              • Static left sidebar with navigation buttons
//              • Top area with 4 summary stat cards
//              • Recent appointments JTable
//              • Right-side content panel (swaps modules in)
//  Location: src/clinic/Dashboard.java
// ============================================================

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Dashboard extends JFrame {

    // ── Layout panels ──────────────────────────────────────
    private JPanel  sidebarPanel;      // left navigation
    private JPanel  contentPanel;      // right area (swaps between modules)
    private JPanel  dashboardHome;     // the default home view

    // ── Summary card labels (updated from DB) ──────────────
    private JLabel  lblTotalDoctors;
    private JLabel  lblTotalPatients;
    private JLabel  lblTotalAppts;
    private JLabel  lblPendingAppts;

    // ── Recent appointments table ──────────────────────────
    private JTable            recentTable;
    private DefaultTableModel recentModel;

    // ── Who is logged in ──────────────────────────────────
    private String loggedInUser;

    // ── Sidebar button currently selected ─────────────────
    private JButton activeButton = null;

    // ── Colors (reused throughout) ─────────────────────────
    private static final Color SIDEBAR_BG      = new Color(31,  45,  61);   // dark navy
    private static final Color SIDEBAR_HOVER   = new Color(44,  62,  80);   // slightly lighter
    private static final Color SIDEBAR_ACTIVE  = new Color(39, 174,  96);   // green
    private static final Color TOPBAR_BG       = new Color(44,  62,  80);
    private static final Color CONTENT_BG      = new Color(245, 246, 250);  // very light grey

    // ── Constructor ────────────────────────────────────────
    public Dashboard(String username) {
        this.loggedInUser = username;
        initComponents();
        setWindowProperties();
        loadDashboardStats();   // pull numbers from DB on open
    }

    // ═══════════════════════════════════════════════════════
    //  BUILD THE FULL UI
    // ═══════════════════════════════════════════════════════
    private void initComponents() {

        setLayout(new BorderLayout(0, 0));

        // ── 1. Top bar ─────────────────────────────────────
        add(buildTopBar(), BorderLayout.NORTH);

        // ── 2. Sidebar ────────────────────────────────────
        sidebarPanel = buildSidebar();
        add(sidebarPanel, BorderLayout.WEST);

        // ── 3. Content panel (CardLayout swaps views) ─────
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CONTENT_BG);
        add(contentPanel, BorderLayout.CENTER);

        // Build the home dashboard view and show it by default
        dashboardHome = buildDashboardHome();
        contentPanel.add(dashboardHome, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════
    //  TOP BAR
    // ═══════════════════════════════════════════════════════
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TOPBAR_BG);
        bar.setPreferredSize(new Dimension(0, 55));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // App title on the left
        JLabel lblTitle = new JLabel("🏥  Clinic Management System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        bar.add(lblTitle, BorderLayout.WEST);

        // User info + logout on the right
        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBar.setOpaque(false);

        JLabel lblUser = new JLabel("👤  " + loggedInUser);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(new Color(174, 214, 241));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setPreferredSize(new Dimension(85, 32));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());

        rightBar.add(lblUser);
        rightBar.add(btnLogout);
        bar.add(rightBar, BorderLayout.EAST);

        return bar;
    }

    // ═══════════════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        // ── Navigation menu items ─────────────────────────
        // Each item: [icon, label, action]
        String[][] menuItems = {
            {"🏠", "Dashboard",    "dashboard"},
            {"👨‍⚕️", "Doctors",      "doctors"},
            {"🧑", "Patients",     "patients"},
            {"📅", "Appointments", "appointments"},
            {"💳", "Billing",      "billing"}
        };

        for (String[] item : menuItems) {
            JButton btn = buildSidebarButton(item[0] + "  " + item[1], item[2]);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(4));  // small gap between buttons

            // Mark Dashboard button as active by default
            if (item[2].equals("dashboard")) {
                setActiveButton(btn);
            }
        }

        // Push everything to the top
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    // ── Build one sidebar button ───────────────────────────
    private JButton buildSidebarButton(String label, String action) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(new Color(189, 195, 199));
        btn.setBackground(SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Full width inside sidebar; fixed height for consistent look
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setPreferredSize(new Dimension(210, 46));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 10));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(SIDEBAR_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(SIDEBAR_BG);
            }
        });

        // Click — swap the content panel to the right module
        btn.addActionListener(e -> {
            setActiveButton(btn);
            navigateTo(action);
        });

        return btn;
    }

    // ── Highlight the selected sidebar button ──────────────
    private void setActiveButton(JButton btn) {
        if (activeButton != null) {
            activeButton.setBackground(SIDEBAR_BG);
            activeButton.setForeground(new Color(189, 195, 199));
        }
        activeButton = btn;
        activeButton.setBackground(SIDEBAR_ACTIVE);
        activeButton.setForeground(Color.WHITE);
        activeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    // ── Swap the content panel to the chosen module ────────
    private void navigateTo(String action) {
        contentPanel.removeAll();   // clear current content

        switch (action) {
            case "dashboard":
                dashboardHome = buildDashboardHome();
                contentPanel.add(dashboardHome);
                loadDashboardStats();        // always refresh on revisit
                break;
            case "doctors":
                contentPanel.add(new DoctorPanel());
                break;
            case "patients":
                contentPanel.add(new PatientPanel());
                break;
            case "appointments":
                contentPanel.add(new AppointmentPanel());
                break;
            case "billing":
                contentPanel.add(new BillingPanel());
                break;
        }

        contentPanel.revalidate();   // re-layout
        contentPanel.repaint();      // redraw
    }

    // ═══════════════════════════════════════════════════════
    //  DASHBOARD HOME VIEW
    //  (summary cards + recent appointments table)
    // ═══════════════════════════════════════════════════════
    private JPanel buildDashboardHome() {
        JPanel home = new JPanel(new BorderLayout(0, 0));
        home.setBackground(CONTENT_BG);
        home.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ── Page title ────────────────────────────────────
        JLabel lblPage = new JLabel("Dashboard Overview");
        lblPage.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblPage.setForeground(new Color(44, 62, 80));
        lblPage.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        home.add(lblPage, BorderLayout.NORTH);

        // ── Center: cards + table stacked vertically ──────
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // 1. Summary cards row
        centerPanel.add(buildCardsRow());
        centerPanel.add(Box.createVerticalStrut(25));

        // 2. Recent appointments section
        centerPanel.add(buildRecentApptsSection());

        home.add(centerPanel, BorderLayout.CENTER);

        return home;
    }

    // ── 4 summary stat cards ──────────────────────────────
    private JPanel buildCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 15, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Card definitions: [title, icon, color]
        String[][] cards = {
            {"Total Doctors",      "👨‍⚕️", "#2980B9"},   // blue
            {"Total Patients",     "🧑",  "#27AE60"},   // green
            {"Total Appointments", "📅",  "#8E44AD"},   // purple
            {"Pending Appts",      "⏳",  "#E67E22"},   // orange
        };

        Color[] colors = {
            new Color(41, 128, 185),
            new Color(39, 174,  96),
            new Color(142, 68, 173),
            new Color(230, 126,  34)
        };

        // Keep references so loadDashboardStats() can update the numbers
        JLabel[] numberLabels = new JLabel[4];

        for (int i = 0; i < cards.length; i++) {
            JPanel card = buildStatCard(cards[i][0], cards[i][1], colors[i]);
            // The number label is the second component added inside the card
            // We store references for later DB update
            numberLabels[i] = findNumberLabel(card);
            row.add(card);
        }

        // Store references at class level for use by loadDashboardStats()
        lblTotalDoctors  = numberLabels[0];
        lblTotalPatients = numberLabels[1];
        lblTotalAppts    = numberLabels[2];
        lblPendingAppts  = numberLabels[3];

        return row;
    }

    // ── Build a single stat card ──────────────────────────
    private JPanel buildStatCard(String title, String icon, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Coloured top accent bar
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accent);
        accentBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        accentBar.setPreferredSize(new Dimension(0, 5));

        // Icon
        JLabel lblIcon = new JLabel(icon, SwingConstants.LEFT);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        lblIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Number (starts at 0, updated from DB)
        JLabel lblNumber = new JLabel("0");
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 34));
        lblNumber.setForeground(accent);
        lblNumber.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Title
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(new Color(127, 140, 141));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(accentBar);
        card.add(Box.createVerticalStrut(12));
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(6));
        card.add(lblNumber);   // ← index 3 component (found by findNumberLabel)
        card.add(Box.createVerticalStrut(4));
        card.add(lblTitle);

        return card;
    }

    // ── Find the number JLabel inside a stat card ─────────
    // The number label is the first JLabel with a large bold font
    private JLabel findNumberLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                if (lbl.getFont().getSize() >= 30) return lbl;
            }
        }
        return new JLabel(); // fallback (should never happen)
    }

    // ── Recent appointments section ───────────────────────
    private JPanel buildRecentApptsSection() {
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setOpaque(false);

        // Section header row
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblSectionTitle = new JLabel("Recent Appointments");
        lblSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSectionTitle.setForeground(new Color(44, 62, 80));
        header.add(lblSectionTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("⟳  Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setPreferredSize(new Dimension(100, 30));
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadDashboardStats());
        header.add(btnRefresh, BorderLayout.EAST);

        section.add(header, BorderLayout.NORTH);

        // ── Table columns ─────────────────────────────────
        String[] cols = {"Appt ID", "Patient Name", "Doctor Name",
                         "Date", "Time", "Status"};
        recentModel = new DefaultTableModel(cols, 0) {
            // Make table cells non-editable (read-only display)
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        recentTable = new JTable(recentModel);
        styleTable(recentTable);

        JScrollPane scroll = new JScrollPane(recentTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scroll.setBackground(Color.WHITE);
        section.add(scroll, BorderLayout.CENTER);

        return section;
    }

    // ── Style the JTable uniformly ─────────────────────────
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(52, 152, 219, 80));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        // ── FIXED: L&F-proof header renderer added ─────────────────────
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
                setBackground(TOPBAR_BG);  // Dark Blue Color
                setForeground(Color.WHITE);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(80, 100, 120)),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)
                ));
                return this;
            }
        });

        // Alternate row shading renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 246, 250));
                }

                // Colour-code Status column (last column)
                if (col == t.getColumnCount() - 1 && val != null) {
                    switch (val.toString()) {
                        case "Pending":
                            setForeground(new Color(230, 126, 34));   break;
                        case "Completed":
                            setForeground(new Color(39, 174, 96));    break;
                        case "Cancelled":
                            setForeground(new Color(192, 57, 43));    break;
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
    //  LOAD DASHBOARD STATISTICS FROM DATABASE
    //  Called on open and on every Refresh click
    // ═══════════════════════════════════════════════════════
    public void loadDashboardStats() {
        Connection conn = DBConnect.getConnection();
        if (conn == null) return;

        try {
            // ── COUNT queries ──────────────────────────────
            lblTotalDoctors .setText(queryCount(conn, "SELECT COUNT(*) FROM doctors"));
            lblTotalPatients.setText(queryCount(conn, "SELECT COUNT(*) FROM patients"));
            lblTotalAppts   .setText(queryCount(conn, "SELECT COUNT(*) FROM appointments"));
            lblPendingAppts .setText(queryCount(conn,
                "SELECT COUNT(*) FROM appointments WHERE status = 'Pending'"));

            // ── Recent appointments (last 10) ──────────────
            String sql =
                "SELECT a.appt_id, p.name AS patient, d.name AS doctor, " +
                "       a.appt_date, a.appt_time, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors  d ON a.doc_id     = d.doc_id " +
                "ORDER BY a.appt_id DESC " +
                "LIMIT 10";

            recentModel.setRowCount(0);   // clear old rows

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    recentModel.addRow(new Object[]{
                        rs.getInt   ("appt_id"),
                        rs.getString("patient"),
                        rs.getString("doctor"),
                        rs.getString("appt_date"),
                        rs.getString("appt_time"),
                        rs.getString("status")
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Helper: run a COUNT query and return result as String
    private String queryCount(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? String.valueOf(rs.getInt(1)) : "0";
        }
    }

    // ═══════════════════════════════════════════════════════
    //  LOGOUT
    // ═══════════════════════════════════════════════════════
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            DBConnect.closeConnection();   // close DB connection cleanly
            new Login().setVisible(true);  // show login window again
            this.dispose();                // destroy dashboard
        }
    }

    // ═══════════════════════════════════════════════════════
    //  WINDOW PROPERTIES
    // ═══════════════════════════════════════════════════════
    private void setWindowProperties() {
        setTitle("Clinic Management System — Dashboard");
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 580));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    // ═══════════════════════════════════════════════════════
    //  TEMPORARY MODULE STUBS
    //  These inner classes keep Dashboard.java self-contained
    //  and will be replaced by full modules in Stages 4-7.
    // ═══════════════════════════════════════════════════════
    // DoctorPanel stub removed — replaced by src/clinic/DoctorPanel.java (Stage 4)

    // PatientPanel stub removed — replaced by src/clinic/PatientPanel.java (Stage 5)

    // AppointmentPanel stub removed — replaced by src/clinic/AppointmentPanel.java (Stage 6)

    // BillingPanel stub removed — replaced by src/clinic/BillingPanel.java (Stage 7)
}