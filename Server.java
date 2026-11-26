import com.sun.net.httpserver.HttpServer;

import DB.UserDao;
import java.net.*;
import java.io.IOException;
import java.nio.file.*;

import handler.AuthHandler;
import handler.MonitorHandler;
import handler.UserHandler;
import handler.StaticHandler;
import handler.CountHandler;

import services.ParkingService;
import services.SessionManager;
import services.HardwareCounter;


class Server {

    public static void main(String[] args) 
    {

        try 
        {
        HttpServer serv = HttpServer.create(new InetSocketAddress(8080),0);

        ParkingService parkingService = new ParkingService();
        SessionManager sessionManager = new SessionManager();
        UserDao userDao = new UserDao();

        // reads data/cars.csv (written by the Arduino bridge) and counts the cars inside
        HardwareCounter hardware = new HardwareCounter("data/cars.csv");
        hardware.start();

        AuthHandler authHandler = new AuthHandler(parkingService, sessionManager, userDao);
        MonitorHandler monitorHandler = new MonitorHandler(parkingService, sessionManager);
        UserHandler userHandler = new UserHandler(parkingService, sessionManager);
        CountHandler countHandler = new CountHandler(parkingService, sessionManager, hardware);

        serv.createContext("/api/login", authHandler);
        serv.createContext("/api/logout", authHandler);
        serv.createContext("/api/me", authHandler);

        serv.createContext("/api/monitor/status", monitorHandler);

        serv.createContext("/api/lots", userHandler);
        serv.createContext("/api/park", userHandler);
        serv.createContext("/api/leave", userHandler);
        serv.createContext("/api/car", userHandler);

        serv.createContext("/api/count", countHandler);

        serv.createContext("/", new StaticHandler("ui"));
        


        serv.start();
        System.out.println("Server started on http://localhost:8080");

        } catch (IOException e) 
        {
            e.printStackTrace();
        }     
        

  
    }
}