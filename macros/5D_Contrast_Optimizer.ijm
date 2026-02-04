/*
 * Copyright (C), 2023, Jan Brocher / BioVoxxel. All rights reserved.
 * 
 * Original macro written by Jan Brocher/BioVoxxel.
 * 
 * BSD-3 License
 * 
 * Redistribution and use in source and binary forms of all plugins and macros, 
 * with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
 *    in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * DISCLAIMER:
 * 
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
#@File[] (label = "Choose image files", required=false, style="files", persist="false", description="if no images are specified, \"Run on open images\" must be active") inputImages
#@Boolean (label = "Run on open images", value=false, description="Considers all open images. Make sure only images which should be adjusted are open") runOnOpen
#@Float (label = "Pixel saturation (%) Ch 1", value="0.05", min="0.00", max="100.00", style="format:0.00", stepSize="0.01", description="Avoid too much oversaturation. Best stay below 0.10 %") sat_ch_1
#@Float (label = "Pixel saturation (%) Ch 2", value="0.05", min="0.00", max="100.00", style="format:0.00", stepSize="0.01", description="Avoid too much oversaturation. Best stay below 0.10 %") sat_ch_2
#@Float (label = "Pixel saturation (%) Ch 3", value="0.05", min="0.00", max="100.00", style="format:0.00", stepSize="0.01", description="Avoid too much oversaturation. Best stay below 0.10 %") sat_ch_3
#@Float (label = "Pixel saturation (%) Ch 4", value="0.05", min="0.00", max="100.00", style="format:0.00", stepSize="0.01", description="Avoid too much oversaturation. Best stay below 0.10 %") sat_ch_4
#@Float (label = "Pixel saturation (%) Ch 5", value="0.05", min="0.00", max="100.00", style="format:0.00", stepSize="0.01", description="Avoid too much oversaturation. Best stay below 0.10 %") sat_ch_5
#@Float (label = "Pixel saturation (%) Ch 6", value="0.05", min="0.00", max="100.00", style="format:0.00", stepSize="0.01", description="Avoid too much oversaturation. Best stay below 0.10 %") sat_ch_6
#@Float (label = "Pixel saturation (%) Ch 7", value="0.05", min="0.00", max="100.00", style="format:0.00", stepSize="0.01", description="Avoid too much oversaturation. Best stay below 0.10 %") sat_ch_7
#@Boolean (label = "Apply contrast (non-reversable)", value=false, description="If active the adjustment is a non-reversible point operation which kannot be undone") applyContrast
#@File (label = "Save in subfolder", required=false, style="directory", description="If empty, images will not be saved, otherwise end up as .tif images in the specified folder") resultsFolder
#@Boolean (label = "Print used limits", value=false, description="prints the used contrast limits into the log window") printExtremes
#@Boolean (label = "Run in background", value=false, description="Runs in background without showing the images.") runInBackground
#@String (label = "Usage", value="<html>This tool applies the maximum possible contrast inside good scientific practice limits<br><b>equally</b> to all considered images (all open ones or only the above specified).<br>Keep saturation values between <i>0.01-0.10</i>. Higher values oversaturate too many pixels!<br> This is <b>different</b> from normal auto contrast since all images stay optically comparable.<br>All images which later should be optically compared need to be processed here together in one step!</html>", visibility="MESSAGE", required="false") contrast_optimizer_usage
var images;

saturation = newArray(sat_ch_1, sat_ch_2, sat_ch_3, sat_ch_4, sat_ch_5, sat_ch_6, sat_ch_7);

if (runInBackground) {
	setBatchMode(true);
}

if (!runOnOpen) {
	imageString = "";
	for (f = 0; f < inputImages.length; f++) {
		open(inputImages[f]);
		currentImage = getTitle();
		if (bitDepth() == 24) {
			close(currentImage);
		} else {
			imageString += getTitle() + ",";
		}
	}
	images = split(imageString, ",");
	if (images.length < 1) {
		exit("No images to be adjusted");
	}
	images = Array.deleteIndex(images, images.length-1);
} else {
	images = getList("image.titles");
}


channels = 0;
slices = 0;
frames = 0;

selectImage(images[0]);
inputDir = getDirectory("image");

for (i = 0; i < images.length; i++) {
	selectImage(images[i]);
	if (bitDepth() != 24) {
		getDimensions(w, h, ch, sl, fr);
		channels = Math.max(channels, ch);
		slices = Math.max(slices, sl);
		frames = Math.max(frames, fr);
	}
}


min = newArray(channels);
max = newArray(channels);

absoluteMin = newArray(channels);
absoluteMax = newArray(channels);
Array.fill(absoluteMin, 1e30);

for (i = 0; i < images.length; i++) {
	selectImage(images[i]);
	if (bitDepth() != 24) {
		
		getDimensions(width, height, currentChannel, currentSlice, currentFrames);
		for (c = 0; c < currentChannel; c++) {
			Stack.setChannel(c+1);
			for (s = 0; s < currentSlice; s++) {
				Stack.setSlice(s+1);
				for (f = 0; f < currentFrames; f++) {
					Stack.setFrame(f+1);
					resetMinAndMax();
					run("Enhance Contrast...", "saturated=" + saturation[c] + " process_all use");
					getMinAndMax(min[c], max[c]);
					absoluteMin[c] = Math.min(absoluteMin[c], min[c]);
					absoluteMax[c] = Math.max(absoluteMax[c], max[c]);
				}
			}
		}
	}
}

if (printExtremes) {
	for (n = 0; n < channels; n++) {
		print("Channel = " + (n+1) + "   -->   min = " + absoluteMin[n] + " / max = " + absoluteMax[n] + "   (saturation = " + saturation[n] + " %)");
	}
}

for (i = 0; i < images.length; i++) {
	selectImage(images[i]);
	if (bitDepth() != 24) {
		metadata = getMetadata("Info");
		existingAdjustmentMetadataIndex = lastIndexOf(metadata, "\nContrast adjustment");
		if (existingAdjustmentMetadataIndex > 0) {
			metadata = substring(metadata, 0, existingAdjustmentMetadataIndex);		
		}
		metadata = metadata + "\nContrast adjustment:\n";
		getDimensions(width, height, currentChannel, currentSlice, currentFrames);
		
		for (c = 0; c < currentChannel; c++) {
			Stack.setChannel(c+1);
			for (s = 0; s < currentSlice; s++) {
				Stack.setSlice(s+1);
				for (f = 0; f < currentFrames; f++) {
					Stack.setFrame(f+1);
					setMinAndMax(absoluteMin[c], absoluteMax[c]);
					if (applyContrast) {
						run("Apply LUT", "slice");
	//					resetMinAndMax();
					}
					metadata = metadata + "Channel " + c+1 + "/Slice " + s+1 + "/Frame " + f+1 + ": min=" + absoluteMin[c] + " / max=" + absoluteMax[c] + "   (saturation = " + saturation[c] + " %)\n";
				}
			}
			if (applyContrast) resetMinAndMax();
		}
		setMetadata("Info", metadata);
	}
}

if (resultsFolder != 0) {

	for (i = 0; i < images.length; i++) {
		selectImage(images[i]);
		if (bitDepth() != 24) {
			save(resultsFolder + File.separator + File.getNameWithoutExtension(images[i]) + "_contr-adj.tif");
			
			if (runInBackground) {
				close(images[i]);
			}
		}
	}
}

setBatchMode(false);

