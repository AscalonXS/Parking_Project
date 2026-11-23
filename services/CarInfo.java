package  services;

class  CarInfo
{
    String number; 
    String owner_name;
    String car_type;
    int balance; 
    boolean parked; 
    String lot_name; 
    String space_id;
    String start_time;

    public CarInfo(String number, String on, String ct, int b, boolean  p, String ln, String si, String st) 
    {
        this.number = number;
        this.owner_name = on;
        this.car_type = ct;
        this.balance = b;
        this.parked = p;
        this.lot_name = ln;
        this.space_id = si;
        this.start_time = st;
    }
}