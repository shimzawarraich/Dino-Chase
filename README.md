# Dino Chase

## Project Information  
**Dino Chase** is an interactive multiplayer Java application where players control dinosaurs on a graphical game map, trying to avoid becoming the "Chaser." The last dinosaur tagged when the timer ends is the loser.  

---

## Main Functionalities  
- **Real-time Multiplayer Gameplay** – Supports 2–4 players.  
- **Dynamic Chasing Mechanics** – Avoid the "Chaser" or become one to tag opponents.  
- **Interactive GUI** – Features a user-friendly menu, custom cursor, vibrant game map, and animated dinosaurs.  
- **User Authentication & Progress Tracking** – Log in/sign up and track progress after each round.  
- **Loserboard** – A leaderboard highlighting players with the most losses.  
- **Sound Effects & Music** – Enhanced gameplay with audio.  
- **Chat Lobby** – Communicate with other players in real time.  

---


## How to Run the Application  

### **Prerequisites**  
- **Java Development Kit (JDK)**: Version 11 or higher.  
- **Operating Systems**: Windows, macOS, or Linux.  
- **Build Tool**: Maven or an IDE supporting Maven (e.g., IntelliJ IDEA, Eclipse).  

### **Setup Instructions**  
1. Clone the repository
   
   ```bash
   git clone https://github.com/OntarioTech-CS-program/w25-csci2020u-finalproject-khan-morshed-morshed-warraich-khan.git
   ```
   
3. Navigate to Project Directory
   
- Open your terminal or command prompt and navigate to your project's root folder.
  
   ```bash
     cd dino-chase
   ```
   
3. Open the repository in your desired maven supported IDE or directly build the project using maven
   
4. Open the application and run the server first.
   - Run Server.java directly from your IDE. You can find this at `src/main/java/org/example/Server.java`
   - Right-click Server.java and then select run
  
7. Connect the Clients to the server: Start multiple client instances (Client.java) to connect to the server.
You can find client server in `src/main/java/org/example/Server.java`
   - Run Client.java directly from your IDE
   - Right-click Client.java and then select run


## Screenshots

<div style="display: flex; justify-content: center; flex-wrap: wrap; gap: 10px;">
    <img src="./images/screenshot1.png" width="45%" />
    <img src="./images/screenshot2.png" width="45%" />
</div>

<div style="display: flex; justify-content: center; flex-wrap: wrap; gap: 10px; margin-top: 20px;">
    <img src="./images/screenshot3.png" width="30%" />
    <img src="./images/screenshot4.png" width="30%" />
    <img src="./images/screenshot5.png" width="30%" />
</div>

<div style="display: flex; justify-content: center; flex-wrap: wrap; gap: 10px; margin-top: 20px;">
    <img src="./images/screenshot6.png" width="45%" />
    <img src="./images/screenshot7.png" width="45%" />
</div>

<div style="display: flex; justify-content: center; flex-wrap: wrap; gap: 10px; margin-top: 20px;">
    <img src="./images/screenshot8.png" width="45%" />
    <img src="./images/screenshot9.png" width="45%" />
</div>



---

## Resources

### Libraries and Tools Used
- Java Swing (Built-in GUI library)
- Java Sound API (Built in audio library)
- Java Sockets (Networking and real-time multiplayer synchronization)
- Maven (Project build and dependency management)

### References 
- Utilized basic Java Swing examples for GUI structure.
- Utilized video tutorials for menu implementation. 

## Collaborators 
- Malasa Khan 
- Mehreen Morshed 
- Muskan Morshed 
- Rameen Khan 
- Shimza Warraich
