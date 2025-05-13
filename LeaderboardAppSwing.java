package leaderboard_app_part4;

//Import necessary Java AWT and Swing packages for GUI components, layout, and event handling.
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
//Import necessary Java IO packages for file input/output operations (saving and loading scores).
import java.io.*;
//Import necessary Java Time package for handling dates (specifically, the date of a score entry).
import java.time.LocalDate;
//Import necessary Java Util packages for data structures (Lists, Maps, Sets, etc.) and streams.
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* LeaderboardAppSwing is a Java Swing application that provides a graphical user interface
* for managing and displaying leaderboard scores for various games and players.
* It allows users to submit scores, filter scores by game, search for player scores,
* modify existing scores, and delete scores or entire player/game data.
* Data is persisted to a text file ("scores.txt").
*/
public class LeaderboardAppSwing {

 // --- UI Components ---
 // These are declarations for the various Swing components that make up the application's GUI.

 private JFrame frame; // The main window of the application.
 private JComboBox<String> playerNameInputComboBox; // Editable combo box for player name input/selection.
 private JTextField scoreField; // Text field for entering a player's score.
 private JTextField modifyPointsField; // Text field for entering points for custom modification.
 private JComboBox<String> gameNameInputComboBox; // Editable combo box for game name input/selection.
 private DefaultListModel<ScoreEntry> listModel = new DefaultListModel<>(); // Model for the JList that displays scores. It holds ScoreEntry objects.
 private JList<ScoreEntry> scoreList = new JList<>(listModel); // JList component to display the scores.
 private JComboBox<String> gameFilterComboBox; // Combo box to filter scores by a specific game or show all games.
 private JComboBox<String> customOperationComboBox; // Combo box to select '+' or '-' for custom point modification.

 // --- Core Data Structures ---
 // These data structures are used to store and manage the leaderboard data internally.

 // A custom linked list to store ScoreEntry objects.
 // While present, its direct usage for main data manipulation is often superseded by scoreMap,
 // but it's used, for example, as an intermediate step in refreshLeaderboard.
 private ScoreLinkedList scores = new ScoreLinkedList();

 // A Binary Search Tree to store ScoreEntry objects, potentially for efficient searching or sorting by name.
 // It is maintained alongside scoreMap.
 private final ScoreBST scoreTree = new ScoreBST();

 // A HashMap to store ScoreEntry objects, using a composite key (player name + game name) for quick lookups.
 // This is the primary data structure for managing unique scores and current score values.
 private final HashMap<String, ScoreEntry> scoreMap = new HashMap<>();

 // A Queue to hold ScoreEntry objects that are pending processing.
 // In the current implementation, it's flushed immediately after adding an entry.
 private Queue<ScoreEntry> pendingQueue = new LinkedList<>();

 // A HashSet to store unique player names, ensuring no duplicates and providing quick lookups.
 // Used to populate the player name input combo box.
 private final HashSet<String> uniquePlayerNames = new HashSet<>();

 // A HashSet to store unique game names, ensuring no duplicates and providing quick lookups.
 // Used to populate game name input and filter combo boxes.
 private final HashSet<String> uniqueGameNames = new HashSet<>();

 // Static string holding the welcome message and instructions for the application.
 // This text is displayed in a dialog when the "Help/Instructions" button is clicked.
 private static final String INSTRUCTION_TEXT = "Welcome to the Leaderboard System!\n\n" +
         "How to Use:\n\n" +
         "Note: Most actions like submitting, deleting, or modifying scores will now ask for your confirmation before proceeding.\n\n" +
         "1. Submitting Scores & Managing Player Data:\n" +
         "   - Player Name: Type a new name or select an existing one. Click 'Submit Player' to add a typed player name to the lists without submitting a score. The 'Delete This Player's Data' button next to this input field removes all scores for the currently entered/selected player (confirmation required).\n" +
         "   - Player Score: Enter a whole number.\n" +
         "   - Game Name: Type a new game or select an existing one from the dropdown. Click 'Submit Game' to add a typed game name to the lists without submitting a score.\n" +
         "   - Click 'Submit Score'.\n" +
         "   - Note: Initial negative scores will be automatically set to 0 with a warning.\n" +
         "   - If a score for that player/game exists, it's updated if the new score is different. Identical scores are ignored.\n\n" +
         "2. Managing Game Categories:\n" +
         "   - To add a game: Type its name in the 'Game Name:' input box and click 'Submit Game'.\n" +
         "   - To delete a game category: Select it in the 'Filter by Game:' dropdown and click 'Delete Selected Game' (confirmation required).\n\n" +
         "3. Viewing and Filtering Scores:\n" +
         "   - Use the 'Filter by Game:' dropdown to see scores for a specific game or 'All Games'.\n" +
         "   - The leaderboard is sorted by Score (descending), then Date (most recent first), then Player Name (alphabetical).\n\n" +
         "4. Searching for a Player:\n" +
         "   - Enter or select a player's name in the 'Player Name:' input at the top.\n" +
         "   - Then, click the 'Search Player Name' button located in the bottom operations panel.\n" +
         "   - The list will display all scores for that player across all games.\n" +
         "   - To return to the normal filtered view, change the 'Filter by Game:' selection.\n\n" +
         "5. Modifying Entries & Deleting Scores (actions in the bottom panel, unless specified otherwise):\n" +
         "   - Delete Selected Player Score: Select one or more entries from the list and click this button.\n" +
         "   - Set Selected Player Score To 0: Select one or more entries from the list and click this button.\n" +
         "   - Custom Point Modifier (single selection): Choose '+/-', type the point amount, and click 'Apply Custom'.\n" +
         "   - Quick Point Modifier (single selection): Choose '+/-', select a point amount, and click 'Apply Quick'.\n" +
         "   - Score Validation: If any point modification results in a score below zero, it will be automatically set to 0 with a warning.\n\n" +
         "6. Data Persistence:\n" +
         "   - All scores and changes are automatically saved to 'scores.txt'.\n\n" +
         "7. Help:\n" +
         "   - Click the 'Help/Instructions' button at the bottom of the window to see these instructions again.\n\n" +
         "Enjoy using the Leaderboard System!";

 /**
  * The main method, entry point of the application.
  * It uses SwingUtilities.invokeLater to ensure that the GUI creation and manipulation
  * are done on the Event Dispatch Thread (EDT), which is crucial for Swing applications
  * to prevent threading issues.
  * @param args Command-line arguments (not used in this application).
  */
 public static void main(String[] args) {
     // Schedule a job for the event dispatch thread:
     // creating and showing this application's GUI.
     SwingUtilities.invokeLater(() -> new LeaderboardAppSwing().createAndShowGUI());
 }

 /**
  * Displays a dialog box containing the application's instructions.
  * The instructions are taken from the INSTRUCTION_TEXT constant.
  * Uses a JTextArea inside a JScrollPane to allow scrolling for longer text.
  */
 private void showInstructionsDialog() {
     // Create a JTextArea to display the instruction text.
     JTextArea textArea = new JTextArea(INSTRUCTION_TEXT);
     textArea.setEditable(false); // Make the text area read-only.
     textArea.setWrapStyleWord(true); // Enable word wrapping.
     textArea.setLineWrap(true); // Enable line wrapping.
     textArea.setCaretPosition(0); // Scroll to the top of the text.
     textArea.setOpaque(false); // Make it transparent to use panel's background (though not strictly necessary here).
     textArea.setBorder(null); // Remove border.
     textArea.setFont(UIManager.getFont("Label.font")); // Use a standard label font.

     // Create a JScrollPane to allow scrolling if the text is too long for the dialog.
     JScrollPane scrollPane = new JScrollPane(textArea);
     scrollPane.setPreferredSize(new Dimension(500, 350)); // Set preferred size for the scroll pane.

     // Show the instructions in a JOptionPane.
     JOptionPane.showMessageDialog(frame, scrollPane, "Welcome & Instructions", JOptionPane.INFORMATION_MESSAGE);
 }

 /**
  * Creates and sets up the main GUI components, layout, and event listeners.
  * This method initializes all UI elements, arranges them in panels,
  * defines their behavior through action listeners, loads initial data,
  * and makes the main frame visible.
  */
 private void createAndShowGUI() {
     // --- Frame Setup ---
     frame = new JFrame("Leaderboard System"); // Create the main application window with a title.
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensure the application exits when the window is closed.
     frame.setSize(950, 600); // Set the initial size of the window.

     // --- UI Component Initialization ---
     // Player Name Input: Editable JComboBox for entering or selecting player names.
     playerNameInputComboBox = new JComboBox<>();
     playerNameInputComboBox.setEditable(true); // Allows users to type new names.
     playerNameInputComboBox.setPreferredSize(new Dimension(130, playerNameInputComboBox.getPreferredSize().height)); // Set preferred width.

     // Score Input: JTextField for entering scores.
     scoreField = new JTextField(5); // Accommodates roughly 5 characters.

     // Modify Points Input: JTextField for the custom point modification amount.
     modifyPointsField = new JTextField(5);

     // Game Name Input: Editable JComboBox for entering or selecting game names.
     gameNameInputComboBox = new JComboBox<>();
     gameNameInputComboBox.setEditable(true); // Allows users to type new game names.
     gameNameInputComboBox.setPreferredSize(new Dimension(130, gameNameInputComboBox.getPreferredSize().height)); // Set preferred width.

     // Score List: JList to display ScoreEntry objects, allowing multiple selections.
     scoreList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Allows selecting multiple items.

     // --- Button Initialization ---
     // Buttons for various actions within the application.
     JButton submitButton = new JButton("Submit Score"); // Submits a new score.
     JButton searchPlayerButton = new JButton("Search Player Name"); // Searches for scores of a specific player.
     JButton deletePlayerScoreButton = new JButton("Delete Selected Player Score"); // Deletes selected score(s) from the list.
     JButton submitPlayerButton = new JButton("Submit Player"); // Button to add a player name to the known list.
     JButton deletePlayerByNameButton = new JButton("Delete This Player's Data"); // Deletes all data for a player specified in playerNameInputComboBox.
     JButton setSelectedToZeroButton = new JButton("Set Selected Player Score To 0"); // Sets score of selected entries to 0.
     JButton customModifyPointsButton = new JButton("Apply Custom"); // Applies custom point modification.
     JButton enterGameButton = new JButton("Submit Game"); // Adds a new game category.
     JButton deleteGameCategoryButton = new JButton("Delete Selected Game"); // Deletes a selected game category and its scores.
     JButton showInstructionsButton = new JButton("Help/Instructions"); // Shows the help dialog.

     // Quick Point Modifier Components:
     JLabel quickModifyLabel = new JLabel("Quick Point Modifier:"); // Label for the quick modifier section.
     JComboBox<String> operationComboBox = new JComboBox<>(new String[]{"+", "-"}); // Combo box for +/- operation.
     JComboBox<Integer> pointValueComboBox = new JComboBox<>(new Integer[]{1, 2, 5, 10, 20, 50, 100}); // Combo box for predefined point values.
     JButton applyQuickModifyButton = new JButton("Apply Quick"); // Applies the quick point modification.

     // Custom Operation ComboBox for the custom point modifier.
     customOperationComboBox = new JComboBox<>(new String[]{"+", "-"}); // For custom modification operation (+/-).

     // Game Filter ComboBox: Allows filtering displayed scores by game.
     gameFilterComboBox = new JComboBox<>();
     gameFilterComboBox.addItem("All Games"); // Default option to show scores from all games.
     // Action listener to refresh the leaderboard when the filter selection changes.
     gameFilterComboBox.addActionListener(e -> refreshLeaderboard());

     // --- Panel Setup and Layout ---
     // Panels are used to organize components within the frame.

     // Input Controls Panel (Top Panel): Contains inputs for player name, score, game name, and game filter.
     JPanel inputControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Arranges components from left to right.
     inputControlsPanel.add(new JLabel("Player Name:"));
     inputControlsPanel.add(playerNameInputComboBox);
     inputControlsPanel.add(submitPlayerButton);
     inputControlsPanel.add(deletePlayerByNameButton);
     JLabel spacerPlayerDelete = new JLabel(); // Spacer for visual separation.
     spacerPlayerDelete.setPreferredSize(new Dimension(10, 1));
     inputControlsPanel.add(spacerPlayerDelete);
     inputControlsPanel.add(new JLabel("Player Score:"));
     inputControlsPanel.add(scoreField);
     inputControlsPanel.add(submitButton);
     JLabel spacerA = new JLabel(); // Spacer.
     spacerA.setPreferredSize(new Dimension(10, 1));
     inputControlsPanel.add(spacerA);
     inputControlsPanel.add(new JLabel("Game Name:"));
     inputControlsPanel.add(gameNameInputComboBox);
     inputControlsPanel.add(enterGameButton);
     JLabel spacerC = new JLabel(); // Spacer.
     spacerC.setPreferredSize(new Dimension(5, 1));
     inputControlsPanel.add(spacerC);
     inputControlsPanel.add(new JLabel("Filter by Game:"));
     inputControlsPanel.add(gameFilterComboBox);
     inputControlsPanel.add(deleteGameCategoryButton);

     // Operations Panel (Part of Bottom Panel): Contains buttons for operations on selected scores or player searches.
     JPanel operationsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
     operationsPanel.add(searchPlayerButton);
     // --- MODIFIED --- Add spacer between "Search Player Name" and "Delete Selected Player Score"
     JLabel spacerOps1 = new JLabel();
     spacerOps1.setPreferredSize(new Dimension(10, 1));
     operationsPanel.add(spacerOps1);
     // --- END MODIFIED ---
     operationsPanel.add(deletePlayerScoreButton);
     JLabel spacerX = new JLabel(); // This spacer is between "Delete Selected Player Score" and "Set Selected Player Score To 0"
     spacerX.setPreferredSize(new Dimension(10, 1));
     operationsPanel.add(spacerX);
     operationsPanel.add(setSelectedToZeroButton);
     JLabel spacer1 = new JLabel(); // Spacer.
     spacer1.setPreferredSize(new Dimension(25, 1));
     operationsPanel.add(spacer1);
     operationsPanel.add(new JLabel("Custom Point Modifier:"));
     operationsPanel.add(customOperationComboBox);
     operationsPanel.add(modifyPointsField);
     operationsPanel.add(customModifyPointsButton);
     JLabel spacer2 = new JLabel(); // Spacer.
     spacer2.setPreferredSize(new Dimension(25, 1));
     operationsPanel.add(spacer2);
     operationsPanel.add(quickModifyLabel);
     operationsPanel.add(operationComboBox);
     operationsPanel.add(pointValueComboBox);
     operationsPanel.add(applyQuickModifyButton);

     // Help Panel (Part of Bottom Panel): Contains the help button.
     JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Aligns the help button to the right.
     helpPanel.add(showInstructionsButton);

     // South Container Panel: Groups the operationsPanel and helpPanel at the bottom of the frame.
     JPanel southContainerPanel = new JPanel(new BorderLayout());
     southContainerPanel.add(operationsPanel, BorderLayout.NORTH); // Operations at the top of this container.
     southContainerPanel.add(helpPanel, BorderLayout.SOUTH); // Help button at the bottom of this container.

     // --- Main Frame Layout ---
     // Uses BorderLayout to arrange the main panels.
     frame.setLayout(new BorderLayout());
     frame.add(inputControlsPanel, BorderLayout.NORTH); // Input controls at the top.
     frame.add(new JScrollPane(scoreList), BorderLayout.CENTER); // Scrollable list of scores in the center.
     frame.add(southContainerPanel, BorderLayout.SOUTH); // Operations and help at the bottom.

     // --- Action Listener Setup ---
     // Defines the behavior when buttons are clicked or specific actions occur.

     // Action Listener for 'Submit Player' button.
     submitPlayerButton.addActionListener(e -> {
         // Get the new player name from the input combo box.
         String newPlayerName = getComboBoxText(playerNameInputComboBox).trim();
         // Validate that the player name is not empty.
         if (newPlayerName.isEmpty()) {
             showError("Please type a player name to submit.");
             playerNameInputComboBox.requestFocus();
             return;
         }

         // Confirm adding the new player name.
         int confirmation = JOptionPane.showConfirmDialog(frame, "Add player '" + newPlayerName + "' to the list of known players?", "Confirm Add Player", JOptionPane.YES_NO_OPTION);
         if (confirmation != JOptionPane.YES_OPTION) {
             return; // Stop if user cancels.
         }

         // Attempt to add the new player name to the set of unique player names.
         if (uniquePlayerNames.add(newPlayerName)) {
             // If added successfully (i.e., it was a new player name):
             updatePlayerNameInputComboBoxModel(); // Update the player name input combo box.
             JOptionPane.showMessageDialog(frame, "Player '" + newPlayerName + "' added to the list.", "Player Added", JOptionPane.INFORMATION_MESSAGE);
         } else {
             // If the player name already exists.
             JOptionPane.showMessageDialog(frame, "Player '" + newPlayerName + "' already exists in the list.", "Player Exists", JOptionPane.INFORMATION_MESSAGE);
         }
         // Clear the player name input field.
         setComboBoxText(playerNameInputComboBox, "");
         playerNameInputComboBox.requestFocus();
     });

     // Action Listener for 'Submit Score' button.
     submitButton.addActionListener(e -> {
         // Get input values from the UI fields.
         String name = getComboBoxText(playerNameInputComboBox);
         String game = getComboBoxText(gameNameInputComboBox);
         String scoreTextVal = scoreField.getText().trim();

         // Validate that player name and game name are not empty.
         if (name.isEmpty() || game.isEmpty()) {
             showError("Player Name and Game fields cannot be empty.");
             return; // Stop processing if validation fails.
         }
         // Validate that score is not empty.
         if (scoreTextVal.isEmpty()){
             showError("Player Score field cannot be empty.");
             scoreField.requestFocus(); // Set focus to the score field.
             return;
         }

         // Confirm submission with the user.
         int confirmation = JOptionPane.showConfirmDialog(frame, "Submit score for " + name + " in " + game + "?", "Confirm Submission", JOptionPane.YES_NO_OPTION);
         if (confirmation != JOptionPane.YES_OPTION) {
             return; // Stop if user cancels.
         }

         try {
             int scoreVal;
             // Try to parse the score text as an integer.
             try {
                 scoreVal = Integer.parseInt(scoreTextVal);
             } catch (NumberFormatException ex) {
                 showError("Invalid score. Please enter a whole number.");
                 scoreField.requestFocus();
                 return;
             }

             // Handle negative initial scores.
             if (scoreVal < 0) {
                 Object[] options = {"Set Score to 0", "Re-enter Score"};
                 // Ask user how to handle the negative score.
                 int choice = JOptionPane.showOptionDialog(frame, "Initial score (" + scoreVal + ") cannot be negative.", "Score Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                 if (choice == 0) { // User chose "Set Score to 0".
                     scoreVal = 0;
                 } else { // User chose "Re-enter Score" or closed dialog.
                     scoreField.setText("");
                     scoreField.requestFocus();
                     return;
                 }
             }

             // Create a new ScoreEntry object.
             ScoreEntry submittedEntry = new ScoreEntry(name, scoreVal, LocalDate.now(), game);
             // Add the new entry to the pending queue (which is then immediately flushed).
             pendingQueue.add(submittedEntry);
             flushQueueToScores(); // Process the queue to update main data structures.

             // Clear input fields after submission.
             setComboBoxText(playerNameInputComboBox, "");
             scoreField.setText("");
             setComboBoxText(gameNameInputComboBox, "");

             // Refresh the displayed leaderboard.
             refreshLeaderboard();
         } catch (Exception ex) {
             // Catch any other unexpected errors during submission.
             showError("Error during submission: " + ex.getMessage());
         }
     });

     // Action Listener for 'Submit Game' button.
     enterGameButton.addActionListener(e -> {
         // Get the new game name from the input combo box.
         String newGameName = getComboBoxText(gameNameInputComboBox).trim();
         // Validate that the game name is not empty.
         if (newGameName.isEmpty()) {
             showError("Please type a game name.");
             gameNameInputComboBox.requestFocus();
             return;
         }

         // Confirm adding the new game category.
         int confirmation = JOptionPane.showConfirmDialog(frame, "Add '" + newGameName + "' to game categories?", "Confirm Add Game", JOptionPane.YES_NO_OPTION);
         if (confirmation != JOptionPane.YES_OPTION) {
             return; // Stop if user cancels.
         }

         // Attempt to add the new game name to the set of unique game names.
         if (uniqueGameNames.add(newGameName)) {
             // If added successfully (i.e., it was a new game name):
             updateGameFilterComboBox(); // Update the filter combo box.
             updateGameInputComboBoxModel(); // Update the game input combo box.
             JOptionPane.showMessageDialog(frame, "Game '" + newGameName + "' added.", "Game Entered", JOptionPane.INFORMATION_MESSAGE);
         } else {
             // If the game name already exists.
             JOptionPane.showMessageDialog(frame, "Game '" + newGameName + "' already exists.", "Game Exists", JOptionPane.INFORMATION_MESSAGE);
         }
         // Clear the game name input field.
         setComboBoxText(gameNameInputComboBox, "");
         gameNameInputComboBox.requestFocus();
     });

     // Action Listener for 'Delete Selected Game' button.
     deleteGameCategoryButton.addActionListener(e -> {
         // Get the selected game to delete from the filter combo box.
         String gameToDelete = (String) gameFilterComboBox.getSelectedItem();
         // Ensure a specific game is selected (not "All Games").
         if (gameToDelete == null || "All Games".equals(gameToDelete)) {
             showError("Select a specific game category to delete.");
             return;
         }

         // Confirm deletion with a strong warning.
         int confirmation = JOptionPane.showConfirmDialog(frame, "Delete game category '" + gameToDelete + "' and ALL its scores?\nThis cannot be undone.", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
         if (confirmation != JOptionPane.YES_OPTION) {
             return; // Stop if user cancels.
         }

         // Find all scoreMap keys corresponding to the game to be deleted.
         List<String> keysToRemove = scoreMap.entrySet().stream()
                                    .filter(entry -> entry.getValue().getGameName().equals(gameToDelete))
                                    .map(Map.Entry::getKey)
                                    .collect(Collectors.toList());
         boolean actuallyRemovedScores = !keysToRemove.isEmpty(); // Check if any scores were associated with this game.

         // Remove these entries from the scoreMap.
         keysToRemove.forEach(scoreMap::remove);
         // Remove the game from the set of unique game names.
         boolean removedFromUnique = uniqueGameNames.remove(gameToDelete);

         // If any scores were removed or the game was removed from the unique list:
         if (actuallyRemovedScores || removedFromUnique) {
             // Rebuild the 'scores' linked list and 'scoreTree' from the updated scoreMap.
             scores.clear();
             scores.addAll(scoreMap.values());
             scoreTree.clear();
             scoreMap.values().forEach(scoreTree::insert);

             // Update the UI components related to game names.
             updateGameFilterComboBox();
             updateGameInputComboBoxModel();
             saveScores(); // Persist changes to file.
             refreshLeaderboard(); // Update the displayed list.
             JOptionPane.showMessageDialog(frame, "Game category '" + gameToDelete + "' deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
         } else {
             // If no changes were made (e.g., game didn't exist or had no scores).
             JOptionPane.showMessageDialog(frame, "Game category '" + gameToDelete + "' not found or no scores to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
         }
     });

     // Action Listener for 'Delete This Player's Data' button.
     // This button acts on the player name entered/selected in 'playerNameInputComboBox'.
     deletePlayerByNameButton.addActionListener(e -> {
         // Get the player name from the player name input combo box.
         String playerNameToDelete = getComboBoxText(playerNameInputComboBox);

         // Validate that a player name is provided.
         if (playerNameToDelete.isEmpty()) {
             showError("Please enter or select a player name in the 'Player Name' input field to delete all their data.");
             playerNameInputComboBox.requestFocus();
             return;
         }

         // Confirm deletion with a strong warning.
         int confirmation = JOptionPane.showConfirmDialog(frame,
                 "Are you sure you want to delete ALL scores for player '" + playerNameToDelete + "' across all games?\n" +
                 "This action cannot be undone.",
                 "Confirm Player Data Deletion",
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.WARNING_MESSAGE);

         if (confirmation != JOptionPane.YES_OPTION) {
             return; // Stop if user cancels.
         }

         // Perform the deletion of all data for the specified player.
         // The performPlayerDataDeletion method handles removal from scoreMap and uniquePlayerNames.
         boolean overallDataChanged = performPlayerDataDeletion(Collections.singleton(playerNameToDelete));

         if (overallDataChanged) {
             // If data was actually changed (scores were deleted):
             // Rebuild the auxiliary data structures (scores list and score tree) from the updated scoreMap.
             scores.clear();
             scores.addAll(scoreMap.values());
             scoreTree.clear();
             scoreMap.values().forEach(scoreTree::insert);

             // Update the player name input combo box model as the player might no longer exist.
             updatePlayerNameInputComboBoxModel();

             // Check if any game categories became empty and need to be removed from game combo boxes.
             HashSet<String> gamesStillPresent = scoreMap.values().stream()
                                                .map(ScoreEntry::getGameName)
                                                .collect(Collectors.toCollection(HashSet::new));
             boolean gameListNeedsUpdate = uniqueGameNames.size() != gamesStillPresent.size() ||
                                          !uniqueGameNames.containsAll(gamesStillPresent);

             if(gameListNeedsUpdate){
                  uniqueGameNames.clear();
                  uniqueGameNames.addAll(gamesStillPresent);
                  updateGameFilterComboBox(); // Update game filter combo box.
                  updateGameInputComboBoxModel(); // Update game input combo box.
             }

             saveScores(); // Persist changes.
             refreshLeaderboard(); // Update the displayed list.
             JOptionPane.showMessageDialog(frame, "All scores for player '" + playerNameToDelete + "' have been deleted.", "Player Data Deleted", JOptionPane.INFORMATION_MESSAGE);
         } else {
             // If no scores were found for the player.
             JOptionPane.showMessageDialog(frame, "No scores found for player '" + playerNameToDelete + "' to delete.", "Information", JOptionPane.INFORMATION_MESSAGE);
             // Even if no scores were deleted from map, the name might have been typed and not submitted,
             // but existed in uniquePlayerNames. Ensure combo box is updated if name was removed from unique list.
             if (!uniquePlayerNames.contains(playerNameToDelete) && getComboBoxText(playerNameInputComboBox).equals(playerNameToDelete)) {
                  updatePlayerNameInputComboBoxModel();
             }
         }
         // Clear the player name input field after the action.
         setComboBoxText(playerNameInputComboBox, "");
     });

     // Action Listener for 'Help/Instructions' button.
     showInstructionsButton.addActionListener(e -> showInstructionsDialog()); // Calls the method to display the help dialog.

     // Action Listener for 'Search Player Name' button.
     searchPlayerButton.addActionListener(e -> {
         // Get the player name to search from the top input area.
         String nameToSearch = getComboBoxText(playerNameInputComboBox);
         // Validate that a name is provided.
         if (nameToSearch.isEmpty()) {
             showError("Enter a player name in the top input area to search.");
             return;
         }

         // Confirm search action.
         int confirmation = JOptionPane.showConfirmDialog(frame, "Search for player '" + nameToSearch + "'?", "Confirm Search", JOptionPane.YES_NO_OPTION);
         if (confirmation != JOptionPane.YES_OPTION) {
             return; // Stop if user cancels.
         }

         // Filter scoreMap values to find entries matching the player name (case-insensitive).
         ArrayList<ScoreEntry> searchResults = scoreMap.values().stream()
             .filter(entry -> entry.getName().equalsIgnoreCase(nameToSearch))
             .collect(Collectors.toCollection(ArrayList::new));

         listModel.clear(); // Clear the current display list.
         if (searchResults.isEmpty()) {
             // If no scores found, show an info message.
             JOptionPane.showMessageDialog(frame, "No scores for: " + nameToSearch, "Search Result", JOptionPane.INFORMATION_MESSAGE);
         } else {
             // If scores are found, sort them and add to the list model.
             mergeSort(searchResults, 0, searchResults.size() - 1); // Sort results.
             searchResults.forEach(listModel::addElement); // Display results.
         }
     });

     // Action Listener for 'Delete Selected Player Score' button.
     deletePlayerScoreButton.addActionListener(e -> {
         // Get the list of currently selected ScoreEntry objects from the JList.
         List<ScoreEntry> selectedEntries = scoreList.getSelectedValuesList();
         // Check if any entries are selected.
         if (selectedEntries.isEmpty()) {
             showError("Select player scores from the list to delete.");
             return;
         }

         // Confirm deletion of the selected scores.
         int confirmation = JOptionPane.showConfirmDialog(frame, "Delete " + selectedEntries.size() + " selected score(s)?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
         if (confirmation != JOptionPane.YES_OPTION) {
             return; // Stop if user cancels.
         }

         HashSet<String> distinctPlayerNamesAffected = new HashSet<>(); // To track players whose scores were deleted.
         HashSet<String> distinctGameNamesAffected = new HashSet<>(); // To track games affected by deletion.
         boolean actualDeletionsOccurred = false; // Flag to check if any data was actually changed.

         // Iterate over each selected entry to delete it.
         for (ScoreEntry entryToDelete : selectedEntries) {
             // Skip any "System Message" entries if they were somehow selectable (defensive coding).
             if ("System Message".equals(entryToDelete.getGameName())) continue;

             // Attempt to remove the entry from the scoreMap using its composite key.
             if (scoreMap.remove(getCompositeKey(entryToDelete.getName(), entryToDelete.getGameName())) != null) {
                 actualDeletionsOccurred = true; // Mark that a deletion occurred.
                 // Also remove from auxiliary data structures.
                 scoreTree.delete(entryToDelete.getName());
                 scores.deleteSpecificEntry(entryToDelete.getName(), entryToDelete.getGameName(), entryToDelete.getScore(), entryToDelete.getDate());

                 // Record the affected player and game names.
                 distinctPlayerNamesAffected.add(entryToDelete.getName());
                 distinctGameNamesAffected.add(entryToDelete.getGameName());
             }
         }

         // If no actual deletions happened (e.g., selected item was already removed by another action), exit.
         if (!actualDeletionsOccurred) return;

         // Check if any players need to be removed from uniquePlayerNames (if all their scores are gone).
         boolean playerComboBoxNeedsUpdate = false;
         for (String playerName : distinctPlayerNamesAffected) {
             boolean playerStillExists = scoreMap.values().stream().anyMatch(entry -> entry.getName().equals(playerName));
             if (!playerStillExists) {
                 if (uniquePlayerNames.remove(playerName)) playerComboBoxNeedsUpdate = true;
             }
         }
         if (playerComboBoxNeedsUpdate) updatePlayerNameInputComboBoxModel(); // Update player name dropdown.

         // Check if any games need to be removed from uniqueGameNames (if all scores for that game are gone).
         boolean gameComboBoxesNeedUpdate = false;
         for (String gameName : distinctGameNamesAffected) {
             boolean gameStillExists = scoreMap.values().stream().anyMatch(entry -> entry.getGameName().equals(gameName));
             if (!gameStillExists) {
                if (uniqueGameNames.remove(gameName)) gameComboBoxesNeedUpdate = true;
             }
         }
         if (gameComboBoxesNeedUpdate) {
             updateGameFilterComboBox(); // Update game filter dropdown.
             updateGameInputComboBoxModel(); // Update game name input dropdown.
         }

         saveScores(); // Persist changes.
         refreshLeaderboard(); // Update the displayed list.
     });

     // Action Listener for 'Set Selected Player Score To 0' button.
     setSelectedToZeroButton.addActionListener(e -> {
         // Get the list of selected entries from the JList.
         List<ScoreEntry> selectedEntries = scoreList.getSelectedValuesList();
         // Count how many are valid score entries (not system messages).
         long validEntriesCount = selectedEntries.stream().filter(entry -> !"System Message".equals(entry.getGameName())).count();

         // If no valid entries selected, show error.
         if (validEntriesCount == 0) {
             showError("Please select valid score entries to set to zero.");
             return;
         }

         // Confirm setting scores to zero.
         int confirmation = JOptionPane.showConfirmDialog(frame, "Set score to 0 for " + validEntriesCount + " selected entries?", "Confirm Set to 0", JOptionPane.YES_NO_OPTION);
         if (confirmation != JOptionPane.YES_OPTION) {
             return; // Stop if user cancels.
         }

         boolean dataChanged = false; // Flag to track if any data was actually modified.
         // Iterate over selected entries.
         for(ScoreEntry selectedEntry : selectedEntries){
             if ("System Message".equals(selectedEntry.getGameName())) continue; // Skip system messages.
             // Get the corresponding entry from the main scoreMap.
             ScoreEntry entryInMap = scoreMap.get(getCompositeKey(selectedEntry.getName(), selectedEntry.getGameName()));
             // If found and score is not already 0, update it.
             if (entryInMap != null && entryInMap.score != 0) {
                 entryInMap.score = 0; // Set score to 0.
                 entryInMap.date = LocalDate.now(); // Update the date to reflect modification time.
                 dataChanged = true; // Mark that data has changed.
             }
         }
         // If data changed, save and refresh.
         if(dataChanged){
             saveScores();
             refreshLeaderboard();
         }
     });

     // Action Listener for 'Apply Custom' button (custom point modification).
     customModifyPointsButton.addActionListener(e -> {
         // Get the single selected entry from the list.
         ScoreEntry selectedEntry = scoreList.getSelectedValue();
         // Validate that a single, valid score entry is selected.
         if (selectedEntry == null || "System Message".equals(selectedEntry.getGameName())) {
             showError("Please select a valid score entry to modify.");
             return;
         }
         // Get the points to modify and the operation (+/-).
         String pointsText = modifyPointsField.getText().trim();
         String operation = (String) customOperationComboBox.getSelectedItem();
         // Validate inputs for modification.
         if (pointsText.isEmpty() || operation == null) {
             showError("Enter points and select an operation (+/-) for custom modifier.");
             return;
         }
         int pointsOffset;
         // Parse the points string to an integer.
         try {
             pointsOffset = Integer.parseInt(pointsText);
         }
         catch (NumberFormatException ex) {
             showError("Invalid points. Please enter a whole number.");
             modifyPointsField.requestFocus();
             return;
         }
         // If operation is '-', make the offset negative.
         if ("-".equals(operation)) pointsOffset *= -1;

         // Confirm the modification.
         int confirmation = JOptionPane.showConfirmDialog(frame, "Apply modification of " + pointsOffset + " to " + selectedEntry.getName() + "'s score?", "Confirm Modification", JOptionPane.YES_NO_OPTION);
         if (confirmation != JOptionPane.YES_OPTION) {
             return; // Stop if user cancels.
         }

         // Get the actual entry from scoreMap to modify it.
         ScoreEntry entryInMap = scoreMap.get(getCompositeKey(selectedEntry.getName(), selectedEntry.getGameName()));
         if (entryInMap == null) {
             showError("Selected entry not found in map."); // Should not happen if list is in sync.
             return;
         }
         // Calculate potential new score.
         int potentialNewScore = entryInMap.score + pointsOffset;
         // Handle scores that would become negative.
         if (potentialNewScore < 0) {
             Object[] options = {"Set Score to 0", "Re-enter Amount"};
             // Ask user how to handle the negative result.
             int choice = JOptionPane.showOptionDialog(frame, "Resulting score (" + potentialNewScore + ") negative.", "Score Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
             if (choice == 0) { // User chose "Set Score to 0".
                 entryInMap.score = 0;
             } else { // User chose "Re-enter Amount" or closed.
                 modifyPointsField.setText("");
                 modifyPointsField.requestFocus();
                 return;
             }
         } else {
             // If not negative, update the score.
             entryInMap.score = potentialNewScore;
         }
         entryInMap.date = LocalDate.now(); // Update modification date.
         saveScores(); // Persist changes.
         refreshLeaderboard(); // Refresh display.
         modifyPointsField.setText(""); // Clear the points field.
     });

     // Action Listener for 'Apply Quick' button (quick point modification).
     applyQuickModifyButton.addActionListener(e -> {
         // Get the single selected entry from the list.
         ScoreEntry selectedEntry = scoreList.getSelectedValue();
         // Validate that a single, valid score entry is selected.
         if (selectedEntry == null || "System Message".equals(selectedEntry.getGameName())) {
              showError("Please select a valid score entry for quick modification.");
              return;
         }
         // Get selected operation (+/-) and point value.
         String operation = (String) operationComboBox.getSelectedItem();
         Integer value = (Integer) pointValueComboBox.getSelectedItem();
         // Validate that both are selected.
         if (operation == null || value == null) {
             showError("Select operation and value for quick modify.");
             return;
         }
         int pointsToApply = value;
         // If operation is '-', make the points negative.
         if ("-".equals(operation)) pointsToApply *= -1;

         // Confirm the quick modification.
         int confirmation = JOptionPane.showConfirmDialog(frame, "Apply quick modification of " + pointsToApply + " to " + selectedEntry.getName() + "'s score?", "Confirm Modification", JOptionPane.YES_NO_OPTION);
         if (confirmation != JOptionPane.YES_OPTION) {
             return; // Stop if user cancels.
         }

         // Call helper method to apply the modification.
         modifyPointsForSelectedEntry(pointsToApply);
     });

     // --- Initial Setup ---
     loadScores(); // Load scores from file when the application starts.
     refreshLeaderboard(); // Populate the leaderboard display with loaded scores.

     // Make the main frame visible.
     frame.setVisible(true);
     // Show instructions dialog on startup.
     showInstructionsDialog();
 }

 /**
  * Deletes all score data for a given set of player names.
  * This method iterates through the player names, removes their entries from the `scoreMap`,
  * and also removes them from the `uniquePlayerNames` set.
  *
  * @param playerNamesToProcess A Set of player names whose data needs to be deleted.
  * @return {@code true} if any data was actually changed (scores deleted from map), {@code false} otherwise.
  */
 private boolean performPlayerDataDeletion(Set<String> playerNamesToProcess) {
     boolean dataActuallyChanged = false; // Flag to track if any scoreMap entries were removed.
     // Iterate over each player name provided for deletion.
     for (String pName : playerNamesToProcess) {
         boolean scoresRemovedForThisPlayer = false; // Flag specific to the current player.
         // Use an iterator to safely remove entries from scoreMap while iterating.
         Iterator<Map.Entry<String, ScoreEntry>> iterator = scoreMap.entrySet().iterator();
         while(iterator.hasNext()){
             Map.Entry<String, ScoreEntry> mapEntry = iterator.next();
             // If the current score entry belongs to the player being processed, remove it.
             if(mapEntry.getValue().getName().equals(pName)){
                 iterator.remove(); // Remove from scoreMap.
                 scoresRemovedForThisPlayer = true; // Mark that a score was removed.
             }
         }

         // If scores were removed for this player, it means data changed overall.
         if (scoresRemovedForThisPlayer) {
             dataActuallyChanged = true;
             uniquePlayerNames.remove(pName);
         } else {
             uniquePlayerNames.remove(pName);
         }
     }
     return dataActuallyChanged;
 }


 /**
  * Retrieves the currently selected or entered text from an editable JComboBox.
  *
  * @param comboBox The JComboBox from which to get the text.
  * @return The trimmed text content of the JComboBox's editor or selected item. Returns an empty string if null.
  */
 private String getComboBoxText(JComboBox<String> comboBox) {
     Object item = comboBox.getSelectedItem(); // Get the selected item (could be from dropdown).
     if (item != null) {
         return item.toString().trim(); // If an item is selected, use its string representation.
     }
     // If no item is selected (e.g., user typed custom text), get text from the editor component.
     Component editorComponent = comboBox.getEditor().getEditorComponent();
     if (editorComponent instanceof JTextField) {
         return ((JTextField) editorComponent).getText().trim();
     }
     return ""; // Default to empty string if unable to retrieve text.
 }

 /**
  * Sets the text in the editor component of an editable JComboBox.
  *
  * @param comboBox The JComboBox whose editor text needs to be set.
  * @param text The text to set in the editor.
  */
 private void setComboBoxText(JComboBox<String> comboBox, String text) {
     // Sets the item in the combo box's editor, which effectively changes the displayed/editable text.
     comboBox.getEditor().setItem(text);
 }

 /**
  * Creates a composite key string from a player's name and game name.
  * This key is used for storing and retrieving ScoreEntry objects in the `scoreMap`,
  * ensuring uniqueness for each player-game combination.
  *
  * @param name The player's name.
  * @param gameName The game's name.
  * @return A string representing the composite key (e.g., "PlayerName::GAME::GameName").
  */
 private String getCompositeKey(String name, String gameName) {
     // A simple concatenation with a unique delimiter to distinguish player and game names.
     return name + "::GAME::" + gameName;
 }

 /**
  * Modifies the score of the currently selected entry in the `scoreList` by a given offset.
  * Handles cases where the resulting score might become negative.
  *
  * @param pointsOffset The amount by which to change the score (can be positive or negative).
  */
 private void modifyPointsForSelectedEntry(int pointsOffset) {
     // Get the ScoreEntry object that is currently selected in the JList.
     ScoreEntry selectedEntry = scoreList.getSelectedValue();
      // Validate: Ensure an entry is selected and it's not a "System Message".
      if (selectedEntry == null || "System Message".equals(selectedEntry.getGameName())) {
         showError("Please select a valid score entry to modify.");
         return;
      }
     // Retrieve the corresponding entry from the scoreMap to ensure modifications are on the source data.
     ScoreEntry entryInMap = scoreMap.get(getCompositeKey(selectedEntry.getName(), selectedEntry.getGameName()));
     // Validate: Ensure the entry exists in the map.
     if (entryInMap == null) {
         showError("Selected entry not found in internal map."); // This indicates a potential sync issue.
         return;
     }
     // Calculate the potential new score.
     int potentialNewScore = entryInMap.score + pointsOffset;
     // Handle scores that would become negative due to the modification.
     if (potentialNewScore < 0) {
         Object[] options = {"Set Resulting Score to 0", "Cancel Modification"};
         // Ask the user how to proceed with the negative resulting score.
         int choice = JOptionPane.showOptionDialog(frame,
                 "Resulting score (" + potentialNewScore + ") would be negative. Choose an action:",
                 "Score Adjustment Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                 null, options, options[0]);
         if (choice == 0) { // User chose "Set Resulting Score to 0".
             entryInMap.score = 0;
         } else { // User chose "Cancel Modification" or closed the dialog.
             return; // Do not apply the modification.
         }
     } else {
         // If the resulting score is not negative, update it.
         entryInMap.score = potentialNewScore;
     }
     // Update the date of the score entry to reflect the modification time.
     entryInMap.date = LocalDate.now();
     // Save the updated scores to the persistent storage.
     saveScores();
     // Refresh the leaderboard display to show the changes.
     refreshLeaderboard();
 }

 /**
  * Processes entries from the `pendingQueue` and updates the main data structures (`scoreMap`,
  * `scores` linked list, `scoreTree`, `uniquePlayerNames`, `uniqueGameNames`).
  * If an entry for a player/game already exists, it updates the score if the new score is different.
  * Otherwise, it adds the new entry.
  */
 private void flushQueueToScores() {
     boolean newPlayerNameAdded = false; // Flag to track if a new player name was added to uniquePlayerNames.
     boolean newGameTitleAdded = false;  // Flag to track if a new game name was added to uniqueGameNames.
     boolean dataChanged = false;        // Flag to track if any score data was actually modified or added.

     // Process each ScoreEntry in the pendingQueue.
     while (!pendingQueue.isEmpty()) {
         ScoreEntry newEntryToProcess = pendingQueue.poll(); // Retrieve and remove the head of the queue.
         // Generate the composite key for the scoreMap.
         String compositeKey = getCompositeKey(newEntryToProcess.getName(), newEntryToProcess.getGameName());
         // Check if an entry already exists in the scoreMap for this player/game.
         ScoreEntry existingEntry = scoreMap.get(compositeKey);

         if (existingEntry != null) {
             // If entry exists, check if the new score is different from the existing score.
             if (newEntryToProcess.getScore() != existingEntry.getScore()) {
                 existingEntry.score = newEntryToProcess.getScore(); // Update score.
                 // Ensure score doesn't go below 0 (though initial submission already handles this, this is a safeguard).
                 if (existingEntry.score < 0) existingEntry.score = 0;
                 existingEntry.date = newEntryToProcess.getDate(); // Update date to reflect latest submission.
                 dataChanged = true; // Mark that data has changed.
             }
             // If scores are identical, no action is taken (as per instructions).
         } else {
             // If entry does not exist, add the new entry to scoreMap and other data structures.
             scoreMap.put(compositeKey, newEntryToProcess);
             scores.add(newEntryToProcess); // Add to the linked list (though primarily for refreshLeaderboard).
             scoreTree.insert(newEntryToProcess); // Insert into the BST.
             dataChanged = true; // Mark that data has changed (new entry added).

             // Add player name to uniquePlayerNames set; returns true if it was a new name.
             if (uniquePlayerNames.add(newEntryToProcess.getName())) {
                 newPlayerNameAdded = true;
             }
             // Add game name to uniqueGameNames set; returns true if it was a new game.
             if (uniqueGameNames.add(newEntryToProcess.getGameName())) {
                 newGameTitleAdded = true;
             }
         }
     }

     // If new player names were added, update the player name input combo box model.
     if (newPlayerNameAdded) {
         updatePlayerNameInputComboBoxModel();
     }
     // If new game titles were added, update both game-related combo box models.
     if (newGameTitleAdded) {
         updateGameFilterComboBox();
         updateGameInputComboBoxModel();
     }
     // If any data was changed (score updated or new entry added), save all scores.
     if (dataChanged) {
         saveScores();
     }
 }

 /**
  * Updates the model of the `playerNameInputComboBox` with the current list of unique player names.
  * Preserves the text currently in the editor if possible.
  * Player names are sorted alphabetically.
  */
 private void updatePlayerNameInputComboBoxModel() {
     // Store the current text from the combo box editor (if any) to try and restore it.
     String previousEditorText = getComboBoxText(playerNameInputComboBox);

     // Create a new DefaultComboBoxModel.
     DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
     // Convert the HashSet of unique player names to an ArrayList for sorting.
     ArrayList<String> sortedPlayerNames = new ArrayList<>(uniquePlayerNames);
     Collections.sort(sortedPlayerNames); // Sort player names alphabetically.
     // Add each sorted player name to the new model.
     for (String pn : sortedPlayerNames) {
         model.addElement(pn);
     }
     // Set the new model for the player name combo box.
     playerNameInputComboBox.setModel(model);

     // Attempt to restore the previously entered/selected text.
     if (model.getSize() == 0) {
         // If the model is empty (no players), clear the editor text.
         setComboBoxText(playerNameInputComboBox, "");
     } else {
          setComboBoxText(playerNameInputComboBox, previousEditorText);
     }
 }

 /**
  * Updates the model of the `gameNameInputComboBox` with the current list of unique game names.
  * Preserves the text currently in the editor if possible.
  * Game names are sorted alphabetically.
  */
 private void updateGameInputComboBoxModel() {
     // Store the current text from the combo box editor.
     String previousEditorText = getComboBoxText(gameNameInputComboBox);

     // Create a new model for the combo box.
     DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
     // Get unique game names, sort them, and add to the model.
     ArrayList<String> sortedGameNames = new ArrayList<>(uniqueGameNames);
     Collections.sort(sortedGameNames); // Sort game names alphabetically.
     for (String gn : sortedGameNames) {
         model.addElement(gn);
     }
     // Set the new model for the game name input combo box.
     gameNameInputComboBox.setModel(model);

     // Restore the previously entered/selected text.
     if (model.getSize() == 0) {
         setComboBoxText(gameNameInputComboBox, "");
     } else {
         setComboBoxText(gameNameInputComboBox, previousEditorText);
     }
 }

 /**
  * Refreshes the main leaderboard display (`scoreList`).
  * It rebuilds the `scores` linked list from `scoreMap`, filters entries based on the
  * `gameFilterComboBox` selection, sorts them, and then updates the `listModel`.
  */
 private void refreshLeaderboard() {
     // First, ensure the 'scores' LinkedList is synchronized with the 'scoreMap' (the source of truth).
     scores.clear();
     scores.addAll(scoreMap.values()); // Populate 'scores' with all current ScoreEntry objects from 'scoreMap'.

     // Create a list to hold the entries that will be displayed.
     ArrayList<ScoreEntry> entriesToDisplay = new ArrayList<>();
     // Get the currently selected game from the filter combo box.
     String selectedGame = (String) gameFilterComboBox.getSelectedItem();

     // Determine which entries to display based on the filter.
     if (selectedGame == null || "All Games".equals(selectedGame)) {
         // If "All Games" is selected or no filter is active, add all scores.
         entriesToDisplay.addAll(scores.toArrayList());
     } else {
         // If a specific game is selected, filter scores for that game.
         for (ScoreEntry entry : scores.toArrayList()) {
             if (entry.getGameName().equals(selectedGame)) {
                 entriesToDisplay.add(entry);
             }
         }
     }

     // Sort the entries to be displayed according to the ScoreEntry's compareTo method
     // (Score DESC, Date DESC, Player Name ASC).
     mergeSort(entriesToDisplay, 0, entriesToDisplay.size() - 1);

     // Clear the existing items in the JList's model.
     listModel.clear();
     // Add the sorted and filtered entries to the list model, which updates the JList display.
     for (ScoreEntry entry : entriesToDisplay) {
         listModel.addElement(entry);
     }
 }

 /**
  * Updates the `gameFilterComboBox` with the current list of unique game names.
  * It preserves the currently selected game if it still exists.
  * Game names are sorted alphabetically, with "All Games" as the first option.
  */
 private void updateGameFilterComboBox() {
     // Store the previously selected game filter to try and reselect it.
     String previouslySelected = (String) gameFilterComboBox.getSelectedItem();

     // Get the current model of the game filter combo box.
     DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) gameFilterComboBox.getModel();
     model.removeAllElements(); // Clear existing items.

     // Add the "All Games" option as the first item.
     model.addElement("All Games");

     // Get unique game names, sort them, and add to the model.
     ArrayList<String> sortedGameNames = new ArrayList<>(uniqueGameNames);
     Collections.sort(sortedGameNames); // Sort game names alphabetically.
     for (String gameName : sortedGameNames) {
         model.addElement(gameName);
     }

     // Attempt to reselect the previously selected game filter.
     boolean reselected = false;
     if (previouslySelected != null) {
         for (int i = 0; i < model.getSize(); i++) {
             if (model.getElementAt(i).equals(previouslySelected)) {
                 gameFilterComboBox.setSelectedIndex(i);
                 reselected = true;
                 break;
             }
         }
     }
     // If the previously selected item couldn't be reselected (e.g., it was deleted),
     // default to selecting "All Games".
     if (!reselected) {
         gameFilterComboBox.setSelectedItem("All Games");
     }
 }

 /**
  * Saves all current scores from `scoreMap` to the "scores.txt" file.
  * Scores are written one per line, comma-separated: name,score,date,gameName.
  * The list of scores is sorted before saving to ensure consistent file output,
  * though the primary order in the application is determined by `refreshLeaderboard`.
  */
 private void saveScores() {
     // Create a list from the values in scoreMap for consistent saving.
     ArrayList<ScoreEntry> consistentScores = new ArrayList<>(scoreMap.values());
     Collections.sort(consistentScores);

     // Use try-with-resources to ensure the PrintWriter is closed automatically.
     try (PrintWriter writer = new PrintWriter(new FileWriter("scores.txt"))) {
         // Write each ScoreEntry to the file.
         for (ScoreEntry entry : consistentScores) {
             writer.println(entry.getName() + "," + entry.getScore() + "," + entry.getDate() + "," + entry.getGameName());
         }
     } catch (IOException e) {
         // Handle potential IO errors during saving.
         e.printStackTrace(); // Print stack trace for debugging.
         showError("Error saving scores: " + e.getMessage()); // Show error dialog to user.
     }
 }

 /**
  * Loads scores from the "scores.txt" file into the application's data structures
  * (`scoreMap`, `uniquePlayerNames`, `uniqueGameNames`, `scores` linked list, `scoreTree`).
  * Clears existing data before loading. Handles potential file errors and malformed lines.
  */
 private void loadScores() {
     File file = new File("scores.txt"); // Represents the scores file.

     // Clear all existing data structures before loading from file.
     scoreMap.clear();
     uniquePlayerNames.clear();
     uniqueGameNames.clear();
     scores.clear(); // Custom linked list.
     scoreTree.clear(); // Custom BST.

     // Check if the scores file exists.
     if (file.exists()) {
         // Use try-with-resources for automatic closing of BufferedReader.
         try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
             String line;
             // Read the file line by line.
             while ((line = reader.readLine()) != null) {
                 // Split each line by comma, expecting 4 parts (name, score, date, gameName).
                 String[] parts = line.split(",", 4);
                 if (parts.length == 4) {
                     try {
                         // Parse data from parts.
                         String name = parts[0].trim();
                         int scoreVal = Integer.parseInt(parts[1].trim());
                         if (scoreVal < 0) scoreVal = 0; // Ensure loaded scores are not negative.
                         LocalDate date = LocalDate.parse(parts[2].trim()); // Parse date string.
                         String gameName = parts[3].trim();

                         // Create ScoreEntry and add to data structures.
                         ScoreEntry entry = new ScoreEntry(name, scoreVal, date, gameName);
                         scoreMap.put(getCompositeKey(name, gameName), entry); // Add to the primary map.
                         uniquePlayerNames.add(name); // Add to set of unique player names.
                         uniqueGameNames.add(gameName); // Add to set of unique game names.
                     } catch (Exception ex) {
                         // Catch errors during parsing of a line (e.g., NumberFormatException, DateTimeParseException).
                         System.err.println("Skipping malformed line: '" + line + "'. Error: " + ex.getMessage());
                     }
                 } else {
                     // If a line doesn't have 4 parts, it's considered malformed.
                     System.err.println("Skipping malformed line (incorrect number of parts): " + line);
                 }
             }
             // After loading all entries into scoreMap, populate auxiliary structures.
             scores.addAll(scoreMap.values()); // Populate the ScoreLinkedList.
             for(ScoreEntry se : scoreMap.values()) { // Populate the ScoreBST.
                 scoreTree.insert(se);
             }
         } catch (IOException e) {
             // Handle IO errors during file reading.
             e.printStackTrace();
             showError("Error loading scores: " + e.getMessage());
         }
     }
     // After loading (or if file doesn't exist), update UI components that depend on this data.
     updatePlayerNameInputComboBoxModel();
     updateGameFilterComboBox();
     updateGameInputComboBoxModel();
 }

 /**
  * Utility method to display an error message dialog.
  * @param message The error message to display.
  */
 private void showError(String message) {
     JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
 }

 /**
  * Sorts an ArrayList of ScoreEntry objects using the Merge Sort algorithm.
  * This is a recursive implementation.
  * @param list The ArrayList of ScoreEntry objects to sort.
  * @param left The starting index of the portion of the list to sort.
  * @param right The ending index of the portion of the list to sort.
  */
 private void mergeSort(ArrayList<ScoreEntry> list, int left, int right) {
     // Base case for recursion: if the segment has 0 or 1 element, it's already sorted.
     if (left < right) {
         int mid = (left + right) / 2; // Find the middle point to divide the list into two halves.
         // Recursively sort the first half.
         mergeSort(list, left, mid);
         // Recursively sort the second half.
         mergeSort(list, mid + 1, right);
         // Merge the two sorted halves.
         merge(list, left, mid, right);
     }
 }

 /**
  * Merges two sorted sub-arrays of ScoreEntry objects into a single sorted sub-array.
  * This is a helper method for `mergeSort`.
  * The sorting order is defined by `ScoreEntry.compareTo()`.
  * @param list The original ArrayList containing the sub-arrays.
  * @param left The starting index of the first sub-array.
  * @param mid The ending index of the first sub-array.
  * @param right The ending index of the second sub-array.
  */
 private void merge(ArrayList<ScoreEntry> list, int left, int mid, int right) {
     // Create temporary ArrayLists for the left and right sub-arrays.
     ArrayList<ScoreEntry> leftList = new ArrayList<>(list.subList(left, mid + 1));
     ArrayList<ScoreEntry> rightList = new ArrayList<>(list.subList(mid + 1, right + 1));

     // Initialize indices for iterating through leftList, rightList, and the original list.
     int i = 0, j = 0; // Pointers for leftList and rightList respectively.
     int k = left;     // Pointer for the original list (where merged elements are placed).

     // Compare elements from leftList and rightList and place the smaller one into the original list.
     while (i < leftList.size() && j < rightList.size()) {
         if (leftList.get(i).compareTo(rightList.get(j)) <= 0) {
             list.set(k++, leftList.get(i++));
         } else {
             list.set(k++, rightList.get(j++));
         }
     }

     // Copy any remaining elements from leftList (if any).
     while (i < leftList.size()) {
         list.set(k++, leftList.get(i++));
     }
     // Copy any remaining elements from rightList (if any).
     while (j < rightList.size()) {
         list.set(k++, rightList.get(j++));
     }
 }

 /**
  * Represents a single score entry in the leaderboard.
  * Contains player's name, score, date of score, and game name.
  * Implements Comparable for sorting purposes.
  */
 static class ScoreEntry implements Comparable<ScoreEntry> {
     String name;        // Player's name.
     int score;          // Player's score.
     LocalDate date;     // Date the score was achieved/recorded.
     String gameName;    // Name of the game.

     /**
      * Constructor for ScoreEntry.
      * @param name Player's name.
      * @param score Player's score.
      * @param date Date of the score.
      * @param gameName Name of the game.
      */
     public ScoreEntry(String name, int score, LocalDate date, String gameName) {
         this.name = name;
         this.score = score;
         this.date = date;
         this.gameName = gameName;
     }

     // Getter methods for the fields.
     public String getName() { return name; }
     public int getScore() { return score; }
     public LocalDate getDate() { return date; }
     public String getGameName() { return gameName; }

     /**
      * Compares this ScoreEntry with another for ordering.
      * The primary sort key is score (descending).
      * Secondary sort key is date (most recent first, i.e., descending).
      * Tertiary sort key is player name (alphabetical, ascending).
      * @param other The other ScoreEntry to compare against.
      * @return A negative integer, zero, or a positive integer as this object
      * is less than, equal to, or greater than the specified object according to the sort order.
      */
     @Override
     public int compareTo(ScoreEntry other) {
         // Compare scores: higher score comes first (descending).
         int scoreCompare = Integer.compare(other.score, this.score);
         if (scoreCompare != 0) return scoreCompare;
         // If scores are equal, compare dates: more recent date comes first (descending).
         int dateCompare = other.date.compareTo(this.date);
         if (dateCompare != 0) return dateCompare;
         // If scores and dates are equal, compare names alphabetically (ascending).
         return this.name.compareTo(other.name);
     }

     /**
      * Provides a string representation of the ScoreEntry, used for display in the JList.
      * @return Formatted string (e.g., "PlayerName - Score - Date (GameName)").
      */
     @Override
     public String toString() {
         return name + " - " + score + " - " + date + " (" + gameName + ")";
     }

     /**
      * Checks if this ScoreEntry is equal to another object.
      * Equality is based on all fields: name, score, date, and gameName.
      * @param o The object to compare with.
      * @return True if the objects are equal, false otherwise.
      */
     @Override
     public boolean equals(Object o) {
         if (this == o) return true; // Same object instance.
         if (o == null || getClass() != o.getClass()) return false; // Different type or null.
         ScoreEntry that = (ScoreEntry) o; // Cast to ScoreEntry.
         // Compare all fields for equality.
         return score == that.score &&
                Objects.equals(name, that.name) &&
                Objects.equals(date, that.date) &&
                Objects.equals(gameName, that.gameName);
     }

     /**
      * Generates a hash code for this ScoreEntry.
      * Based on all fields: name, score, date, and gameName.
      * @return The hash code value.
      */
     @Override
     public int hashCode() {
         return Objects.hash(name, score, date, gameName);
     }
 }

 /**
  * A simple singly linked list implementation to store ScoreEntry objects.
  */
 static class ScoreLinkedList {
     private ScoreNode head; // Head of the linked list.

     /**
      * Adds a new ScoreEntry to the end of the linked list.
      * @param entry The ScoreEntry to add.
      */
     public void add(ScoreEntry entry) {
         ScoreNode newNode = new ScoreNode(entry);
         if (head == null) { // If list is empty, new node becomes the head.
             head = newNode;
         } else { // Traverse to the end of the list and add the new node.
             ScoreNode current = head;
             while (current.next != null) {
                 current = current.next;
             }
             current.next = newNode;
         }
     }

     /**
      * Adds all ScoreEntry objects from a collection to this linked list.
      * @param entries A collection of ScoreEntry objects.
      */
     public void addAll(Collection<ScoreEntry> entries) {
         for (ScoreEntry entry : entries) {
             this.add(entry);
         }
     }

     /**
      * Deletes a specific ScoreEntry from the linked list that matches all provided fields.
      * This is a precise match deletion.
      * @param name Player's name of the entry to delete.
      * @param gameName Game name of the entry to delete.
      * @param score Score of the entry to delete.
      * @param date Date of the entry to delete.
      */
     public void deleteSpecificEntry(String name, String gameName, int score, LocalDate date) {
         if (head == null) return; // List is empty, nothing to delete.

         // Check if the head node is the one to be deleted.
         ScoreEntry headData = head.data;
         if (headData.getName().equals(name) && headData.getGameName().equals(gameName) &&
             headData.getScore() == score && headData.getDate().equals(date)) {
             head = head.next; // Update head to the next node.
             return;
         }

         // Traverse the list to find the node to delete.
         ScoreNode current = head;
         ScoreNode prev = null;
         while (current != null) {
             ScoreEntry currentData = current.data;
             if (currentData.getName().equals(name) && currentData.getGameName().equals(gameName) &&
                 currentData.getScore() == score && currentData.getDate().equals(date)) {
                 // Found the node to delete.
                 if (prev != null) {
                     prev.next = current.next; // Unlink the node from the list.
                 }
                 return; // Exit after deleting.
             }
             prev = current;
             current = current.next;
         }
     }

     /**
      * Converts the linked list to an ArrayList of ScoreEntry objects.
      * @return An ArrayList containing all ScoreEntry objects from the linked list.
      */
     public ArrayList<ScoreEntry> toArrayList() {
         ArrayList<ScoreEntry> list = new ArrayList<>();
         ScoreNode current = head;
         while (current != null) { // Traverse the list.
             list.add(current.data); // Add each entry to the ArrayList.
             current = current.next;
         }
         return list;
     }

     /**
      * Clears all entries from the linked list by setting the head to null.
      */
     public void clear() {
         head = null;
     }

     /**
      * Inner class representing a node in the ScoreLinkedList.
      * Each node holds a ScoreEntry and a reference to the next node.
      */
     static class ScoreNode {
         ScoreEntry data;    // The ScoreEntry stored in this node.
         ScoreNode next;     // Reference to the next node in the list.

         public ScoreNode(ScoreEntry data) {
             this.data = data;
             this.next = null; // Initially, the next node is null.
         }
     }
 }

 /**
  * A Binary Search Tree (BST) implementation to store ScoreEntry objects.
  * The BST is ordered by player name (String comparison).
  */
 static class ScoreBST {
     private Node root; // Root of the BST.

     /**
      * Inserts a ScoreEntry into the BST.
      * If a ScoreEntry with the same player name already exists, its data is updated.
      * @param entry The ScoreEntry to insert.
      */
     public void insert(ScoreEntry entry) {
         root = insertRec(root, entry);
     }

     /**
      * Recursive helper method for inserting a ScoreEntry into the BST.
      * Ordering is based on player name.
      * @param currentRoot The current root of the subtree to insert into.
      * @param entry The ScoreEntry to insert.
      * @return The root of the modified subtree.
      */
     private Node insertRec(Node currentRoot, ScoreEntry entry) {
         if (currentRoot == null) {
             return new Node(entry);
         }
         int nameComparison = entry.getName().compareTo(currentRoot.data.getName());
         if (nameComparison < 0) {
             currentRoot.left = insertRec(currentRoot.left, entry);
         } else if (nameComparison > 0) {
             currentRoot.right = insertRec(currentRoot.right, entry);
         } else {
             currentRoot.data = entry;
         }
         return currentRoot;
     }

     /**
      * Finds a ScoreEntry in the BST by player name.
      * @param name The player name to search for.
      * @return The ScoreEntry if found, otherwise null.
      */
     public ScoreEntry findByName(String name) {
         Node current = root;
         while (current != null) {
             int cmp = name.compareTo(current.data.getName());
             if (cmp == 0) return current.data;
             current = (cmp < 0) ? current.left : current.right;
         }
         return null;
     }

     /**
      * Deletes a ScoreEntry from the BST based on player name.
      * @param name The player name of the entry to delete.
      */
     public void delete(String name) {
         root = deleteRec(root, name);
     }

     /**
      * Recursive helper method for deleting a ScoreEntry from the BST by name.
      * @param currentRoot The current root of the subtree to delete from.
      * @param name The player name to delete.
      * @return The root of the modified subtree.
      */
     private Node deleteRec(Node currentRoot, String name) {
         if (currentRoot == null) return null;
         int cmp = name.compareTo(currentRoot.data.getName());
         if (cmp < 0) {
             currentRoot.left = deleteRec(currentRoot.left, name);
         } else if (cmp > 0) {
             currentRoot.right = deleteRec(currentRoot.right, name);
         } else {
             if (currentRoot.left == null) return currentRoot.right;
             if (currentRoot.right == null) return currentRoot.left;
             Node minNode = findMin(currentRoot.right);
             currentRoot.data = minNode.data;
             currentRoot.right = deleteRec(currentRoot.right, minNode.data.getName());
         }
         return currentRoot;
     }

     /**
      * Finds the node with the minimum value (leftmost node) in a given subtree.
      * @param node The root of the subtree.
      * @return The node with the minimum value.
      */
     private Node findMin(Node node) {
         while (node.left != null) {
             node = node.left;
         }
         return node;
     }

     /**
      * Clears all entries from the BST by setting the root to null.
      */
     public void clear() {
         root = null;
     }

     /**
      * Inner class representing a node in the ScoreBST.
      * Each node holds a ScoreEntry and references to left and right children.
      */
     static class Node {
         ScoreEntry data;    // The ScoreEntry stored in this node.
         Node left, right;   // References to left and right child nodes.

         public Node(ScoreEntry data) {
             this.data = data;
             left = right = null; // Initialize children to null.
         }
     }
 }
}