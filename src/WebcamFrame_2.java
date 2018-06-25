import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.border.EmptyBorder;

import org.opencv.core.Mat;

import javax.swing.JSplitPane;

public class WebcamFrame_2 {

	private JFrame frame;
	private JPanel contentPane;
	private WebcamPanel webcamPanel;
	private WebcamOptionPanel optionPanel;
	private boolean isClosed;

	/**
	 * Create the frame.
	 */
	public WebcamFrame_2() {
		
		FileReadWrite.ReadConfigFile();
		
		frame = new JFrame();
		frame.setAlwaysOnTop(true);
		frame.getContentPane().setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setBounds(100, 100, 750, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		//setContentPane(contentPane);
		
		JSplitPane splitPane = new JSplitPane();
		
		webcamPanel = new WebcamPanel();
		//webcamPanel.setBounds(75, 75, 450, 300);
		splitPane.setLeftComponent(webcamPanel);
		
		optionPanel = new WebcamOptionPanel();
		splitPane.setRightComponent(optionPanel);
		
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		//frame.setUndecorated(true);
		//frame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		
		frame.getContentPane().add(contentPane);
		frame.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent e) {
			    //do something
				  FileReadWrite.WriteConfigFile();
				  isClosed = true;
				  System.out.println("window closing... " + isClosed);
				  return;
			  }
			  
			  public void windowClosed(WindowEvent e) {
			        System.out.println("window closed");
			    }
			});
		
		isClosed = false;
	}
	
	public void dispose() {
		frame.dispose();
	}
	
	public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
	
	public boolean isClosed() {
		return isClosed;
	}

	public void render(Mat image) {
        Image i = toBufferedImage(image);
        webcamPanel.setImage(i);
        webcamPanel.repaint();
        frame.pack();
    }
	
	public void setResultText(String result) {
		optionPanel.setScanResultText(result);
	}
	
	public String getResultText() {
		return optionPanel.getScanResultText();
	}
	
	public void refreshUI() {
		optionPanel.refreshUI();
	}
 
	public void setButtonsEnabled(boolean isEnabled) {
		optionPanel.setButtonsEnalbed(isEnabled);
	}
	
    public static Image toBufferedImage(Mat m){
          // Code from http://stackoverflow.com/questions/15670933/opencv-java-load-image-to-gui
 
          // Check if image is grayscale or color
          int type = BufferedImage.TYPE_BYTE_GRAY;
          if ( m.channels() > 1 ) {
              type = BufferedImage.TYPE_3BYTE_BGR;
          }
 
          // Transfer bytes from Mat to BufferedImage
          int bufferSize = m.channels()*m.cols()*m.rows();
          byte [] b = new byte[bufferSize];
          m.get(0,0,b); // get all the pixels
          BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
          final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
          System.arraycopy(b, 0, targetPixels, 0, b.length);
          return image;
      }
}
