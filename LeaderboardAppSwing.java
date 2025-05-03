package leaderboardapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class LeaderboardAppSwing {

    private final ArrayList<ScoreEntry> scores = new ArrayList<>();
    private final ScoreBST scoreTree = new ScoreBST(); // Binary Search Tree
    private final HashMap<String, ScoreEntry> scoreMap = new HashMap<>(); // Hash table

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> scoreList = new JList<>(listModel);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LeaderboardAppSwing::new);
    }

    public LeaderboardAppSwing() {
        JFrame frame = new JFrame("Leaderboard System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JTextField nameField = new JTextField(10);
        JTextField scoreField = new JTextField(5);
        JButton submitButton = new JButton("Submit");
        JButton searchButton = new JButton("Search");

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Score:"));
        inputPanel.add(scoreField);
        inputPanel.add(submitButton);
        inputPanel.add(searchButton);

        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(scoreList), BorderLayout.CENTER);

        submitButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                int score = Integer.parseInt(scoreField.getText().trim());
                ScoreEntry entry = new ScoreEntry(name, score, LocalDate.now());
                scores.add(entry);
                scoreTree.insert(entry);
                scoreMap.put(name, entry); // update HashMap
                saveScores();
                refreshLeaderboard();
                nameField.setText("");
                scoreField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid score. Enter a number.");
            }
        });

        searchButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            ScoreEntry found = scoreTree.findByName(name);
            if (found != null) {
                JOptionPane.showMessageDialog(frame, "Found in Tree:\n" + found);
            } else {
                JOptionPane.showMessageDialog(frame, "Player not found.");
            }
        });

        loadScores();
        refreshLeaderboard();

        frame.setVisible(true);
    }

    private void refreshLeaderboard() {
        mergeSort(scores, 0, scores.size() - 1);
        listModel.clear();
        for (ScoreEntry entry : scores) {
            listModel.addElement(entry.toString());
        }
    }

    private void saveScores() {
        try (PrintWriter writer = new PrintWriter("scores.txt")) {
            for (ScoreEntry entry : scores) {
                writer.println(entry.name + "," + entry.score + "," + entry.date);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadScores() {
        File file = new File("scores.txt");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            scores.clear();
            scoreTree.clear();
            scoreMap.clear();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    ScoreEntry entry = new ScoreEntry(parts[0], Integer.parseInt(parts[1]), LocalDate.parse(parts[2]));
                    scores.add(entry);
                    scoreTree.insert(entry);
                    scoreMap.put(entry.name, entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mergeSort(ArrayList<ScoreEntry> list, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(list, left, mid);
            mergeSort(list, mid + 1, right);
            merge(list, left, mid, right);
        }
    }

    private void merge(ArrayList<ScoreEntry> list, int left, int mid, int right) {
        ArrayList<ScoreEntry> leftList = new ArrayList<>(list.subList(left, mid + 1));
        ArrayList<ScoreEntry> rightList = new ArrayList<>(list.subList(mid + 1, right + 1));
        int i = 0, j = 0, k = left;

        while (i < leftList.size() && j < rightList.size()) {
            if (leftList.get(i).compareTo(rightList.get(j)) <= 0) {
                list.set(k++, leftList.get(i++));
            } else {
                list.set(k++, rightList.get(j++));
            }
        }
        while (i < leftList.size()) list.set(k++, leftList.get(i++));
        while (j < rightList.size()) list.set(k++, rightList.get(j++));
    }

    // Inner class ScoreEntry
    static class ScoreEntry implements Comparable<ScoreEntry> {
        String name;
        int score;
        LocalDate date;

        public ScoreEntry(String name, int score, LocalDate date) {
            this.name = name;
            this.score = score;
            this.date = date;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            return Integer.compare(other.score, this.score); // Descending
        }

        @Override
        public String toString() {
            return name + " - " + score + " - " + date;
        }
    }

    // Binary Search Tree for ScoreEntry
    static class ScoreBST {
        private class Node {
            ScoreEntry data;
            Node left, right;

            Node(ScoreEntry data) {
                this.data = data;
            }
        }

        private Node root;

        public void insert(ScoreEntry entry) {
            root = insertRec(root, entry);
        }

        private Node insertRec(Node root, ScoreEntry entry) {
            if (root == null) return new Node(entry);
            if (entry.name.compareTo(root.data.name) < 0)
                root.left = insertRec(root.left, entry);
            else if (entry.name.compareTo(root.data.name) > 0)
                root.right = insertRec(root.right, entry);
            return root;
        }

        public ScoreEntry findByName(String name) {
            Node current = root;
            while (current != null) {
                int cmp = name.compareTo(current.data.name);
                if (cmp == 0) return current.data;
                current = (cmp < 0) ? current.left : current.right;
            }
            return null;
        }

        public void clear() {
            root = null;
        }
    }
}