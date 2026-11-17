package services;

public class ParkResult
{
    public boolean success;     // true if the car parked
    public String message;      // response to the user 
    public String lotName;      // filled when success
    public String spaceId;      // the slot the car got, e.g. "A3"

    public ParkResult(boolean success, String message, String lotName, String spaceId)
    {
        this.success = success;
        this.message = message;
        this.lotName = lotName;
        this.spaceId = spaceId;
    }
}