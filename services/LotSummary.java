package  services;

public class LotSummary
{
    String name;
    String addr;
    int total;
    int free; 

    public LotSummary(String n, String a, int t, int f) 
    {
        this.name = n;
        this.addr = a;
        this.total = t;
        this.free = f;
    }   
}