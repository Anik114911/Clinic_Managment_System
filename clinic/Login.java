package clinic;

// ============================================================
//  Login.java  (Refined UI — v3)
//  Fixes applied:
//    1. setResizable(true)  — window can now be maximized
//    2. setSize(1000, 600)  — larger default starting size
//    3. Left panel title    — no wrapping, proper centering
//    4. Login button        — text centered vertically
//    5. Direct Redirect     — Removed success popup
//    6. Eye Icon            — Added Show/Hide password toggle
//  Location: src/clinic/Login.java
// ============================================================

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JFrame {

    // ── UI Components ──────────────────────────────────────
    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JButton        btnClear;
    private JLabel         lblStatus;

    // ── Constructor ────────────────────────────────────────
    public Login() {
        initComponents();
        setWindowProperties();
    }

    // ── Build the entire UI manually ───────────────────────
    private void initComponents() {

        setLayout(new BorderLayout());

        // ══════════════════════════════════════════════════
        //  LEFT PANEL — dark navy decorative side panel
        // ══════════════════════════════════════════════════
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(31, 45, 61));        // deep navy
        // FIX 3: Give left panel a generous fixed width so
        //        text never has to wrap at the default size.
        leftPanel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints lGbc = new GridBagConstraints();
        lGbc.gridx      = 0;
        lGbc.gridy      = GridBagConstraints.RELATIVE;  // stack items vertically
        lGbc.fill       = GridBagConstraints.HORIZONTAL;
        lGbc.insets     = new Insets(8, 20, 8, 20);     // side padding prevents wrapping
        lGbc.anchor     = GridBagConstraints.CENTER;

        // ── Icon label ────────────────────────────────────
        JLabel lblIcon = new JLabel("🏥", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        lblIcon.setForeground(Color.WHITE);
        leftPanel.add(lblIcon, lGbc);

        // FIX 3: Each word on its OWN JLabel — no HTML, no wrapping.
        //        All labels share the same GBC so they stack cleanly.
        String[] titleWords = {"Clinic", "Management", "System"};
        for (String word : titleWords) {
            JLabel lbl = new JLabel(word, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lbl.setForeground(Color.WHITE);
            leftPanel.add(lbl, lGbc);
        }

        // ── Thin white divider line below title ───────────
        JSeparator divider = new JSeparator(SwingConstants.HORIZONTAL);
        divider.setForeground(new Color(255, 255, 255, 60));  // semi-transparent white
        divider.setBackground(new Color(255, 255, 255, 60));
        GridBagConstraints divGbc = new GridBagConstraints();
        divGbc.gridx  = 0;
        divGbc.fill   = GridBagConstraints.HORIZONTAL;
        divGbc.insets = new Insets(4, 30, 12, 30);
        leftPanel.add(divider, divGbc);

        // ── Subtitle ──────────────────────────────────────
        JLabel lblSub = new JLabel("Admin Portal", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSub.setForeground(new Color(174, 214, 241));        // light blue
        leftPanel.add(lblSub, lGbc);

        add(leftPanel, BorderLayout.WEST);

        // ══════════════════════════════════════════════════
        //  RIGHT PANEL — light grey background
        // ══════════════════════════════════════════════════
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(236, 240, 241));
        add(rightPanel, BorderLayout.CENTER);

        // ══════════════════════════════════════════════════
        //  FORM CARD — white rounded card in the center
        // ══════════════════════════════════════════════════
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)   // generous internal padding
        ));

        GridBagConstraints fc = new GridBagConstraints();
        fc.insets = new Insets(10, 12, 10, 12);
        fc.fill   = GridBagConstraints.HORIZONTAL;

        // ── Card title ────────────────────────────────────
        JLabel lblTitle = new JLabel("Admin Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(31, 45, 61));
        fc.gridx = 0; fc.gridy = 0; fc.gridwidth = 2;
        formCard.add(lblTitle, fc);

        // ── Divider ───────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(189, 195, 199));
        fc.gridy = 1;
        formCard.add(sep, fc);

        // ── Username row ──────────────────────────────────
        fc.gridwidth = 1;
        fc.anchor    = GridBagConstraints.EAST;

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        fc.gridx = 0; fc.gridy = 2;
        formCard.add(lblUser, fc);

        txtUsername = new JTextField(20);
        styleTextField(txtUsername);
        fc.gridx = 1; fc.gridy = 2; fc.anchor = GridBagConstraints.WEST;
        formCard.add(txtUsername, fc);

        // ── Password row ──────────────────────────────────
        fc.anchor = GridBagConstraints.EAST;

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        fc.gridx = 0; fc.gridy = 3;
        formCard.add(lblPass, fc);

        // FIX 6: Create a panel to hold both password field and eye button
        JPanel passPanel = new JPanel(new BorderLayout(5, 0));
        passPanel.setBackground(Color.WHITE);

        txtPassword = new JPasswordField(15);
        styleTextField(txtPassword);
        // Adjust width slightly so it aligns with username field when button is added
        txtPassword.setPreferredSize(new Dimension(180, 36)); 
        passPanel.add(txtPassword, BorderLayout.CENTER);

        // The Show/Hide Toggle Button
        JToggleButton btnShowPass = new JToggleButton("👁");
        btnShowPass.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btnShowPass.setBackground(new Color(236, 240, 241));
        btnShowPass.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        btnShowPass.setFocusPainted(false);
        btnShowPass.setToolTipText("Show/Hide Password");
        btnShowPass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnShowPass.setPreferredSize(new Dimension(45, 36));

        // Save the default dot/star character
        char defaultEcho = txtPassword.getEchoChar();

        btnShowPass.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                txtPassword.setEchoChar((char) 0); // Show text
            } else {
                txtPassword.setEchoChar(defaultEcho); // Hide text
            }
        });

        passPanel.add(btnShowPass, BorderLayout.EAST);

        fc.gridx = 1; fc.gridy = 3; fc.anchor = GridBagConstraints.WEST;
        formCard.add(passPanel, fc);

        // ── Status / error label ──────────────────────────
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblStatus.setForeground(new Color(192, 57, 43));       // red
        fc.gridx = 0; fc.gridy = 4; fc.gridwidth = 2;
        fc.anchor = GridBagConstraints.CENTER;
        formCard.add(lblStatus, fc);

        // ── Button row ────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(Color.WHITE);

        btnLogin = buildButton("  Login  ", new Color(39, 174, 96));
        btnClear = buildButton("  Clear  ", new Color(127, 140, 141));

        btnPanel.add(btnLogin);
        btnPanel.add(btnClear);

        fc.gridx = 0; fc.gridy = 5; fc.gridwidth = 2;
        formCard.add(btnPanel, fc);

        // ── Place form card into right panel ──────────────
        GridBagConstraints rGbc = new GridBagConstraints();
        rGbc.gridx = 0; rGbc.gridy = 0;
        rightPanel.add(formCard, rGbc);

        // ── Wire up actions ───────────────────────────────
        btnLogin.addActionListener(e -> performLogin());
        btnClear.addActionListener(e -> clearFields());

        // ENTER key in username → jump to password
        txtUsername.addActionListener(e -> txtPassword.requestFocus());
        // ENTER key in password → attempt login
        txtPassword.addActionListener(e -> performLogin());
    }

    // ── Helper: uniform text-field styling ────────────────
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(230, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
    }

    // ── Helper: build a styled button ─────────────────────
    private JButton buildButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // FIX 4: Set Margin so text has equal vertical space above and below.
        btn.setMargin(new Insets(10, 20, 10, 20));

        // FIX 4: setPreferredSize locks width+height so the layout manager
        //        cannot squash the button vertically.
        btn.setPreferredSize(new Dimension(130, 42));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    // ── Login Logic ────────────────────────────────────────
    private void performLogin() {

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("⚠  Please enter both username and password.");
            return;
        }

        // PreparedStatement — safe against SQL Injection
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (
            Connection conn = DBConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // ── SUCCESS ───────────────────────────────
                // FIX 5: Removed JOptionPane popup. Now redirects directly.
                new Dashboard(username).setVisible(true);
                this.dispose();

            } else {
                // ── FAILED ────────────────────────────────
                lblStatus.setText("⚠  Invalid username or password. Try again.");
                txtPassword.setText("");
                txtPassword.requestFocus();
            }

            rs.close();

        } catch (SQLException e) {
            lblStatus.setText("⚠  Database error. Check connection.");
            e.printStackTrace();
        }
    }

    // ── Clear fields ───────────────────────────────────────
    private void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
        lblStatus.setText(" ");
        txtUsername.requestFocus();
    }

    // ── Window properties ──────────────────────────────────
    private void setWindowProperties() {
        setTitle("Clinic Management System — Login");

        // FIX 2: Much larger default size — no cramping at startup
        setSize(1000, 600);

        // FIX 1: Allow the user to maximize the window
        setResizable(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);   // open centered on screen
    }

    // ── Application entry point ────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new Login().setVisible(true);
        });
    }
}