# BioVoxxel Figure Tools
 
The SVG Exporter is meant to export images and all related overlays in images, embedd images and objects in SVG files to enable loss-less scalability of scientific data.

## Installation

The BioVoxxel Figure tools can be installed in Fiji via its own update site.

Run: _>Help >Update..._ and activate the **BioVoxxel Figure Tools** update site and then apply changes in the updater.

The *BioVoxxel Figure Tools* come with a convenience menu which can be retreived from the `More >>` tools icon.


## Functions

### LUT Channels Tool
This tool is the equivalent of the normal ImageJ channels tool and based on its functionalities. Advantage is that multiple LUT collections can be gathered in one folder and switched between
Therefore, create a new sub-folder in the `Fiji.app > luts` folder and add .lut files you like in that folder. After restarting the LUT Channels Tool a right-click in the panel area around the buttons enables you to set the lut folder used to create the buttons panel. Click any button to apply a certain LUT to the current image or active channel of a composite image.
Channels can be further shown or hidden using the channel checkboxes.
`Split Channels` calls the IJ split channels command. The same is true for `Merge Channels`.
`CDV Test` usis IJ's function to simulate the most common color-deficient vision issues to check if the currently used LUT is useful for scientific publications.

![image](https://user-images.githubusercontent.com/10721817/233982982-08f0cde1-5600-4aa1-9a1a-c39eab770a61.png)

### 5D Contrast Optimizer
This tool will check all channels, z-slices and time frames for all open or provided images and adjust the contrast equally to all images avoiding strong oversaturation. And all of that with basically just one click. CAUTION: All images that should be compared to each other in a figure need to be adjusted here at the same time.

![image](https://github.com/biovoxxel/BioVoxxel-Figure-Tools/assets/10721817/bca3ab15-6766-4645-a981-b37250ade297)

Comparing images before and after adjustment:
![image](https://github.com/biovoxxel/BioVoxxel-Figure-Tools/assets/10721817/a9735ba6-4df3-40bd-871f-78b8e8c68a61)

### RGB Contrast Optimizer
This is the same as the 5D Contrast Optimizer but for RGB true-color images (such as bright-field, histological sections,...)

![image](https://github.com/biovoxxel/BioVoxxel-Figure-Tools/assets/10721817/19c2ec4d-255f-459e-ab32-f94ce3e16b8c)

Comparing images before and after adjustment:
![image](https://github.com/biovoxxel/BioVoxxel-Figure-Tools/assets/10721817/1bace310-69b0-4016-94e4-e68046197e18)


### Creating zoomed-in and resized versions of image areas (e.g. as insets)
Simple zoomed-in inset images can be created at fixed integer size factors to avoid pixel artifacts by using the funtion `Create framed inset zoom`

![image](https://github.com/biovoxxel/BioVoxxel-Figure-Tools/assets/10721817/ef6761a6-f91f-4a88-9a84-be41ab37fec6)

### Exporting SVG vector graphics directly from Fiji

![image](https://github.com/biovoxxel/BioVoxxel-Figure-Tools/assets/10721817/3b910b28-d3df-418f-b22b-f18f782381e0)

The functions allow exporting images together with all added overlays (ROIs from a Roi Manager, scale bars, calibration bars, arrows, etc.) in one step into an SVG vector graphics file. It supports multichannel composite images and ImageJ hyperstacks (so no need to flatten to an RGB). In composite images only visible channels will be exported enabling the user to export different versions of channel merges in an easy manner. 
Even more convenient, you can export all open images at once as individual SVG files and import them easily in [Inkscape](https://inkscape.org/) (unfortunately Adobe Illustrator (TM) seems to currently not support this saved SVG file type).


### Exporting time points of time series (also from Hyperstacks)

The function `Export Time Series to SVGs` allows to export time points from a specific starting point on with a defines increment as individual SVGs.
For hyperstacks containing z-slices only the visible (active) z-slice will be exported. Visible composite channels will be merged and all overlays will be exported as vector graphics objects.

![image](https://user-images.githubusercontent.com/10721817/220351083-ff3c2eb7-f793-4b5b-9ba9-2a964306045d.png)

Image source: [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.6139958.svg)](https://doi.org/10.5281/zenodo.6139958)

Those can then be imported in [Inkscape](https://inkscape.org/) and easily arranged with a grid alignment functionalitiy

![image](https://user-images.githubusercontent.com/10721817/220352194-e96a0b8b-26ef-4916-b48d-f73b0bee6e68.png)

### Aligned SVGs in Inkscape after export from ImageJ

![image](https://user-images.githubusercontent.com/10721817/213476261-4ce8f48c-4725-4e45-b689-6da70fa47d82.png)


### Metadate Recording with *Meta-D-Rex*

This Recorder makes use or the native ImageJ macro recorder. While the recorder is running automatically in the background, recording of Meta-D-Rex can be  paused and under *settings* specific command can be excluded from recording to avoid cluttering of metadata.

*The native recorder can be displayed by right-clicking on the area nect to the *Settings* button if needed*

The recording happens specifically for each image separately and is automatically saved into the metadata (Info panel) of the image.
Meta-D-Rex also reads existing metadata saved in the image file from microscopic software if imported correctly in Fiji, e.g. via Bio-Formats.

![image](https://user-images.githubusercontent.com/10721817/213480267-b761f086-c5ee-45be-883e-1f07e5bd2589.png)

If the image is either saved as a .tif file or any other format that supports ImageJ metadata saving those will be retained in the image file itself.
If the SVG exporter (see above) is used, the recorded metadata will be also available in the SVG files' *Description* panel.
This way, image processing can be stored on a per image level and will be available even during figure creation. Thus, image editing can be easier reported in a reproducible manner.

---

### Rationale

Why another figure creation-related release?
Tools like QuickFigure are amazing and offer more options. I personally, however, prefer the freedom Inkscape gives me and I want to achieve that…

* images exported from ImageJ have the highest possible, best original quality, when used for publications (if not altered by the user before)
* images are directly embedded in SVG documents
* interpolation is not used to visualize the original pixels of the micrographs in the published document which enables the reader/observer to see exactly what the experimenter saw to draw a concludion based on the same information. Currently, many publication figures either suffer from JPEG artifacts, are too low in resolution or massively blurred due to interpolation.
* The figure creation tools also offers to Create a framed inset zoom version of a selected area, which uses fixed frame sizes (this is where a small restriction exists) but creates a zoomed inset as separate image with the original image quality, without interpolation and correct scaling information to add an updated scale bar.
* All indicators like scale bars, calibration bars and all other ROIs in the image overlay should be transferred as editable vector graphics objects
* best, some metadata is still present. Therefore, the SVG export adds all metadata present in ImageJ to the Image description of the SVG file (see below), which can then be revised in Inkscape still (unfortunately it does not transfer to PDFs)

---

### Acknowledgement
[](url)

Thanks to [@Wayne](https://forum.image.sc/u/wayne/summary) (Rasband), [@K_Taz](https://forum.image.sc/u/k_taz/summary) (Kévin Terretaz), [@NicoDF](https://forum.image.sc/u/nicodf/summary) (Nicolás De Francesco) for the implementation of the inverting LUT function.
