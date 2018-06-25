import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileReadWrite {
	private static String config = "WebcamDetector.ini";
	public static void WriteConfigFile() {
		
		try {
			// Assume default encoding.
            FileWriter fileWriter =
                new FileWriter(config);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter =
                new BufferedWriter(fileWriter);

            // Note that write() does not automatically
            // append a newline character.
            bufferedWriter.write("MinRadius\t" + WebcamVariables.MinRadius);
            bufferedWriter.newLine();
            bufferedWriter.write("MaxRadius\t" + WebcamVariables.MaxRadius);
            bufferedWriter.newLine();
            bufferedWriter.write("DetectMethod\t" + WebcamVariables.DetectMethod);
            bufferedWriter.newLine();
            bufferedWriter.write("SubAreaPosX\t" + WebcamVariables.SubAreaPosX);
            bufferedWriter.newLine();
            bufferedWriter.write("SubAreaPosY\t" + WebcamVariables.SubAreaPosY);
            bufferedWriter.newLine();
            bufferedWriter.write("SubAreaWidth\t" + WebcamVariables.SubAreaWidth);
            bufferedWriter.newLine();
            bufferedWriter.write("SubAreaHeight\t" + WebcamVariables.SubAreaHeight);
            bufferedWriter.newLine();
            
            bufferedWriter.write("MinDistance\t" + WebcamVariables.MinDistance);
            bufferedWriter.newLine();
            bufferedWriter.write("MaxDistance\t" + WebcamVariables.MaxDistance);
            bufferedWriter.newLine();
            bufferedWriter.write("CurrentPermission\t" + WebcamVariables.CurrentPermission);
            bufferedWriter.newLine();
            bufferedWriter.write("MinVertices\t" + WebcamVariables.MinVertices);
            bufferedWriter.newLine();
            bufferedWriter.write("MaxVertices\t" + WebcamVariables.MaxVertices);
            bufferedWriter.newLine();
            bufferedWriter.write("MinEllipseDiff\t" + WebcamVariables.MinEllipseDiff);
            bufferedWriter.newLine();
            bufferedWriter.write("MaxEllipseDiff\t" + WebcamVariables.MaxEllipseDiff);
            bufferedWriter.newLine();
            
            bufferedWriter.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	public static void ReadConfigFile() {
		
		File file = new File(config);
		if (file.exists() == false) {
			return;
		}
		
		try {
			// FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(config);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            
            String line;
            String[] items;
            
            // First of all, fill default config
            // After that, read file and fill the configs based on saved values.
            FillDefaultConfig();
            
            while((line = bufferedReader.readLine()) != null) {
                
            	items = line.split("\t");
                if(items == null || items.length == 0) {
                	continue;
                }
                
                switch(items[0]) {
                case "MinRadius":
                	WebcamVariables.MinRadius = Integer.parseInt(items[1]);
                	break;
                case "MaxRadius":
                	WebcamVariables.MaxRadius = Integer.parseInt(items[1]);
                	break;
                case "DetectMethod":
                	WebcamVariables.DetectMethod = DetectType.valueOf(items[1]);
                	break;
                case "SubAreaPosX":
                	WebcamVariables.SubAreaPosX = Integer.parseInt(items[1]);
                	break;
                case "SubAreaPosY":
                	WebcamVariables.SubAreaPosY = Integer.parseInt(items[1]);
                	break;
                case "SubAreaWidth":
                	WebcamVariables.SubAreaWidth = Integer.parseInt(items[1]);
                	break;
                case "SubAreaHeight":
                	WebcamVariables.SubAreaHeight = Integer.parseInt(items[1]);
                	break;
                case "CurrentPermission":
                	WebcamVariables.CurrentPermission = Permission.valueOf(items[1]);
                	break;
                case "MinDistance":
                	WebcamVariables.MinDistance = Integer.parseInt(items[1]);
                	break;
                case "MaxDistance":
                	WebcamVariables.MaxDistance = Integer.parseInt(items[1]);
                	break;
                case "MinVertices":
                	WebcamVariables.MinVertices = Integer.parseInt(items[1]);
                	break;
                case "MaxVertices":
                	WebcamVariables.MaxVertices = Integer.parseInt(items[1]);
                	break;
                case "MinEllipseDiff":
                	WebcamVariables.MinEllipseDiff = Integer.parseInt(items[1]);
                	break;
                case "MaxEllipseDiff":
                	WebcamVariables.MaxEllipseDiff = Integer.parseInt(items[1]);
                	break;
                }
            } 
            
            bufferedReader.close();            
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	private static void FillDefaultConfig() {
		WebcamVariables.MinRadius = 10;
		WebcamVariables.MaxRadius = 70;
		WebcamVariables.DetectMethod = DetectType.Contour;
		WebcamVariables.SubAreaPosX = 0;
		WebcamVariables.SubAreaPosY = 0;
		WebcamVariables.SubAreaWidth = 100;
		WebcamVariables.SubAreaHeight = 100;
		
		WebcamVariables.MinDistance = 10;
		WebcamVariables.MaxDistance = 20;
		WebcamVariables.CurrentPermission = Permission.Operator;
		WebcamVariables.MinVertices = 8;
		WebcamVariables.MaxVertices = 10;
		WebcamVariables.MinEllipseDiff = 2;
		WebcamVariables.MaxEllipseDiff = 10;
	}
}
