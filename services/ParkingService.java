package  services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import  java.util.Map;
import  DB.ParkingDao;
import Parking_Classes.Car;
import Parking_Classes.ParkSpace;
import  Parking_Classes.Parking;
    
    public class ParkingService 
    {
        private ParkingDao temp = new ParkingDao();
        private Map<String, Parking> lots = new HashMap<>();
        private  Map<String, Car> cars = new HashMap<>();
        private Map<String, Integer> open_sessions = new HashMap<>();

        public ParkingService() 
        {
            //loading the parking 
            for(Parking a : temp.loadAllLots())
            {
                lots.put(a.getParkName(), a);
            }

            //loading the cars 
            for(Car a : temp.loadAllCars())
            {
                cars.put(a.getNumber(), a);
            }

        //load all sessions 
            for(String[] a : temp.loadOpenSessions())
            {
                open_sessions.put(a[0], Integer.parseInt(a[3]));
            }
        }

        public ParkResult synchronizedPark(String carNumber,String lotName)
        {
            // take Inputed Car and Parking lot
            Car theCar = cars.get(carNumber);
            Parking lot = lots.get(lotName);
            
            // Check some conditions 
            if(theCar == null)
            {
                // Car is not registered in the data base
                return new ParkResult(false,"Car is not Registered",null,null);
            }

            if(lot == null)
            {
                // Parking lot is a hallucination ~
                return new ParkResult(false,"lot is not found",null,null);
            }

            if (theCar.hasPublicSpace()) 
            {
                // The Car Already have a park Space
                return  new ParkResult(false, "The Car is already parked", theCar.getPs().getBelongToWhichParking().getParkName(), null );   
            }

            if (lot.isFull())
                {
                //Parking lot is full then return message IS FULL DAWG YOU CANT
                return  new ParkResult(false,"Lot is Full", theCar.getPs().getBelongToWhichParking().getParkName(), null );   

            }   
            

            // Take the ps            
            ParkSpace ps = lot.takeParkSpace();
            theCar.takeParkSpace(ps);
            
            // Update the Data base
            ParkingDao a = new ParkingDao();
            a.saveSpaceStatus(lot.getParkName(), ps.getID(), true);
            int sessionId = a.startSession(theCar.getNumber(),lot.getParkName(), ps.getID(), theCar.getTakePlace());
            // save to our running sessions
            open_sessions.put(theCar.getNumber(), sessionId);
            
            
            return  new ParkResult(true, "The Car is parked", theCar.getPs().getBelongToWhichParking().getParkName(), ps.getID());
        }

        public LeaveResult synchronizeLeaveResult(String carNumber)
        {
            Car theCar = cars.get(carNumber);

            if(theCar == null)
            {
                return new LeaveResult(false, "Car is not Found", 0,0,0);
            }          

            if(!theCar.hasPublicSpace()) 
            {
                return new LeaveResult(false, "Car is not parked", 0,0,0);
            }

           ParkSpace ps = theCar.getPs();       
           Duration minutes = Duration.between(theCar.getTakePlace(), LocalDateTime.now()); 
           int fare = theCar.calculate_fare(minutes);
           theCar.releaseParkSpace(theCar.getPs());
           ParkingDao a = new ParkingDao();
           
           a.updateBalance(theCar.getNumber(), theCar.getBalance());
           a.saveSpaceStatus(ps.getBelongToWhichParking().getParkName(), ps.getID(), false);
           a.endSession(open_sessions.get(carNumber),LocalDateTime.now(), fare);

           return  new LeaveResult(true,"Leave the Park Space is Succeed", fare, theCar.getBalance(), minutes.toMinutes());
        }

        public List<LotSummary> getLots()
        {
            List<LotSummary> temp = new ArrayList<>();       

            for(Parking a : lots.values())
            {
                temp.add(new LotSummary(a.getParkName(), a.getAddr(),a.getTotalSpace(),a.countAvailablSpace()));
            }

            return temp;
        }
        
        public LotDetail getLotDetail(String lotName)
        {
                Parking temp = lots.get(lotName);
                if (temp == null) 
                {
                    return  null;    
                }                
                List<SpaceInfo> spaces = new ArrayList<SpaceInfo>();
                
                for(ParkSpace i : temp.getAllSpaces())
                {
                    spaces.add(new SpaceInfo(i.getID(), i.isTaken()));
                }                

                return new LotDetail(temp.getParkName(), temp.getAddr(), temp.getTotalSpace(), temp.countAvailablSpace(), spaces);
        }

        public List<LotDetail> getAllStatus()
        {
                List<LotDetail> temp = new ArrayList<LotDetail>();
                for(Parking a : lots.values())
                {
                    temp.add(getLotDetail(a.getParkName()));
                }    

                return  temp;
        }   

        public CarInfo getCarInfo(String carNumber)
        {
            if(cars.get(carNumber) == null)
            {
                return  null;
            }
            Car temp = cars.get(carNumber);
            String lot_name; 
            String space_id;
            String start_time;
            
            if(temp.getPs() == null)
            {
             lot_name = null; 
             space_id = null;
             start_time = null;
            }
            else
            {
             lot_name = temp.getPs().getBelongToWhichParking().getParkName(); 
             space_id = temp.getPs().getID();
             start_time = temp.getTakePlace().toString();

            }
            return new CarInfo(temp.getNumber(), temp.getOwnerName(), temp.getCarType(), temp.getBalance(), temp.hasPublicSpace(),lot_name, space_id, start_time);
        }
    }