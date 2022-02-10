package co.gerger.pagelet2;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "ValueListResponder", urlPatterns = { "/valuelistresponder" })
public class ValueListResponder extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        //String event=request.getParameter("action");
        //doLog("ACTION="+event);
        String result= "";
        doLog("step 1");
        try {
            result = PageletServerImpl.doValueListRequest(request,response);
        } catch (PageletServerException e) {
            out.print("ServerException:");
            out.print(e.getMessage());
        }
        doLog("step 4");
        response.setContentType(CONTENT_TYPE);
        out.print(result);                        
        out.close();
        doLog("step 5");
    }
    
    private void doLog(String message){
        //System.out.println("HTTP Servlet: "+message);
        Logger.getLogger("ValueListProvider").log(Level.WARNING, message);  
    }   
}
