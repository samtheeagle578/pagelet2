package co.gerger.pagelet2;


import java.util.HashMap;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PageletServerImpl {
    
    private static ConcurrentHashMap<String,Application> applications = new ConcurrentHashMap<>();
    
    public PageletServerImpl() {
        super();
    }
    
    public static String doRequest(HttpServletRequest request, HttpServletResponse response) throws PageletServerException {
        //this.session = request.getSession();
        Cookie[] cookies = request.getCookies();
        Cookie accessTokenCookie = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("pagelet2accesstoken")) {
                    accessTokenCookie = cookie; 
                }
            }
        }
        String action = request.getParameter("action");
        String text = "";
        String appName = request.getParameter("application");
        String packageName = request.getParameter("package");
        if (appName==null){
            throw new PageletServerException("Please specify an application name.");
        }
        Application app = applications.get(appName);
        if (app==null){
            app = new ApplicationImpl(packageName);
            applications.putIfAbsent(appName, app);
            app = applications.get(appName);
        }        
        if (action!=null && action.equals("listservermethods")){
            text = app.getServerMethods();
        }
        else if (action!=null && action.equals("execute")){            
            String controllerName = request.getParameter("controller");
            String methodName = request.getParameter("method");
            String inputs = request.getParameter("inputs");
            text = app.execute(controllerName,methodName,inputs,accessTokenCookie,response);
        }
        return text;
    }
}