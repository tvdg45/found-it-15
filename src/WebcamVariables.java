import java.io.Serializable;

enum DetectType {
	Hough, // deprecated
	Contour,
	Contour2,
	Triangle,
	Rectangle,
	Square,
	Barcode
}

enum Permission {
	Operator,
	Manager
}

enum TrainImage {
	None,
	Training,
	Black,
	White,
	False,
	Start
}

public class WebcamVariables implements Serializable {
	
	public static int MinRadius;
	public static int MaxRadius;
	// 0: Hough Method
	// 1: Contour Method
	public static DetectType DetectMethod;
	
	public static int SubAreaPosX;
	public static int SubAreaWidth;
	public static int SubAreaPosY;
	public static int SubAreaHeight;
	
	public static TrainImage Train = TrainImage.Start;
	
	public static int TotalCameraCount = 1;
	public static int CurrentCameraID = 0;
	
	public static int MinDistance = 5;
	public static int MaxDistance = 10;
	
	public static int MinVertices = 8;
	public static int MaxVertices = 10;
	
	public static int MinEllipseDiff = 5;
	public static int MaxEllipseDiff = 5;
	
	public static Permission CurrentPermission = Permission.Operator;
}
