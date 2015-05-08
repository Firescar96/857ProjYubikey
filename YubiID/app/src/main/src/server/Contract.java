package server;

import android.provider.BaseColumns;

public class Contract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public Contract() {}

    /* Inner class that defines the table contents */
    public static abstract class Exchanges implements BaseColumns {
        public static final String TABLE_NAME = "exchanges";
        public static final String COLUMN_NAME_SENDER_YUBIKEY = "senderyubikey";
        public static final String COLUMN_NAME_PUBLIC_YUBIKEY = "publicyubikey";
        public static final String COLUMN_NAME_AMOUNT = "amount";
       
    }
   

}
