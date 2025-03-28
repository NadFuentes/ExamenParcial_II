/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package examenparcial_ii;

/**
 *
 * @author Nadiesda Fuentes
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PSNUsersGUI extends JFrame {
    private PSNUsers psnUsers;
    private JTextField usernameField;
    private JTextArea infoArea;
    private JTable trophyTable;

    public PSNUsersGUI() {
        setTitle("PSN Users Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de entrada
        JPanel inputPanel = new JPanel(new GridLayout(2, 1));
        inputPanel.setBackground(new Color(173, 216, 230));

        JPanel userPanel = new JPanel();
        userPanel.add(new JLabel("Username:"));
        usernameField = new JTextField(20);
        userPanel.add(usernameField);

        JPanel buttonPanel = new JPanel();
        JButton addUserBtn = new JButton("Agregar Usuario");
        JButton deactivateBtn = new JButton("Desactivar Usuario");
        JButton searchUserBtn = new JButton("Buscar Usuario");
        JButton addTrophyBtn = new JButton("Agregar Trofeo");
        JButton clearBtn = new JButton("Limpiar");

        buttonPanel.add(addUserBtn);
        buttonPanel.add(deactivateBtn);
        buttonPanel.add(searchUserBtn);
        buttonPanel.add(addTrophyBtn);
        buttonPanel.add(clearBtn);

        inputPanel.add(userPanel);
        inputPanel.add(buttonPanel);

        // Panel de información
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        JScrollPane scrollInfo = new JScrollPane(infoArea);

        String[] columnNames = {"Fecha", "Tipo", "Juego", "Nombre"};
        trophyTable = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollTrophy = new JScrollPane(trophyTable);

        infoPanel.add(scrollInfo, BorderLayout.NORTH);
        infoPanel.add(scrollTrophy, BorderLayout.CENTER);

        add(inputPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);

        try {
            PSNUsers.initializeFiles("psn_users.dat", "psn_trophies.dat");
            psnUsers = new PSNUsers("psn_users.dat");
        } catch (IOException e) {
            showError("Error al inicializar: " + e.getMessage());
        }

        addUserBtn.addActionListener(e -> addUser());
        deactivateBtn.addActionListener(e -> deactivateUser());
        searchUserBtn.addActionListener(e -> searchUser());
        addTrophyBtn.addActionListener(e -> addTrophy());
        clearBtn.addActionListener(e -> clearFields());
    }

    private void addUser() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError("Ingrese un username");
            return;
        }

        try {
            psnUsers.addUser(username);
            showSuccess("Usuario agregado exitosamente");
            usernameField.setText("");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

   private void deactivateUser() {
    String username = usernameField.getText().trim();
    if (username.isEmpty()) {
        showError("Ingrese un username");
        return;
    }

    try {
        // Verificar primero si el usuario existe
        if (!psnUsers.userExists(username)) {
            showError("El usuario no existe");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "¿Está seguro que desea desactivar este usuario?",
            "Confirmar desactivación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            psnUsers.deactivateUser(username);
            showSuccess("Usuario desactivado exitosamente");
            clearFields();
        }
    } catch (Exception e) {
        showError("Error al desactivar usuario: " + e.getMessage());
    }
}

    private void searchUser() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError("Ingrese un username");
            return;
        }

        try {
            PSNUsers.UserInfo userInfo = psnUsers.playerInfo(username);
            displayUserInfo(userInfo);
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void displayUserInfo(PSNUsers.UserInfo userInfo) {
        infoArea.setText("");
        DefaultTableModel model = (DefaultTableModel) trophyTable.getModel();
        model.setRowCount(0);

        infoArea.append("=== INFORMACIÓN DEL USUARIO ===\n");
        infoArea.append(String.format("%-15s: %s\n", "Username", userInfo.username));
        infoArea.append(String.format("%-15s: %s\n", "Estado", userInfo.isActive ? "Activo" : "Inactivo"));
        infoArea.append(String.format("%-15s: %d\n", "Trofeos", userInfo.trophyCount));
        infoArea.append(String.format("%-15s: %d\n", "Puntos", userInfo.points));

        if (userInfo.trophies.isEmpty()) {
            infoArea.append("\nEl usuario no tiene trofeos.\n");
        } else {
            infoArea.append("\n=== TROFEOS ===\n");
            for (PSNUsers.TrophyInfo trophy : userInfo.trophies) {
                model.addRow(new Object[]{trophy.date, trophy.type, trophy.game, trophy.name});
            }
        }
    }

    private void addTrophy() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError("Ingrese un username");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField gameField = new JTextField();
        JTextField trophyNameField = new JTextField();
        JSpinner pointsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        JLabel pointsLabel = new JLabel("Puntos (1-5):");

        panel.add(new JLabel("Juego:"));
        panel.add(gameField);
        panel.add(new JLabel("Nombre del Trofeo:"));
        panel.add(trophyNameField);
        panel.add(pointsLabel);
        panel.add(pointsSpinner);

        int result = JOptionPane.showConfirmDialog(
            this, panel, "Agregar Trofeo", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                int points = (int) pointsSpinner.getValue();
                Trophy trophy = Trophy.getByPoints(points);
                
                psnUsers.addTrophyTo(
                    username, 
                    gameField.getText().trim(), 
                    trophyNameField.getText().trim(), 
                    trophy
                );
                showSuccess("Trofeo agregado exitosamente");
                searchUser(); // Actualizar vista
            } catch (NumberFormatException e) {
                showError("Los puntos deben ser un número entre 1-5");
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            } catch (Exception e) {
                showError("Error al agregar trofeo: " + e.getMessage());
            }
        }
    }

    private void clearFields() {
        usernameField.setText("");
        infoArea.setText("");
        ((DefaultTableModel) trophyTable.getModel()).setRowCount(0);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PSNUsersGUI gui = new PSNUsersGUI();
            gui.setVisible(true);
        });
    }
}