import java.awt.Color;
import java.awt.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.ANN_MLP;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;
import org.opencv.ml.TrainData;
import org.opencv.utils.Converters;

enum MarkType {
	White,
	Black,
	None,
}

public class ImageProcess {
	private static Mat train_responses;
	private static int trainedImages = 0;
	private static KNearest knn = KNearest.create();
	private static ANN_MLP ann;
	
	private static int black = 0;
	private static int white = 0;
	private static int falseImage = 0;
	
	private static Mat blackmat = new Mat();
	private static Mat whitemat = new Mat();
	private static Mat falsemat = new Mat();
	
	private static ArrayList<Mat> blackList = new ArrayList<Mat>();
	private static ArrayList<Mat> whiteList = new ArrayList<Mat>();
	private static ArrayList<Mat> falseList = new ArrayList<Mat>();
	
	private static Mat trainSamples = new Mat();
	private static ArrayList<Integer> trainLabs = new ArrayList<Integer>();
	
	private static Mat reshape(Mat img, boolean isLocal) {
		
		img.convertTo(img, CvType.CV_32FC1);       
        Mat imgResized = img;
        
        Size dsize;
        if(isLocal == true) {
        	dsize = new Size(30, 30);
        } else {
        	dsize = new Size(30, 90);
        }
        
        Imgproc.resize(imgResized, imgResized, dsize);
        
        imgResized.convertTo(imgResized, CvType.CV_32FC1);
        imgResized = imgResized.reshape(1, 1);
        
        return imgResized;
	}
	
	public static void learnFromImages() {
		
		ann = ANN_MLP.create();
		File directoryBlack = new File("trainedImages\\black");
		File directoryWhite = new File("trainedImages\\white");
		File directoryFalse = new File("trainedImages\\false");
		
		int countBlack = directoryBlack.list().length;
		int countWhite = directoryWhite.list().length;
		int countFalse = directoryFalse.list().length;
	    int fileCount = countBlack + countWhite + countFalse;
	    
	    if(fileCount == 0) {
	    	return;
	    }
	    
	    double[] images = new double[fileCount];
		
	    train_responses = new Mat(1, fileCount, CvType.CV_32FC1);
	    
		int index = 0;
		
		for (int i = 0; i < countFalse ; i++) {
	    	String path = "trainedImages\\false\\" + directoryFalse.list()[i];
	    	Mat img = Imgcodecs.imread(path);
	        if(img.empty()) {
	        	continue;
	        }
	        
	        Mat imgResized = reshape(img, true);
	        
	        
	        learnImage(imgResized, TrainImage.False, false);
	        //falseImage++;
	        index++;
	        /*
	        trainSamples.push_back(imgResized);
	        images[index++] = 2;
	        trainLabs.add(2);
	        falseImage++;
	        falsemat.push_back(imgResized);
	        */
	    }

		boolean isBlackFinish = false;
		boolean isWhiteFinish = false;
		boolean isBlack = false;
		
		while(isBlackFinish != true || isWhiteFinish != true) {
			if(isBlack == true && isBlackFinish != true) {
				if(black < countBlack) { // if black learning is not finished
	    			String path = "trainedImages\\black\\" + directoryBlack.list()[black];
			    	Mat img = Imgcodecs.imread(path);
			        if(img.empty()) {
			        	continue;
			        }
			        
			        Mat imgResized = reshape(img, true);
			        
			        learnImage(imgResized, TrainImage.Black, false);
			        //black++;
			        index++;
			        /*
			        trainSamples.push_back(imgResized);
			        images[index++] = 1;
			        trainLabs.add(1);
			        black++;
			        blackmat.push_back(imgResized);
			        */
	    		} else {
	    			isBlackFinish = true;
	    		}
			}
			else if(isBlack == false && isWhiteFinish != true) {
				if(white < countWhite) { // if white learning is not finished
	    			String path = "trainedImages\\white\\" + directoryWhite.list()[white];
			    	Mat img = Imgcodecs.imread(path);
			        if(img.empty()) {
			        	continue;
			        }
			        
			        Mat imgResized = reshape(img, true);
			        
			        learnImage(imgResized, TrainImage.White, false);
			        //white++;
			        index++;
			        /*
			        trainSamples.push_back(imgResized);
			        images[index++] = 0;
			        trainLabs.add(0);
			        white++;
			        whitemat.push_back(imgResized);
			        */
	    		} else {
	    			isWhiteFinish = true;
	    		}
			}
			
			isBlack = !isBlack;
		}
	    
	    System.out.println("Total loaded: " + fileCount + " actual learned: " + index + " (black: " + black + ", white: " + white + ", false: " + falseImage + ")");
	    
	    //knn.clear();
	    
	    //train_responses.put(0, 0, images); 
	    //knn.train(trainSamples, Ml.ROW_SAMPLE, /*train_responses*/Converters.vector_int_to_Mat(trainLabs));
	    
 	    JOptionPane.showMessageDialog(null, 
	    		"Total loaded: " + fileCount + " actual learned: " + index + " (black: " + black + ", white: " + white + ", false: " + falseImage + ")"
	    		, "InfoBox", JOptionPane.INFORMATION_MESSAGE);
	    
	    System.out.println("Image from local learned: " + index);
	}
	
	public static void learnImage(Mat img, TrainImage train, boolean isWrite) {
		double[] images;
		if(train_responses == null) {
			trainedImages = 0;
			images = new double[trainedImages + 1];
		} else {
			double[] temp = train_responses.get(0, 0);
			trainedImages = temp.length;
			
			images = new double[trainedImages + 1];
			for(int i = 0; i < trainedImages; i++) {
				images[i] = temp[i];
			}
		}
		
		train_responses = new Mat(1, trainedImages + 1, CvType.CV_32FC1);
		
		if(img.empty()) {
        	return;
        }
        
		Mat imgResized = reshape(img, false);
		
		trainSamples.push_back(imgResized);
	
        String filename = "";
        if(train == TrainImage.Black) {
        	images[trainedImages] = 1; // if dot is black, then train it as 1
        	trainLabs.add(1);
        	blackmat.push_back(imgResized);
        	
        	blackList.add(imgResized);
        	
        	black++;
        	filename = "trainedImages\\black\\trained_black_" + black + ".png";
        } else if(train == TrainImage.White) {
        	images[trainedImages] = 0; // if dot is white, then train it as 0
        	trainLabs.add(0);
        	whitemat.push_back(imgResized);
        	
        	whiteList.add(imgResized);
        	
        	white++;
        	filename = "trainedImages\\white\\trained_white_" + white + ".png";
        } else if(train == TrainImage.False) {
        	images[trainedImages] = 2; // if learning false, then train it as 2
        	trainLabs.add(2);
        	falsemat.push_back(imgResized);
        	
        	falseList.add(imgResized);
        	
        	falseImage++;
        	filename = "trainedImages\\false\\trained_false_" + falseImage + ".png";
        }

	    train_responses.put(0, 0, images);
	    
	    knn.clear();
	    ann.clear();
	    
	    knn.train(trainSamples, Ml.ROW_SAMPLE, Converters.vector_int_to_Mat(trainLabs));
	    
	    //TrainData td = TrainData.create(trainSamples, Ml.ROW_SAMPLE, Converters.vector_int_to_Mat(trainLabs));
	    //ann.train(td);
	    
	    if(isWrite == true) {
	    	Imgcodecs.imwrite(filename, img);	
	    }
	}
	
	public static int predictImage(Mat img) {
		if(img.empty()) {
        	return -1;
        }
		
		Mat imgResized = reshape(img, false);
        
        Mat results = new Mat();
        results.convertTo(results, CvType.CV_32FC1);
        
        float numberDetected = -1;
        try {
        	numberDetected = knn.findNearest(imgResized, 1, results);
        	//float test = ann.predict(imgResized);
        } catch (Exception e) {
        	//System.out.println(e.toString());
        }
        
        if(numberDetected == 0) {
        	return 0; // white dot
        } else if (numberDetected == 1){
        	return 1; // black dot
        } else {
        	return -1;
        }
	}
	
	public static int predictImage2(Mat img) {
		if(img.empty()) {
        	return -1;
        }
		
		Mat imgResized = reshape(img, false);
		
		double result1 = 0;
		double result2 = 0;
		double result3 = 0;
		
		for(int i = 0; i < blackList.size(); i++) {
			result1 += Imgproc.matchShapes(imgResized, blackList.get(i), Imgproc.CV_CONTOURS_MATCH_I1, 0);
		}
		
		for(int i = 0; i < whiteList.size(); i++) {
			result2 += Imgproc.matchShapes(imgResized, whiteList.get(i), Imgproc.CV_CONTOURS_MATCH_I1, 0);
		}
		
		for(int i = 0; i < falseList.size(); i++) {
			result3 += Imgproc.matchShapes(imgResized, falseList.get(i), Imgproc.CV_CONTOURS_MATCH_I1, 0);
		}
		
		if(result1 < result2 && result1 < result3) {
			if(result1 < 1 * blackList.size()) {
				return 1;
			} else {
				return 2;
			}
		} else if(result2 < result1 && result2 < result3) {
			if(result2 < 1 * whiteList.size()) {
				return 0;
			} else {
				return 2;
			}
		} 
		
		return 2;
	}
	
	private static boolean isPointInsideContour(MatOfPoint2f contour, Point pt) {
		double result = Imgproc.pointPolygonTest(contour, pt, false);
		if(result <= 0) {
			return false;
		}
		return true;
	}
	
	private static void convexHull(MatOfPoint contour) {
		
		Imgproc.convexHull(contour, new MatOfInt());
	}
	
	public static MarkType getMarkType(Mat img, MatOfPoint2f contour, int compensateX, int compensateY) {
		
		if(img.empty()) {
        	return MarkType.None;
        }
		
		int white = 0;
		int black = 0;
		for(int row = 0; row < img.rows(); row++) {
			for(int col = 0; col < img.cols(); col++) {
				
				if(isPointInsideContour(contour, new Point(row + compensateX, col + compensateY)) == true) {
					double pixel = img.get(row, col)[0];
					if(pixel == 255) {
						white++;
					} else {
						black++;
					}
				}
			}
		}
		
		int totalPixel = white + black;
		
		double percentage = ((double) white / (double) totalPixel) * 100.0;
		
		if (percentage >= 65) {
			return MarkType.Black;
		} else if (percentage <= 30) {
			return MarkType.White;
		}
		
		return MarkType.None;
	}
	
	private static double angle(Point pt1, Point pt2, Point pt0) {
	    double dx1 = pt1.x - pt0.x;
	    double dy1 = pt1.y - pt0.y;
	    double dx2 = pt2.x - pt0.x;
	    double dy2 = pt2.y - pt0.y;
	    return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
	}
	
	public static void drawText(Mat src, Point ofs, String text) {
	    Imgproc.putText(src, text, ofs, Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255,255,25));
	}
	
	// Check whether the input is circle or not.
	// This result may include ellipsed circle.
	public static boolean isCircle(MatOfPoint2f mop2f) {
		
		MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(mop2f, approx, 0.02 * Imgproc.arcLength(mop2f, true), true);

        int vertices = (int)approx.total();
        if(vertices < WebcamVariables.MinVertices || vertices > WebcamVariables.MaxVertices) {
        	return false;
        }
        
        return true;
	}
	
	public static boolean isTriangle(MatOfPoint2f mop2f) {
		
		MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(mop2f, approx, 0.02 * Imgproc.arcLength(mop2f, true), true);

        int vertices = (int)approx.total();
        if(vertices == 3) {
        	return true;
        }
        
        return false;
	}
	
	public static boolean isRectangle(MatOfPoint2f mop2f, boolean isClosed) {
		
		MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(mop2f, approx, 0.02 * Imgproc.arcLength(mop2f, isClosed), isClosed);

        int vertices = (int)approx.total();
        if(vertices == 4) {
        	
        	ArrayList<Double> cos = new ArrayList<>();
            Point[] points = approx.toArray();
            for (int j = 2; j < vertices + 1; j++) {
                cos.add(angle(points[(int) (j % vertices)], points[j - 2], points[j - 1]));
            }
            
            Collections.sort(cos);
            Double minCos = cos.get(0);
            Double maxCos = cos.get(cos.size() - 1);
            boolean isRect = vertices == 4 && minCos >= -0.8 && maxCos <= 0.8;
        	return isRect;
        } 
        
        return false;
	}
	
	public static boolean isSquare(MatOfPoint2f mop2f) {
		
		boolean result = isRectangle(mop2f, true);
		if(result == false) {
			return false;
		}
		
		double ratio = Math.abs(1 - (double) mop2f.width() / mop2f.height());
		if(ratio > 0.02) {
			return false;
		}
		
		return true;
	}
	
	// Check whether the input is ellipse or not
	public static boolean isEllipse(MatOfPoint2f mop2f) {
		
		RotatedRect ellipseBox = Imgproc.fitEllipse(mop2f);
        double diff = Math.abs(ellipseBox.size.height - ellipseBox.size.width);
        if(diff > WebcamVariables.MaxEllipseDiff || diff < WebcamVariables.MinEllipseDiff) {
        	return true;
        }
        
        return false;
	}
}