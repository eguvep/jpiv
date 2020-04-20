# JPIV – Particle Image Velocimetry (PIV) evaluation software

## What is this?

JPIV is a complete and open source software package for 2D [Particle Image Velocimetry (PIV)](https://en.wikipedia.org/wiki/Particle_image_velocimetry) – an image based fluid flow measurement technology.

## Features

- graphical user interface providing an efficient workflow and batch processing
- entirely written in Java, therefore platform independent
- highly accurate and fast because of multi-pass, multi-grid, FFT based correlation algorithmm
- ensemble correlation (correlation averaging) implemented
- complete ready for Micro-PIV and high resolution boundary flow measurements: rectangular evaluation areas possible, single pixel resolution ensemble evaluation implemented
- outlier detection
- calculation of various statistical information as well as some derivatives like vorticity
- 3d velocity field reconstruction from 2d planes in incompressible flows
- publication ready vector plots and export in various vector graphics and pixel formats
- arbitrary velocity profile extraction
- region of interest
- synthetic image generation
- extension and automation by user scripts

## Running JPIV

Prerequisite: A Java Runtime Environment (JRE).

1. Download the [zip-archive](./jpiv.zip).
2. Extract it to your hard disc.
3. Open a terminal and `cd` into the new directory.
4. Execute `java -jar JPIV.jar`.

## Using JPIV

- [Introduction for PIV Novices](https://eguvep.github.io/jpiv/introduction.html)
- [Quick Start Guide for JPIV Beginners](https://eguvep.github.io/jpiv/fiststeps.html)
- [HOWTO for Expert Users](https://eguvep.github.io/jpiv/howto.html)
- [JPIV Parameter Reference](https://eguvep.github.io/jpiv/settings.html)
- [Test Images](https://eguvep.github.io/jpiv/download.html)

## Extending JPIV

- [User Script Coding Intro](https://eguvep.github.io/jpiv/scripting.html)
- [JPIV Code Documentation](https://eguvep.github.io/jpiv/javadoc/)
