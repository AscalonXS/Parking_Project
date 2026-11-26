package handler;

import DB.UserDao;
import DB.UserDao.UserAccount;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Map;
import services.ParkingService;
import services.SessionManager;

public class AuthHandler extends BaseHandler
{
    private final UserDao userDao;

    /* The JSON the login page sends: {username, password} */
    private static class LoginRequest
    {
        String username;
        String password;
    }

    public AuthHandler(ParkingService service, SessionManager sessions, UserDao userDao)
    {
        super(service, sessions);
        this.userDao = userDao;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException
    {
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();

        try
        {
            if (path.equals("/api/login"))
            {
                if (!method.equals("POST"))
                {
                    sendError(ex, 405, "Use POST");
                    return;
                }
                login(ex);
            }
            else if (path.equals("/api/logout"))
            {
                if (!method.equals("POST"))
                {
                    sendError(ex, 405, "Use POST");
                    return;
                }
                logout(ex);
            }
            else if (path.equals("/api/me"))
            {
                if (!method.equals("GET"))
                {
                    sendError(ex, 405, "Use GET");
                    return;
                }
                me(ex);
            }
            else
            {
                sendError(ex, 404, "Not found");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            sendError(ex, 500, "Server error");
        }
    }

    private void login(HttpExchange ex) throws IOException
    {
        LoginRequest req;
        try
        {
            req = gson.fromJson(readBody(ex), LoginRequest.class);
        }
        catch (JsonSyntaxException e)
        {
            req = null;
        }

        if (req == null || req.username == null || req.password == null)
        {
            sendError(ex, 400, "Send a username and a password");
            return;
        }

        if (!userDao.checkPassword(req.username, req.password))
        {
            sendError(ex, 401, "Wrong username or password");
            return;
        }

        UserAccount user = userDao.findByUsername(req.username);
        String token = sessions.createSession(user);

        // the browser sends this cookie back on every later request
        ex.getResponseHeaders().add("Set-Cookie", "session=" + token + "; HttpOnly; Path=/");
        sendJson(ex, 200, Map.of("username", user.getUsername(), "role", user.getRole()));
    }

    private void logout(HttpExchange ex) throws IOException
    {
        sessions.removeSession(getToken(ex));

        ex.getResponseHeaders().add("Set-Cookie", "session=; Max-Age=0; Path=/");   // deletes the cookie
        sendJson(ex, 200, Map.of("ok", true));
    }

    private void me(HttpExchange ex) throws IOException
    {
        UserAccount user = sessions.getUser(getToken(ex));

        if (user == null)
        {
            sendError(ex, 401, "Please log in");
            return;
        }
        sendJson(ex, 200, user);   // {id, username, role, carNumber}; carNumber is left out when null
    }
}