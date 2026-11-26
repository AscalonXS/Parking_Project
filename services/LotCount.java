package services;

public class LotCount
{
    String name;
    int count;      // cars inside, counted by the hardware

    public LotCount(String name, int count)
    {
        this.name = name;
        this.count = count;
    }
}
