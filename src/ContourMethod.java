
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

enum TapingType {
	ITaping(0),
	HTaping_Normal(1),
	HTaping_Short(2),
	Error(10),
	NotFound(11);
	
	private final int value;
    private TapingType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

final class MyEntry<K, V> implements Map.Entry<K, V> {
    private final K key;
    private V value;

    public MyEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
}

public class ContourMethod extends Thread {

	private Rect submatRoi = new Rect();
	
	private ArrayList<Integer> tapingMethod = new ArrayList<Integer>();
	private ArrayList<Entry<MatOfPoint, Integer>> capturedContours = new ArrayList<Entry<MatOfPoint, Integer>>();
	private ArrayList<Entry<MatOfPoint, MatOfPoint>> filteredCapturedContourSet = new ArrayList<Entry<MatOfPoint, MatOfPoint>>();
	
	private Scalar pink = new Scalar(255, 0, 255);
	private Scalar mint = new Scalar(255, 255, 0);
	private Scalar whiteGray = new Scalar(155, 155, 155);
	
	private final int EROSION_SIZE = 5;
    private final int DILATION_SIZE = 5;
    
    private final Mat EROSION_ELEMENT = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(2 * EROSION_SIZE + 1, 2 * EROSION_SIZE + 1));
    private final Mat DILATION_ELEMENT = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(2 * DILATION_SIZE + 1, 2 * DILATION_SIZE + 1));
    
	public Mat run(Mat image) {
		
		Mat src = image;
		
		Mat hsvImage = new Mat();
        Imgproc.blur(image, image, new Size(3,3));  
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);           

        Mat redMask = new Mat();
        redMask = thresholdHue(hsvImage, 200, 20, 50, 10);

        Mat kernel = new Mat();
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(2, 2));        

        Mat dilateMat = new Mat();
        Imgproc.dilate(redMask, dilateMat, kernel);                         

        //Imgcodecs.imwrite("redCircleLikeContours.png", redMask);                

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(dilateMat.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);           
        
        System.out.println("Contour --> Circles found: " + contours.size());

        //Imgproc.drawContours(src, contours, -1, new Scalar(0, 100, 100));
        for (int x = 0; x < contours.size(); x++) {
        	MatOfPoint2f mop2f = new MatOfPoint2f();
            contours.get(x).convertTo(mop2f,CvType.CV_32F);
            RotatedRect rectangle = Imgproc.minAreaRect(mop2f);
            
            Point center = rectangle.center;
            center.x += WebcamVariables.SubAreaPosX;
            center.y += WebcamVariables.SubAreaPosY;
            // circle center
            Imgproc.circle(src, center, 1, new Scalar(0, 100, 100), 3, 8, 0 );
            int radius = (int)rectangle.size.width;
            // circle outline
            //int radius = (int) Math.round(c[2]);
            Imgproc.circle(src, center, radius, new Scalar(255, 0, 255), 3, 8, 0 );
        }
        
        return src;
	}
	
	public Mat run_2(Mat image, TapingType[] tapingType) {
		
		Mat src = image;
		
		Mat gray = new Mat();
        Mat thresh = new Mat();

        int posX, posY, width, height;
        posX = WebcamVariables.SubAreaPosX;
    	posY = WebcamVariables.SubAreaPosY;
    	width = WebcamVariables.SubAreaWidth;
    	height = WebcamVariables.SubAreaHeight;
        
        if((posX != 0 || posY != 0) && (width != 0 && height != 0)) {
			submatRoi.x = posX;
			submatRoi.y = posY;
			submatRoi.width = width;
			submatRoi.height = height;
			Imgproc.rectangle(src, new Point(submatRoi.x, submatRoi.y), new Point(submatRoi.x + submatRoi.width, submatRoi.y + submatRoi.height), new Scalar(0, 255, 0));
			image = image.submat(submatRoi);
		}
        
        //convert the image to black and white
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        
        Imgproc.adaptiveThreshold(gray, thresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
        		Imgproc.THRESH_BINARY_INV, 11, 20);
        
        Mat temp = thresh.clone();
        //find the contours
        Mat hierarchy = new Mat();

        Imgproc.Canny(temp, temp, 100, 300);
        
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(temp, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        hierarchy.release();

        Collections.sort(contours, new Comparator<MatOfPoint>() {
            public int compare(MatOfPoint p1, MatOfPoint p2) {
            	long sumMop1 = 0;
        	    long sumMop2 = 0;
        	    for( Point p: p1.toList() ){
        	    	sumMop1 += p.x;
        	    }
        	    for( Point p: p2.toList() ){
        	    	sumMop2 += p.x;
        	    }
        	    
        	    if( sumMop1 > sumMop2)
        	    	return 1;
        	    else if( sumMop1 < sumMop2 )
        	        return -1;
        	    else
        	        return 0;
            }
        });

        int count = 0;
        int black = 0;
        int white = 0;
        int falseImage = 0;
        
        tapingMethod.clear();
        
        for (int x = 0; x < contours.size(); x++) {
        	
        	MatOfPoint2f mop2f = new MatOfPoint2f();
        	contours.get(x).convertTo(mop2f, CvType.CV_32FC1);

            if(ImageProcess.isCircle(mop2f) == false) {
            	continue;
            }
            
            if(ImageProcess.isEllipse(mop2f) == true) {
            	continue;
            }

            Rect rect = Imgproc.boundingRect(contours.get(x));
            if(rect.width >= WebcamVariables.MinRadius && rect.height >= WebcamVariables.MinRadius &&
            		rect.width <= WebcamVariables.MaxRadius && rect.height <= WebcamVariables.MaxRadius) {
            	
            	if(ImageProcess.isTriangle(mop2f) == false) {
                	continue;
                }
                
                double ratio = Math.abs(1 - (double) rect.width / rect.height);
                
                ImageProcess.drawText(src, rect.tl(), "triangle");
                
            	boolean isTrueCircle = false;
            	Scalar toFill = null;
            	
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

                			tapingMethod.add(result);
                			toFill = mint;
                		} else if(result == 1) {
                			isTrueCircle = true;
                			black++;
                			
                			tapingMethod.add(result);
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
                	
                    RotatedRect rectangle = Imgproc.minAreaRect(mop2f);
                    drawContourCircle(src, rectangle, toFill);
                }
            }
        }
        
        if(WebcamVariables.Train == TrainImage.Black ||
    			WebcamVariables.Train == TrainImage.White || 
    			WebcamVariables.Train == TrainImage.False) {
        	WebcamVariables.Train = TrainImage.Training;
        }
        
        if(WebcamVariables.Train == TrainImage.Start) {
        	
        	tapingType[0] = getTapingType();
        	//Imgproc.drawContours(src, contours, -1, new Scalar(0, 100, 100));
        	// System.out.println("Contour --> Circles found: " + count + " (Black: " + black + " , White: " + white + " , False: " + falseImage + " Taping type: " + tapingType + ")");	
        }
        
        return src;
	}
	
	public Mat run_3(Mat image, TapingType[] tapingType) {
		
		//Imgproc.blur(image, image, new Size(3,3));  
		
		Mat src = image;
		
		Mat gray = new Mat();
        Mat thresh = new Mat();

        int posX, posY, width, height;
        posX = WebcamVariables.SubAreaPosX;
    	posY = WebcamVariables.SubAreaPosY;
    	width = WebcamVariables.SubAreaWidth;
    	height = WebcamVariables.SubAreaHeight;
        
        if((posX != 0 || posY != 0) && (width != 0 && height != 0)) {
			submatRoi.x = posX;
			submatRoi.y = posY;
			submatRoi.width = width;
			submatRoi.height = height;
			Imgproc.rectangle(src, new Point(submatRoi.x, submatRoi.y), new Point(submatRoi.x + submatRoi.width, submatRoi.y + submatRoi.height), new Scalar(0, 255, 0));
			image = image.submat(submatRoi);
		}
        
        //convert the image to black and white
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        //Imgproc.medianBlur(gray, gray, 5);
        
        //Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
        /* reduce the noise so we avoid false circle detection */
        //Imgproc.GaussianBlur(gray, gray, new Size(9, 9), 2, 2);
        
        //convert the image to black and white does (8 bit)
        //Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        
        Imgproc.adaptiveThreshold(gray, thresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
        		Imgproc.THRESH_BINARY_INV, 11, 20);
        
        Mat temp = thresh.clone();
        //find the contours
        Mat hierarchy = new Mat();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(temp, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        hierarchy.release();

        Collections.sort(contours, new Comparator<MatOfPoint>() {
            public int compare(MatOfPoint p1, MatOfPoint p2) {
            	long sumMop1 = 0;
        	    long sumMop2 = 0;
        	    for( Point p: p1.toList() ){
        	    	sumMop1 += p.x;
        	    }
        	    for( Point p: p2.toList() ){
        	    	sumMop2 += p.x;
        	    }
        	    
        	    if( sumMop1 > sumMop2)
        	    	return 1;
        	    else if( sumMop1 < sumMop2 )
        	        return -1;
        	    else
        	        return 0;
            }
        });

        int count = 0;
        int black = 0;
        int white = 0;
        int falseImage = 0;
        
        tapingMethod.clear();
        capturedContours.clear();
        filteredCapturedContourSet.clear();
        
        Scalar toFill = null;
        for (int x = 0; x < contours.size(); x++) {
        	
        	MatOfPoint2f mop2f = new MatOfPoint2f();
            contours.get(x).convertTo(mop2f, CvType.CV_32FC1);
            Rect rect = Imgproc.boundingRect(contours.get(x));

            if(ImageProcess.isCircle(mop2f) == false) {
            	continue;
            }
            
            if(ImageProcess.isEllipse(mop2f) == true) {
            	continue;
            }
            
            if(rect.width >= WebcamVariables.MinRadius && rect.height >= WebcamVariables.MinRadius &&
            		rect.width <= WebcamVariables.MaxRadius && rect.height <= WebcamVariables.MaxRadius) {
            	
            	boolean isTrueCircle = false;
            	
            	int predictResult = -1;
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
                		
                		//RotatedRect box = Imgproc.fitEllipse(mop2f);
                        //Imgproc.ellipse(src, box, new Scalar(255, 0, 0));
                		
                		predictResult = ImageProcess.predictImage(subMat);
                		//predictResult = ImageProcess.predictImage2(subMat);
                		if(predictResult == 0) {
                			isTrueCircle = true;
                			white++;

                			//tapingMethod.add(predictResult);
                			//toFill = mint;
                		} else if(predictResult == 1) {
                			isTrueCircle = true;
                			black++;
                			
                			//tapingMethod.add(predictResult);
                			//toFill = pink;
                		} else {
                			falseImage++;
                			//toFill = whiteGray;
                		}
                	}
                } else {
                	isTrueCircle = true;
                	//toFill = whiteGray;
                }
                
                count++;
                if(isTrueCircle == true) {
                    
                    capturedContours.add(new MyEntry<MatOfPoint, Integer>(contours.get(x), predictResult));
                    //count++;	
                }
            }
        }
        
        if(WebcamVariables.Train == TrainImage.Black ||
    			WebcamVariables.Train == TrainImage.White || 
    			WebcamVariables.Train == TrainImage.False) {
        	WebcamVariables.Train = TrainImage.Training;
        }
        
        drawContourListCircle(src, capturedContours);
        
        if(WebcamVariables.Train == TrainImage.Start) {
        	
        	tapingType[0] = getTapingType();
        	//Imgproc.drawContours(src, contours, -1, new Scalar(0, 100, 100));
        	// System.out.println("Contour --> Circles found: " + count + " (Black: " + black + " , White: " + white + " , False: " + falseImage + " Taping type: " + tapingType + ")");	
        }
        
        return src;
	}
	
	// run_4 detect triangle or rectangle (square)
	public Mat run_4(Mat image, TapingType[] tapingType) {
		
		Mat src = image;
		
		Mat gray = new Mat();
        Mat thresh = new Mat();
        Mat thresh2 = new Mat();

        int posX, posY, width, height;
        posX = WebcamVariables.SubAreaPosX;
    	posY = WebcamVariables.SubAreaPosY;
    	width = WebcamVariables.SubAreaWidth;
    	height = WebcamVariables.SubAreaHeight;
        
        if((posX != 0 || posY != 0) && (width != 0 && height != 0)) {
			submatRoi.x = posX;
			submatRoi.y = posY;
			submatRoi.width = width;
			submatRoi.height = height;
			Imgproc.rectangle(src, new Point(submatRoi.x, submatRoi.y), new Point(submatRoi.x + submatRoi.width, submatRoi.y + submatRoi.height), new Scalar(0, 255, 0));
			image = image.submat(submatRoi);
		}
        
        //convert the image to black and white
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        
        //Imgproc.adaptiveThreshold(gray, thresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 11, 20);
        //Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        
        Imgproc.adaptiveThreshold(gray, thresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 20);
        Imgproc.threshold(thresh, thresh, 0, 255, Imgproc.THRESH_BINARY_INV);
        
        Imgproc.threshold(gray, thresh2, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Imgproc.threshold(thresh2, thresh2, 0, 255, Imgproc.THRESH_BINARY_INV);
        
        //find the contours
        Mat hierarchy = new Mat();

        //Imgproc.Canny(temp, temp, 100, 300);
        
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        hierarchy.release();

        List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(thresh2, contours2, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        hierarchy.release();
        
        for(int i = 0; i < contours2.size(); i++) {
        	contours.add(contours2.get(i));
        }
        
        int count = 0;
        int black = 0;
        int white = 0;
        int falseImage = 0;
        
        tapingMethod.clear();
        capturedContours.clear();
        
        for (int x = 0; x < contours.size(); x++) {
        	
        	MatOfPoint2f mop2f = new MatOfPoint2f();
        	contours.get(x).convertTo(mop2f, CvType.CV_32FC1);
            Rect rect = Imgproc.boundingRect(contours.get(x));

            if(rect.width >= WebcamVariables.MinRadius && rect.height >= WebcamVariables.MinRadius &&
            		rect.width <= WebcamVariables.MaxRadius && rect.height <= WebcamVariables.MaxRadius) {
            	
            	if(WebcamVariables.DetectMethod == DetectType.Triangle) {
            		if(ImageProcess.isTriangle(mop2f) == false) {
                    	continue;
                    }
            	} else if(WebcamVariables.DetectMethod == DetectType.Rectangle) {
            		if(ImageProcess.isRectangle(mop2f, true) == false) {
            			if(ImageProcess.isRectangle(mop2f, false) == false) {
                        	continue;
            			}
                    }
            	} else if(WebcamVariables.DetectMethod == DetectType.Square) {
            		if(ImageProcess.isSquare(mop2f) == false) {
                    	continue;
                    }
            	}
                
            	boolean isTrueCircle = false;
            	int result = -1;
            	Scalar toFill = null;
            	
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
                		
                		result = ImageProcess.predictImage(subMat);
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
                	
                    //RotatedRect rectangle = Imgproc.minAreaRect(mop2f);
                    //drawContourCircle(src, rectangle, toFill);
                	
                	capturedContours.add(new MyEntry<MatOfPoint, Integer>(contours.get(x), result));
                }
            }
        }
        
        if(WebcamVariables.Train == TrainImage.Black ||
    			WebcamVariables.Train == TrainImage.White || 
    			WebcamVariables.Train == TrainImage.False) {
        	WebcamVariables.Train = TrainImage.Training;
        }
        
        //System.out.println("drawContourListCircle: capturedContour total size: " + capturedContours.size());
		
        drawContourListCircle(src, capturedContours);
        
        if(WebcamVariables.Train == TrainImage.Start) {

			//System.out.println("Total taping method count: " + tapingMethod.size()	);
        	tapingType[0] = getTapingType();	
        }
        
        return src;
	}
	
	// run_5 detect rectangle by comparing pixels
	public Mat run_5(Mat image, TapingType[] tapingType) {
		
		Mat src = image;
		
		Mat gray = new Mat();
        Mat thresh = new Mat();
        Mat thresh2 = new Mat();

        int posX, posY, width, height;
        posX = WebcamVariables.SubAreaPosX;
    	posY = WebcamVariables.SubAreaPosY;
    	width = WebcamVariables.SubAreaWidth;
    	height = WebcamVariables.SubAreaHeight;
        
        if((posX != 0 || posY != 0) && (width != 0 && height != 0)) {
			submatRoi.x = posX;
			submatRoi.y = posY;
			submatRoi.width = width;
			submatRoi.height = height;
			Imgproc.rectangle(src, new Point(submatRoi.x, submatRoi.y), new Point(submatRoi.x + submatRoi.width, submatRoi.y + submatRoi.height), new Scalar(0, 255, 0));
			image = image.submat(submatRoi);
		}
        
        //convert the image to black and white
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        
        //Imgproc.adaptiveThreshold(gray, thresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 11, 20);
        //Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        
        // erosion
        Imgproc.erode(gray, gray, EROSION_ELEMENT);
        Imgproc.erode(gray, gray, EROSION_ELEMENT);
        
        // dilation
        Imgproc.dilate(gray, gray, DILATION_ELEMENT);
        Imgproc.dilate(gray, gray, DILATION_ELEMENT);
        
        Imgproc.adaptiveThreshold(gray, thresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 20);
        Imgproc.threshold(thresh, thresh, 0, 255, Imgproc.THRESH_BINARY_INV);
        
        Imgproc.threshold(gray, thresh2, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Imgproc.threshold(thresh2, thresh2, 0, 255, Imgproc.THRESH_BINARY_INV);
        
        //find the contours
        Mat hierarchy = new Mat();

        //Imgproc.Canny(temp, temp, 100, 300);
        
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        hierarchy.release();

        List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(thresh2, contours2, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        hierarchy.release();
        
        for(int i = 0; i < contours2.size(); i++) {
        	contours.add(contours2.get(i));
        }
        
        int count = 0;
        int black = 0;
        int white = 0;
        int falseImage = 0;
        
        tapingMethod.clear();
        capturedContours.clear();
        
        for (int x = 0; x < contours.size(); x++) {
        	
        	MatOfPoint2f mop2f = new MatOfPoint2f();
        	Imgproc.convexHull(contours.get(x), new MatOfInt());
        	contours.get(x).convertTo(mop2f, CvType.CV_32FC1);
            Rect rect = Imgproc.boundingRect(contours.get(x));

            if(rect.width >= WebcamVariables.MinRadius && rect.height >= WebcamVariables.MinRadius &&
            		rect.width <= WebcamVariables.MaxRadius && rect.height <= WebcamVariables.MaxRadius) {
            	
            	boolean isTrueCircle = false;
            	Scalar toFill = null;
            	
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
                		
                		MarkType mark = ImageProcess.getMarkType(subMat, mop2f, rect.x, rect.y);
                				
                		if(mark == MarkType.White) {
                			isTrueCircle = true;
                			white++;

                			toFill = mint;
                		} else if(mark == MarkType.Black) {
                			isTrueCircle = true;
                			black++;
                			
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
                	
                    RotatedRect rectangle = Imgproc.minAreaRect(mop2f);
                    //Imgproc.drawContours(image, contours, x, toFill);
                    drawContourCircle(image, rectangle, toFill);
                }
            }
        }
        
        if(WebcamVariables.Train == TrainImage.Black ||
    			WebcamVariables.Train == TrainImage.White || 
    			WebcamVariables.Train == TrainImage.False) {
        	WebcamVariables.Train = TrainImage.Training;
        }
        
        if(WebcamVariables.Train == TrainImage.Start) {

			//System.out.println("Total taping method count: " + tapingMethod.size()	);
        	tapingType[0] = getTapingType();
        }
        
        return image;
	}
	
	private void drawContourListCircle(Mat src, ArrayList<Entry<MatOfPoint, Integer>> capturedContours) {
		
		MatOfPoint2f mof2f1 = new MatOfPoint2f();
        MatOfPoint2f mof2f2 = new MatOfPoint2f();
        Scalar toFill;
        int lastFoundIndex = -1;
        ArrayList<MatOfPoint> finished = new ArrayList<MatOfPoint>();
        for(int i = 0; i < capturedContours.size(); i++) {
        	
        	MatOfPoint point1 = capturedContours.get(i).getKey();
        	//mof2f1.release();
        	point1.convertTo(mof2f1, CvType.CV_32FC1);
        	RotatedRect rectangle1 = Imgproc.minAreaRect(mof2f1);
        	
        	for(int j = i + 1; j < capturedContours.size(); j++) {
        		MatOfPoint point2 = capturedContours.get(j).getKey();
        		//mof2f2.release();
        		point2.convertTo(mof2f2, CvType.CV_32FC1);
        		RotatedRect rectangle2 = Imgproc.minAreaRect(mof2f2);
        		
        		double distanceX = Math.abs(rectangle2.center.x - rectangle1.center.x);
        		double distanceY = Math.abs(rectangle2.center.y - rectangle1.center.y);
        		
        		double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
        		
        		if(distance > WebcamVariables.MinDistance && distance < WebcamVariables.MaxDistance) {
        			
        			if(finished.contains(point1) == true || finished.contains(point2)) {
        				continue;
        			}
        			
        			if(capturedContours.get(i).getValue() == 0) {
        				toFill = mint;
        				tapingMethod.add(capturedContours.get(i).getValue());
        			} else if(capturedContours.get(i).getValue() == 1) {
        				toFill = pink;
        				tapingMethod.add(capturedContours.get(i).getValue());
        			} else {
        				toFill = whiteGray;
        			}
        			
        			drawContourCircle(src, rectangle1, toFill);
        			
        			if(capturedContours.get(j).getValue() == 0) {
        				toFill = mint;
        				tapingMethod.add(capturedContours.get(j).getValue());
        			} else if(capturedContours.get(j).getValue() == 1) {
        				toFill = pink;
        				tapingMethod.add(capturedContours.get(j).getValue());
        			} else {
        				toFill = whiteGray;
        			}
        			
        			drawContourCircle(src, rectangle2, toFill);
        			
        			finished.add(point1);
        			finished.add(point2);
        		}
        	}
        }
        
        //System.out.println("drawContourListCircle: tapingMethod size: " + tapingMethod.size());
	}

	private void drawContourCircle(Mat src, RotatedRect rectangle, Scalar toFill) {
		
    	Point center = rectangle.center;
    	int posX, posY;
    	posX = WebcamVariables.SubAreaPosX;
		posY = WebcamVariables.SubAreaPosY;
		
    	center.x += posX;
        center.y += posY;
    	int radius = (int)rectangle.size.width / 2;
        Imgproc.circle(src, center, radius, toFill, 3, 8, 0 );
	}
	
	private TapingType getTapingType() {
		if(tapingMethod.size() != 2) {
			return TapingType.Error;
		}
		
		if(tapingMethod.get(0) == 0 && tapingMethod.get(1) == 0) {
			return TapingType.ITaping;
		} else if(tapingMethod.get(0) == 0 && tapingMethod.get(1) == 1) {
			return TapingType.HTaping_Normal;
		} else if(tapingMethod.get(0) == 1 && tapingMethod.get(1) == 0) {
			return TapingType.HTaping_Normal;
		} else if(tapingMethod.get(0) == 1 && tapingMethod.get(1) == 1) {
			return TapingType.HTaping_Short;
		}
		
		return TapingType.NotFound;
	}
	
	int errCount = 0;
	
	private Mat getSubMat(Mat img, Rect rectangle) {
		Mat subMat = img.clone();
		
    	try { 
    		subMat = subMat.submat(rectangle);
    		
    		Mat gray = new Mat();
            Mat thresh = new Mat();
            
        	//convert the image to black and white
            Imgproc.cvtColor(subMat, gray, Imgproc.COLOR_BGR2GRAY);

            Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            Imgproc.threshold(thresh, thresh, 0, 255, Imgproc.THRESH_BINARY_INV);
            
            subMat = thresh;
    		
    	} catch (Exception e) {

    		System.out.println("new method error!!!!!!!!!!!!!!");
    		errCount++;
    		Imgcodecs.imwrite("trainedImages\\error\\submat_error_" + errCount + ".png", subMat);	
    		subMat = null;
    		//System.out.println(e.toString());
    	}
    	
    	return subMat;
	}
	
	private boolean isWord(Mat image) {
		
		Mat cloned = image.clone();
		Imgproc.pyrDown(cloned, cloned);
		Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3));
		Imgproc.morphologyEx(cloned, cloned, Imgproc.MORPH_GRADIENT , morphKernel);
		Imgproc.threshold(cloned, cloned, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
		
		Mat connected = new Mat();
        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 1));
        Imgproc.morphologyEx(cloned, connected, Imgproc.MORPH_CLOSE  , morphKernel);
        
        Mat mask = Mat.zeros(cloned.size(), CvType.CV_8UC1);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Mat hierarchy = new Mat();
        Imgproc.findContours(connected, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        hierarchy.release();
        
        for(int idx = 0; idx < contours.size(); idx++) {
        	
            Rect rect = Imgproc.boundingRect(contours.get(idx));

            Mat maskROI = new Mat(mask, rect);

            Imgproc.drawContours(mask, contours, idx, new Scalar(255, 255, 255), Core.FILLED);

            double r = (double)Core.countNonZero(maskROI)/(rect.width*rect.height);

            if (r > .45 && (rect.height > 8 && rect.width > 8)) {
                return true;
            }
       }    
        
        return false;
	}
	
	private Mat thresholdHue(Mat hsvImage, int hueVal, int range, int minSat, int minValue) {   
        Mat mask = new Mat(); 

        List<Mat> channels = new ArrayList<Mat>();
        Core.split(hsvImage, channels);

        int targetHueVal = 180 / 2; 
        int shift = targetHueVal - hueVal;
        if (shift < 0) shift += 180;

        Mat shiftedHue = shiftChannel(channels.get(0), shift, 180);

        List<Mat> newChannels = new ArrayList<Mat>();

        newChannels.add(shiftedHue);
        newChannels.add(channels.get(1));
        newChannels.add(channels.get(2));
        Mat shiftedHSV = new Mat();
        Core.merge(newChannels, shiftedHSV);

        Core.inRange(shiftedHSV, new Scalar(targetHueVal - range, minSat, minValue), new Scalar(targetHueVal + range, 255, 255), mask);

        return mask;
    }

    private Mat shiftChannel(Mat H, int shift, int maxVal) {

        Mat shiftedH = H.clone();
        for (int j = 0; j < shiftedH.rows(); ++j)
        for (int i = 0; i < shiftedH.cols(); ++i)
        {
            shiftedH.put(j, i,(shiftedH.get(j,i)[0] + shift) % maxVal);
        }

        return shiftedH;
    }
}
