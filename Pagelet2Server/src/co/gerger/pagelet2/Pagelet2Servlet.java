package co.gerger.pagelet2;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

@WebServlet(name = "Pagelet2Servlet", urlPatterns = { "/pagelet2servlet" })
public class Pagelet2Servlet extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
    
    private void doLog(String message){
        //System.out.println("HTTP Servlet: "+message);
        Logger.getLogger("Pagelet2Servlet").log(Level.WARNING, message);  
    }   

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //doLog("gitblit="+);
        //counter = counter + 1;
        PrintWriter out = response.getWriter();
        String event=request.getParameter("action");
        doLog("ACTION="+event);
        String result= "";
        doLog("step 1");
        try {
            result = PageletServerImpl.doRequest(request,response);
        } catch (PageletServerException e) {
            e.printStackTrace();
            //out.print("ServerException:");
            //out.print(e.getMessage());
        }
        doLog("step 4");
        response.setContentType(CONTENT_TYPE);
        out.print(result);                        
        out.close();
        doLog("step 5");

    }
}