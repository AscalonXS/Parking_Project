import com.sun.net.httpserver.HttpServer;

import DB.UserDao;
import java.net.*;
import java.io.IOException;
import java.nio.file.*;

import handler.AuthHandler;
import handler.MonitorHandler;
import handler.UserHandler;

import services.ParkingService;
import services.SessionManager;


class Server {

    public static void main(String[] args) 
    {

        try 
        {
        HttpServer serv = HttpServer.create(new InetSocketAddress(8080),0);

        ParkingService parkingService = new ParkingService();
        SessionManager sessionManager = new SessionManager();
        UserDao userDao = new UserDao();

        AuthHandler authHandler = new AuthHandler(parkingService, sessionManager, userDao);
        MonitorHandler monitorHandler = new MonitorHandler(parkingService, sessionManager);
        UserHandler userHandler = new UserHandler(parkingService, sessionManager);

        serv.createContext("/api/login", authHandler);
        serv.createContext("/api/logout", authHandler);
        serv.createContext("/api/me", authHandler);

        serv.createContext("/api/monitor/status", monitorHandler);

        serv.createContext("/api/lots", userHandler);
        serv.createContext("/api/park", userHandler);
        serv.createContext("/api/leave", userHandler);
        serv.createContext("/api/car", userHandler);
        

        serv.createContext("/", exchange -> {

                String p = exchange.getRequestURI().getPath();
                if("/".equals(p)) {
                    p = "/ui/index.html";
                }
                Path file_path = Path.of("." + p);

                if (!Files.exists(file_path) || Files.isDirectory(file_path)) 
                {
                    String rb = "404 not F0uNd";

                    exchange.sendResponseHeaders(404, rb.length());
                    exchange.getResponseBody().write(rb.getBytes());
                    exchange.close();
                    return;
                }
                else
                {
                    byte[] data = Files.readAllBytes(file_path);
                    String content_type = Files.probeContentType(file_path);

                    if(content_type == null ){
                    
                        if(p.endsWith(".js")) content_type = "application/javascript";
                        else if(p.endsWith(".css")) content_type = "text/css";
                        else if(p.endsWith(".html")) content_type = "text/html";
                        else if(p.endsWith(".png")) content_type = "image/png";
                        else content_type = "application/octet-stream";
                                                

                    }
                    exchange.getResponseHeaders().set("Content-Type", content_type);
                    exchange.sendResponseHeaders(200, data.length);
                    exchange.getResponseBody().write(data);

                    exchange.close();
                }
        });  

        serv.start();
        System.out.println("Server started on http://localhost:8080");

        } catch (IOException e) 
        {
            e.printStackTrace();
        }     
        

  
    }
}