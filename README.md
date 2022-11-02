# Telegram Bot for expenses accounting on Java.
Using a Telegram bot is convenient becouse it is always at hand on the phone, does not require additional settings and memory. The entered amounts will not be lost, as they will be saved in the database in the cloud. As bot was written for a Russian-speaking user, the keywords and messages of the bot are written in Russian.

## Features:
- saves in the PostGre database the amount of expenses by date, category and user;
- division into categories proceeds automatically according to the entered keyword or when selecting the appropriate item from the drop-down menu;
- keywords are pre-specified in the Enumeration;
- shows expenses by the one user or for all;
- shows expenses for all categories or only for the selected one from the menu that appears;
- user can unload the table in excel;
- you can delete the last incorrect entry in the table;
- you can enter the receipt of money and receive a report on the balance;
- you can view the list of keywords by selecting the appropriate command from the list in the menu.

## Screenshot

![screen1](/screenshot1.jpg)

![screen1](https://drive.google.com/file/d/1RfqRLmTCKGTmenmjOjXeyiN9_gkzrh0_/view?usp=sharing)

## How to start

 1. In telegram in the search bar you are looking for the @FinanceToBot bot.
 2. Add it to your chat.
 3. Enter the expense.
 
      3.1. Write a message consisting of sums (arithmetic operations of addition and subtraction can also be used) and a comment containing a keyword. The integer separator in the sum is ".".
      
      For example:
      
      100+70 в кафе с Машей      
      45.45 развлечение прогулка с Таней
      
      3.2 or select the menu item "/enter_expense" and select the appropriate category and enter the amount of the expense. You can add a comment if you wish.
   4. If there was an error in the message, but the entry has already been made to the table, you can delete it using the "/delete_insert" function from the menu.
   5. Using the menu items, get the necessary reports or upload the table to Excel.

![screen2](/screenshot2.jpg,"TelegramBot")


![screen2](https://drive.google.com/file/d/1MWis2fd1I-3EOM-64zwy8ZR6loA_rvac/view?usp=sharing)