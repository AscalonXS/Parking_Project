# Parking System 

This is a program for Parking System,
The flow will be 
Java server Will Receive an request and response and check it with an allocated DB 

Response and Update the db if needed 
then give the response to use or monitor

Kinda like this

## Info About each Folder 

    - DB -> Data Base Builder and Data Access 
    - Parking Classes -> Holds Program Logic 
    - Parking_Projecr -> idk i will delete it later 
    - Web -> Holds Web Files And Handler 



# Each File Info 

    Info about Each File For The Big Picture Based on Each Folder 

## Parking_Classes Package  

### Car.java 

**Attributes**
    - carType : String 
    - OwnerName : String 
    - Number : String 
    - haveParkSpace : boolean 
    - ps : ParkSpace 
    - balance : int
    - takePlace : LocalDateTIme 

*Method*   
    - Car : Car Class Constructor, Initial The Car Object. 

    - takeParkSpace(ParkSpace) : Car Taking ParkSpace and time for taking place is Started return **void** 

    - CalculateFare :  take the duration, and return how much won needed 

    - releaseParkSpace : Reset all 
    takeParkSpace changed, and calculate the balance based on  duration (start to stop) ret *void*

    - getNumber : return String Number Of Plate 

    - getOwnerName : return String OwnerName 

    - getBalance : return Balance int 

    - getPs : return the taken ParkSpace 

    - hasPublicSpace : return true if car taking a parkSpace and viceVersa 

    - getTakePlace : return localDateTime when it takes place 

### Parking.java 

    It's a Parking Lot Class 

*Attributes*

    - ParkList : LinkedList <ParkSpace> 
    - ParkName : String 
    - addr : String                     // Address of Parking Place 
    - available_park_space : Deqeue<ParkSpace> 


*Methods*
    - Parking (String Name , String addr) : Constructor for Parking Lot 

    - Initialiazing_List_Park_Space(String ... temp) : return void, executed when we want to initialize parking Lot with what Parking space it has and available parking space. 

    - addAvailableParkSpace(ParkSpace a) : the parking space, we add it back to available park

    - takeParkSpace() : returning ParkSpace and removeit from available parklist 

    - isFull() : return true if park is full

    - display_Status() : Just for Debugging 

    - getParkName() : Return String of Parking Lot's Name 

    - getAddr() : Return String of Parking Lots's Address

    -  getTotalSpace() : return int number of parking space that parking Lot has
    
    - countAvailableSpace() : return int number of AvailableSpace

    - getAllSpaces() : return available Spaces 

    - findSpaceById(String) : return ParkSpace if found and null if not found 

### ParkingManager.java 

    Inherits Parking 

*Additional Method*

    - addParkingSpace(String) : adding new Parking Space to Parklist with assumption the parkSpace is empty
                                and newley created 
    
    - removeParkSpace(String) : remove ParkSpace from parking lots

### ParkSpace.java 

*Attributes*
    isTaken : boolean 
    ID : String 
    belongToWhichParking : Parking 

*Methods*
    ParkSpace(String, Parking,) : initialize isTaken as False By default
                                  and construct ParkSpace then return it 

     getID() : return String ID

     isTaken() : return true if parkspace is taken 

     getBelongToWhichParking() : getter for Belong to which Parking 

     setIsTaken(bool) : setter for isTaken

     equals(Object) : OverRide the equals method, 
                      whether method checking the ID 

    hashCode() : we change how hashcode is taken, why ? usually it's still possible for Difference Parking Lot
                 has the same parking id. 

## DB 


### DataBase.java

    Methods : 
        - getConnection() : return Connection to mysql
 
### ParkingDB.SQL 

*Data Bases* 
    -> ParkingDB  

Tables : 
    - users 
        Columns : 
            + id (Primary Key)
            + username (String)
            + password_hash
            + role (user + monitor)

    - parking_lots 
        Columns : 
            + id (Primary Key)
            + name(String)
            + address (String)

    - park_spaces 
        Columns : 
            + id (Primary Key)
            + lot_id (Int)
            + space_Code (String)
            + is_taken (Bit)
            + fk_spaces_lot (Foreign Key)

    - Cars 
        Columns : 
            + id (Primary Key)
            + plate_number (String)
            + owner_name (String)
            + car_type (String)
            + balance (int)
            + user_id (int)

    - sessions 
        Columns : 
            + id (Primary Key)
            + space_id (int)
            + car_id (int)
            + Start_time (DateTime)
            + end_time (DateTime)
            + fare (int)

### UserDAO.java
*Attributes*
    - id : int
    - username : String
    - role : String
    - carNumber : String

*Methods*
    - UserAccount(
        int -> id, 
        String -> username, 
        String -> role (user/monitor),
        String -> carNumber    
    ) : Constructor for UserAccount Object 

    - getID() : return int id 
    - getUserName() : return String username
    - getRole() : return role String 
    - getCarNumber() : return String carNumber
    - findByUsername(String username) : return string UserAccount Object if found but null if not found
    - createUser(String username,
                 String password,
                 String role 
    ) : return true if the input new account is succeed. 
        throw illegal_argument if role is not correct 
        throw RunTimeException()

### ParkingDao.java

*Helper class (private, inside ParkingDao.java)*
    - LotRow : holds one lot's data while the query is being read
        - name : String
        - addr : String
        - codes : List<String> (every slot code of the lot, in order)
        - taken : Set<String> (the codes that are taken)

*Methods*
    - loadAllLots()
        : returns List<Parking>, one ParkingManager per lot with all its slots
        : a lot with no slots is still returned (with 0 slots)
        : slots with is_taken = 1 are marked taken and left out of the available list
        : throws RuntimeException on a database error

    - buildLot(String name, String addr, List<String> codes, Set<String> takenCodes)
        : static, package-private (used by loadAllLots)
        : returns a Parking whose slots are all the codes, with the takenCodes marked taken
        : slots keep the same order as codes
        : does not touch the database

    - loadCar(String number)
        : number is the plate number
        : returns the Car (type, owner, plate, balance), not parked
        : returns null if the plate is not in the database
        : throws RuntimeException on a database error

    - loadOpenSessions()
        : returns List<String[]>, one row per car that is parked right now
        : each row is {carNumber, lotName, spaceId, sessionId, startTime}
        : startTime is text in the form 2026-11-16T09:30:00
        : returns an empty list if no car is parked
        : throws RuntimeException on a database error

    - saveSpaceStatus(String lotName, String spaceId, boolean taken)
        : saves is_taken for one slot (true = taken, false = free)
        : returns nothing (void)
        : throws IllegalArgumentException if there is no slot spaceId in lot lotName
        : throws RuntimeException on a database error

    - startSession(String carNumber, String lotName, String spaceId, LocalDateTime start)
        : records that a car parked; end_time and fare stay empty
        : returns int, the id of the new session
        : throws IllegalArgumentException if the car or the slot does not exist
        : throws RuntimeException on a database error (for example the car or the slot already has an open session)

    - endSession(int sessionId, LocalDateTime end, int fare)
        : sets end_time and fare on that session
        : returns nothing (void)
        : does nothing if the session is already ended or does not exist
        : throws RuntimeException on a database error

    - updateBalance(String carNumber, int newBalance)
        : saves the car's new balance
        : returns nothing (void)
        : does nothing if the plate does not exist
        : throws RuntimeException on a database error

    - getHistory(String carNumber)
        : returns List<String[]>, one row per finished visit, newest first
        : each row is {lotName, spaceId, start, end, fare}, all as text
        : returns an empty list if the car has no finished visits
        : throws RuntimeException on a database error

