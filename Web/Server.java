import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.nio.file.*;


class Server
{
    public static void main(String[] args) 
    {

        try 
        {
        HttpServer serv = HttpServer.create(new InetSocketAddress(8080),0);
        serv.createContext("/", exchange -> {

                String p = exchange.getRequestURI().getPath();
                Path file_path = Path.of("." + p);

                if (!(Files.exists(file_path))) 
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

                    exchange.getResponseHeaders().set("Content-Type", content_type);
                    exchange.sendResponseHeaders(200, data.length);
                    exchange.getResponseBody().write(data);

                    exchange.close();
                }
        });   
        serv.start();
        System.out.println("Server running on http://localhost:8080/ui/index.html");
        System.out.flush();

        } catch (IOException e) 
        {
            
        }     
        

  
    }
}