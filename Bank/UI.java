import java.io.*;
import java.sql.*;
import java.util.Scanner;

import java.util.Random;

public class joy222 {

    public static void main(String[] args) throws java.lang.ClassNotFoundException, IOException, SQLException
    {
        boolean success = true; //declaring and initializing all the variables
        boolean fetchacct = false, fetchbranch = false;
        Scanner sc = new Scanner(System.in);
        Connection sqlconnect = null;
        String customername, username, password;
        int acctnum = 0, branchnum, managefirst;
     

        do 
        {
            success = true; //success condition

            try
            {
                System.out.println("Oracle User ID: "); //login stuff
                username = sc.nextLine();
                System.out.println("Oracle User Password: ");
                password = sc.nextLine();
                sqlconnect = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", username, password);
                break;
            }

            catch (Exception ex)
            {
                success = false;
                System.out.println("Authentication failed, Please try again."); //when authentification has failed, catch exception
            }

        } while (success == false); //repeat until it is true

        mainmenu(sqlconnect);
    }

    public static void mainmenu(Connection sqlconnect) throws SQLException
    {
        boolean success = true; //declaring and initializing all the variables
        boolean fetchacct = false, fetchbranch = false;
        Scanner sc = new Scanner(System.in);
        String customername, username, password;
        int acctnum = 0, branchnum, managefirst;
        String accttypequery = "select accttype from account where acctnum = ?";
        String accounttype = "";

        System.out.println("Welcome to 241 Bank!");
        String namequery = "SELECT CUSTNAME FROM CUSTOMER";
        System.out.println("");
        System.out.println("Press 1 to manage transactions,");
        System.out.println("Press 2 to Create a New Account,");
        System.out.println("Press 3 to Purchase an item Using Your Debit/Credit Card,");
        System.out.println("Press 4 to quit.");
        
        while (true)
        {
            try
            {
                managefirst = Integer.parseInt(sc.next());

                while (managefirst < 1 || managefirst > 4)
                {
                    System.out.println("Invalid Input. Please try again.");
                    managefirst = Integer.parseInt(sc.next());
                }

                break;
            }

            catch(NumberFormatException ex)
            {
                System.out.println("Invalid Input. Please try again.");
            }
        }
        
        if (managefirst == 1)
        {
            managetransaction(sqlconnect);
        }

        else if (managefirst == 2)
        {
            createNewAccount(sqlconnect);
            
        }

        else if (managefirst == 3)
        {
            CardPurchases(sqlconnect);
        }

        else if (managefirst == 4)
        {
            System.exit(0);
        }
    }

    public static void managetransaction(Connection sqlconnect) throws SQLException
    {
        String namequery = "SELECT CUSTNAME FROM CUSTOMER";
        String purchasequery = "{call transactionpkg.createpurchase(?, ?)}";
        boolean success = true; //declaring and initializing all the variables
        boolean fetchacct = false, fetchbranch = false;
        Scanner sc = new Scanner(System.in);
        String customername, username, password;
        int acctnum = 0, branchnum, managefirst;
        String accttypequery = "select accttype from account where acctnum = ?";
        String accounttype = "";
        PreparedStatement preparedStatement = null;
        CallableStatement transactions = null; 
        ResultSet resultSet = null;
        boolean validnum = false;

        preparedStatement = sqlconnect.prepareStatement(namequery);
        resultSet = preparedStatement.executeQuery();
        System.out.println("Here are the list of names that are currently in the system.");

        while (resultSet.next())
        {
            System.out.println(resultSet.getString(1));
        }

     

        String acctquery = "SELECT ACCTNUM FROM HAS,customer WHERE has.custid = customer.custid and customer.CUSTNAME = ?";
        String branchquery = "SELECT * FROM BRANCH WHERE BRANCHID = ?";
        String depositquery = "{call transactionpkg.depositmoney('d', ?, ?)}";
        String withdrawlquery = "{call transactionpkg.withdrawlmoney('w', ?, ?)}";
        String checkbalance = "SELECT acctbal FROM ACCOUNT WHERE ACCTNUM = ?";
        String branchlocquery = "select branchID,branchName, branchtype from branch";
        int transtype = 0, status = 0;
        double amount = 0.0;         
          
        while (acctnum == 0)
        {
            System.out.println("\nPlease enter your Name: ");
            
            customername = sc.nextLine();
            

            preparedStatement = sqlconnect.prepareStatement(acctquery);
            preparedStatement.setString(1, customername);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                acctnum = resultSet.getInt(1);
            }

            if (acctnum == 0)
            {
                System.out.println("No such account number for the name exist. Please Try again.");
            }
        }

        System.out.println("This is your account ID: " + acctnum);
        System.out.println("These are the currently available Branches.");
        System.out.println("");

        preparedStatement = sqlconnect.prepareStatement(accttypequery); //check the account type
        preparedStatement.setInt(1, acctnum);
        resultSet = preparedStatement.executeQuery();

        if (resultSet.next())
        {
            accounttype = resultSet.getString(1);
        }

        System.out.println("Branch ID   |   Branch Locations");
        preparedStatement = sqlconnect.prepareStatement(branchlocquery);
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            System.out.println("    " + resultSet.getString(1) + "          " + resultSet.getString(2));
        }

        System.out.println("Please enter your branch ID: ");
        branchnum = sc.nextInt();
        preparedStatement = sqlconnect.prepareStatement(branchquery);
        preparedStatement.setInt(1, branchnum);
        resultSet = preparedStatement.executeQuery();
        
        while (!resultSet.next())
        {
            System.out.println("Invalid branch ID. Please Try again.");
            System.out.println("Please enter your branch ID: ");
            branchnum = sc.nextInt();
            preparedStatement = sqlconnect.prepareStatement(branchquery);
            preparedStatement.setInt(1, branchnum);
            resultSet = preparedStatement.executeQuery();
        }

        if (resultSet.getString(4).toUpperCase().equals("A") || resultSet.getString(4).equals("a"))// change everythings
        {
            System.out.println("The branch ID you have entered offers only ATM machines.");
            System.out.println("Please select your transaction type.");
            System.out.println("2 to withdrawl money or 3 for checking balance.");
            transtype = sc.nextInt();

        }

        else if (resultSet.getString(4).equals("T") || resultSet.getString(4).equals("t"))
        {
            System.out.println("The branch ID you have entered offers in-person transactions with tellers.");
            System.out.println("Please select your transaction type.");
            System.out.println("1 to deposit money, 2 to withdrawl money or 3 to check balance.");
            transtype = sc.nextInt();
        }

        else if (resultSet.getString(4).equals("B") || resultSet.getString(4).equals("b"))
        {
            System.out.println("The branch ID you have entered offers both ATM machines and in-person transactions");
            System.out.println("Please select your transaction type.");
            System.out.println("1 to deposit money, 2 to withdrawl money or 3 to check balance.");
            transtype = sc.nextInt();
        } 

        if (transtype == 1)
        {
            System.out.println("Please enter the amount you wish to deposit");
            amount = sc.nextDouble();
            transactions = sqlconnect.prepareCall(depositquery);
            transactions.setInt(1, acctnum);
            transactions.setDouble(2, amount);
            transactions.execute();
            
            System.out.println("Transaction was Complete.");
            System.out.println("$" + amount + " was deposited into your account.");

            transactions.close();

            
            preparedStatement = sqlconnect.prepareStatement(checkbalance);
            preparedStatement.setInt(1, acctnum);
            resultSet = preparedStatement.executeQuery();

            double acctbal = 0.0;

            while (resultSet.next())
            {
                acctbal = resultSet.getDouble(1);
            }
        
            System.out.println("Your remaining balance is: " + acctbal);
        }

        else if (transtype == 2)
        {
            String overdraft = "select acctbal from account where acctnum = ?";
            double realacctbal = 0.0;
            System.out.println("Please enter the amount you wish to withdrawl");
            amount = sc.nextDouble();

            preparedStatement = sqlconnect.prepareStatement(overdraft);
            preparedStatement.setInt(1, acctnum);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            {
                realacctbal = resultSet.getDouble(1);
            }

            while ((realacctbal - amount) < 0)
            {
                System.out.println("Error. Attempting to overdraft.");
                System.out.println("Please re-enter the amount you wish to withdrawl.");
                amount = sc.nextDouble();
            }

            if ((realacctbal - amount) < 200)
            {
                System.out.println("Balance lower than minimum balance for a savings account. Minimum balance fee of $30 is applied.");
            }

            transactions = sqlconnect.prepareCall(withdrawlquery);
            transactions.setInt(1, acctnum);
            transactions.setDouble(2, amount);
            transactions.execute();

            System.out.println("Transaction was Complete.");
            System.out.println("$" + amount + " was withdrawled from your account.");

            transactions.close();

            preparedStatement = sqlconnect.prepareStatement(checkbalance);
            preparedStatement.setInt(1, acctnum);
            resultSet = preparedStatement.executeQuery();

            double acctbal = 0.0;

            while (resultSet.next())
            {
                acctbal = resultSet.getDouble(1);
            }

            System.out.println("Your remaining balance is: " + acctbal);

            while (status != 1 || status != 2)
            {
                System.out.println("Press 1 to return to the main menu or 2 to quit.");
                status = sc.nextInt();

                if (status == 1)
                {
                    mainmenu(sqlconnect);
                }

                else if (status == 2)
                {
                    System.exit(0);
                }
            }
        }

        else if (transtype == 3)
        {
            preparedStatement = sqlconnect.prepareStatement(checkbalance);
            preparedStatement.setInt(1, acctnum);
            resultSet = preparedStatement.executeQuery();

            double acctbal = 0.0;

            while (resultSet.next())
            {
                acctbal = resultSet.getDouble(1);
            }

            System.out.println("Your remaining balance is: " + acctbal);

            while (status != 1 || status != 2)
            {
                System.out.println("Press 1 to return to the main menu or 2 to quit.");
                status = sc.nextInt();
                System.out.println(status);

                if (status == 1)
                {
                    mainmenu(sqlconnect);
                }

                else if (status == 2)
                {
                    System.exit(0);
                }
            }
        }    
    }

    

    public static void CardPurchases (Connection sqlconnect) throws SQLException
    {
        String namequery = "SELECT CUSTNAME FROM CUSTOMER";
        String acctquery = "SELECT ACCTNUM FROM HAS,customer WHERE has.custid = customer.custid and customer.CUSTNAME = ?";
        String branchquery = "SELECT * FROM BRANCH WHERE BRANCHID = ?";
        String depositquery = "{call transactionpkg.depositmoney('d', ?, ?)}";
        String withdrawlquery = "{call transactionpkg.withdrawlmoney('w', ?, ?)}";
        String purchasequery = "{call transactionpkg.createpurchase(?, ?)}";
        String checkbalance = "SELECT acctbal FROM ACCOUNT WHERE ACCTNUM = ?";
        String branchlocquery = "select branchID,branchName, branchtype from branch";
        String dcardlistquery = "select cardID from debitcard";
        String ccardlistquery = "select cardId from creditcard";
        String dcardquery = "select dcardnum from debitcard where cardid = ?";
        String ccardquery = "select ccardnum from creditcard where cardid = ?";
        boolean success = true; //declaring and initializing all the variables
        boolean fetchacct = false, fetchbranch = false;
        Scanner sc = new Scanner(System.in);
        String customername, username, password;
        int acctnum = 0, branchnum, managefirst;
        String accttypequery = "select accttype from account where acctnum = ?";
        String accounttype = "";
        int cardID = 1324512345;
        long dcardnum = 0;
        String ccardnum = "";


        System.out.println("");


        PreparedStatement preparedStatement = null;
        CallableStatement transactions = null; 
        ResultSet resultSet = null;
        boolean validnum = false;

        preparedStatement = sqlconnect.prepareStatement(namequery);
        resultSet = preparedStatement.executeQuery();
        System.out.println("Here are the list of names that are currently in the system.");

        while (resultSet.next())
        {
            System.out.println(resultSet.getString(1));
        }

     

        int check = 0, status = 0;
        int debitcardID = 0, creditcardID = 0;
        String debitcard = "DC";
        String creditcard = "CC";
        String cardtype = "";
        int transtype = 0;
        String customerID = "";
        String customerIDquery = "select custId from customer where customer.custname = ?";
        double amount = 0.0;         
          
        while (customerID.isEmpty())
        {
            System.out.println("\nPlease enter your Name: ");
            
            customername = sc.nextLine();
            

            preparedStatement = sqlconnect.prepareStatement(customerIDquery);
            preparedStatement.setString(1, customername);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                customerID = resultSet.getString(1);
            }

            if (customerID.isEmpty())
            {
                System.out.println("No such Customer ID for the name exist. Please Try again.");
            }
        }
        
        System.out.println("This is your customer ID: " + customerID);
        String realcardtype = "";
        String rightacctnum = "select acctnum from has where has.custid = ?";
        String rightdebitcardID = "select cardid from debitcard where debitcard.acctnum = ?";
        String rightcreditcardID ="select cardid from creditcard where creditcard.custid = ?"; 

        preparedStatement = sqlconnect.prepareStatement(rightacctnum);
        preparedStatement.setString(1, customerID);
        resultSet = preparedStatement.executeQuery();

        if (resultSet.next())
        {
            int realaccountnum = resultSet.getInt(1);

            preparedStatement = sqlconnect.prepareStatement(rightdebitcardID);
            preparedStatement.setInt(1, realaccountnum);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
            {
                realcardtype = "DC";
                debitcardID = resultSet.getInt(1);

                System.out.println("You only have a debit card currently.");

                System.out.println("Your debit card ID is : " + debitcardID);
            }

            else
            {
                double limit = 0.0, balance= 0.0;
                preparedStatement = sqlconnect.prepareStatement(rightcreditcardID);
                preparedStatement.setString(1, customerID);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next())
                {
                    realcardtype = "CC";
                    creditcardID = resultSet.getInt(1);
                    String creditlimitquery = "select creditlim, cardbal from creditcard where creditcard.custid = ?";
                    preparedStatement = sqlconnect.prepareStatement(creditlimitquery);
                    preparedStatement.setString(1, customerID);
                    resultSet = preparedStatement.executeQuery();

                    while (resultSet.next())
                    {
                        limit = resultSet.getDouble(1);
                        balance = resultSet.getDouble(2);
                    }
                    double remainingbal = limit - balance;
                    System.out.println("\nYou only have a credit card currently. Your credit card ID is:" + creditcardID);
                    System.out.println(" Your credit limit for this month is $" + limit +", and you can spend $" + remainingbal + " more.");
                }

                else
                {
                    System.out.println("\n\n\n\n\nThis account does not have a card set-up yet. Returning to the main menu.\n\n\n\n");
                    mainmenu(sqlconnect);
                }
            }
            
        }

        while (true)
        {
            System.out.println("\n\n\nPlease re-enter your card ID For security purpose.");
            try
            {
                cardID = Integer.parseInt(sc.next());

                if (cardID == debitcardID || cardID == creditcardID)
                {
                    break;
                }
            }

            catch (NumberFormatException ignore)
            {
                System.out.println("Invalid Input.");
            }
        }
         
        String vendorname = "";
        double goodprice = 0.0;
        System.out.println("Please enter the vendor's name you are purchasing from.");
        String flush = sc.nextLine();
        vendorname = sc.nextLine();


        while (true) 
        {
            System.out.println("Please enter the price of the good you are purchasing.");
            try 
            {
                goodprice = Double.parseDouble(sc.next());
                break; // if double, break out of the loop
            } 
            
            catch (NumberFormatException ignore) 
            {
                System.out.println("Invalid Input.");
            }
        }

            //if debitcard purchase, check if goodprice is bigger than cardbal/acctbal. if yes, abort and ask the user again. If not, then proceed in creating
            //a purchase row and subtracting from cardbal/acctbal.
            //if creditcard purchase, check if goodprice is bigger than credit limit. if Yes, abort and ask the user again. If not, then proceed in creating
            //a purchase row and subtracting from creditlim.
            String dcardbalquery = "select cardbal from debitcard where cardid = ?";
            String ccardlimquery = "select creditlim from creditcard where cardid = ?";

            if (realcardtype.equals(debitcard))
            {
                preparedStatement = sqlconnect.prepareStatement(dcardbalquery);
                preparedStatement.setInt(1, cardID);
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next())
                {
                    amount = resultSet.getDouble(1);
                }

                while (amount < goodprice)
                {
                    System.out.println("Purchase unsuccessful. Remaining balance is too low.");

                    while (true) 
                    {
                        System.out.println("Please enter the price of the good you are purchasing.");
                        try 
                        {
                            goodprice = Double.parseDouble(sc.next());
                            break; // if double, break out of the loop
                        } 
                        
                        catch (NumberFormatException ignore) 
                        {
                            System.out.println("Invalid Input.");
                        }
                    }            
                }

                transactions = sqlconnect.prepareCall(purchasequery);
                transactions.setString(1, vendorname);
                transactions.setInt(2, cardID);
                transactions.execute();

                transactions.close();

                transactions = sqlconnect.prepareCall(withdrawlquery);
                transactions.setInt(1, acctnum);
                transactions.setDouble(2, goodprice);
                transactions.execute();

                transactions.close();

                System.out.println("Purchase was successful and complete.");
                System.out.println("$" + goodprice + " was withdrawled from your Debit Card.");

                while (status != 1 || status != 2)
                {
                    System.out.println("Press 1 to return to the main menu or 2 to quit.");
                    status = sc.nextInt();
                    if (status == 1)
                    {
                        mainmenu(sqlconnect);
                    }

                    else if (status == 2)
                    {
                        System.exit(0);
                    }
                }
                
                
                
            }

            else if (realcardtype.equals(creditcard))
            {
                double creditlimit = 0.0;
                preparedStatement = sqlconnect.prepareStatement(ccardlimquery);
                preparedStatement.setInt(1, cardID);
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next())
                {
                    creditlimit = resultSet.getDouble(1);
                }

                while (creditlimit < goodprice)
                {
                    System.out.println("Purchase unsuccessful. You've reached your credit limit for this month.");

                    while (true) 
                    {
                        System.out.println("Please enter the price of the good you are purchasing.");

                        try 
                        {
                            goodprice = Double.parseDouble(sc.next());
                            break; // if double, break out of the loop
                        } 
                        catch (NumberFormatException ignore) 
                        {
                            System.out.println("Invalid Input.");
                        }
                    }
                }
                String ccardpurchasequery = "{call transactionpkg.creditpurchase(?, ?, ?)}";

                transactions = sqlconnect.prepareCall(purchasequery);
                transactions.setString(1, vendorname);
                transactions.setInt(2, cardID);
                transactions.execute();

                transactions.close();

                transactions = sqlconnect.prepareCall(ccardpurchasequery);
                transactions.setInt(1, cardID);
                transactions.setInt(2, acctnum);
                transactions.setDouble(3, goodprice);
                transactions.execute();

                transactions.close();


                System.out.println("Purchase was successful and complete.");
                System.out.println("$" + goodprice + " was withdrawled from your Credit Card.");

                while (status != 1 || status != 2)
                {
                    System.out.println("Press 1 to return to the main menu or 2 to quit.");
                    status = sc.nextInt();

                    if (status == 1)
                    {
                        mainmenu(sqlconnect);
                    }
    
                    else if (status == 2)
                    {
                       System.exit(0);
                    }
                }
                
                
            }
        
    }

    public static void createNewAccount(Connection sqlconnect) throws SQLException
    {
        String customerName = "";
        int branchnum = 0;
        double beginningbal = 0;
        String accounttype ="";
        Scanner sc = new Scanner(System.in);
        PreparedStatement preparedStatement = null;
        CallableStatement transactions = null; 
        ResultSet resultSet = null;
        String CheckingAccount = "CA";
        String SavingsAccount = "SA";  
        String customeraddress = "";
        String branchlocquery = "select branchID, branchName, branchtype from branch";
        String branchquery = "SELECT * FROM BRANCH WHERE BRANCHID = ?";
        int newAcctnum;
        String newcustID = "";
        String newcustIDquery = "select dbms_random.string('X', 8) from dual";
        String newcustquery = "insert into customer (custid, custname, custaddr) values (?, ?, ?)";
        String newhasquery = "insert into has (custid, acctnum) values (?, ?)";
        Random rand = new Random();
        //insert into account (acctnum, accttype, acctbal, branchID) values (1001, 'SA', 1500, 21);
        String createAccount = "insert into account (acctnum, accttype, acctbal, branchID) values (?, ?, ?, ?)";
        //insert into checkingacct(acctnum, debitID) values (1001, (select DBMS_RANDOM.STRING('X', 6) from dual));
        String createCA = "insert into checkingacct(acctnum, debitID) values (?, (select DBMS_RANDOM.STRING('X', 6) from dual))";
        //insert into savingsacct(acctnum, interest, minbal, minbalfee) values (1002, (select trunc(DBMS_RANDOM.VALUE(0.05, 0.20),2) from dual) , 200, 30);
        String createSA = "insert into savingsacct(acctnum, interest, minbal, minbalfee) values (?, (select trunc(DBMS_RANDOM.VALUE(0.05, 0.20),2) from dual) , 200, 30)";

        System.out.println("Thank you for choosing 241 Bank.");
        System.out.println("Please enter your name for a new Account.");
        customerName = sc.nextLine();

        while (customerName.equals(null))
        {
            System.out.println("Your name cannot be blank. Please try again.");
            System.out.println("Please enter your name for a new Account.");
            customerName = sc.nextLine();
        }

        System.out.println("Please enter your address.");
        customeraddress = sc.nextLine();

        while (customeraddress.equals(null))
        {
            System.out.println("Your address cannot be blank. Please try again.");
            System.out.println("Please enter your address for your new Account.");
            customeraddress = sc.nextLine();
        }

        while(!(accounttype.toUpperCase().equals(CheckingAccount) || accounttype.toUpperCase().equals(SavingsAccount)))
        {
            System.out.println("To create a Checking Account, please type \"CA\", for a Savings Account, please type \"SA\".");
            accounttype = sc.nextLine();
        }

        System.out.println("These are the branch IDs and Locations that are currently available.");
        System.out.println("Branch ID   |   Branch Locations");
        preparedStatement = sqlconnect.prepareStatement(branchlocquery);
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            System.out.println("    " + resultSet.getString(1) + "          " + resultSet.getString(2));
        }

        System.out.println("Please enter the Branch ID you would like to create your account from.");
        branchnum = sc.nextInt();
        preparedStatement = sqlconnect.prepareStatement(branchquery);
        preparedStatement.setInt(1, branchnum);
        resultSet = preparedStatement.executeQuery();
        
        while (!resultSet.next())
        {
            System.out.println("Invalid branch ID. Please Try again.");
            System.out.println("Please enter your branch ID: ");
            branchnum = sc.nextInt();
            preparedStatement = sqlconnect.prepareStatement(branchquery);
            preparedStatement.setInt(1, branchnum);
            resultSet = preparedStatement.executeQuery();
        }

        while (true) 
        {
            System.out.println("Please enter the amount you wish to deposit into your newly created account.");
            try 
            {
                beginningbal = Double.parseDouble(sc.next());
                break; // will only get to here if input was a double
            } catch (NumberFormatException ignore) {
                System.out.println("Invalid Input.");

            }
        }
        
      
        newAcctnum = rand.nextInt(8999) + 1000;
        preparedStatement = sqlconnect.prepareStatement(newcustIDquery);
        resultSet = preparedStatement.executeQuery();
        if (resultSet.next())
        {
            newcustID = resultSet.getString(1);
            preparedStatement = sqlconnect.prepareStatement(newcustquery);
            preparedStatement.setString(1, newcustID);
            preparedStatement.setString(2, customerName);
            preparedStatement.setString(3, customeraddress);

            resultSet = preparedStatement.executeQuery();
        }

        preparedStatement = sqlconnect.prepareStatement(createAccount);
        preparedStatement.setInt(1, newAcctnum);
        preparedStatement.setString(2, accounttype);
        preparedStatement.setDouble(3, beginningbal);
        preparedStatement.setInt(4, branchnum);

        resultSet = preparedStatement.executeQuery();

        if (accounttype.toUpperCase().equals(CheckingAccount))
        {
            preparedStatement = sqlconnect.prepareStatement(createCA);
            preparedStatement.setInt(1, newAcctnum);
            resultSet = preparedStatement.executeQuery();
        }

        else if (accounttype.toUpperCase().equals(SavingsAccount))
        {
            preparedStatement = sqlconnect.prepareStatement(createSA);
            preparedStatement.setInt(1, newAcctnum);
            resultSet = preparedStatement.executeQuery();
        }

        preparedStatement = sqlconnect.prepareStatement(newhasquery);
        preparedStatement.setString(1, newcustID);
        preparedStatement.setInt(2, newAcctnum);
        
        resultSet = preparedStatement.executeQuery();

        if (resultSet.next())
        {
            System.out.println("Your account has been created successfully.");
            System.out.println("Your account number is : " + newAcctnum + ".");
            System.out.println("Your Branch ID is : " + branchnum + ".");
            System.out.println("Your Beginning Balance is : $" + beginningbal + ".");
            System.out.println("Thanks for choosing 241 Bank again!");
            System.out.println("");
        }

        int menuorquit = 0;
        System.out.println("To return to the Main Menu, enter 1.");
        System.out.println("To quit, enter 2.");
        menuorquit = sc.nextInt();

        while (!(menuorquit == 1 || menuorquit == 2))
        {
            System.out.println("Please select from the given options.");
            System.out.println("To return to the Main Menu, enter 1.");
            System.out.println("To quit, enter 2.");

            menuorquit = sc.nextInt();
        }

        if (menuorquit == 1)
        {
            mainmenu(sqlconnect);
        }

        if (menuorquit == 2)
        {
            System.exit(0);
        }
        
    }

}