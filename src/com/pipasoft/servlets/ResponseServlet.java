package com.pipasoft.servlets;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet that will return content as configured by response.properties
 * 
 * @author bpipa
 *
 */
public class ResponseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static String filesFolder;
    private static final String STATUS=".statuscode";
    private static final String CONTENT=".content";
    private static final String CONTENT_TYPE=".content.type";
    private static final String CONTENT_FILE=".content.file";
    private static final String DELAY=".delay";
    private static final String DEFAULT_CONTENT="default content";
    

    /**
     * the servlet init. Sets up the filesFolder
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        filesFolder = getServletContext().getRealPath("responses")+File.separatorChar;
    }
    
    /**
     * the servlet service method. Will accept POST or GET
     */
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        //allow cross-domain for all
        res.setHeader("Access-Control-Allow-Origin","*");
                
        String name = req.getParameter("responsename");
        if (name == null || name.isEmpty()) {
            name="help";
        }
        name=name.trim();
        
        //load properties each time in case it changed
        Properties props = new Properties();
        props.load(getServletContext().getResourceAsStream("/response.properties"));       
        
        outputResponse(name, props, res);        
    }

    /**
     * Outputs the desired response
     * The status code and the content type is set elsewhere
     * 
     * @param content the content to send
     * @param res HttpServletResponse
     * @throws IOException
     */
    private void outputResponse(String content, HttpServletResponse res) throws IOException {
        
        PrintWriter out = null;
        try {
            out = res.getWriter();
            if (content != null && !content.isEmpty()) {
                out.print(content);    
            } else {
                out.print(DEFAULT_CONTENT);
            }
                                                    
        } catch (Exception e) {
            if (out != null) {
                out.print(e.getMessage());   
            }                
        } finally {
            if (out != null) {
                out.flush();
                out.close();                
            }
        }
    }
    
    /**
     * Outputs the desired response
     * 
     * @param name the root key to use for the settings
     * @param props the Properties object
     * @param res HttpServletResponse
     * @throws IOException
     */
    private void outputResponse(String name, Properties props, HttpServletResponse res) throws IOException {
        String content = "default content";
        String filename = null;
        
        if (props.containsKey(name+CONTENT_FILE)) {
            filename = (String)props.get(name+CONTENT_FILE);
        } else { 
            if (props.containsKey(name+CONTENT)) {
                content = (String)props.get(name+CONTENT);
            }                    
        }
        
        int statusCode = HttpServletResponse.SC_OK;      
        if (props.containsKey(name+STATUS)) {
            try {
                statusCode = Integer.parseInt((String)props.get(name+STATUS));
            } catch (NumberFormatException nfe) {
                // wasn't an int. ignore it, send back 200
            }             
        }        
         
        String contentType = "";
        if (props.containsKey(name+CONTENT_TYPE)) {
            contentType = (String)props.get(name+CONTENT_TYPE);
        }

        if (props.containsKey(name+DELAY)) { 
            doDelay((String)props.get(name+DELAY));
        }        
        
        setStatusAndType(statusCode, contentType, res);
        if (filename != null && !filename.isEmpty()) {
            outputResponse(new File(filesFolder+filename), res);
        } else {
            outputResponse(content, res);
        }        
    }
    
    /**
     * adds in an artificial delay as specified in the props file
     * 
     * @param delayString a String representing the number 
     *          of ms to delay sending the response
     */
    private void doDelay(String delayString) {
        int delay = 0;
        try {
            delay = Integer.parseInt(delayString);
        } catch (NumberFormatException nfe) {
            // wasn't an int. ignore it and return
            return;
        }           
        
        if (delay > 0) {
            try {
                Thread.sleep(delay);   
            } catch (InterruptedException ie) {
                //we don't care really, just ignore it
                ie.printStackTrace();
            }
        }
    }
    
    /**
     * sets the status code and the content type
     * 
     * @param statusCode the status code to send back
     * @param contentType the content type to set (like "text/html")
     * @param res HttpServletResponse 
     */
    private void setStatusAndType(int statusCode, String contentType, HttpServletResponse res) {
        if (contentType != null && !contentType.isEmpty()) {
            res.setContentType(contentType);    
        }        
        res.setStatus(statusCode);
    }
    
    /**
     *  Sends the contents of the file
     *
     *  @param res The response
     *  @param fileToSend the file to send back
     */
    private void outputResponse(File fileToSend, HttpServletResponse res) throws IOException
    {
        if (!fileToSend.exists()) {
            setStatusAndType(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "text/plain", res);
            outputResponse("file "+fileToSend.getAbsolutePath()+" does not exist", res);
            return;
        }
        
        int length;
        ServletOutputStream op = res.getOutputStream();
        res.setContentLength( (int)fileToSend.length() );

        //  Stream to the requester.
        byte[] bbuf = new byte[256];
        DataInputStream in = new DataInputStream(new FileInputStream(fileToSend));

        while ((in != null) && ((length = in.read(bbuf)) != -1)) {
            op.write(bbuf,0,length);
        }

        in.close();
        op.flush();
        op.close();
    }    
}
//test4
