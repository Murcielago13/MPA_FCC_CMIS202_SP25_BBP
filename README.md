# MPA_FCC_CMIS202_SP25_BBP

[Leaderboard System]

(Who is this for?)
This software is for game developers, students, and hobbyists who want to integrate a simple high score or leaderboard system into their games or applications.

(What is the purpose?)
The purpose of this software is to track, manage, and display player scores for various games in a sorted leaderboard. It saves player names, scores, game names, and the date the score was achieved, allowing for game-specific views and comprehensive data administration.

(Where and When is it used?)
This can be used in desktop-based Java games or learning environments where keeping a persistent record of scores is necessary. It is designed to run on any desktop with Java installed and can be used after each game session or during game runtime for score submissions and viewing.

(How does it work?)
Players interact with a graphical user interface (GUI) to input their name (or select an existing one from a dynamic list), their score, and the game name (which can also be selected or newly added). Submitted scores are briefly placed in a queue before being processed and integrated. The system offers robust data management: users can filter the leaderboard by specific games, search for all scores by a particular player, modify existing scores (set to zero, or apply custom/quick point adjustments), and manage player and game data (e.g., delete all data for a specific player via the name input field, or remove an entire game category and all its associated scores). Most critical operations like submissions, deletions, and modifications are now protected by confirmation dialogs to ensure data integrity. Internally, a custom singly linked list is used for one representation of score entries, a binary search tree (BST) allows for efficient player name lookups, and a hash table (HashMap) provides constant-time access to specific score records (player-game unique). All scores are saved to a local file ("scores.txt") for persistence, and the leaderboard displays entries sorted by score (descending), then date (most recent), then player name, using the Merge Sort algorithm.

(Why use this system?)
Easy to integrate and customize, offering comprehensive controls for score management with enhanced safety through operation confirmations. Includes efficient sorting, a well-structured object-oriented design, and demonstrates practical usage of both linear and nonlinear data structures (queue, custom linked list, BST, HashMap). Provides persistent file storage without needing a database. It serves as an excellent learning tool for Java fundamentals, OOP principles, advanced GUI programming with Swing (including dynamic component updates and user confirmations), file I/O, robust data structure implementation, and algorithm application.
