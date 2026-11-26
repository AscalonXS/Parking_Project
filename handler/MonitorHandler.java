package handler;

import DB.UserDao.UserAccount;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.List;
import services.LotDetail;
import services.ParkingService;
import services.SessionManager;

    public class MonitorHandler extends BaseHandler{
    public MonitorHandler(ParkingService service, SessionManager sessions){
        // pass shared service and session to BaseHandler
        super(service, sessions);
    }

    @Override 
    public void handle(HttpExchange ex) throws IOException{
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();

        try{
            if(path.equals("/api/monitor/status")){
                // only GET is allowed for monitor status
                if(!method.equals("GET")){
                    sendError(ex, 405, "Use GET");
                    return;
                }

                // allow only logged-in users with role "monitor"
                UserAccount user = requireRole(ex, "monitor");
                if(user == null){
                    return;
                }

                // read all lots and slots without changing any data
                List<LotDetail> status = service.getAllStatus();
                sendJson(ex, 200, status);
            }
            else{
                sendError(ex, 404, "Not Found");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            sendError(ex, 500, "Server error");
        }
    }
}