package org.utn.util;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServletUtils {
    private static final Gson gson = new Gson();

    public static void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(data));
    }

    public static <T> T parseJsonRequest(HttpServletRequest req, Class<T> clazz) throws IOException {
        return gson.fromJson(new InputStreamReader(req.getInputStream()), clazz);
    }
}