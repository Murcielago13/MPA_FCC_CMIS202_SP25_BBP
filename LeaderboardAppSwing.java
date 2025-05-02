package leaderboardapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class LeaderboardAppSwing {

    private JFrame frame;
    private JTextField nameField, scoreField;
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> scoreList = new JList<>(listModel);

    private ScoreLinkedList scores = new ScoreLinkedList();
    private Queue<ScoreEntry> pendingQueue = new LinkedList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LeaderboardAppSwing().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Leaderboard System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        nameField = new JTextField(10);
        scoreField = new JTextField(5);
        JButton submitButton = new JButton("Submit");

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Score:"));
        inputPanel.add(scoreField);
        inputPanel.add(submitButton);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(scoreList), BorderLayout.CENTER);

        submitButton.addActionListener(e -> addScore());

        loadScores();
        refreshLeaderboard();
        frame.setVisible(true);
    }

    private void addScore() {
        String name = nameField.getText().trim();
        String scoreText = scoreField.getText().trim();
        if (name.isEmpty() || scoreText.isEmpty()) {
            showError("Please fill in both name and score.");
            return;
        }
        try {
            int score = Integer.parseInt(scoreText);
            ScoreEntry entry = new ScoreEntry(name, score, LocalDate.now());
            pendingQueue.add(entry);

            if (pendingQueue.size() >= 5) {
                flushQueueToScores();
            }

            nameField.setText("");
            scoreField.setText("");
            refreshLeaderboard();
        } catch (NumberFormatException e) {
            showError("Score must be a number.");
        }
    }

    private void flushQueueToScores() {
        while (!pendingQueue.isEmpty()) {
            ScoreEntry entry = pendingQueue.poll();
            scores.add(entry);
        }
        saveScores();
    }

    private void refreshLeaderboard() {
        ArrayList<ScoreEntry> scoreList = scores.toArrayList();
        mergeSort(scoreList, 0, scoreList.size() - 1);
        listModel.clear();
        for (ScoreEntry entry : scoreList) {
            listModel.addElement(entry.toString());
        }
    }

    private void saveScores() {
        try (PrintWriter writer = new PrintWriter("scores.txt")) {
            for (ScoreEntry entry : scores.toArrayList()) {
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
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    scores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1]), LocalDate.parse(parts[2])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Merge sort for sorting score entries by descending score
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

    // Data class
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
            return Integer.compare(other.score, this.score); // descending
        }

        public String toString() {
            return name + " - " + score + " - " + date;
        }
    }

    // Custom linked list implementation
    static class ScoreNode {
        ScoreEntry data;
        ScoreNode next;

        public ScoreNode(ScoreEntry data) {
            this.data = data;
        }
    }

    static class ScoreLinkedList {
        private ScoreNode head;

        public void add(ScoreEntry entry) {
            ScoreNode newNode = new ScoreNode(entry);
            if (head == null) {
                head = newNode;
            } else {
                ScoreNode current = head;
                while (current.next != null) current = current.next;
                current.next = newNode;
            }
        }

        public ArrayList<ScoreEntry> toArrayList() {
            ArrayList<ScoreEntry> list = new ArrayList<>();
            ScoreNode current = head;
            while (current != null) {
                list.add(current.data);
                current = current.next;
            }
            return list;
        }

        public void clear() {
            head = null;
        }
    }
}