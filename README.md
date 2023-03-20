# BioVoxxel Figure Tools
 
The SVG Exporter is meant to export images and all related overlays in images, embedd images and objects in SVG files to enable loss-less scalability of scientific data.

## Installation

The BioVoxxel Figure tools are automatically installed together with the BioVoxxel Toolbox.

Run: >Help >Update... and activate the BioVoxxel update site and then apply changes in the updater.

The *BioVoxxel Figure Tools* come with a convenience menu which can be retreived from the `More >>` tools icon.


## Functions

### Exporting SVG vector graphics directly from Fiji

![image](https://user-images.githubusercontent.com/10721817/213475119-ac5dd2dc-e214-45d9-9499-ecc0df0311ca.png)

The functions allow exporting images together with all added overlays (ROIs from a Roi Manager, scale bars, calibration bars, arrows, etc.) in one step into an SVG vector graphics file. It supports multichannel composite images and ImageJ hyperstacks (so no need to flatten to an RGB). In composite images only visible channels will be exported enabling the user to export different versions of channel merges in an easy manner. 
Even more convenient, you can export all open images at once as individual SVG files and import them easily in [Inkscape](https://inkscape.org/) (unfortunately Adobe Illustrator (TM) seems to currently not support this saved SVG file type).
Simple zoomed-in inset images can be created at fixed size changes to avoid pixel artifacts 

### Aligned SVGs in Inkscape after export from ImageJ

![image](https://user-images.githubusercontent.com/10721817/213476261-4ce8f48c-4725-4e45-b689-6da70fa47d82.png)

### Exporting time points of time series (also from Hyperstacks)

The function `Export Time Series to SVGs` allows to export time points from a specific starting point on with a defines increment as individual SVGs.
For hyperstacks containing z-slices only the visible (active) z-slice will be exported. Visible composite channels will be merged and all overlays will be exported as vector graphics objects.

![image](https://user-images.githubusercontent.com/10721817/220351083-ff3c2eb7-f793-4b5b-9ba9-2a964306045d.png)

Image source: [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.6139958.svg)](https://doi.org/10.5281/zenodo.6139958)

Those can then be imported in [Inkscape](https://inkscape.org/) and easily arranged with a grid alignment functionalitiy

![image](https://user-images.githubusercontent.com/10721817/220352194-e96a0b8b-26ef-4916-b48d-f73b0bee6e68.png)

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
