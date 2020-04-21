# Test Images

This folder contains test images for JPIV:

## synt.zip

Simple particle images mimicing a Poiseuille flow in a microchannel.

## synt3d

This zip-archive contains particle image files that represent a scan through a cube of particles that are subject to a Poiseuille flow. The Poiseuille flow ist turned and tilted by 45 degrees, so that the flow field crosses the images diagonal from top left to bottom right and from the top image to the bottom image.

The image creation is a four step procedure.

1. A random particle position data file is created. The parameters of the particle space are as follows:
particle space x min (unit: real) = -0.1414
particle space x max (unit: real) = 0.1414
particle space y min (unit: real) = -0.1414
particle space y max (unit: real) = 0.1414
particle space z min (unit: real) = -0.1414
particle space z max (unit: real) = 0.1414
number of particles per volume = 1000000 

2. A second particle position file is created, where the particles are shifted relative to the random file according to the following equation:
dz = -dp / ( (vx/6) * (vx/6) ) * ( x*x+y*y ) + dp
with
dz Displacement of the particles (real units).
dp Maximum displacement (real units) = 0.0024
vx The x dimension of the particle volume (real units) = x max - x min
vy The y dimension of the particle volume (real units) = y max - y min
vz The z dimension of the particle volume (real units) = z max - z min
x The x position of each random particle
y The y position of each random particle

3. The particle volume is turned and tilted by an Eularian coordinate transformation:
Eularian nutation angle of the particle volume = 45 deg
Eularian rotation angle of the particle volume = 0
Eularian precession angle of the particle volume = 45 deg

The actual image creation from the random position particle data files relies on the EUROPIV II Project "Synthetic Image Generator" (SIG), Feb. 2001, by Bertrand Lecordier: Bertrand.Lecordier@coria.fr, Jose Nogueira: goriba@ing.uc3m.es, and Jerry Westerweel: j.westerweel@wbmt.tudelft.nl. The parameters are listed below:

General
number of scan planes (images) = 13 
distance of scan planes (unit: real) = 0.02 

Image dimension
image width (pixel) = 512
image height (pixel) = 512
left strip (pixel) = 0
right strip (pixel) = 0
top strip (pixel) = 0
bottom strip (pixel)  = 0

Light sheet information
light sheet type = gaussian
initial light sheet position, will be shifted in positive direction during scan (unit: real) = -0.12
light sheet thickness (unit: real) = 0.02

Particle size distribution
size distribution = uniform
minimum particle diameter (relative) = 0.05
maximum particle diameter (relative) = 2.0
mean particle diameter (relative) = 0.5
standard deviation of the gaussian size distribution (relative) = 0.2

Pattern information
particle image size (pixel) = 2.0
particle image size (pixel) = 2.0

CCD information
CCD fill ratio = 0.7
ccd_fill_ratio_y = 0.7
ccd_saturation_level = 1.0
ccd_background_type = uniform
ccd_background_mean_level = 30.0
ccd_background_std_noise = 3.0
ccd_pixel_horizontal_pitch = 155.0
ccd_pixel_vertical_pitch = 155.0

Optic information
object - lens distance = 1.0
CCD - lens distance = 40.0

You may use this images for testing your scanning particle image velocimetry (PIV) code. Reference PIV data that corresponds to the images is included (for parameters, see below).

x-coordinate of first vector location (pixel) = 32
y-coordinate of first vector location (pixel) = 32
horizontal vector spacing (pixel) = 16
vertical vector spacing (pixel) = 16
number of horizontal nodes (pixel) = 29
number of vertical nodes (pixel) = 29 
