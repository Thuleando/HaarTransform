package HaarTransform;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * <p>Small Demo program to show the effects of a Haar transformation. Displays both
 * the image as it undergoes successive transformations as well as an expanded
 * view of the averaged portion of the transformed image to demonstrate the
 * image degradation. Has the option to save all stages as image files.
 *
 * <p>Supported image formats: JPG, BMP, PNG, GIF
 * <p>NOTE(GIF): Will only generateTransformImages the main image of an animated GIF and will not
 * animate the result.
 * <p>NOTE(PNG): Some PNG files do not display correctly, and have a washed out look. To
 * view these images in their true state, turn on the option to save stages as
 * image files and view the saved image files.
 * @author Jason Gould
 */
public class HaarDemo{
    private final HaarTransform haarTransformer;
    private ArrayList<ImageIcon> recoverPPics;
    private ArrayList<ImageIcon> recoverSPics;
    private ArrayList<ImageIcon> transformPPics;
    private ArrayList<ImageIcon> transformSPics;

    private File theFile;

    /**
     *
     */
   public HaarDemo(){
       haarTransformer = new HaarTransform();

       recoverPPics = new ArrayList<>();
       recoverSPics = new ArrayList<>();
       transformPPics = new ArrayList<>();
       transformSPics = new ArrayList<>();
    }

    /*protected File getOriginalImageFile(){
        return theFile;
    }*/

    protected ImageIcon loadImage(String fileName) throws TransformException{
        theFile = new File(fileName);
        if (!theFile.exists()) {
            throw new TransformException("loadImage:\nCannot find " + theFile.getPath());
        }
        BufferedImage loadedImage = haarTransformer.loadImage(theFile);
        return new ImageIcon(loadedImage);
    }

    protected void clearTransformImages(){
        transformPPics.clear();
        transformSPics.clear();
    }

    protected void clearRecoverImages(){
        recoverPPics.clear();
        recoverSPics.clear();
    }

    protected ImageIcon getTransformSPic(int index){
        return transformSPics.get(index);
    }

    protected ImageIcon getTransformPPic(int index){
        return transformPPics.get(index);
    }

    protected ImageIcon getRecoverSPic(int index){
        return recoverSPics.get(index);
    }

    protected ImageIcon getRecoverPPic(int index){
        return recoverPPics.get(index);
    }

    protected int getNumOfTransformSPics(){
        return transformSPics.size();
    }

    protected int getNumOfTransformPPics(){
        return transformPPics.size();
    }

    protected int getNumOfRecoverSPics(){
        return recoverSPics.size();
    }

    protected int getNumOfRecoverPPics(){
        return recoverPPics.size();
    }

    protected void performTransform(boolean generateFiles){
        performTransform(generateFiles, null, null);
    }
    protected void performTransform(boolean generateFiles, SwingWorker worker, JProgressBar progressBar){
        try {
            ArrayList<BufferedImage> transformedImgs =
                    haarTransformer.generateTransformImages(generateFiles, worker, progressBar);
            updateTransformImageIconCollections(transformedImgs);
        }catch(TransformException ex) {
            //Do nothing, SwingWorker parameter handles the cancellation
        }
    }

    private void updateTransformImageIconCollections(ArrayList<BufferedImage> aggregatedImgs){
        for (int count = 0 ; count < aggregatedImgs.size(); count += 2){
            transformSPics.add(new ImageIcon(aggregatedImgs.get(count)));
            transformPPics.add(new ImageIcon(aggregatedImgs.get(count+1)));
        }
    }

    protected void performRecover(boolean generateFiles, SwingWorker worker, JProgressBar progressBar){
       try{
           ArrayList<BufferedImage> recoveredImgs =
                   haarTransformer.generateRecoverImages(generateFiles, worker, progressBar);
           updateRecoverImageIconCollections(recoveredImgs);
       }catch(TransformException ex){
           //Do nothing, SwingWorker parameter handles the cancellation
       }
    }

    private void updateRecoverImageIconCollections(ArrayList<BufferedImage> aggregatedImgs){
        for (int count = 0 ; count < aggregatedImgs.size(); count += 2){
            recoverSPics.add(new ImageIcon(aggregatedImgs.get(count)));
            recoverPPics.add(new ImageIcon(aggregatedImgs.get(count+1)));
        }
    }
}