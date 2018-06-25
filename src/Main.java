
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.opencv.core.Core;

public class Main {
    
    static {
    	
        // Load the native OpenCV library
    	boolean isRunningOnRealMachine = false;
    	
    	if(isRunningOnRealMachine == true) {
    		Path path = FileSystems.getDefault().getPath("").toAbsolutePath();
        	File lib = new File(path.toString() +"\\x64\\" + System.mapLibraryName(Core.NATIVE_LIBRARY_NAME));
        	System.load(lib.getAbsolutePath());
    	} else {
    		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    	}
    }
    
    private static int MAX_BUFFER_SIZE = 10;
    
    public static void main(String[] args) {
    	
    	BlockingQueue<String> webcamMessageQueue = new ArrayBlockingQueue<String>(MAX_BUFFER_SIZE);
    	BlockingQueue<String> serverMessageQueue = new ArrayBlockingQueue<String>(MAX_BUFFER_SIZE);
    	
    	WebcamThread thread = new WebcamThread(webcamMessageQueue, serverMessageQueue);
    	thread.start();
    	
    	WebcamServer server = new WebcamServer(serverMessageQueue, webcamMessageQueue);
    	server.start();
    	
    	while(thread.isAlive() == true) {
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	try {
    		server.close();
			server.join();
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("Main system terminated");
    	System.exit(0);
    }
}