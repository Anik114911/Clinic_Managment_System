package clinic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JFrame {

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JButton        btnClear;
    private JLabel         lblStatus;

    public Login() {
        initComponents();
        setWindowProperties();
    }

    private void initComponents() {

        setLayout(new BorderLayout());
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(31, 45, 61));
        leftPanel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints lGbc = new GridBagConstraints();
        lGbc.gridx      = 0;
        lGbc.gridy      = GridBagConstraints.RELATIVE;
        lGbc.fill       = GridBagConstraints.HORIZONTAL;
        lGbc.insets     = new Insets(8, 20, 8, 20);
        lGbc.anchor     = GridBagConstraints.CENTER;

        JLabel lblIcon = new JLabel("🏥", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        lblIcon.setForeground(Color.WHITE);
        leftPanel.add(lblIcon, lGbc);

        String[] titleWords = {"Clinic", "Management", "System"};
        for (String word : titleWords) {
            JLabel lbl = new JLabel(word, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lbl.setForeground(Color.WHITE);
            leftPanel.add(lbl, lGbc);
        }

        JSeparator divider = new JSeparator(SwingConstants.HORIZONTAL);
        divider.setForeground(new Color(255, 255, 255, 60));  // semi-transparent white
        divider.setBackground(new Color(255, 255, 255, 60));
        GridBagConstraints divGbc = new GridBagConstraints();
        divGbc.gridx  = 0;
        divGbc.fill   = GridBagConstraints.HORIZONTAL;
        divGbc.insets = new Insets(4, 30, 12, 30);
        leftPanel.add(divider, divGbc);

        JLabel lblSub = new JLabel("Admin Portal", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSub.setForeground(new Color(174, 214, 241));
        leftPanel.add(lblSub, lGbc);

        add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(236, 240, 241));
        add(rightPanel, BorderLayout.CENTER);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));

        GridBagConstraints fc = new GridBagConstraints();
        fc.insets = new Insets(10, 12, 10, 12);
        fc.fill   = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Admin Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(31, 45, 61));
        fc.gridx = 0; fc.gridy = 0; fc.gridwidth = 2;
        formCard.add(lblTitle, fc);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(189, 195, 199));
        fc.gridy = 1;
        formCard.add(sep, fc);

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

        fc.anchor = GridBagConstraints.EAST;

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        fc.gridx = 0; fc.gridy = 3;
        formCard.add(lblPass, fc);

        JPanel passPanel = new JPanel(new BorderLayout(5, 0));
        passPanel.setBackground(Color.WHITE);

        txtPassword = new JPasswordField(15);
        styleTextField(txtPassword);
        txtPassword.setPreferredSize(new Dimension(180, 36)); 
        passPanel.add(txtPassword, BorderLayout.CENTER);

        JToggleButton btnShowPass = new JToggleButton("👁");
        btnShowPass.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btnShowPass.setBackground(new Color(236, 240, 241));
        btnShowPass.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        btnShowPass.setFocusPainted(false);
        btnShowPass.setToolTipText("Show/Hide Password");
        btnShowPass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnShowPass.setPreferredSize(new Dimension(45, 36));

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

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblStatus.setForeground(new Color(192, 57, 43));       // red
        fc.gridx = 0; fc.gridy = 4; fc.gridwidth = 2;
        fc.anchor = GridBagConstraints.CENTER;
        formCard.add(lblStatus, fc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(Color.WHITE);

        btnLogin = buildButton("  Login  ", new Color(39, 174, 96));
        btnClear = buildButton("  Clear  ", new Color(127, 140, 141));

        btnPanel.add(btnLogin);
        btnPanel.add(btnClear);

        fc.gridx = 0; fc.gridy = 5; fc.gridwidth = 2;
        formCard.add(btnPanel, fc);

        GridBagConstraints rGbc = new GridBagConstraints();
        rGbc.gridx = 0; rGbc.gridy = 0;
        rightPanel.add(formCard, rGbc);

        btnLogin.addActionListener(e -> performLogin());
        btnClear.addActionListener(e -> clearFields());

        txtUsername.addActionListener(e -> txtPassword.requestFocus());
        txtPassword.addActionListener(e -> performLogin());
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(230, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
    }

    private JButton buildButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.setMargin(new Insets(10, 20, 10, 20));

        btn.setPreferredSize(new Dimension(130, 42));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void performLogin() {

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("⚠  Please enter both username and password.");
            return;
        }

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (
            Connection conn = DBConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                new Dashboard(username).setVisible(true);
                this.dispose();

            } else {
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

    private void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
        lblStatus.setText(" ");
        txtUsername.requestFocus();
    }

    private void setWindowProperties() {
        setTitle("Clinic Management System — Login");

        setSize(1000, 600);

        setResizable(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new Login().setVisible(true);
        });
    }
}