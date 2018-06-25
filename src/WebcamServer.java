import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.json.simple.JSONObject;

public class WebcamServer extends Thread {
	private BlockingQueue<String> receiveQueue;
	private BlockingQueue<String> sendQueue;
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private Logger logger;
	private SimpleFormatter formatterTxt;
	private FileHandler fileTxt;
	
	public WebcamServer(BlockingQueue<String> receiveQueue, BlockingQueue<String> sendQueue) {
		this.receiveQueue = receiveQueue;
		this.sendQueue = sendQueue;
		
		this.logger = Logger.getLogger("webcam_server_log");
		try {
			fileTxt = new FileHandler("server_log.txt", true);
			formatterTxt = new SimpleFormatter();
			fileTxt.setFormatter(formatterTxt);
			logger.addHandler(fileTxt);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			if(clientSocket != null) {
				clientSocket.close();	
			}
			
			if(serverSocket != null) {
				serverSocket.close();	
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {

        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 4444.");
            System.exit(1);
        }

        byte[] buffer = new byte[1024];
        int readBytes = -1;
        
        try {
        	while (true) {
        		
        		// wait until client sends message
        		clientSocket = serverSocket.accept();
        		
        		// get client's input stream
        		InputStream in = clientSocket.getInputStream();
        		
        		// read client's data as byte array
        		readBytes = in.read(buffer);
        		if(readBytes > 1) {
        			
        			// convert byte array to string
        			String data = new String(buffer, 0, readBytes, "UTF-8");
    				System.out.println("echo: " + data);
    			    
    				String id = JSonParser.parseJsonId(data);
    				if(id.toUpperCase().equals("SCANTAPINGTYPE_L") || id.toUpperCase().equals("SCANTAPINGTYPE_R")) {
    					
    					System.out.println("**Server** message received: " + id);
    					logger.log(Level.INFO, "**Server** message received: " + id);
    					
    					sendQueue.put(id);
    					Map<String, String> entry = new HashMap<String, String>();
    					
    					String message = (String) receiveQueue.take();
    					if(message.toUpperCase().equals("EXIT")) {
    						break;
    					}
    					
    					entry.put("type", message);
    					JSONObject json = JSonParser.writeJSonObject(entry);
    					
    					System.out.println("**Server** message send: " + message);
    					logger.log(Level.INFO, "**Server** message send: " + message);
    					
    	        		// create new PrintWriter to send response to client
    	        		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
    					out.write(json.toString());
    					out.flush();
    	    			out.close();
    	    			
    	    			logger.log(Level.INFO, "**Server** message send finished: " + message);
    	    			
    				} else if(id.toUpperCase().equals("STARTO")) {
    					sendQueue.put(id);
    				} else if(id.toUpperCase().equals("STARTM")) {
    					sendQueue.put(id);
    				} else if(id.toUpperCase().equals("AUTO")) {
    					sendQueue.put(id);
    				} else if(id.toUpperCase().equals("MANUAL")) {
    					sendQueue.put(id);
    				} else if(id.toUpperCase().equals("STOP")) {
    					sendQueue.put(id);
    				} else {
    					// TODO
    				}
        		}
    			
    			in.close();
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			close();
		}
    }
}
