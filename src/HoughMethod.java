import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class HoughMethod extends Thread {
	private int minRadius;
	private int maxRadius;
	private Rect submatRoi = new Rect();
	
	private Scalar pink = new Scalar(255, 0, 255);
	private Scalar mint = new Scalar(255, 255, 0);
	private Scalar whiteGray = new Scalar(155, 155, 155);
	
	public HoughMethod() {
		// set default value
		this.minRadius = WebcamVariables.MinRadius;
		this.maxRadius = WebcamVariables.MaxRadius;
	}
	
	// TODO: use MVC pattern to use this function
	public int getMinRadius() {
		return this.minRadius;
	}

	// TODO: use MVC pattern to use this function
	public int getMaxRadius() {
		return this.maxRadius;
	}
	
	// TODO: use MVC pattern to use this function
	public void setMinRadius(int minRadius) {
		this.minRadius = minRadius;
	}
	
	// TODO: use MVC pattern to use this function
	public void setMaxRadius(int maxRadius) {
		this.maxRadius = maxRadius;
	}
	
    public Mat run(Mat image) {
        // Load an image
        Mat src = image;
        // Check if image is loaded fine
        if( src.empty() ) {
            System.out.println("Error opening image!");
            return image;
        }
        
        if((WebcamVariables.SubAreaPosX != 0 || WebcamVariables.SubAreaPosY != 0) && (WebcamVariables.SubAreaWidth != 0 && WebcamVariables.SubAreaHeight != 0)) {
			submatRoi.x = WebcamVariables.SubAreaPosX;
			submatRoi.y = WebcamVariables.SubAreaPosY;
			submatRoi.width = WebcamVariables.SubAreaWidth;
			submatRoi.height = WebcamVariables.SubAreaHeight;
			Imgproc.rectangle(src, new Point(submatRoi.x, submatRoi.y), new Point(submatRoi.x + submatRoi.width, submatRoi.y + submatRoi.height), new Scalar(0, 255, 0));
			image = image.submat(submatRoi);
		}
        
        // TODO: use MVC pattern to remove this portion
        minRadius = WebcamVariables.MinRadius;
        maxRadius = WebcamVariables.MaxRadius;
        
        Mat gray = new Mat();
        Mat thresh = new Mat();
        
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        //Imgproc.medianBlur(gray, gray, 5);
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
        
        Imgproc.adaptiveThreshold(gray, thresh, WebcamVariables.MaxRadius, Imgproc.ADAPTIVE_THRESH_MEAN_C,
        		Imgproc.THRESH_BINARY_INV, 11, WebcamVariables.MinRadius);
        
        Mat temp = thresh.clone();
        
        /*Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2HSV);
        Imgproc.medianBlur(gray, gray, 7);*/
        
        Mat circles = new Mat();
        double minDist = (double)gray.rows()/32;
        
        Imgproc.HoughCircles(temp, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                40, // change this value to detect circles with different distances to each other
                160.0, 10  , minRadius, maxRadius); // change the last two parameters
                // (min_radius & max_radius) to detect larger circles
        
        int count = 0;
        int black = 0;
        int white = 0;
        int falseImage = 0;
        
        Scalar toFill = null;
        
        for (int x = 0; x < circles.cols(); x++) {
        	
        	boolean isTrueCircle = false;
        	double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]) + WebcamVariables.SubAreaPosX, Math.round(c[1]) + WebcamVariables.SubAreaPosY);
            
        	MatOfPoint2f mop2f = new MatOfPoint2f(center);
        	mop2f.convertTo(mop2f, CvType.CV_32FC1);

            /*if(ImageProcess.isCircle(mop2f) == false) {
            	continue;
            }
            
            if(ImageProcess.isEllipse(mop2f) == true) {
            	continue;
            }*/
        	
        	Rect rect = new Rect();
        	
        	
        	int radius = (int)Math.round(c[2]);
        	rect.x = (int)(center.x - radius / 2);
        	rect.y = (int)(center.y - radius / 2);
        	rect.width = (int)((radius - 0.5) * 2);
        	rect.height = (int)((radius - 0.5) * 2);
        	
        	// learning
        	if(WebcamVariables.Train == TrainImage.Black ||
        			WebcamVariables.Train == TrainImage.White || 
        			WebcamVariables.Train == TrainImage.False) {
        		
        		Mat subMat = getSubMat(image, rect);
            	if(subMat == null) {
            		System.out.println("Learn failed: clone has some issue");
            	} else {
            		ImageProcess.learnImage(subMat, WebcamVariables.Train, true);
            		System.out.println("Learn success");
            	}
        	} else if(WebcamVariables.Train == TrainImage.Start) { // predicting
        		
        		Mat subMat = getSubMat(image, rect);
            	if(subMat == null) {
            		System.out.println("*****************getSubMat: size has some issue*********************");
            	} else {
            		
            		int result = ImageProcess.predictImage(subMat);
            		//int result = ImageProcess.predictImage2(subMat);
            		if(result == 0) {
            			isTrueCircle = true;
            			white++;

            			//tapingMethod.add(result);
            			toFill = mint;
            		} else if(result == 1) {
            			isTrueCircle = true;
            			black++;
            			
            			//tapingMethod.add(result);
            			toFill = pink;
            		} else {
            			falseImage++;
            			toFill = whiteGray;
            		}
            	}
            } else {
            	isTrueCircle = true;
            	toFill = whiteGray;
            }
            
            count++;
        	
            if(isTrueCircle == true) {

                // circle center
                Imgproc.circle(src, center, 1, new Scalar(0, 100, 100), 3, 8, 0 );
                // circle outline
                Imgproc.circle(src, center, radius, toFill, 3, 8, 0 );
            }
        }
        
        // for debug
        System.out.println("Hough --> Circles found: " + circles.cols() + " black: " + black + " white: " + white);
        
        return src;
    }
    
    private Mat getSubMat(Mat img, Rect rectangle) {
		Mat subMat = img.clone();   
		
    	try { 
    		subMat = subMat.submat(rectangle);
    		
    		Mat gray = new Mat();
            Mat thresh = new Mat();
            
        	//convert the image to black and white
            Imgproc.cvtColor(subMat, gray, Imgproc.COLOR_BGR2GRAY);

            //convert the image to black and white does (8 bit)
            Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
            
            subMat = thresh;
    		
    	} catch (Exception e) {
    		
    		System.out.println("new method error!!!!!!!!!!!!!!");
    		//errCount++;
    		//Imgcodecs.imwrite("trainedImages\\error\\submat_error_" + errCount + ".png", subMat);	
    		subMat = null;
    		//System.out.println(e.toString());
    	}
    	
    	return subMat;
	}
}