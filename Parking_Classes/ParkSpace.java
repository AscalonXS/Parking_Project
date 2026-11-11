package Parking_Classes;

class  ParkSpace
{
    boolean isTaken;
    String ID;
    Parking belongToWhichParking;

    public ParkSpace(String ID, Parking parkingName)
    {
        this.ID = ID;
        this.isTaken = false;
        this.belongToWhichParking = parkingName;
    }
}