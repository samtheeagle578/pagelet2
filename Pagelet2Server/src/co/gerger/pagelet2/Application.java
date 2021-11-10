package co.gerger.pagelet2;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public interface Application {
    String getServerMethods();
    String execute(String controllerName, String methodName,String inputs, Cookie accessToken, HttpServletResponse response) throws PageletServerException;
}
