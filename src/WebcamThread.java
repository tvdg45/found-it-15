
import java.util.concurrent.BlockingQueue;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class WebcamThread extends Thread {

	private BlockingQueue<String> receiveQueue;
	private BlockingQueue<String> sendQueue;
	private Thread receiveQueueThread;
	private int scanCount;

	private final int MAX_SCAN_COUNT = 20;

	private boolean isStartScan = false;
	private boolean isLeft = true;
	private WebcamFrame_2 mainFrame;

	public WebcamThread(BlockingQueue<String> receiveQueue, BlockingQueue<String> sendQueue) {
		this.receiveQueue = receiveQueue;
		this.sendQueue = sendQueue;
	}

	private void runReceiveQueueThread() {
		receiveQueueThread = new Thread() {
			public void run() {
				
				try {
					while (true) {
						
						// Get message from client
						String message = (String) receiveQueue.take();
						if (message.toUpperCase().equals("SCANTAPINGTYPE_L")) { // Scan taping type with left camera
							isLeft = true;
							isStartScan = true;
							scanCount = 0;
						} else if (message.toUpperCase().equals("SCANTAPINGTYPE_R")) { // Scan taping type with right
																						// camera
							isLeft = true;
							isStartScan = true;
							scanCount = 0;
						} else if (message.toUpperCase().equals("STARTO")) { // Start as operator
							WebcamVariables.CurrentPermission = Permission.Operator;
							WebcamVariables.Train = TrainImage.Start;
							if (mainFrame != null && mainFrame.isClosed() == false) {
								mainFrame.setButtonsEnabled(false);
							}
						} else if (message.toUpperCase().equals("STARTM")) { // Start as manager
							WebcamVariables.CurrentPermission = Permission.Manager;
							WebcamVariables.Train = TrainImage.Start;
							if (mainFrame != null && mainFrame.isClosed() == false) {
								mainFrame.setButtonsEnabled(false);
							}
						} else if (message.toUpperCase().equals("STOP")) { // Stop scanning
							WebcamVariables.Train = TrainImage.Training;
							if (mainFrame != null && mainFrame.isClosed() == false) {
								mainFrame.setButtonsEnabled(true);
							}
						} else if (message.toUpperCase().equals("AUTO")) { // Set detect type as auto (barcode)
							WebcamVariables.DetectMethod = DetectType.Barcode;
						} else if (message.toUpperCase().equals("MANUAL")) { // Set detect type as manual (circle)
							WebcamVariables.DetectMethod = DetectType.Contour;
						} else if (message.toUpperCase().equals("EXIT")) {
							return;
						}

						System.out.println("**Webcam** message dequeued: " + message);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		receiveQueueThread.start();
	}

	private void stopQueue() {
		try {
			receiveQueue.put("exit");
			sendQueue.put("exit");
			receiveQueueThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void enqueueServerMessage(String result) {
		try {

			sendQueue.put(result);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("**Webcam** message inqueued: " + result);
	}

	int test = 0;

	private TapingType getTapingTypeInString(TapingType[] typeList) {
		
		int size = typeList.length;
		int iTapingCount = 0;
		int hTapingNormalCount = 0;
		int hTapingShortCount = 0;
		int errorCount = 0;
		
		for (int i = 0; i < size; i++) {
			TapingType type = typeList[i];
			switch (type) {
			case ITaping:
				iTapingCount++;
				break;
			case HTaping_Normal:
				hTapingNormalCount++;
				break;
			case HTaping_Short:
				hTapingShortCount++;
				break;
			case Error:
			default:
				errorCount++;
				break;
			}
		}

		TapingType result;

		if (iTapingCount > hTapingNormalCount && iTapingCount > hTapingShortCount && iTapingCount > errorCount) { // If it is I-Taping
			result = TapingType.ITaping;
		} else if (hTapingNormalCount > iTapingCount && hTapingNormalCount > hTapingShortCount
				&& hTapingNormalCount > errorCount) { // If it is H-Taping Normal
			result = TapingType.HTaping_Normal;
		} else if (hTapingShortCount > iTapingCount && hTapingShortCount > hTapingNormalCount
				&& hTapingShortCount > errorCount) { // If it is H-Taping Short
			result = TapingType.HTaping_Short;
		} else if (errorCount > iTapingCount && errorCount > hTapingNormalCount && errorCount > hTapingShortCount) {
			result = TapingType.Error;
		} else {
			result = TapingType.Error;
		}

		return result;
	}

	private void calculateInstalledCameraIDs() {
		VideoCapture cap = new VideoCapture();
		int count = 0;
		for (int i = 0; i < 10; i++) {
			cap.open(i);
			if (cap.isOpened() == true) {

				count++;
				cap.release();
			}
		}

		WebcamVariables.TotalCameraCount = count;
	}

	private VideoCapture switchToOtherCamera(VideoCapture cap) {

		int currentIndex = WebcamVariables.CurrentCameraID;

		cap.release();
		cap.open(currentIndex);
		if (cap.isOpened() == false) {
			return null;
		}

		cap.set(Videoio.CV_CAP_PROP_FOCUS, 255);
		cap.set(5, 60);
		cap.set(Videoio.CV_CAP_PROP_AUTOFOCUS, 1);

		return cap;
	}

	private VideoCapture switchToOtherCamera(VideoCapture cap, boolean isLeft) {

		if (WebcamVariables.CurrentCameraID == 0) {
			return cap;
		}

		int cameraIndex = WebcamVariables.CurrentCameraID;
		if (WebcamVariables.TotalCameraCount > 1) {
			if (isLeft == true) {
				cameraIndex = 0;
			} else {
				cameraIndex = 1;
			}
		}

		cap.release();
		cap.open(cameraIndex);
		if (cap.isOpened() == false) {
			return null;
		}

		cap.set(Videoio.CV_CAP_PROP_FOCUS, 255);
		cap.set(5, 60);
		cap.set(Videoio.CV_CAP_PROP_AUTOFOCUS, 1);

		WebcamVariables.CurrentCameraID = cameraIndex;
		return cap;
	}

	public void run() {

		calculateInstalledCameraIDs();

		// Register the default camera
		VideoCapture cap = new VideoCapture();

		cap.open(WebcamVariables.CurrentCameraID);

		// Camera settings
		cap.set(Videoio.CV_CAP_PROP_FOCUS, 0);
		cap.set(5, 60);
		cap.set(Videoio.CV_CAP_PROP_AUTOFOCUS, 1);
		// result = cap.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, 1280);
		// result = cap.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, 720);

		int openedCameraId = WebcamVariables.CurrentCameraID;
		int realScanCameraUseCount = 0;
		int scanAttempt = 0;
		boolean isTestScan = false;

		// Check if video capturing is enabled
		if (!cap.isOpened()) {
			System.exit(0);
		}

		DetectCircle detector = new DetectCircle();
		// DetectBarcode barcodeDetector = new DetectBarcode(); // currently deprecated

		// Matrix for storing image
		Mat image = new Mat();

		// Frame for displaying image
		mainFrame = new WebcamFrame_2();
		mainFrame.setVisible(true);

		ImageProcess.learnFromImages();

		runReceiveQueueThread();
		TapingType[] tapingType = new TapingType[1];

		try {
			// Main loop
			while (mainFrame.isClosed() == false) {
				// Read current camera frame into matrix
				if (openedCameraId != WebcamVariables.CurrentCameraID) {

					cap = switchToOtherCamera(cap); // in this case, WebcamVariables.CurrentCameraID is set by UI
													// selection
					if (cap == null) {
						break;
					}

					openedCameraId = WebcamVariables.CurrentCameraID;
				}

				if (mainFrame.getResultText().toUpperCase().equals("TESTSCAN")) {
					isStartScan = true;
					isTestScan = true;
				}

				cap.read(image);

				// Render frame if the camera is still acquiring images
				if (!image.empty()) {
					if (WebcamVariables.DetectMethod == DetectType.Hough
							|| WebcamVariables.DetectMethod == DetectType.Contour
							|| WebcamVariables.DetectMethod == DetectType.Contour2
							|| WebcamVariables.DetectMethod == DetectType.Triangle
							|| WebcamVariables.DetectMethod == DetectType.Rectangle
							|| WebcamVariables.DetectMethod == DetectType.Square) {
						tapingType[0] = TapingType.NotFound;

						// Imgproc.filter2D(image, image, -1, kernel); // image sharpening

						mainFrame.render(detector.Detect(image, tapingType));

						if (isStartScan == true) {

							if (scanCount == 0) {
								mainFrame.setResultText(""); // reset
								cap = switchToOtherCamera(cap, isLeft);
								isLeft = !isLeft;

								System.out.println("Switched to camera " + WebcamVariables.CurrentCameraID);
							}

							System.out.println("Scan requested result: " + tapingType[0]);

							scanCount++;
							scanAttempt++;

							mainFrame.setResultText("Scan " + scanAttempt + " result: " + tapingType[0]);

							TapingType scanType = getTapingTypeInString(tapingType);
							if (scanType != TapingType.Error) {

								if (isTestScan == false) {
									enqueueServerMessage(scanType.toString().toUpperCase()); // enqueue result message
																								// to send to client
								}

								scanCount = 0;
								isStartScan = false; // reset
								isTestScan = false;
								realScanCameraUseCount = 0;
								scanAttempt = 0;
							} else if (scanType == TapingType.Error && scanCount == MAX_SCAN_COUNT) {

								realScanCameraUseCount++;
								if (realScanCameraUseCount < WebcamVariables.TotalCameraCount) {
									cap = switchToOtherCamera(cap, isLeft);
									// System.out.println("Switched to camera " + WebcamVariables.CurrentCameraID);
									// scanCount = 0; // reset scan count

									// result += ". Try to scan using another camera.";
								} else {

									if (isTestScan == false) {
										enqueueServerMessage(scanType.toString().toUpperCase()); // enqueue result
																									// message to send
																									// to client
									}

									isStartScan = false; // reset
									isTestScan = false;
									realScanCameraUseCount = 0;
									scanAttempt = 0;
								}

								scanCount = 0;
								mainFrame.refreshUI();
							}
						}
					} else { // WebcamVariables.DetectMethod == DetectType.Barcode
						String barcodeText = "";
						mainFrame.render(image);

						if (WebcamVariables.Train != TrainImage.Start) {
							mainFrame.setResultText(barcodeText);
						}

						if (isStartScan == true) {

							if (scanCount == 0) {
								mainFrame.setResultText(""); // reset
							}

							System.out.println("Scan requested result: " + barcodeText);

							scanCount++;
							if (barcodeText != "") { // If barcode is detected
								isStartScan = false; // reset
								scanCount = 0;

								enqueueServerMessage(barcodeText);

								mainFrame.setResultText(barcodeText);
							} else if (scanCount == MAX_SCAN_COUNT) { // If barcode is not detected

								String text = "error";

								realScanCameraUseCount++;
								if (realScanCameraUseCount < WebcamVariables.TotalCameraCount) {
									cap = switchToOtherCamera(cap, true); // in this case,
																			// WebcamVariables.CurrentCameraID is not
																			// set, so we need to increase it
									System.out.println("Switched to camera " + WebcamVariables.CurrentCameraID);
									// scanCount = 0; // reset scan count

									text += ". Try to scan using another camera.";
								} else {
									isStartScan = false; // reset
									realScanCameraUseCount = 0;
									enqueueServerMessage(text); // enqueue result message to send to client
								}

								scanCount = 0; // reset
								mainFrame.refreshUI();
								mainFrame.setResultText(text);
							}
						}
					}
				} else {
					System.out.println("No captured frame -- camera disconnected");
				}
			}

			System.out.println("System terminating...");
			cap.release();
			mainFrame.dispose();
			stopQueue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
		}

		return;
	}
}