package Parking_Classes;

public class ParkingManager extends Parking
{
    ParkingManager(String name, String addr, String ... temp)
    {
        super(name, addr);
        super.Initialiazing_List_Park_Space(temp);
    }

    void addParkingSpace(String temo)
    {
        ParkSpace a = new ParkSpace(temo,this);
        parkList.add(a);
        addAvailableParkSpace(a);
    }

    void removeParkSpace(String temp)
    {
        ParkSpace check = new ParkSpace(temp,this);
        if (parkList.contains(check)) 
        {
            parkList.remove(check);
        }
    }
    
    
}
