package Parking_Classes;
import java.time.*;

public class Car
{
    private String carType;
    private String OwnerName;
    private String number;
    private Boolean haveParkSpace;
    private ParkSpace ps = null;
    private int balance;
    private LocalDateTime takePlace;

    public Car(String carType, String OwnerName, String number, int balance)
    {
        this.carType = carType;
        this.OwnerName = OwnerName;
        this.number = number;
        this.haveParkSpace = false;
        this.ps = null;
        this.balance = balance;
    }

    void takeParkSpace(ParkSpace ps)
    {
        this.ps = ps;
        this.takePlace = LocalDateTime.now();
        this.ps.setIsTaken(true);
    }

    int calculate_fare(Duration a)
    {
        long minutes = a.toMinutes();
        int total_fare = 0; // free if below 15 minutes

        if(minutes < 60 && minutes > 15) // Fare between first 15 minutes before first 1 hour
        {
            total_fare = 5000;
        }
        else if(minutes > 60)
        {
            long hours = a.toHours();
            total_fare = 5000 + (int)((hours - 1)) * 2500;
        }

        return  total_fare;
    }

    void releaseParkSpace(ParkSpace ps)
    {
        Duration duration = Duration.between(this.takePlace, LocalDateTime.now());

        this.haveParkSpace = false;
        this.ps = null;
        this.ps.setIsTaken(false);

        this.balance -= calculate_fare(duration);
        ps.getBelongToWhichParking().addAvailableParkSpace(ps);
    }

    public String getNumber() {
        return number;
    }

    public String getOwnerName() {
        return OwnerName;
    }

    public String getCarType() {
        return carType;
    }

    public int getBalance() {
        return balance;
    }

    public ParkSpace getPs() 
    {
        return ps;
    }


    public boolean hasPublicSpace()
    {
        return  ps != null;
    }

    public LocalDateTime getTakePlace() 
    {
        return takePlace;
    }

}


