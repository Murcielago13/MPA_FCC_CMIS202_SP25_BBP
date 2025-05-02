import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class LeaderboardAppSwing {

    private final JFrame frame = new JFrame("Leaderboard System");
    private final JTextField nameField = new JTextField(10);
    private final JTextField scoreField = new JTextField(5);
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> scoreList = new JList<>(listModel);
    private final ArrayList<ScoreEntry> scores = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LeaderboardAppSwing().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JButton submitButton = new JButton("Submit");

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Score:"));
        inputPanel.add(scoreField);
        inputPanel.add(submitButton);

        JScrollPane scrollPane = new JScrollPane(scoreList);
        scrollPane.setPreferredSize(new Dimension(380, 200));

        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        submitButton.addActionListener(e -> addScore());

        loadScores();
        refreshLeaderboard();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    private void addScore() {
        String name = nameField.getText().trim();
        String scoreText = scoreField.getText().trim();

        if (name.isEmpty() || scoreText.isEmpty()) {
            showError("Both name and score are required.");
            return;
        }

        try {
            int score = Integer.parseInt(scoreText);
            ScoreEntry entry = new ScoreEntry(name, score, LocalDate.now());
            scores.add(entry);
            saveScores();
            refreshLeaderboard();
            nameField.setText("");
            scoreField.setText("");
        } catch (NumberFormatException e) {
            showError("Score must be a number.");
        }
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
            showError("Error saving scores.");
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
            showError("Error loading scores.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Merge Sort: O(n log n)
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

    // Inner class
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

        public String toString() {
            return name + " - " + score + " - " + date;
        }
    }
}
