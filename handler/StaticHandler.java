package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class StaticHandler implements HttpHandler {
    private final Path webRoot;
    
    public StaticHandler(){
        this("Web");
    }
    public StaticHandler(String webRootDir){
        this.webRoot = Paths.get(webRootDir).toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException{
        try{
            String rawPath = ex.getRequestURI().getPath();
            if(rawPath == null || rawPath.isEmpty()){
                rawPath =  "/";
            }
            
            if(rawPath.contains("..")){
                sendText(ex, 403, "Forbidden");
                return;
            }

            String decodedPath = URLDecoder.decode(rawPath, StandardCharsets.UTF_8);
            if(decodedPath.contains("..")){
                sendText(ex, 403, "Forbidden");
                return;
            }

            String relativePath = decodedPath.equals("/") ? "login.html" : decodedPath.replaceFirst("^/+", "");

            if(relativePath.isEmpty()){
                relativePath = "login.html";
            }

            Path targetFile = webRoot.resolve(relativePath).normalize();

            if(!targetFile.startsWith(webRoot) || targetFile.equals(webRoot)){
                sendText(ex, 403, "Forbidden");
                return;
            }

            if(!Files.exists(targetFile) || !Files.isRegularFile(targetFile)){
                sendText(ex, 404, "Not Found");
                return;
            }

            byte[] bytes = Files.readAllBytes(targetFile);
            String contentType = detectContentType(targetFile.getFileName().toString());

            ex.getResponseHeaders().set("Content-Type", contentType);
            ex.sendResponseHeaders(200, bytes.length);

            try(OutputStream os = ex.getResponseBody()){
                os.write(bytes);
            }
        }
        catch(Exception e){
                e.printStackTrace();
                if(!ex.getResponseHeaders().containsKey("Content-Type")){
                    sendText(ex, 500, "Server Error");
                }else{
                    ex.close();
                }         
            }   
    }

    private String detectContentType(String filename){
        String lower = filename.toLowerCase(Locale.ROOT);
        int dotIndex = lower.lastIndexOf(".");
        String ext = dotIndex >= 0 ? lower.substring(dotIndex + 1) : "";

        return switch(ext){
            case "html", "htm" -> "text/html; charset=UTF-8";
            case "css" -> "text/css; charset=UTF-8";
            case "js", "mjs" -> "application/javascript; charset=UTF-8";
            case "json" -> "application/json; charset=UTF-8";
            case "png" -> "image/png";
            case "svg" -> "image/svg+xml";
            case "jpg", "jpeg" -> "image/jpeg";
            case "ico" -> "image/x-icon";
            default -> "application/octet-stream";
        };
    }
    private void sendText(HttpExchange ex, int statusCode, String message) throws IOException{
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF_8");
        ex.sendResponseHeaders(statusCode, bytes.length);
        try(OutputStream os = ex.getResponseBody()){
            os.write(bytes);
        }
    }
}
