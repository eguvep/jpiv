# JPIV Library

This is the place to store 

- platform dependent binariers, 
- scripts, that can be edited in place by the user, and
- other ressources that can not be packed into the JPIV.jar archive.

## config

Configuration file template for the Europiv II Synthetic Image Generator (SIG). The file format is NetCDF. When one of the Syntetic-Image-Generator Scripts is called, a copy of this file will be placed in the user directory. The template will not be changed.

## jsc

Beanshell skripts in this folder are are automatically linked in the Script drop down menu in JPIV. The scripting syntax is Java, but compiling is not necessary. You can edit this scripts without deep Java knowledge. More information at:

https://eguvep.github.io/jpiv/scripting.html

## linux64

Native Binaries for Linux (64 bit). These are:

1. A shared object for reading LaVision IM7 images and the source.

2. The Europiv II Synthetic Image Generator (SIG). More information at:

http://www.meol.cnrs.fr/LML/EuroPIV2/SIG/doc/SIG_Main.htm

## matlab

From within Beanshell scripts, you can conveniently call Matlab/Octave scripts. A few examples are stored in this directory. They are invoked via scripts in the jsc directory, e.g. 2example_resample_vector_field.jsc, apply_mask.jsc.

## win32/win64

Binaries for Windows. This is currently only a shared library for reading LaVision IM7 images.
