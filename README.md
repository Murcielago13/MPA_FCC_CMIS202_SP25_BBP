# MPA_FCC_CMIS202_SP25_BBP

[Leaderboard System] 

(Who is this for?)

This software is for game developers, students, and hobbyists who want to integrate a simple high score or leaderboard system into their games or applications.

(What is the purpose?)

The purpose of this software is to track and display player scores in a sorted leaderboard. It saves player names, scores, and the date the score was achieved.

(Where and When is it used?)

This can be used in desktop-based Java games or learning environments where keeping a persistent record of scores is necessary. It is designed to run on any desktop with Java installed and can be used after each game session or during game runtime.

(How does it work?)

Players enter their name and score into a simple GUI. Scores are first stored in a queue before being processed and added to the leaderboard, ensuring batch handling of new entries. The leaderboard uses a custom singly linked list structure to store score entries, providing a hands-on implementation of linear data structures. All scores are saved to a local file for persistence, and the leaderboard displays the entries sorted in descending order using the Merge Sort algorithm.

(Why use this system?)

Easy to integrate and customize. Includes efficient sorting and a well-structured object-oriented design using inheritance and generics. Demonstrates practical usage of linear data structures by integrating a queue from the Java API and a custom linked list. Provides persistent file storage without needing a database. It is an excellent learning tool for Java fundamentals, OOP principles, GUI programming, file I/O, data structures, and algorithm implementation.
