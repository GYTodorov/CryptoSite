Create new schema in mysql and set it as default then execute the lines below (I named mine cryptosite)
and make sure to update the application.properties to match your schema name along with your login information

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    balance DECIMAL(18,2) DEFAULT 10000.00
);

CREATE TABLE holdings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    pair VARCHAR(50),
    quantity DECIMAL(18,8),
	buy_price double,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    type ENUM('BUY', 'SELL'),
    pair VARCHAR(50),
    quantity DECIMAL(18,8),
    price DECIMAL(18,8),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    profit_loss DECIMAL(18,2),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO users (username) VALUES ("testUser");

Run the code in intelij.
Start cmd administrator and do npm start from the frontend folder to start the react.js
(Don't forget to start your mysql service from Services in windows)
Everything should be working, have fun with this small crypto site trading demo