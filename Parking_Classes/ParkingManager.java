package Parking_Classes;

public class ParkingManager extends Parking
{
    public ParkingManager(String name, String addr, String ... temp)
    {
        super(name, addr);
        super.Initialiazing_List_Park_Space(temp);
    }

    public void addParkingSpace(String temo)
    {
        ParkSpace a = new ParkSpace(temo,this);
        parkList.add(a);
        addAvailableParkSpace(a);
    }

    public void removeParkSpace(String temp)
    {
        ParkSpace check = new ParkSpace(temp,this);
        if (parkList.contains(check)) 
        {
            parkList.remove(check);
            if(availabe_park_list.contains(check))
            {
                availabe_park_list.remove(check);
            } 
        }
    }
    
    
}
