
#@ImagePlus(label = "Test image") imp

getDimensions(width, height, channels, slices, frames);

Stack.getPosition(channel_pos, slice_pos, frame_pos);

setBatchMode("hide");
print("----- Saturation Check for " + getTitle() + " -----");
for (c = 0; c < channels; c++) {
	
	//total_sat_pixels = 0;
	under_sat_pixels = 0;
	over_sat_pixels = 0;
	//total_sat_percentage = 0;
	
	Stack.setChannel(c+1);
	
	for (s = 0; s < slices; s++) {
		Stack.setSlice(s+1);
		
		for (f = 0; f < frames; f++) {
			Stack.setFrame(f+1);
			
			resetMinAndMax();
			getHistogram(values, counts, 256);
			
			under_sat_pixels += counts[0];
			over_sat_pixels += counts[255];
		}
	}
	
	total_sat_pixels = under_sat_pixels + over_sat_pixels;
	
	total_sat_percentage = 100 * total_sat_pixels / (width * height * slices * frames);
	under_sat_percentage = 100 * under_sat_pixels / (width * height * slices * frames);
	over_sat_percentage = 100 * over_sat_pixels / (width * height * slices * frames);
	
	contrast_enhancement = "contrast enhancement NOT recommended!";
	if (under_sat_percentage < 0.1 || over_sat_percentage < 0.1) {
		contrast_enhancement = "contrast enhancement possible";
	}
	
	print("Total saturation in channel " + (c+1) + ":   " + total_sat_percentage + " %   -->   under = " + under_sat_percentage + " % / over = " + over_sat_percentage + " %   -->   " + contrast_enhancement);
	
}

Stack.setPosition(channel_pos, slice_pos, frame_pos);
print("----------");
setBatchMode(false);	

