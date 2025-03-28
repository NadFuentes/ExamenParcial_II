/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package examenparcial_ii;

/**
 *
 * @author Nadiesda Fuentes
 */

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class PSNUsers {
    private RandomAccessFile usersFile;
    private HashTable users;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static class UserInfo {
        public final String username;
        public final boolean isActive;
        public final int trophyCount;
        public final int points;
        public final List<TrophyInfo> trophies;

        public UserInfo(String username, boolean isActive, int trophyCount, int points, List<TrophyInfo> trophies) {
            this.username = username;
            this.isActive = isActive;
            this.trophyCount = trophyCount;
            this.points = points;
            this.trophies = trophies != null ? trophies : new ArrayList<>();
        }
    }

    public static class TrophyInfo {
        public final String date;
        public final String type;
        public final String game;
        public final String name;

        public TrophyInfo(String date, String type, String game, String name) {
            this.date = date;
            this.type = type;
            this.game = game;
            this.name = name;
        }
    }

    public PSNUsers(String filename) throws IOException {
        this.usersFile = new RandomAccessFile(filename, "rw");
        this.users = new HashTable();
        reloadHashTable();
    }

    private void reloadHashTable() throws IOException {
        usersFile.seek(0);
        while (usersFile.getFilePointer() < usersFile.length()) {
            long position = usersFile.getFilePointer();
            String username = usersFile.readUTF();
            boolean isActive = usersFile.readBoolean();
            
            if (isActive) {
                users.add(username, position);
            }
            usersFile.skipBytes(8); // Saltar contadores (2 ints = 8 bytes)
        }
    }

    public boolean userExists(String username) throws IOException {
        return users.search(username) != -1;
    }

    public boolean isUsernameAvailable(String username) throws IOException {
        return !userExists(username);
    }

    public void addUser(String username) throws IOException {
        if (userExists(username)) {
            throw new IllegalArgumentException("El usuario ya existe: " + username);
        }

        usersFile.seek(usersFile.length());
        long position = usersFile.getFilePointer();

        usersFile.writeUTF(username);
        usersFile.writeBoolean(true); // Activo por defecto
        usersFile.writeInt(0); // Contador de trofeos
        usersFile.writeInt(0); // Puntos iniciales

        users.add(username, position);
    }

    public void deactivateUser(String username) throws IOException {
        long position = users.search(username);
        if (position == -1) {
            throw new IllegalArgumentException("Usuario no encontrado: " + username);
        }

        usersFile.seek(position);
        String storedUsername = usersFile.readUTF(); // Leer username
        usersFile.writeBoolean(false); // Marcar como inactivo
        
        users.remove(username); // Eliminar de la tabla hash
    }

    public void addTrophyTo(String username, String game, String trophyName, Trophy type) throws IOException {
        long position = users.search(username);
        if (position == -1) {
            throw new IllegalArgumentException("Usuario no encontrado: " + username);
        }

        // Verificar estado activo
        usersFile.seek(position);
        String storedUsername = usersFile.readUTF();
        boolean isActive = usersFile.readBoolean();
        if (!isActive) {
            throw new IllegalArgumentException("No se pueden agregar trofeos a usuarios inactivos");
        }

        // Validar puntos del trofeo
        if (type.getPoints() < 1 || type.getPoints() > 5) {
            throw new IllegalArgumentException("Puntos de trofeo inválidos. Deben ser entre 1-5");
        }

        // Guardar trofeo
        try (RandomAccessFile trophyFile = new RandomAccessFile("psn_trophies.dat", "rw")) {
            trophyFile.seek(trophyFile.length());
            trophyFile.writeUTF(username);
            trophyFile.writeUTF(type.name());
            trophyFile.writeUTF(game);
            trophyFile.writeUTF(trophyName);
            trophyFile.writeUTF(LocalDate.now().format(DATE_FORMATTER));
        }

        // Actualizar contadores
        usersFile.seek(position + storedUsername.length() + 1); // +1 por el boolean
        int trophyCount = usersFile.readInt();
        int points = usersFile.readInt();

        usersFile.seek(position + storedUsername.length() + 1);
        usersFile.writeInt(trophyCount + 1);
        usersFile.writeInt(points + type.getPoints());
    }

    public UserInfo playerInfo(String username) throws IOException {
        long position = users.search(username);
        if (position == -1) {
            throw new IllegalArgumentException("Usuario no encontrado: " + username);
        }

        usersFile.seek(position);
        String foundUsername = usersFile.readUTF();
        boolean isActive = usersFile.readBoolean();
        int trophyCount = usersFile.readInt();
        int points = usersFile.readInt();

        return new UserInfo(foundUsername, isActive, trophyCount, points, loadTrophies(username));
    }

    private List<TrophyInfo> loadTrophies(String username) throws IOException {
        List<TrophyInfo> trophies = new ArrayList<>();
        File trophiesFile = new File("psn_trophies.dat");
        
        if (!trophiesFile.exists()) {
            return trophies;
        }

        try (RandomAccessFile trophyFile = new RandomAccessFile(trophiesFile, "r")) {
            while (trophyFile.getFilePointer() < trophyFile.length()) {
                String trophyUser = trophyFile.readUTF();
                String type = trophyFile.readUTF();
                String game = trophyFile.readUTF();
                String name = trophyFile.readUTF();
                String date = trophyFile.readUTF();

                if (trophyUser.equals(username)) {
                    trophies.add(new TrophyInfo(date, type, game, name));
                }
            }
        }
        return trophies;
    }

    public List<String> listActiveUsernames() throws IOException {
        return users.getAllKeys();
    }

    public void close() throws IOException {
        usersFile.close();
    }

    public static void initializeFiles(String usersFilename, String trophiesFilename) throws IOException {
        createFileIfNotExists(usersFilename);
        createFileIfNotExists(trophiesFilename);
    }

    private static void createFileIfNotExists(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            file.createNewFile();
        }
    }
}