/*
 * Copyright (C) 2020 Peter Vennemann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package jpiv2;

/**
 *
 * @author Peter Vennemann
 */
public class ImgProc extends Thread {

    /**
     * Remove sliding background.
     */
    public final static int REMOVE_SLIDING_BACKGROUND = 0;
    public final static int JOIN = 1;
    public final static int SPLIT =2;

    private final jpiv2.JPiv jpiv;
    private final int operation;
    private String[] files;
    private String destPath;
    private java.text.DecimalFormat df;

    /**
     * Creates a new instance of ImgProc
     * 
     * @param jpiv
     *            the jpiv2.JPiv parent component.
     * @param operation
     *            one of the constants REMOVE_SLIDING_BACKGROUND, ...
     *            (only this one implemented ;-)
     */
    public ImgProc(jpiv2.JPiv jpiv, int operation) {
        this.jpiv = jpiv;
        this.operation = operation;
        initVariables();
    }

    /**
     * Do not call this function directly, rather use
     * jpiv2.ImgProc().start() to run the data filtering process in a
     * seperate thread.
     */
    @Override
    public void run() {
        synchronized (getClass()) {
            switch (operation) {
                case REMOVE_SLIDING_BACKGROUND: {
                    if (files.length < 3) {
                        System.out.println("Need at least three images to create a valid background.");
                    }
                    else {
                        for (int f=0; f<files.length; f++) {
                            System.out.println("Processing: " + files[f] + " ...");
                            // background images
                            String[] bg = new String[3];
                            if (f < files.length-2) {
                                System.arraycopy(files, f, bg, 0, 3);
                            }
                            else if (f == files.length-2) {
                                System.arraycopy(files, f-1, bg, 0, 3);
                            }
                            else if (f == files.length-1) {
                                System.arraycopy(files, f-2, bg, 0, 3);
                            }
                            // process
                            jpiv2.PivImg img = new jpiv2.PivImg(
                                jpiv, bg, jpiv2.PivImg.RM_SLIDING_BACKGRD );
                            // save
                            img.writePivImageToFile( 
                                "png", 
                                destPath + df.format(f));
                            // append to list
                            jpiv.getListFrame().appendElement( 
                                    destPath + df.format(f) + ".png" );
                        }
                    }
                    break;
                }
                case JOIN: {
                    if (files.length < 2) {
                        System.out.println("Select at least two image files." );
                    } else {
                        for(int f=1; f<files.length; f=f+2) {
                            System.out.println(
                                "Join image " + 
                                files[f-1] + " and " + 
                                files[f]);
                            // read
                            jpiv2.PivImg img = new jpiv2.PivImg( 
                                jpiv, files[f-1], files[f] );
                            // save
                            img.writePivImageToFile( 
                                "png",
                                destPath + df.format(f));
                            // append to list
                            jpiv.getListFrame().appendElement( 
                                destPath + df.format(f) + ".png" );
                        }
                    }
                    break;
                }
                case SPLIT: {
                    for(int f=0; f<files.length; f++) {
                        System.out.println( "Split image " + files[f] );
                        // read
                        jpiv2.PivImg img = new jpiv2.PivImg( jpiv, files[f] );
                        // crop
                        int w = img.getWidth();
                        int h = img.getHeight()/2;
                        javax.media.jai.PlanarImage piA = img.getSubImage(0,0,w,h,0);
                        javax.media.jai.PlanarImage piB = img.getSubImage(0,0,w,h,1);
                        // save
                        saveImage( piA, destPath + df.format(f) + "_a" + ".png", "png");
                        saveImage( piB, destPath + df.format(f) + "_b" + ".png", "png");
                        // append to list
                        jpiv.getListFrame().appendElement( destPath + df.format(f) + "_a" + ".png" );
                        jpiv.getListFrame().appendElement( destPath + df.format(f) + "_b" + ".png" );
                    }
                }
            }
            System.out.println("Finished image processing.");
        }
    }

    private void initVariables() {
        this.files = jpiv.getListFrame().getSelectedElements();
        if (files == null) {
            System.out.println("No files selected. Nothing to do.");
        }
        else {
            this.df = jpiv2.FileHandling.getCounterFormat(files.length);
            this.destPath = chooseDestPath(FlexFileChooser.IMG_WRITE);
            int index = destPath.lastIndexOf('.');
            if (index != -1) destPath = destPath.substring(0, index);
        }
    }

    /**
     * Brings up a file chooser dialog to determine a destination file.
     * 
     * @param type
     *            The file extension ID.
     * @return The selected pathname or null if the user cancelled the dialog.
     */
    private String chooseDestPath(int type) {
            String pathname;
            jpiv2.FlexFileChooser flexFileChooser = jpiv.getFlexFileChooser();
            flexFileChooser.setFiletype(type, false);
            int approve = flexFileChooser.showSaveDialog(jpiv);
            if (approve == javax.swing.JFileChooser.APPROVE_OPTION) {
                    pathname = flexFileChooser.getSelectedFile().toString();
                    return (pathname);
            } else
                    return (null);
    }


    private void saveImage( javax.media.jai.PlanarImage img, 
                            String filename, 
                            String format) {
	java.awt.image.renderable.ParameterBlock pb = new java.awt.image.renderable.ParameterBlock();
	pb.addSource(img);
	pb.add(filename);
	pb.add(format);
	javax.media.jai.JAI.create("filestore", pb);
    }

}
