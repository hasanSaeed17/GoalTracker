import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GoalApp {

    static class User {
        String username, password;
        User(String u, String p) { username = u; password = p; }
    }

    static class Goal {
        static int counter = 1;
        int id;
        String title, description, type;
        boolean done;
        Goal(String title, String desc, String type) {
            this.id = counter++;
            this.title = title;
            this.description = desc;
            this.type = type;
            this.done = false;
        }
    }

    static class Achievement {
        static int counter = 1;
        int id;
        String title, description;
        Achievement(String title, String desc) {
            this.id = counter++;
            this.title = title;
            this.description = desc;
        }
    }

    static List<User> users = new ArrayList<>();
    static List<Goal> goals = new ArrayList<>();
    static List<Achievement> achievements = new ArrayList<>();
    static User currentUser = null;
    static JFrame mainFrame;

    public static void main(String[] args) {
        users.add(new User("demo", "demo"));
        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame("Goal Tracker");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(700, 550);
            mainFrame.setLocationRelativeTo(null);
            showLogin();
            mainFrame.setVisible(true);
        });
    }

    static void setContent(Container panel) {
        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(panel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    // ── Login ─────────────────────────────────────────────────────
    static void showLogin() {

    }

    // ── Register ──────────────────────────────────────────────────
    static void showRegister() {
        JPanel panel = new JPanel(new GridBagLayout());
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Create Account"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = new JTextField(18);
        JPasswordField passField = new JPasswordField(18);
        JPasswordField confirmField = new JPasswordField(18);
        JButton createBtn = new JButton("Create Account");
        JButton backBtn = new JButton("Back to Sign In");
        JLabel errLabel = new JLabel(" ");

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Username:"), c);
        c.gridx = 1; form.add(userField, c);
        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Password:"), c);
        c.gridx = 1; form.add(passField, c);
        c.gridx = 0; c.gridy = 2; form.add(new JLabel("Confirm Password:"), c);
        c.gridx = 1; form.add(confirmField, c);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; form.add(errLabel, c);
        c.gridy = 4;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnRow.add(createBtn);
        btnRow.add(backBtn);
        form.add(btnRow, c);

        createBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            String conf = new String(confirmField.getPassword());
            if (u.isEmpty() || p.isEmpty()) { errLabel.setText("Please fill in all fields."); return; }
            if (!p.equals(conf)) { errLabel.setText("Passwords do not match."); return; }
            if (users.stream().anyMatch(x -> x.username.equals(u))) { errLabel.setText("Username already taken."); return; }
            users.add(new User(u, p));
            JOptionPane.showMessageDialog(mainFrame, "Account created! Please sign in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            showLogin();
        });
        backBtn.addActionListener(e -> showLogin());

        panel.add(form);
        setContent(panel);
        mainFrame.setTitle("Goal Tracker — Register");
    }

    // ── Dashboard ─────────────────────────────────────────────────
    static void showDashboard() {
        JPanel root = new JPanel(new BorderLayout());

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        topBar.add(new JLabel("Welcome, " + currentUser.username), BorderLayout.WEST);
        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.addActionListener(e -> { currentUser = null; showLogin(); });
        topBar.add(logoutBtn, BorderLayout.EAST);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Goals", goalsPanel());
        tabs.addTab("Achievements", achievementsPanel());
        tabs.addTab("Dream Goals", dreamGoalsPanel());

        root.add(topBar, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);

        setContent(root);
        mainFrame.setTitle("Goal Tracker");
    }

    // ── Goals Panel ───────────────────────────────────────────────
    static JPanel goalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Runnable refresh = () -> {
            model.clear();
            for (Goal g : goals) {
                if (g.type.equals("Normal")) {
                    model.addElement((g.done ? "[Done] " : "[ ]  ") + g.title
                            + (g.description.isEmpty() ? "" : "  —  " + g.description));
                }
            }
        };
        refresh.run();

        JButton addBtn = new JButton("Add Goal");
        JButton removeBtn = new JButton("Remove");
        JButton doneBtn = new JButton("Mark as Done");

        addBtn.addActionListener(e -> {
            showAddGoalDialog("Normal", refresh);
        });

        removeBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0) { JOptionPane.showMessageDialog(mainFrame, "Select a goal first."); return; }
            Goal g = getNormalGoals().get(idx);
            int confirm = JOptionPane.showConfirmDialog(mainFrame, "Remove \"" + g.title + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { goals.remove(g); refresh.run(); }
        });

        doneBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0) { JOptionPane.showMessageDialog(mainFrame, "Select a goal first."); return; }
            getNormalGoals().get(idx).done = true;
            refresh.run();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addBtn);
        btnPanel.add(doneBtn);
        btnPanel.add(removeBtn);

        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    static List<Goal> getNormalGoals() {
        List<Goal> result = new ArrayList<>();
        for (Goal g : goals) if (g.type.equals("Normal")) result.add(g);
        return result;
    }

    // ── Achievements Panel ────────────────────────────────────────
    static JPanel achievementsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Runnable refresh = () -> {
            model.clear();
            for (Achievement a : achievements)
                model.addElement(a.title + (a.description.isEmpty() ? "" : "  —  " + a.description));
        };
        refresh.run();

        JButton addBtn = new JButton("Add Achievement");
        JButton removeBtn = new JButton("Remove");

        addBtn.addActionListener(e -> showAddAchievementDialog(refresh));

        removeBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0) { JOptionPane.showMessageDialog(mainFrame, "Select an achievement first."); return; }
            Achievement a = achievements.get(idx);
            int confirm = JOptionPane.showConfirmDialog(mainFrame, "Remove \"" + a.title + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { achievements.remove(a); refresh.run(); }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);

        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ── Dream Goals Panel ─────────────────────────────────────────
    static JPanel dreamGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Runnable refresh = () -> {
            model.clear();
            for (Goal g : goals)
                if (g.type.equals("Dream"))
                    model.addElement("[Dream] " + g.title + (g.description.isEmpty() ? "" : "  —  " + g.description));
        };
        refresh.run();

        JButton addBtn    = new JButton("Add Dream Goal");
        JButton removeBtn = new JButton("Remove");
        JButton convertBtn = new JButton("Convert to Normal Goal");

        addBtn.addActionListener(e -> showAddGoalDialog("Dream", refresh));

        removeBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0) { JOptionPane.showMessageDialog(mainFrame, "Select a dream goal first."); return; }
            Goal g = getDreamGoals().get(idx);
            int confirm = JOptionPane.showConfirmDialog(mainFrame, "Remove \"" + g.title + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { goals.remove(g); refresh.run(); }
        });

        convertBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0) { JOptionPane.showMessageDialog(mainFrame, "Select a dream goal first."); return; }
            Goal g = getDreamGoals().get(idx);
            g.type = "Normal";
            refresh.run();
            JOptionPane.showMessageDialog(mainFrame, "\"" + g.title + "\" moved to Goals!", "Converted", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addBtn);
        btnPanel.add(convertBtn);
        btnPanel.add(removeBtn);

        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    static List<Goal> getDreamGoals() {
        List<Goal> result = new ArrayList<>();
        for (Goal g : goals) if (g.type.equals("Dream")) result.add(g);
        return result;
    }

    // ── Add Goal Dialog ───────────────────────────────────────────
    static void showAddGoalDialog(String defaultType, Runnable onSave) {
        JDialog dialog = new JDialog(mainFrame, "Add Goal", true);
        dialog.setSize(380, 230);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(20);
        JTextField descField  = new JTextField(20);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Normal", "Dream"});
        typeBox.setSelectedItem(defaultType);
        JLabel errLbl = new JLabel(" ");

        JButton saveBtn   = new JButton("Add");
        JButton cancelBtn = new JButton("Cancel");

        c.gridx = 0; c.gridy = 0; content.add(new JLabel("Title:"), c);
        c.gridx = 1; content.add(titleField, c);
        c.gridx = 0; c.gridy = 1; content.add(new JLabel("Description:"), c);
        c.gridx = 1; content.add(descField, c);
        c.gridx = 0; c.gridy = 2; content.add(new JLabel("Type:"), c);
        c.gridx = 1; content.add(typeBox, c);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; content.add(errLbl, c);
        c.gridy = 4;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.add(saveBtn); btnRow.add(cancelBtn);
        content.add(btnRow, c);

        saveBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            if (t.isEmpty()) { errLbl.setText("Title is required."); return; }
            goals.add(new Goal(t, descField.getText().trim(), (String) typeBox.getSelectedItem()));
            onSave.run();
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(content);
        dialog.setVisible(true);
    }

    // ── Add Achievement Dialog ────────────────────────────────────
    static void showAddAchievementDialog(Runnable onSave) {
        JDialog dialog = new JDialog(mainFrame, "Add Achievement", true);
        dialog.setSize(380, 190);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(20);
        JTextField descField  = new JTextField(20);
        JLabel errLbl = new JLabel(" ");
        JButton saveBtn   = new JButton("Add");
        JButton cancelBtn = new JButton("Cancel");

        c.gridx = 0; c.gridy = 0; content.add(new JLabel("Title:"), c);
        c.gridx = 1; content.add(titleField, c);
        c.gridx = 0; c.gridy = 1; content.add(new JLabel("Description:"), c);
        c.gridx = 1; content.add(descField, c);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; content.add(errLbl, c);
        c.gridy = 3;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.add(saveBtn); btnRow.add(cancelBtn);
        content.add(btnRow, c);

        saveBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            if (t.isEmpty()) { errLbl.setText("Title is required."); return; }
            achievements.add(new Achievement(t, descField.getText().trim()));
            onSave.run();
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(content);
        dialog.setVisible(true);
    }
}
