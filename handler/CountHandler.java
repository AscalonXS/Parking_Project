package handler;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import services.HardwareCounter;
import services.LotCount;
import services.ParkingService;
import services.SessionManager;
    
public class CountHandler extends BaseHandler{
    private final HardwareCounter hardware;

    public CountHandler(ParkingService service, SessionManager sessions, HardwareCounter hardware){
        super(service, sessions);
        this.hardware = hardware;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException{
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();

        try{
            // only GET is allowed, the count is read-only
            if(!method.equals("GET")){
                sendError(ex, 405, "Use GET");
                return;
            }

            if(path.equals("/api/count")){
                getCounts(ex);
            }
            else if(path.startsWith("/api/count/")){
                getCountByLot(ex, path);
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

    // GET /api/count -> cars inside for every lot the hardware reported
    // (no login needed on purpose, the count is not private. Add requireRole(...) here to change that)
    private void getCounts(HttpExchange ex) throws IOException{
        sendJson(ex, 200, hardware.getAllCounts());
    }

    // GET /api/count/{lot} -> cars inside one lot
    private void getCountByLot(HttpExchange ex, String path) throws IOException{
        // extract and URL-decode the lot name from /api/count/{lot}
        String rawName = path.substring("/api/count/".length());
        String lotName = URLDecoder.decode(rawName, StandardCharsets.UTF_8);

        if(lotName.isEmpty()){
            sendError(ex, 404, "Lot Not Found");
            return;
        }
        sendJson(ex, 200, new LotCount(lotName, hardware.getCount(lotName)));
    }
}