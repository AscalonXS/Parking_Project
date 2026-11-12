package Parking_Classes;

public class  ParkSpace
{
    private  boolean isTaken;
    private String ID;
    private Parking belongToWhichParking;

    public ParkSpace(String ID, Parking parkingName)
    {
        this.ID = ID;
        this.isTaken = false;
        this.belongToWhichParking = parkingName;
    }

    public String getID() 
    {
        return ID;
    }

    public boolean isTaken() {
        return isTaken;
    }

    public Parking getBelongToWhichParking() 
    {
        return belongToWhichParking;
    }

    public void setIsTaken(boolean isTaken) 
    {
        this.isTaken = isTaken;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (!(obj instanceof ParkSpace))
            return false;

        ParkSpace other = (ParkSpace) obj;

        return this.ID.equals(other.ID) && this.belongToWhichParking.equals(other.belongToWhichParking);
    }

    @Override
    public int hashCode()
    {
        return this.belongToWhichParking.hashCode() + this.ID.hashCode();
    }


   
}