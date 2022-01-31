package co.gerger.pagelet2;


import freemarker.core.ParseException;

import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;

import freemarker.template.TemplateNotFoundException;

import java.io.IOException;

import java.util.HashMap;

import java.util.concurrent.ConcurrentHashMap;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PageletServerImpl {
    
    public PageletServerImpl() {
        super();
    }
    
    public static String getAuthorizationToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        String accessToken = null;
        log("doRequest:about to loop cookies");
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log("doRequest:cookie name="+cookie.getName());
                if (cookie.getName().equals("pagelet2accesstoken")) {
                    accessToken = cookie.getValue();
                }
            }
        }  
        return accessToken;
    }
    
    public static String doRequest(HttpServletRequest request, HttpServletResponse response) throws PageletServerException {
        //this.session = request.getSession();
        log("doRequest:start");
        String accessToken = getAuthorizationToken(request);
        String action = request.getParameter("action");
        String text = "";
        String packageName = request.getParameter("package");
        ApplicationImpl.initializeFreeMarker(request.getServletContext().getRealPath("/"));
        ApplicationImpl.processServerMethods(packageName);        
        if (action!=null && action.equals("listservermethods")){
            text = ApplicationImpl.getServerMethods();
        }
        else if (action!=null && action.equals("execute")){            
            String controllerName = request.getParameter("controller");
            String methodName = request.getParameter("method");
            String inputs = request.getParameter("inputs");
            text = ApplicationImpl.execute(controllerName,methodName,inputs,accessToken,response);
        }
        return text;
    }
    
    public static Template getTemplate(String name) throws PageletServerException {
        Template temp = null;
        try{
            temp = ApplicationImpl.getTemplate(name);    
        } catch (MalformedTemplateNameException | ParseException | TemplateNotFoundException e) {
            e.printStackTrace();
            throw new PageletServerException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new PageletServerException(e.getMessage());
        }
        return temp;
    }
    
    private static void log(String message){
        //System.out.println("PageletServerImpl: "+message);
        Logger.getLogger("PageletServerImpl").log(Level.WARNING, message);  
    }
}