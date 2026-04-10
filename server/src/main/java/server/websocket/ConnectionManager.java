package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

public class ConnectionManager {

    // gameID -> set of sessions
    private final ConcurrentHashMap<Integer, Set<Session>> connections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    // ADD
    public void add(int gameID, Session session) {
        connections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    // REMOVE
    public void remove(int gameID, Session session) {
        if (connections.containsKey(gameID)) {
            connections.get(gameID).remove(session);
        }
    }

    // REMOVE (on disconnect)
    public void remove(Session session) {
        for (Set<Session> sessions : connections.values()) {
            sessions.remove(session);
        }
    }

    // SEND (to one client)
    public void send(Session session, ServerMessage message) throws IOException {
        if (session.isOpen()) {
            System.out.println("connection manager sending to one to client " + gson.toJson(message));
            session.getRemote().sendString(gson.toJson(message));
        }
    }

    // BROADCAST
    public void broadcast(int gameID, Session excludeSession, ServerMessage message) throws IOException {
        Set<Session> sessions = connections.get(gameID);

        if (sessions == null) { return; }
        String msg = gson.toJson(message);
        System.out.println("Connection manager broadcasting " + msg);

        for (Session s : sessions) {
            if (s.isOpen() && (excludeSession == null || !s.equals(excludeSession))) {
                s.getRemote().sendString(msg);
            }
        }
    }
}
