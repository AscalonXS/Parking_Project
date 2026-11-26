package handler;

import DB.UserDao.UserAccount;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import services.LeaveResult;
import services.LotDetail;
import services.ParkResult;
import services.ParkingService;
import services.SessionManager;

public class UserHandler extends BaseHandler{
    // JSON body sent by POST /api/park: {"lot": "A1"}
    private static class ParkRequest{
        String lot;
    }
    public UserHandler(ParkingService service, SessionManager sessions){
        super(service, sessions);
    }
    @Override 
    public void handle(HttpExchange ex) throws IOException{
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();

        try{
            if(path.equals("/api/lots")){
                if(!method.equals("GET")){
                    sendError(ex, 405, "Use GET");
                    return;
                }
                getLots(ex);
            }
            else if(path.startsWith("/api/lots/")){
                if (!method.equals("GET")){
                    sendError(ex, 405, "Use GET");
                    return;
                }
                getLotByName(ex, path);
            }
            else if(path.equals("/api/park")){
                if(!method.equals("POST")){
                    sendError(ex, 405, "Use POST");
                    return;
                }
                park(ex);
            }
            else if(path.equals("/api/leave")){
                if(!method.equals("POST")){
                    sendError(ex, 405, "Use POST");
                    return;
                }
                leave(ex);
            }
            else if(path.equals("/api/car")){
                if(!method.equals("GET")){
                    sendError(ex, 405, "Use GET");
                    return;
                }
                getCar(ex);
            }
            else{
                sendError(ex, 404, "Not Found");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            sendError(ex, 500, "Server Error");
        }
    }

    // GET /api/lots ->return summary of all parking lots
    private void getLots(HttpExchange ex) throws IOException{
        UserAccount user = requireRole(ex, "user");
        if(user == null){
            return;
        }
        sendJson(ex, 200, service.getLots());
    }

    // GET /api/lots/{name} -> return details and slots of one lot
    private void getLotByName(HttpExchange ex, String path) throws IOException{
        UserAccount user = requireRole(ex, "user");
        if(user == null){
            return;
        }
        // extract and URL-decode the lot name from /api/lots/{name}
        String rawName = path.substring("/api/lots/".length());
        String lotName = URLDecoder.decode(rawName, StandardCharsets.UTF_8);

        if(lotName.isEmpty()){
            sendError(ex, 404, "Lot Not Found");
            return;
        }

        LotDetail detail = service.getLotDetail(lotName);
        if(detail == null){
            sendError(ex, 404, "Lot not found");
            return;
        }
        sendJson(ex, 200, detail);
    }

    // POST /api/park - > park the logged-in user's car in the chosen lot
    private void park(HttpExchange ex) throws IOException{
        UserAccount user = requireRole(ex, "user");
        if(user == null){
            return;
        }

        ParkRequest req;
        try{
            req = gson.fromJson(readBody(ex), ParkRequest.class);
        }
        catch(JsonSyntaxException e){
            req = null;
        }

        if(req == null || req.lot == null || req.lot.isBlank()){
            sendError(ex, 400, "Send a lot name");
            return;
        }

        // always use user.getCarNumber() so nobody can park someone else's car
        ParkResult res = service.synchronizedPark(user.getCarNumber(), req.lot);
        if(!res.success){
            sendError(ex, 409, res.message);
            return;
        }
        sendJson(ex, 200, res);
    }

    // POST /api/leave -> release the slot and calculate fare
    private void leave(HttpExchange ex) throws IOException{
        UserAccount user = requireRole(ex, "user");
        if(user == null){
            return;
        }
        
        LeaveResult res = service.synchronizeLeaveResult(user.getCarNumber());
        if(!res.success){
            sendError(ex, 409, res.message);
            return;
        }
        sendJson(ex, 200, res);
    }

    // GET /api/car -> return info about the logged-in user's car
    private void getCar(HttpExchange ex) throws IOException{
        UserAccount user = requireRole(ex, "user");

        if(user == null){
            return;
        }

        Object info = service.getCarInfo(user.getCarNumber());
        if(info == null){
            sendError(ex, 404, "Car not found");
            return;
        }
        sendJson(ex, 200, info);
    }
}
