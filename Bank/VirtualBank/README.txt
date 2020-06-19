Joshua Yang
joy222
CSE 241 Semester Long Project

WHAT IS WHERE:
	All of my data files that I have created randomly and input are in the "infos" directory.
	All the procedures that I have wrote for transactions(deposit, withdrawl, checkbalance, transactions) are
	in "Packages and Java" directory.
	My tables are listed in "checkpoint 2.txt"(inside directory infos) and all the necessary files to run the 
	program is within the same directory. 
	The interface is named "joy222.java" inside "User Interface" directory and you will be able to find all the 
	User Interface code I used for inside this file.
	I hope you stay healthy and thank you for reading!

Oracle ID: joy222
Oracle Password: Didtngus318!

HOW TO RUN:
Run these commands in the cmdline to compile and run.
javac -cp .;.ojdbc8.jar joy222.java

jar cfmv joy222.jar Manifest.txt joy222.class

java -jar joy222.jar

To begin, enter the oracle ID and password given to access my database.
You will be asked to enter 1,2,3 or 4 to do following commands:
1. Manage Transactions
2. Create a new account.
3. Purchase an item using a debit/credit card
4. quit
Entering anything invalid(i.e, non-numbers or numbers other than 1,2,3, or 4)
will force you to enter again until you enter a valid number. 
(This applies for future inputs too. Entering invalid format for i.e account number will loop until you enter
a valid account number).

1. Manage Transactions
	Here you can either deposit, withdrawl, or check your balance by entering one of the names given
	from the database system. Then, you will be asked to choose the branch ID out of the given branches
	from the databse. Some branches have only ATMs, some have Tellers, and some have both tellers and ATM
	machines. Depending on which branch you choose, you can either do all three transactions(deposit, withdrawl,
	check balance) or only withdrawl and check balance. After choosing the branch, the system will determine if
	the amounnt you have entered is valid (overdraft, minimum balance) and proceed as you wish. Obviously, drafting
	more than what you have will cause and error and ask you to re-enter the amount. After all the transactions you
	wish to do, you can go back to the main menu or quit.)

2. Create a New Account
	Here you can create a new account by entering your name, address and the amount you want to deposit into your new
	account. The system will automatically create an account with random account number and customerID after you have
	entered all the information asked by the program.

3. Purchase an item Using your debit/credit card
	Here you are able to purchase an item using your debit/credit card depending on which card you have. Each customer
	will have either debit/credit card and debit card is related to the account the customer has. Credit cards, however,
	will have a credit limit that you cannot go over. Both cards output an error if you wish to spend more than what you
	have(account balance for debit, credit limit for credit) and will ask you to enter the amount again until your input
	is valid.

4. Quit
	This is just a quit function.

IMPORTANT NOTES

	Some Accounts that you may want to test are 
	
	"Shay L. Brooks"
	"Kiona O. Sellers"
	or basically any names that show up in the system.
	You can also create a new account for yourself to test various functions out.

Thank you and Enjoy!
