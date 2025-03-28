/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package examenparcial_ii;

/**
 *
 * @author Nadiesda Fuentes
 */

import java.util.ArrayList;
import java.util.List;

public class HashTable {
    private Entry[] table;
    private int capacity;
    private int size;

    public HashTable() {
        this(100);
    }

    public HashTable(int capacity) {
        this.capacity = capacity;
        this.table = new Entry[capacity];
        this.size = 0;
    }

    private int hash(String key) {
        return Math.abs(key.hashCode()) % capacity;
    }

    public void add(String key, long value) {
        int index = hash(key);
        Entry newEntry = new Entry(key, value);

        if (table[index] == null) {
            table[index] = newEntry;
        } else {
            Entry current = table[index];
            while (current.next != null) {
                if (current.key.equals(key)) {
                    current.value = value;
                    return;
                }
                current = current.next;
            }
            current.next = newEntry;
        }
        size++;
    }

    public long search(String key) {
        int index = hash(key);
        Entry current = table[index];

        while (current != null) {
            if (current.key.equals(key) && !current.isDeleted) {
                return current.value;
            }
            current = current.next;
        }
        return -1;
    }

    public void remove(String key) {
        int index = hash(key);
        Entry current = table[index];
        Entry prev = null;

        while (current != null) {
            if (current.key.equals(key)) {
                current.isDeleted = true;
                size--;
                return;
            }
            prev = current;
            current = current.next;
        }
    }

    public List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();
        for (Entry entry : table) {
            Entry current = entry;
            while (current != null) {
                if (!current.isDeleted) {
                    keys.add(current.key);
                }
                current = current.next;
            }
        }
        return keys;
    }

    private static class Entry {
        String key;
        long value;
        boolean isDeleted;
        Entry next;

        Entry(String key, long value) {
            this.key = key;
            this.value = value;
            this.isDeleted = false;
            this.next = null;
        }
    }
}