# 🦖 Dino Chase

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


## Screenshots and Demo Video

## Demo Video
https://drive.google.com/file/d/1vH7Iqzu0IqSv7AKIRclqLK-bA6kUp07b/view?usp=sharing

## 🎮 Menu

When you launch the game, you'll be greeted with the main menu.

<img width="592" alt="Screenshot 2025-04-06 at 10 55 38 PM" src="https://github.com/user-attachments/assets/5fdaa80d-3a29-4d8c-9979-ac5d2cd343bd" />


---

## 🔐 Login & Signup

### Logging In

After pressing **Play**, you're taken to a login window.

<img width="592" alt="Screenshot 2025-04-06 at 10 58 13 PM" src="https://github.com/user-attachments/assets/f6ac5b4f-124d-4c67-93d6-80471a386a57" />



Type in your credentials and press **OK**.

- If your account doesn't exist, it will prompt you to **sign up**.

<img width="595" alt="Sign-up" src="https://github.com/user-attachments/assets/6f83ee99-0ff6-4317-8b08-b63f246e0947" />


### Signing Up

Once you register successfully, you’ll see a confirmation message.
<img width="586" alt="Screenshot 2025-04-06 at 11 05 12 PM" src="https://github.com/user-attachments/assets/623d6976-5f6a-4ac0-afef-80a7ffe81b00" />



You can then log in using your newly created credentials.

- If you enter an incorrect password, an error message will be displayed.
<img width="592" alt="Screenshot 2025-04-06 at 11 05 42 PM" src="https://github.com/user-attachments/assets/325e4e19-ccee-46f3-86da-45c6c1305126" />



---

## 🕹️ Game Setup

After successful login:

- You are taken to the **game window**.
- If you're the **first player** to join the server, you’ll be asked how many players will be in the game.
<img width="850" alt="Screenshot 2025-04-06 at 11 07 05 PM" src="https://github.com/user-attachments/assets/95c451c7-1c30-4455-98e9-3fc93dabf4e3" />

<img width="273" alt="Screenshot 2025-04-06 at 11 07 18 PM" src="https://github.com/user-attachments/assets/0bf6ab31-89bd-4009-8900-75dd1048edc6" />



Then, you'll see a waiting screen until all players join:
<img width="865" alt="Screenshot 2025-04-06 at 11 07 40 PM" src="https://github.com/user-attachments/assets/7a7d9a36-4c1f-4e55-8cd4-e061b88b55aa" />



---

## 💬 Chat System

On the **left side** of the game window, there is a chat box available before the game begins.

<img width="256" alt="Screenshot 2025-04-06 at 11 11 47 PM" src="https://github.com/user-attachments/assets/ebbff70a-4774-4604-aa19-42b2c37996fb" />


You can chat with other players who are also in the lobby waiting.
<img width="1075" alt="Screenshot 2025-04-06 at 11 10 19 PM" src="https://github.com/user-attachments/assets/59baffa8-1917-4618-9849-b125d9b8ae17" />



---

## 🎯 Game Start - Chaser Selection

Once all players have joined, a **roulette wheel** animation appears to randomly select the **chaser**, along with a message showing who it is.
<img width="611" alt="Screenshot 2025-04-06 at 11 15 36 PM" src="https://github.com/user-attachments/assets/26fd4694-27b1-4ad0-9dbb-340baa085740" />

<img width="758" alt="Screenshot 2025-04-06 at 11 16 48 PM" src="https://github.com/user-attachments/assets/c3269454-b6fa-4666-ae16-830258b8519f" />



---

## 🔴 Gameplay

The game begins!

<img width="850" alt="Screenshot 2025-04-06 at 11 50 27 PM" src="https://github.com/user-attachments/assets/c59ec594-0b57-4fa9-8a91-4f0d63210412" />

- The chaser must tag other players.
- When someone is tagged:
  - Their **screen flashes red**
  - Their **name turns red**
<img width="429" alt="Screenshot 2025-04-06 at 11 39 44 PM" src="https://github.com/user-attachments/assets/a03a6b1d-118c-472f-8431-e364fec8a991" />



The event is also posted in the chat:

<img width="716" alt="Screenshot 2025-04-06 at 11 40 32 PM" src="https://github.com/user-attachments/assets/dcb11ec2-a0a2-4e4d-997c-6e5c6aa57d64" />


---

## ⏰ Game Over

When the timer runs out:

- A message is shown announcing the **loser**
- The **scoreboard** is displayed
<img width="737" alt="Screenshot 2025-04-06 at 11 21 39 PM" src="https://github.com/user-attachments/assets/8ab43327-3757-4e66-8681-b05e01131022" />
<img width="372" alt="Screenshot 2025-04-06 at 11 22 01 PM" src="https://github.com/user-attachments/assets/607419ff-3560-4833-9de0-b83e925f490f" />


---

## ❓ Help Menu

Clicking the **Help** button shows a guide for controls or instructions.
<img width="590" alt="Screenshot 2025-04-06 at 10 59 23 PM" src="https://github.com/user-attachments/assets/068ad158-757d-4851-bb10-dd178720a49b" />



---

## 🏆 Loserboard

Clicking the **Loserboard** shows past game results or rankings.

<img width="490" alt="Screenshot 2025-04-06 at 11 00 47 PM" src="https://github.com/user-attachments/assets/4bbaf473-7244-4256-ba44-7aff2e4ccdeb" />


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
