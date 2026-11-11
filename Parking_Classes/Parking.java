package Parking_Classes;
import java.util.*;


class Parking
{
    /*Parking Place Class*/
    LinkedList<ParkSpace> parkList = new LinkedList<ParkSpace>();
    String ParkName;
    String addr;
    Deque<ParkSpace> availabe_park_list = new LinkedList<ParkSpace>();

    public Parking(String name, String addr)
    {
        // Initialiazing the Parking Space
        this.ParkName = name;
        this.addr = addr;
    }

    void Initialiazing_List_Park_Space(String ... temp)
    {
        /*Take every posibble Parking Space add it to the parking Space list then initialize to the parkspace*/

        for (String elem : temp)
        {
            parkList.add(new ParkSpace(elem,this));
        }

        for (ParkSpace elem : this.parkList)
        {
            availabe_park_list.add(elem);
        }
    }

    void addAvailableParkSpace(ParkSpace a)
    {
        // Add to Available
        availabe_park_list.addFirst(a);
    }

    ParkSpace takeParkSpace()
    {
        return availabe_park_list.removeFirst();
    }

    boolean isFull()
    {
        return  availabe_park_list.isEmpty();
    }

    void  display_Status()
    {
        System.out.println("===== PARKING STATUS =====");
        System.out.println("Parking Name : " + ParkName);
        System.out.println("Address      : " + addr);
        System.out.println("Total Spaces : " + parkList.size());

        int available = 0;
        int taken = 0;

        for (ParkSpace space : parkList)
        {
            if (space.isTaken)
            {
                taken++;
            }
            else
            {
                available++;
            }
        }

        System.out.println("Available    : " + available);
        System.out.println("Taken        : " + taken);

        System.out.println("--------------------------");

        for (ParkSpace space : parkList)
        {
            System.out.println(
                    space.ID + " : " +
                            (space.isTaken ? "Taken" : "Available")
            );
        }

        System.out.println("==========================");
    }
}
