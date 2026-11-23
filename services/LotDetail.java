package  services;

import java.util.List;

public class  LotDetail 
{
    String name;
    String addr;
    int total;
    int free;
    List<SpaceInfo> spaces;

    public LotDetail(String n, String a, int t, int f, List<SpaceInfo> spaces) 
    {
        this.name = n;
        this.addr = a;
        this.total = t; 
        this.free = f;
        this.spaces = spaces;
    }

}