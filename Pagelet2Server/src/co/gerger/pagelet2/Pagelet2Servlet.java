package co.gerger.pagelet2;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

@WebServlet(name = "Pagelet2Servlet", urlPatterns = { "/pagelet2servlet" })
public class Pagelet2Servlet extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
    private PageletServerImpl server;
    
    private void doLog(String message){
        System.out.println("HTTP Servlet: "+message);    
    }   

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>GitoraForDataServlet</title></head>");
        out.println("<body>");
        out.println("<p>The servlet has received a GET. This is the reply.</p>");
        out.println("</body></html>");
        out.close();
    }

        public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            //doLog("gitblit="+);
            //counter = counter + 1;
            PrintWriter out = response.getWriter();
            String event=request.getParameter("action");
            doLog("ACTION="+event);
            boolean initEvent = false;
            //doLog("EVENT="+event);
            if (event!=null && event.equals("listservermethods")){
                initEvent=true;
            }            
            HttpSession session = request.getSession(false);
            if (session==null && initEvent==false){
                out.print("RELOAD");
                doLog("SESSION CANNOT BE FOUND");
            }
            else{
                //doLog("SESSION ID="+session.getId());    
                
                String result= "";
                doLog("step 1");
                if (this.server == null) {
                    this.server = new PageletServerImpl(request);
                }
                doLog("step 2");
                try {
                    result = server.doRequest(request);
                } catch (PageletServerException e) {
                    out.print("ServerException:");
                    out.print(e.getMessage());
                }
                doLog("step 4");
                response.setContentType(CONTENT_TYPE);
                out.print(result);                        
            }
            
            
            out.close();
            doLog("step 5");

        }
}