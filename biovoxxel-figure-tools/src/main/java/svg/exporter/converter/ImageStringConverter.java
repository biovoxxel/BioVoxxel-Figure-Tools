package svg.exporter.converter;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.apache.commons.io.FileUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;

public class ImageStringConverter {

	
	private final String TEMP_FILE_PATH = System.getProperty("java.io.tmpdir") + "temp_image_file.tif"; 
	
	

	public String getBase64StringFromImagePlus(ImagePlus imp) {
		String encodedImage = "";

		if (imp != null) {
			
			new FileSaver(imp).saveAsTiff(TEMP_FILE_PATH);
			
			File imageFile = new File(TEMP_FILE_PATH);
			
			
			try {
				
				byte[] imageAsByteArray = FileUtils.readFileToByteArray(imageFile);
				
				encodedImage = Base64.getEncoder().encodeToString(imageAsByteArray);
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				imageFile.deleteOnExit();
			}
		}
		
		//System.out.println("base64_encoded_image = " + encodedImage);
		return encodedImage;
		
	}

	
	public ImagePlus getImagePlusFromBase64String(String bas64String, String filePath) throws IOException {
		
		byte[] imageAsByteArray = Base64.getDecoder().decode(bas64String);
		
		FileUtils.writeByteArrayToFile(new File(filePath), imageAsByteArray);
				
		ImagePlus decodedImage = new ImagePlus(filePath);
		
		return decodedImage;
	}
	
	
	
	
	
	public static void main(String[] args) {
		
		ImagePlus testImage = IJ.openImage("http://imagej.nih.gov/ij/images/clown.jpg");
		
		
		new ImageStringConverter().getBase64StringFromImagePlus(testImage);
	}
	
}
