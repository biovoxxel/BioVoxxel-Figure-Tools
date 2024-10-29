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


#@File (label = "Choose directory", required=false, style="directory", persist=false, description="Only folders can be specified, not individual images") inputFolder
#@String(value="All images in the specified folder need to be of RGB type and of the same size", visibility="MESSAGE") info
#@Boolean (label = "Run on active open image", value=false, description="Runs on the actively selected single image, ONLY, but works on stacks") runOnOpen
#@Float (label = "Pixel saturation (%)", value="0.01", min="0.00", max="1.00", style="format:0.00", stepSize="0.01", description="Avoid too much oversaturation. Best stay below 0.10 %") saturation
#@File (label = "Save in subfolder", required=false, style="directory", description="If empty, images will not be saved, otherwise end up as .tif images in the specified folder") rgbTargetFolder
#@Boolean (label = "Print used limits", value=false, description="prints the used contrast limits into the log window") printExtremes
#@Boolean (label = "Close saved images", value=false, description="Closes the images after processing.") closeImages
#@String (label = "Usage", value="<html>This tool applies the maximum possible contrast inside good scientific practice limits<br><b>equally</b> to all images in the specified folder.<br>Keep saturation values between <i>0.01-0.10</i>. Higher values oversaturate too many pixels!<br> This is <b>different</b> from normal auto contrast since all images stay optically comparable.<br>All images which later should be optically compared need to be processed here together in one step!</html>", visibility="MESSAGE") message


inputFolder += File.separator;


if (!runOnOpen) {
	setBatchMode(true);
	File.openSequence(inputFolder, "scale=100");
}

originalStack = getTitle();
getDimensions(width, height, channels, slices, frames);


labels = newArray(slices);

for (i = 1; i <= slices; i++) {
	Stack.setSlice(i);
	if (bitDepth() != 24) {
		exit("Close all non RGB color images");
	}
	labels[i-1] = getInfo("slice.label");
}

min = 0;
max = 0;
absoluteMin = 1e30;
absoluteMax = 0;

run("HSB Stack");
//setBatchMode("show");

getDimensions(width, height, channels, slices, frames);

absoluteMin = 1e30;
absoluteMax = 0;

Stack.setChannel(3);

for (s = 1; s <= slices; s++) {
	Stack.setSlice(s);
	run("Enhance Contrast...", "saturated=" + saturation);
	getMinAndMax(min, max);
	absoluteMin = Math.min(absoluteMin, min);
	absoluteMax = Math.max(absoluteMax, max);
}

if (printExtremes) {
	print("Contrast adjustment with --> min = " + absoluteMin + " / max = " + absoluteMax + "   (saturation = " + saturation + " %)");	
}

for (s2 = 1; s2 <= slices; s2++) {
	Stack.setSlice(s2);
	setMinAndMax(absoluteMin, absoluteMax);
	getMinAndMax(min2, max2);
	run("Apply LUT", "slice");
}



run("RGB Color", "slices");
resultStack = getTitle();
setBatchMode("show");
getDimensions(width, height, channels, slices, frames);

for (s = 1; s <= slices; s++) {
	Stack.setSlice(s);
	if (runOnOpen) {
		rename(resultStack + "_contr-adj");
	} else {
		setMetadata("Label", labels[s-1] + "_contr-adj");
	}
}

metadata = getMetadata("Info");
existingAdjustmentMetadataIndex = lastIndexOf(metadata, "\nContrast adjustment");
if (existingAdjustmentMetadataIndex > 0) {
	metadata = substring(metadata, 0, existingAdjustmentMetadataIndex);		
}
metadata = metadata + "\nContrast adjustment:\nmin=" + absoluteMin + " / max=" + absoluteMax + "   (saturation = " + saturation + " %)\n";
setMetadata("Info", metadata);

setBatchMode(false);
//run("Stack to Images");

if (rgbTargetFolder != 0) {
	run("Image Sequence... ", "dir=[" + rgbTargetFolder + File.separator + "] format=TIFF name=[] use");
	if (closeImages) {
		close(resultStack);
	}
}
