package edu.ucsb.cs156.happiercows.errors;

public class BuyPositiveException extends Exception {
    public BuyPositiveException(String messageString){
        super(messageString);
    }
}