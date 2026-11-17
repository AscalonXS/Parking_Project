package services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import DB.UserDao.UserAccount;

public class SessionManager
{
    // token -> the user who logged in with it
    private final Map<String, UserAccount> sessions = new ConcurrentHashMap<>();

    public String createSession(UserAccount user)
    {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public UserAccount getUser(String token)
    {
        if (token == null)
        {
            return null;   
        }
        return sessions.get(token);
    }

    public void removeSession(String token)
    {
        if (token != null)
        {
            sessions.remove(token);
        }
    }
}