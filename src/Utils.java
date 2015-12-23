import java.io.File;
import java.util.ArrayList;

public class Utils {

	public static ArrayList<File> listFiles(String folderName) {
		
		File folder = new File(folderName);
		ArrayList<File> out = new ArrayList<File>();
		
	    for (final File fileEntry : folder.listFiles()) {
	        out.add(fileEntry);
	    }
	    
	    return out;
	}
}
