package services;

public class LeaveResult
{
    public boolean success;
    public String message;
    public int fare;             
    public int newBalance;        
    public long minutesParked;

    public LeaveResult(boolean success, String message, int fare, int newBalance, long minutesParked)
    {
        this.success = success;
        this.message = message;
        this.fare = fare;
        this.newBalance = newBalance;
        this.minutesParked = minutesParked;
    }
}
