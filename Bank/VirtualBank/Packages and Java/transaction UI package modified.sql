--transaction UI.

create or replace PACKAGE transactionpkg AS

Procedure createtrans (actualtranstype varchar, actualtransamt number, actualacctnum number);
Procedure createpurchase (vendorname varchar, cardnumber number);
Procedure creditpurchase (actualcardid in number, numberactualacctnum in number, actualtransamt in number);
Procedure depositmoney (actualtranstype in varchar, actualacctnum in number, actualtransamt in number);
Procedure withdrawlmoney (actualtranstype in varchar, actualacctnum in number, actualtransamt in number);

end transactionpkg;
/

create or replace package body transactionpkg as

Procedure createpurchase (vendorname varchar, cardnumber number) AS
    new_purchase_id number (25, 0);

    BEGIN
        new_purchase_id := purchase_sequence.nextval;


        Insert into purchases(vendorid, vendorname, purchasedate, cardID) values (new_purchase_id, vendorname, sysdate, cardnumber);
        commit;

        Exception when others then
        DBMS_OUTPUT.PUT_LINE('ERROR OCCURED WHILE CREATING A NEW PURCHASE. QUITTING...');

END createpurchase;

Procedure createtrans (actualtranstype varchar, actualtransamt number, actualacctnum number) AS
    new_transaction_id number(25, 0);

        Begin
            -- Use the Oracle sequence to get the next transaction ID
            new_transaction_id := transaction_sequence.nextval;

            Insert into transaction(transactionID, transtype, transamt, transdate) values (new_transaction_id, actualtranstype, actualtransamt, sysdate);
            Insert into happens(transactionID, acctnum) values (new_transaction_id, actualacctnum);
            commit;

        Exception when others then
            DBMS_OUTPUT.PUT_LINE('ERROR OCCURED WHILE CREATING TRANSACTION. QUITTING...');

END createtrans;

Procedure depositmoney (actualtranstype in varchar, actualacctnum in number, actualtransamt in number) AS

    faketype varchar(2);
    curbal number (25, 2);
    curcardbal number (25, 2);
    aftercardbal number (25, 2);
    afterbal number (25, 2);
    lowbalancefee number := 30;
    minbal number := 200;
    any_rows_found number(1,0);
    
    
    Begin

        select accttype, acctbal into faketype, curbal 
        from account 
        where acctnum = actualacctnum;

        select count(*)
        into   any_rows_found
        from   debitcard
        where  rownum = 1 and acctnum = actualacctnum;

       


        if (any_rows_found = 0) then
            createtrans(actualtranstype, actualtransamt, actualacctnum);
            DBMS_OUTPUT.PUT_LINE('SUCCESSFUL');
            afterbal := (curbal + actualtransamt);
            update account set acctbal = afterbal where acctnum = actualacctnum;
            commit;
            
            return;
        end if;

        
        select cardbal into curcardbal
        from debitcard
        where acctnum = actualacctnum;


        createtrans(actualtranstype, actualtransamt, actualacctnum);
        DBMS_OUTPUT.PUT_LINE('SUCCESSFUL');
        afterbal := (curbal + actualtransamt);
        aftercardbal := (curcardbal + actualtransamt);
        
        if (lower(faketype) = 'ca') then
            update debitcard set cardbal = aftercardbal where acctnum = actualacctnum;
        end if;
        
        update account set acctbal = afterbal where acctnum = actualacctnum;

       


        commit;

        exception when others then
        
        DBMS_OUTPUT.PUT_LINE('Error Occurred when depositing money');

    End depositmoney;

------------------------------------
Procedure creditpurchase (actualcardid in number, numberactualacctnum in number, actualtransamt in number) as
    faketype varchar(2);
    curbal number (25, 2);
    afterbal number (25, 2);
    curcardbal number (25, 2);
    aftercardbal number (25, 2);
    credlim number (25, 2);
    

    Begin

        select cardbal,creditlim into curcardbal, credlim
        from creditcard
        where cardid = actualcardid;

        aftercardbal := (curcardbal + actualtransamt);

        if (aftercardbal > credlim) THEN

            DBMS_OUTPUT.PUT_LINE('ERROR OCCURRED. YOU HAVE REACHED YOUR CREDIT LIMIT FOR THIS MONTH');
            return;
        end if;

        update creditcard set cardbal = aftercardbal where cardid = actualcardid;
        COMMIT;

    End creditpurchase;

Procedure withdrawlmoney (actualtranstype in varchar, actualacctnum in number, actualtransamt in number) as


    faketype varchar(2);
    curbal number (25, 2);
    afterbal number (25, 2);
    curcardbal number (25, 2);
    aftercardbal number (25, 2);
    lowbalancefee number := 30;
    minbal number := 200;
    any_rows_found number(1,0);

    Begin  
        
        select accttype, acctbal into faketype, curbal
            from account
            where acctnum = actualacctnum;

        select count(*)
        into   any_rows_found
        from   debitcard
        where  rownum = 1 and acctnum = actualacctnum;

        if (any_rows_found = 0) then
            
            afterbal := (curbal - actualtransamt);

            if (afterbal < 0) THEN
                DBMS_OUTPUT.PUT_LINE('Error Occurred. Remaining Balance too low!');
                return;
            end if; 
            
            createtrans(actualtranstype, actualtransamt, actualacctnum);
            DBMS_OUTPUT.PUT_LINE('SUCCESSFUL');
            update account set acctbal = afterbal where acctnum = actualacctnum;
            commit;
            
            return;
        end if;

        select cardbal into curcardbal
        from debitcard
        where acctnum = actualacctnum;

        afterbal := (curbal - actualtransamt);
        aftercardbal := (curcardbal - actualtransamt);


        if (afterbal < 0) then
            -- overdraft
            -- abort the transaction and return some error
            DBMS_OUTPUT.PUT_LINE('Error Occurred. Remaining balance too low!');


            return;
        end if;



        if (lower(faketype) = 'ca') then
            createtrans(actualtranstype, actualtransamt, actualacctnum);
            update debitcard set cardbal = aftercardbal where acctnum = actualacctnum;
            update account set acctbal = afterbal where acctnum = actualacctnum;
        
            -- update the checking account with the new afterbal

            
        elsif (lower(faketype) = 'sa') then        
            if (afterbal < minbal) then

            Begin
                afterbal := afterbal - lowbalancefee;

                if (afterbal < 0) then
                    
                    DBMS_OUTPUT.PUT_LINE('Error Occurred. savings account balance too low!');
                    return; --return?

                end if;

                createtrans(actualtranstype, actualtransamt, actualacctnum);
                update account set acctbal = afterbal where acctnum = actualacctnum;
            end;

            else
                createtrans(actualtranstype, actualtransamt, actualacctnum);
                update account set acctbal = afterbal where acctnum = actualacctnum;

            -- update the savings account with the new afterbal
        end if;

        else
            DBMS_OUTPUT.PUT_LINE('Invalid account type, Error Occurred.');
            return;
            -- invalid account type
        end if;
    End withdrawlmoney;

End transactionpkg;
