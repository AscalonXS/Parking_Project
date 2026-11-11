package Parking_Classes;
import java.time.*;

class Car
{
    String carType;
    String OwnerName;
    String number;
    Boolean haveParkSpace;
    ParkSpace ps = null;
    int balance;
    LocalTime takePlace;

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
        this.takePlace = LocalTime.now();
    }

    void releaseParkSpace(ParkSpace ps)
    {
        Duration duration = Duration.between(this.takePlace, LocalTime.now());
        long minutes = duration.toMinutes();
        int total_fare = 0; // free if below 15 minutes

        if(minutes < 60 && minutes > 15) // Fare between first 15 minutes before first 1 hour
        {
            total_fare = 5000;
        }
        else if(minutes > 60)
        {
            long hours = duration.toHours();
            total_fare = 5000 + (int)((hours - 1)) * 2500;
        }

        this.haveParkSpace = false;
        this.ps = null;

        this.balance -= total_fare;
        ps.belongToWhichParking.addAvailableParkSpace(ps);
    }

}


