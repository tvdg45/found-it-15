import org.opencv.core.Mat;

public class DetectCircle {
	private static HoughMethod hcr;
	private static ContourMethod cm;
	
	public DetectCircle() {
		hcr = new HoughMethod();
		cm = new ContourMethod();
		System.out.println("HoughMethod and ContourMethod have been initialized");
	}
	
	public Mat Detect(Mat image, TapingType[] tapingType) {
		if(WebcamVariables.DetectMethod == DetectType.Hough) {
			return hcr.run(image);
		} else if (WebcamVariables.DetectMethod == DetectType.Contour) {
			return cm.run_2(image, tapingType);
		} else if (WebcamVariables.DetectMethod == DetectType.Contour2) {
			return cm.run_3(image, tapingType);
		} else if (WebcamVariables.DetectMethod == DetectType.Triangle) {
			return cm.run_4(image, tapingType);
		} else if (WebcamVariables.DetectMethod == DetectType.Rectangle) {
			return cm.run_5(image, tapingType);
		} else if (WebcamVariables.DetectMethod == DetectType.Square) {
			return cm.run_4(image, tapingType);
		} else {
			return cm.run_2(image, tapingType);
		}
	}
}