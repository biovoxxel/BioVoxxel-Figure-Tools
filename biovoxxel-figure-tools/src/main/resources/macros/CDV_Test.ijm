currentImageID = getImageID();
currentImageName = getTitle();

if (bitDepth() != 24) {
	run("Flatten", "slice");
	currentImageID = getImageID();
	currentImageName = getTitle();
}

setBatchMode(true);
selectImage(currentImageID);
run("Duplicate...", "title=[CDV_Protanopia_(no_red)_"+currentImageName+"]");
run("Simulate Color Blindness", "mode=[Protanopia (no red)]");
selectImage(currentImageID);
run("Duplicate...", "title=[CDV_Deuteranopia_(no_green)_"+currentImageName+"]");
run("Simulate Color Blindness", "mode=[Deuteranopia (no green)]");
selectImage(currentImageID);
run("Duplicate...", "title=[CDV_Tritanopia_(no_blue)_"+currentImageName+"]");
run("Simulate Color Blindness", "mode=[Tritanopia (no blue)]");
run("Images to Stack", "  title=CDV_ use");
setBatchMode(false);
