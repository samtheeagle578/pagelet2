package co.gerger.pagelet2;


import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class PageletServerImpl {
    
    private HttpSession session;
    
    private static String servletContextRealPath;
    private static String requestURL;
    
    private HashMap<String,Application> applications;
    
    public PageletServerImpl(HttpServletRequest request) {
        super();
        this.applications = new HashMap<>();
        PageletServerImpl.requestURL = request.getRequestURL().toString();
        PageletServerImpl.servletContextRealPath = request.getServletContext().getRealPath("/");
    }
    
    String doRequest(HttpServletRequest request) throws PageletServerException {
        this.session = request.getSession();
        String action = request.getParameter("action");
        String text = "";
        if (action!=null && action.equals("listservermethods")){
            String appName = request.getParameter("application");
            String packageName = request.getParameter("package");
            if (appName==null){
                throw new PageletServerException("Please specify an application name.");
            }
            Application app = this.applications.get(appName);
            if (app==null){
                app = new ApplicationImpl(appName,packageName);
                this.applications.put(appName, app);
            }
            text = app.getServerMethods();
        }
        else if (action!=null && action.equals("execute")){
            String appName = request.getParameter("application");
            Application app = this.applications.get(appName);
            String controllerName = request.getParameter("controller");
            String methodName = request.getParameter("method");
            String inputs = request.getParameter("inputs");
            text = app.execute(controllerName,methodName,inputs);
        }
        return text;
    }
    
    public static String getServeletContextRealPath(){
        return PageletServerImpl.servletContextRealPath;
    }

    public static String getRequestURL() {
        return requestURL;
    }
}