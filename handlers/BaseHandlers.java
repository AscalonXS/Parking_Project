package handlers;

import DB.UserDao.UserAccount;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import services.ParkingService;
import services.SessionManager;

public abstract class BaseHandlers implements HttpHandler
{
    protected final ParkingService service;
    protected final SessionManager sessions;
    protected final Gson gson = new Gson();

    public BaseHandlers(ParkingService service, SessionManager sessions)
    {
        this.service = service;
        this.sessions = sessions;
    }

    /* Reads the request body as text. Returns "" when there is no body. */
    protected String readBody(HttpExchange ex) throws IOException
    {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    /* Turns data into JSON and sends it with the given status code (200, 404, ...). */
    protected void sendJson(HttpExchange ex, int status, Object data) throws IOException
    {
        byte[] bytes = gson.toJson(data).getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = ex.getResponseBody())
        {
            os.write(bytes);
        }
    }

    /* Sends {"error": message} with the given status code. */
    protected void sendError(HttpExchange ex, int status, String message) throws IOException
    {
        sendJson(ex, status, Map.of("error", message));
    }

    /* Reads the session value from the Cookie header. Returns null when there is none. */
    protected String getToken(HttpExchange ex)
    {
        String header = ex.getRequestHeaders().getFirst("Cookie");
        if (header == null)
        {
            return null;
        }

        for (String part : header.split(";"))
        {
            String[] pair = part.trim().split("=", 2);
            if (pair.length == 2 && pair[0].equals("session"))
            {
                return pair[1];
            }
        }
        return null;
    }

    /*
     * Returns the logged-in user if they have this role.
     * Otherwise it sends 401 (not logged in) or 403 (wrong role) and returns null,
     * so the caller must stop when it gets null.
     */
    protected UserAccount requireRole(HttpExchange ex, String role) throws IOException
    {
        UserAccount user = sessions.getUser(getToken(ex));

        if (user == null)
        {
            sendError(ex, 401, "Please log in");
            return null;
        }
        if (!user.getRole().equals(role))
        {
            sendError(ex, 403, "Not allowed");
            return null;
        }
        return user;
    }
}