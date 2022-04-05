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
        log("GET AUTH TOKEN:about to loop cookies");
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log("GET AUTH TOKEN:cookie name="+cookie.getName());
                log("GET AUTH TOKEN:cookie value="+cookie.getValue());
                if (cookie.getName().equals("pagelet2accesstoken")) {
                    log("PAGELET2 COOKIE DETECTED");
                    accessToken = cookie.getValue();
                }else{
                    log("NOT PAGELET2 COOKIE:cookieName="+cookie.getName());
                }
            }
        }
        log("GET AUTH TOKEN:accessToken="+accessToken);
        return accessToken;
    }
    
    public static String doRequest(HttpServletRequest request, HttpServletResponse response) throws PageletServerException {
        //this.session = request.getSession();
        log("doRequest:start");
        log("DOREQUEST:URLS="+request.getRequestURI()+",,,,"+request.getRequestURL()+",,,"+request.getServerName());
        String accessToken = getAuthorizationToken(request);
        String action = request.getParameter("action");
        String text = "";
        //I can get rid of packe parameter if I send the package name or even better a hash pointing to the package name to the client with the methods
        String packageName = request.getParameter("package");
        ApplicationImpl.initializeFreeMarker(request.getServletContext().getRealPath("/"));
        ApplicationImpl.processServerMethods(packageName);
        log("doRequest:ACTION="+action);
        if (action!=null && action.equals("listservermethods")){
            text = ApplicationImpl.getServerMethods();
        }
        else if (action!=null && action.equals("execute")){            
            String controllerName = request.getParameter("controller");
            String methodName = request.getParameter("method");
            String inputs = request.getParameter("inputs");
            text = ApplicationImpl.execute(controllerName,methodName,inputs,accessToken,response,request);
        }
        return text;
    }
    
    public static String doValueListRequest(HttpServletRequest request, HttpServletResponse response) throws PageletServerException {
        String accessToken = getAuthorizationToken(request);
        String text = "";
        //I can get rid of packe parameter if I send the package name or even better a hash pointing to the package name to the client with the methods
        String valueListName = request.getParameter("valuelist");
        log("DO VALUE LIST REQUEST:valueListName="+valueListName);
        text = ApplicationImpl.getValueList(valueListName,accessToken);
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