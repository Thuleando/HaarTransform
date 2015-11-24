package HaarTransform;

import java.awt.image.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * <p>Class that handles all the Haar transformation and recovery calculations for
 * the HaarDemo front-end class. The Haar Transformation employed for images with dimensions
 * of (2^x, 2^x) is a typical Haar generateTransformImages. Images without equal sides
 * or sides that are not a power of two, are handled by my custom Haar Transform
 * which provides for encoding of the last remaining single pixel that occurs in various
 * stages of the generateTransformImages when the sides are not a power of two.
 *
 * <p>These leftover pixels are handled by adding the previous pixel to the leftover
 * (unencoded/original) pixel and dividing by two. Then subtracting the result of the previous
 * calculation from the leftover pixel and storing the result in the last spot of the transformation
 * area. Recovering these pixels is done by adding two times the stored value for the leftover pixel
 * to the recovered value of the previous pixel.
 *
 * <p>Provides both basic Haar generateTransformImages images and expanded images of the averaged portion of
 * each step to demonstrate the degradation of the image quality.
 *
 * <p>Provides the ability to generate image files for each stage of the transformation.
 * @author Jason Gould
 */
public class HaarTransform {
    private static final int ZERO_INDEX_OFFSET = 1;
    private double[][][] imagePixelArray;
    private String fileExt;
    int numOfBands;
    int numOfRows;
    int numOfColumns;
    int numColumnTransformsNeeded;
    int numRowTransformsNeeded;
    private ColorModel colorModel;
    private Raster originalRaster;

    /**
     * Converts an image file to a three dimensional array where the first
     * dimension equates to a numOfBands in the image, and the other two dimensions form
     * the 2D matrix of pixels
     * @param imageFile The file name of the image to be converted as a String
     */
    public BufferedImage loadImage(File imageFile)throws TransformException{

        try {
            BufferedImage inputImage = ImageIO.read(imageFile);
            if (inputImage == null){
                throw new IOException();
            }
            setFileExt(imageFile);
            setImageAttribs(inputImage);
            imagePixelArray = copyRasterTo3DArray(originalRaster);
            return inputImage;
        }
        catch (IOException ex) {
            throw new TransformException("loadImage: Could not parse\n" + imageFile.getPath() + "\ninto an image.\n");
        }
    }

    private void setFileExt(File imageFile){
        int indexOfExt = imageFile.getName().lastIndexOf('.')+1;
        fileExt = imageFile.getName().substring(indexOfExt);
    }

    private void setImageAttribs(BufferedImage image){
        colorModel = image.getColorModel();
        originalRaster = image.getData();
        numOfBands = image.getSampleModel().getNumBands();
        numOfRows = image.getHeight();
        numOfColumns = image.getWidth();
        numColumnTransformsNeeded = calcTransformsNeeded(numOfRows);
        numRowTransformsNeeded = calcTransformsNeeded(numOfColumns);
    }

    private int calcTransformsNeeded(int size){
        int numOfTransformsNeeded = 0;
        while(size > 1) {
            numOfTransformsNeeded++;
            size/=2;
        }
        return numOfTransformsNeeded;
    }

    private double[][][] copyRasterTo3DArray(Raster inputRaster){
        int columns = inputRaster.getWidth();
        int rows = inputRaster.getHeight();
        int bands = inputRaster.getNumBands();
        double[][][] tempArray = new double[numOfColumns][numOfRows][numOfBands];

        for(int bandCount = 0; bandCount < bands; bandCount++) {
            for (int columnCount = 0; columnCount < columns; columnCount++) {
                for ( int rowCount = 0; rowCount < rows; rowCount++){
                    tempArray[columnCount][rowCount][bandCount] =
                            inputRaster.getSample(columnCount, rowCount, bandCount);
                }
            }
        }
        return tempArray;
    }
    ////////////////////////////////////////////////////////////
    //Transform methods
    ////////////////////////////////////////////////////////////

    /**
     * Transforms the last image that has been converted to a 3D array by this object
     * Determines how many row and column transforms are needed to generateTransformImages the image
     * via Haar transformation until one averaged pixel remains and the rest of
     * the image is coefficients from the transformation given the image size.
     * Then calls the performRowTransform and performColumnTransform methods the requisite number
     * of times to restore the image. Also calls generateImage and convert
     * array2ImageExpansion after each transformation phase.
     //* @param imgList ArrayList to store the images from the generateTransformImages
     * @param genFiles Flag to control the generation of files for the generateTransformImages stages
     */
    public ArrayList<BufferedImage> generateTransformImages(boolean genFiles) throws TransformException{
        return generateTransformImages(genFiles, null, null);
    }

    public ArrayList<BufferedImage> generateTransformImages(boolean genFiles, SwingWorker worker) throws TransformException {
        return generateRecoverImages(genFiles, worker, null);
    }

    public ArrayList<BufferedImage> generateTransformImages(boolean genFiles, SwingWorker worker, JProgressBar progressBar)
            throws TransformException{
        ArrayList<BufferedImage> resultImages = new ArrayList<>();
        int transformsPerformed = 0;
        int progressIncrementStep = (100/((numColumnTransformsNeeded+numRowTransformsNeeded)/2))/4;
        int progress = 0;
        //Add original images to the results
        resultImages.add(generateImage());
        updateProgress(worker, progressBar, progress+= (progressIncrementStep/2));
        resultImages.add(generatePixelExpansionImage(transformsPerformed, transformsPerformed));
        updateProgress(worker, progressBar, progress+= (progressIncrementStep/2));

        while ( transformsPerformed < numRowTransformsNeeded || transformsPerformed < numColumnTransformsNeeded) {
            if (transformsPerformed < numRowTransformsNeeded){
                performRowTransform(transformsPerformed);
            }
            updateProgress(worker, progressBar, progress+= progressIncrementStep);
            if (transformsPerformed < numColumnTransformsNeeded){
                performColumnTransform(transformsPerformed);
            }
            updateProgress(worker, progressBar, progress+= progressIncrementStep);

            transformsPerformed++;

            resultImages.add(generateImage());
            updateProgress(worker, progressBar, progress+= progressIncrementStep);
            resultImages.add(generatePixelExpansionImage(transformsPerformed, transformsPerformed));
            updateProgress(worker, progressBar, progress+= progressIncrementStep);
        }

        if(genFiles) {
            createImageFilesForAllStages(resultImages, "Transform");
        }
        return resultImages;
    }

    public BufferedImage generateImage(){
        WritableRaster outputRaster = copy3DArrayToRaster(imagePixelArray);
        BufferedImage image = new BufferedImage(colorModel, outputRaster, false, null);

        try{
            ImageIO.write(image, fileExt, new File("Temp."+ fileExt));
            image = (ImageIO.read(new File("Temp."+ fileExt)));
            Files.deleteIfExists(new File("Temp." + fileExt).toPath());
        } catch (IOException ex) {
            Logger.getLogger(HaarTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

    private WritableRaster copy3DArrayToRaster( double[][][] sourceArray){
        int columns = sourceArray.length;
        int rows = sourceArray[0].length;
        int bands = sourceArray[0][0].length;
        WritableRaster tempRaster = originalRaster.createCompatibleWritableRaster();

        for(int bandCount = 0; bandCount < bands; bandCount++) {
            for (int columnCount = 0; columnCount < columns; columnCount++) {
                for ( int rowCount = 0; rowCount < rows; rowCount++){
                     tempRaster.setSample(columnCount, rowCount, bandCount,
                             sourceArray[columnCount][rowCount][bandCount]);
                }
            }
        }
        return tempRaster;
    }

    public BufferedImage generatePixelExpansionImage(int rowTransformsDone, int columnTransformsDone){
        double[][][] result3DArray = calculatePixelExpansion(rowTransformsDone, columnTransformsDone);
        WritableRaster outputRaster = copy3DArrayToRaster(result3DArray);
        BufferedImage image = new BufferedImage(colorModel, outputRaster, false, null);
        try{
            ImageIO.write(image, fileExt, new File("Temp." + fileExt));
            image = ImageIO.read(new File("Temp." + fileExt));
            Files.deleteIfExists(new File("Temp." + fileExt).toPath());
        } catch (IOException ex) {
            Logger.getLogger(HaarTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

    private double[][][] calculatePixelExpansion(int rowTransformsDone, int columnTransformsDone){
        int columnScalingFactor = (int)Math.pow(2,rowTransformsDone);
        int rowScalingFactor = (int)Math.pow(2,columnTransformsDone);
        int numOfSubImageRows = numOfRows /rowScalingFactor;
        int numOfSubImageColumns = numOfColumns /columnScalingFactor;
        double[][][] result3DArray = new double[numOfColumns][numOfRows][numOfBands];

        for(int bandIndex = 0; bandIndex < numOfBands; bandIndex++){
            for(int currSubImageColumn = numOfSubImageColumns-ZERO_INDEX_OFFSET; currSubImageColumn >= 0; currSubImageColumn--){
                for(int currSubImageRow = numOfSubImageRows-ZERO_INDEX_OFFSET; currSubImageRow >= 0; currSubImageRow--){
                    for(int currColumnOffset = 0; currColumnOffset < columnScalingFactor; currColumnOffset++){
                        for (int currRowOffset = 0; currRowOffset < rowScalingFactor; currRowOffset++){
                            result3DArray[(currSubImageColumn * columnScalingFactor) + currColumnOffset]
                                    [(currSubImageRow * rowScalingFactor) + currRowOffset][bandIndex] =
                                    imagePixelArray[currSubImageColumn][currSubImageRow][bandIndex];
                        }
                    }
                }
            }
        }
        return result3DArray;
    }

    protected void createImageFilesForAllStages(ArrayList<BufferedImage> images, String processThatCreatedImage){
        File stageFile, expansionFile;
        int numOfImageInSequence = 0;

        for(BufferedImage image: images) {
            if (isStandardImage(image, images)) {
                stageFile = new File(processThatCreatedImage + "_" + numOfImageInSequence + "." + fileExt);
                generateImageFile(stageFile, image);
            }
            else {
                expansionFile = new File(processThatCreatedImage + "PE_" + numOfImageInSequence + "." + fileExt);
                generateImageFile(expansionFile, image);
                numOfImageInSequence++;
            }

        }
    }

    private boolean isStandardImage( BufferedImage image, ArrayList<BufferedImage> images){
        //Images are stored in the ArrayList first Standard, then Expansion starting at index 0
        return images.indexOf(image)%2 == 0;
    }

    protected void generateImageFile(File file, BufferedImage image) {
        try {
            ImageIO.write(image, fileExt, file);
        } catch (IOException ex) {
            Logger.getLogger(HaarTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateProgress(SwingWorker worker, JProgressBar progressBar, int progress) throws TransformException{
        if (worker!= null){
            if (worker.isCancelled()){
                resetImage();
                throw new TransformException("Transform or Recover Cancelled");
            }
        }

        if (progressBar != null){
            progressBar.setValue(progress);
        }
    }

    public void performRowTransform(int transformsDone){
        double[][][] result3DArray = new double[numOfColumns][numOfRows][numOfBands];
        int subImageMaxRow = numOfRows / (int)Math.pow(2, transformsDone);
        subImageMaxRow = (subImageMaxRow > 0) ? subImageMaxRow : 1;
        int subImageMaxColumn = numOfColumns / (int)Math.pow(2, transformsDone);
        subImageMaxColumn = (subImageMaxColumn > 0 ) ? subImageMaxColumn: 1;

        for (int bandIndex = 0; bandIndex < numOfBands; bandIndex++){
            for (int currRow = 0; currRow < numOfRows; currRow++){
                for (int currColumn =0, resultColumn =0; currColumn < numOfColumns; currColumn++, resultColumn++) {
                    if (currColumn != subImageMaxColumn - ZERO_INDEX_OFFSET) {
                        performPixelRowTransformation(result3DArray, subImageMaxColumn, subImageMaxRow,
                                currRow, currColumn, bandIndex, resultColumn);
                        currColumn++;
                    } else {
                        performSpecialPixelRowTransformation(result3DArray, currRow, currColumn, bandIndex);
                    }
                }
            }
        }
        imagePixelArray = result3DArray;
    }

    private void performPixelRowTransformation(double[][][] result3DArray, int subImageMaxColumn,
                                   int subImageMaxRow, int currRow, int currColumn, int bandIndex, int resultColumn){
        double averagedResult, differenceResult;
        if (rowPixelNeedsTransformed(currRow, subImageMaxRow, currColumn, subImageMaxColumn)){
            averagedResult = (imagePixelArray[currColumn][currRow][bandIndex]
                    + imagePixelArray[currColumn+1][currRow][bandIndex]) / 2.0;
            differenceResult = imagePixelArray[currColumn][currRow][bandIndex] - averagedResult;
            result3DArray[resultColumn][currRow][bandIndex] = averagedResult;
            result3DArray[(subImageMaxColumn/2)+ resultColumn][currRow][bandIndex] = differenceResult;
        }
        else{
            result3DArray[currColumn][currRow][bandIndex] = imagePixelArray[currColumn][currRow][bandIndex];
            if (currColumn+1 < numOfColumns) {
                result3DArray[currColumn+1][currRow][bandIndex] = imagePixelArray[currColumn+1][currRow][bandIndex];
            }
        }
    }

    private boolean rowPixelNeedsTransformed(int currRow, int subImageMaxRow, int currColumn, int subImageMaxColumn){
        return currColumn < subImageMaxColumn && currRow < subImageMaxRow;
    }

    private void performSpecialPixelRowTransformation(double[][][] result3DArray, int currRow, int currColumn,
                                                      int bandIndex){
        double averagedResult, differenceResult;

        averagedResult = (imagePixelArray[currColumn][currRow][bandIndex] +
                          imagePixelArray[currColumn-1][currRow][bandIndex])/2;
        differenceResult = imagePixelArray[currColumn][currRow][bandIndex] - averagedResult;
        //Skips adding the avg pixel value, making the next generateTransformImages on an even size vector
        result3DArray[currColumn][currRow][bandIndex]= differenceResult;
    }

    public void performColumnTransform(int transformsDone) {
        double[][][] result3DArray = new double[numOfColumns][numOfRows][numOfBands];
        int subImageMaxRow = numOfRows / (int) Math.pow(2, transformsDone);
        subImageMaxRow = (subImageMaxRow > 0) ? subImageMaxRow : 1;
        int subImageMaxColumn = numOfColumns / (int) Math.pow(2, transformsDone);
        subImageMaxColumn = (subImageMaxColumn > 0) ? subImageMaxColumn : 1;

        for (int bandIndex = 0; bandIndex < numOfBands; bandIndex++) {
            for (int currColumn = 0; currColumn < numOfColumns; currColumn++) {
                for (int currRow = 0, resultRow = 0; currRow < numOfRows; currRow++, resultRow++) {
                    if (currRow != subImageMaxRow - ZERO_INDEX_OFFSET) {
                        performPixelColumnTransformation(result3DArray, subImageMaxColumn, subImageMaxRow,
                                currRow, currColumn, bandIndex, resultRow);
                        currRow++;
                    } else {
                        performSpecialPixelColumnTransformation(result3DArray, currRow, currColumn, bandIndex);
                    }
                }
            }
        }
        imagePixelArray = result3DArray;
    }

    private void performPixelColumnTransformation(double[][][] result3DArray, int subImageMaxColumn,
                                 int subImageMaxRow, int currRow, int currColumn, int bandIndex, int resultRow) {
        double averagedResult, differenceResult;
        if (columnPixelNeedsTransformed(currRow, subImageMaxRow, currColumn, subImageMaxColumn)) {
            averagedResult = (imagePixelArray[currColumn][currRow][bandIndex]
                            + imagePixelArray[currColumn][currRow+1][bandIndex]) / 2.0;
            differenceResult = imagePixelArray[currColumn][currRow][bandIndex] - averagedResult;

            result3DArray[currColumn][resultRow][bandIndex] = averagedResult;
            result3DArray[currColumn][(subImageMaxRow/2) + resultRow][bandIndex] = differenceResult;
        } else {
            result3DArray[currColumn][currRow][bandIndex] = imagePixelArray[currColumn][currRow][bandIndex];
            if (currRow + 1 < numOfRows) {
                result3DArray[currColumn][currRow+1][bandIndex] = imagePixelArray[currColumn][currRow+1][bandIndex];
            }
        }
    }

    private void performSpecialPixelColumnTransformation(double[][][] result3DArray, int currRow, int currColumn,
                                                         int bandIndex){
        double averagedResult, differenceResult;

        averagedResult = (imagePixelArray[currColumn][currRow][bandIndex]
                        + imagePixelArray[currColumn][currRow-1][bandIndex])/2;
        differenceResult = imagePixelArray[currColumn][currRow][bandIndex] - averagedResult;
        //Skips adding the avg pixel value, making the next generateTransformImages on an even size vector
        result3DArray[currColumn][currRow][bandIndex] = differenceResult;
    }

    private boolean columnPixelNeedsTransformed(int currRow, int subImageMaxRow, int currColumn, int subImageMaxColumn){
        return currColumn < subImageMaxColumn && currRow < subImageMaxRow;
    }

    ///////////////////////////////////////////////////////////////////
    // Recover methods
    ///////////////////////////////////////////////////////////////////

    /**
     * Recovers the last image that has undergone an Haar Transform by this object.
     * Determines how many row and column transforms are needed to generateRecoverImages the image
     * if the image has been reduced to one averaged pixel by Haar transformation
     * from the original image given the image size. Then calls the performColumnRecover
     * and performRowRecover methods the requisite number of times to restore the image.
     * Also calls generateImage and convert array2ImageExpansion after each
     * recovery phase.
     //* @param imgList ArrayList to store the images from the generateTransformImages
     * @param genFiles Flag to control the generation of files for the generateTransformImages stages
     */
    public ArrayList<BufferedImage> generateRecoverImages(boolean genFiles) throws TransformException {
        return generateRecoverImages(genFiles, null, null);
    }

    public ArrayList<BufferedImage> generateRecoverImages(boolean genFiles, SwingWorker worker) throws TransformException {
        return generateRecoverImages(genFiles, worker, null);
    }

    public ArrayList<BufferedImage> generateRecoverImages(boolean genFiles, JProgressBar progressBar) throws TransformException {
        return generateRecoverImages(genFiles, null, progressBar);
    }

    public ArrayList<BufferedImage> generateRecoverImages(boolean genFiles, SwingWorker worker, JProgressBar progressBar)
            throws TransformException{
        ArrayList<BufferedImage> resultImages = new ArrayList<>();
        int columnRecoversNeeded = numColumnTransformsNeeded;
        int rowRecoversNeeded = numRowTransformsNeeded;
        int progressIncrementStep = (100/((numColumnTransformsNeeded+numRowTransformsNeeded)/2))/4;
        int progress = 0;

        resultImages.add(generateImage());
        updateProgress(worker, progressBar, progress += (progressIncrementStep / 2));
        resultImages.add(generatePixelExpansionImage(rowRecoversNeeded, columnRecoversNeeded));
        updateProgress(worker, progressBar, progress+= (progressIncrementStep/2));

        for  (boolean columnRecoverPerformed = false, rowRecoverPerformed = false;
              rowRecoversNeeded > 0 || columnRecoversNeeded > 0;
              columnRecoverPerformed = false, rowRecoverPerformed = false) {
            if (columnRecoversNeeded > 0 && columnRecoversNeeded >= rowRecoversNeeded){
                if(columnRecoversNeeded > rowRecoversNeeded) {
                    performColumnRecover(columnRecoversNeeded, rowRecoversNeeded + 1);
                    columnRecoverPerformed = true;
                }
                else{
                    performColumnRecover(columnRecoversNeeded, rowRecoversNeeded);
                    columnRecoverPerformed = true;
                }
            }
            updateProgress(worker, progressBar, progress+= progressIncrementStep);
            if (rowRecoversNeeded > 0 && rowRecoversNeeded >= columnRecoversNeeded){
                if(rowRecoversNeeded > columnRecoversNeeded) {
                    performRowRecover(rowRecoversNeeded, columnRecoversNeeded + 1);
                    rowRecoverPerformed = true;
                }
                else{
                    performRowRecover(rowRecoversNeeded, columnRecoversNeeded);
                    rowRecoverPerformed = true;
                }
            }
            updateProgress(worker, progressBar, progress+= progressIncrementStep);

            if(columnRecoverPerformed){
                columnRecoversNeeded--;
            }

            if(rowRecoverPerformed){
                rowRecoversNeeded--;
            }

            resultImages.add(generateImage());
            updateProgress(worker, progressBar, progress+= progressIncrementStep);
            resultImages.add(generatePixelExpansionImage(rowRecoversNeeded, columnRecoversNeeded));
            updateProgress(worker, progressBar, progress+= progressIncrementStep);
        }

        if(genFiles) {
            createImageFilesForAllStages(resultImages, "Recover");
        }
        return resultImages;
    }

    /**
     * Performs the Haar recovery functions on the designated rows of the section of the transformed image
     * @param rowRecoversRemaining The number of recovery steps left to perform on the rows of the image
     * @param columnRecoversRemaining The number of recovery steps left to perform on the columns of the image
     //* @param even Flag indicating if the current length of the row segment to be
     * transformed is even.
     */
    public void performRowRecover(int rowRecoversRemaining, int columnRecoversRemaining){
        rowRecoversRemaining--;
        columnRecoversRemaining--;
        double[][][] result3DArray = new double[numOfColumns][numOfRows][numOfBands];
        int subImageMaxRow = numOfRows / (int)Math.pow(2, columnRecoversRemaining);
        subImageMaxRow = (subImageMaxRow > 0) ? subImageMaxRow: 1;
        int subImageMaxColumn = numOfColumns / (int)Math.pow(2, rowRecoversRemaining);
        subImageMaxColumn = (subImageMaxColumn > 0) ? subImageMaxColumn: 1;

        //perform row generateRecoverImages transformation
        for (int bandIndex = 0; bandIndex < numOfBands; bandIndex++){
            for (int currRow = 0; currRow < numOfRows; currRow++){
                for (int currColumn = 0, resultColumn = 0; currColumn < numOfColumns; currColumn++, resultColumn++) {
                    if (currColumn != subImageMaxColumn - ZERO_INDEX_OFFSET) {
                        performPixelRowRecover(result3DArray, subImageMaxColumn, subImageMaxRow, currRow,
                                currColumn, bandIndex, resultColumn);
                        currColumn++;
                    } else {
                        performSpecialPixelRowRecover(result3DArray, currRow, currColumn, bandIndex);
                    }
                }
            }
        }
        imagePixelArray = result3DArray;
    }

    private void performPixelRowRecover(double[][][] result3DArray, int subImageMaxColumn, int subImageMaxRow,
                                                  int currRow, int currColumn, int bandIndex, int resultColumn) {
        double firstPixelResult, secondPixelResult;

        if (rowPixelNeedsRecovered(currRow, subImageMaxRow, currColumn, subImageMaxColumn)){
            firstPixelResult = imagePixelArray[resultColumn][currRow][bandIndex]
                             + imagePixelArray[(subImageMaxColumn/2)+ resultColumn][currRow][bandIndex];
            secondPixelResult = imagePixelArray[resultColumn][currRow][bandIndex]
                             - imagePixelArray[(subImageMaxColumn/2)+ resultColumn][currRow][bandIndex];
            result3DArray[currColumn][currRow][bandIndex] = firstPixelResult;
            result3DArray[currColumn+1][currRow][bandIndex] = secondPixelResult;
        }
        else{
            result3DArray[currColumn][currRow][bandIndex] = imagePixelArray[currColumn][currRow][bandIndex];
            if (currColumn+1 < numOfColumns) {
                result3DArray[currColumn+1][currRow][bandIndex] = imagePixelArray[currColumn+1][currRow][bandIndex];
            }
        }
    }

    private void performSpecialPixelRowRecover(double[][][] temp3DArray, int currRow, int currColumn, int bandIndex){
        double pixelResult;

        pixelResult = temp3DArray[currColumn-1][currRow][bandIndex]
                    + (2 * imagePixelArray[currColumn][currRow][bandIndex]);
        temp3DArray[currColumn][currRow][bandIndex] = pixelResult;
    }

    private boolean rowPixelNeedsRecovered(int currRow, int subImageMaxRow, int currColumn, int subImageMaxColumn){
        return currColumn < subImageMaxColumn && currRow < subImageMaxRow;
    }

    /**
     * Performs the Haar recovery functions on the columns of the designated section of the transformed image
     * @param rowRecoversRemaining The number of recovery steps left to perform on the rows of the image
     * @param columnRecoversRemaining The number of recovery steps left to perform on the columns of the image
     //* @param even Flag indicating if the current length of the column segment to be
     * transformed is even.
     */
    public void performColumnRecover(int columnRecoversRemaining, int rowRecoversRemaining) {
        rowRecoversRemaining--;
        columnRecoversRemaining--;
        double[][][] result3DArray = new double[numOfColumns][numOfRows][numOfBands];
        int subImageMaxRow = numOfRows / (int)Math.pow(2, columnRecoversRemaining);
        subImageMaxRow = (subImageMaxRow > 0) ? subImageMaxRow: 1;
        int subImageMaxColumn = numOfColumns / (int)Math.pow(2, rowRecoversRemaining);
        subImageMaxColumn = (subImageMaxColumn > 0) ? subImageMaxColumn: 1;

        //perform column generateTransformImages
        for (int bandIndex = 0; bandIndex < numOfBands; bandIndex++) {
            for (int currColumn = 0; currColumn < numOfColumns; currColumn++) {
                for (int currRow = 0, resultRow = 0; currRow < numOfRows; currRow++, resultRow++) {
                    if (currRow != subImageMaxRow - ZERO_INDEX_OFFSET) {
                        performPixelColumnRecover(result3DArray, subImageMaxColumn, subImageMaxRow,
                                currRow, currColumn, bandIndex, resultRow);
                        currRow++;
                    } else {
                        performSpecialPixelColumnRecover(result3DArray, currRow, currColumn, bandIndex);
                    }
                }
            }
        }
        imagePixelArray = result3DArray;
    }

    private void performPixelColumnRecover(double[][][] result3DArray, int subImageMaxColumn, int subImageMaxRow,
                                        int currRow, int currColumn, int bandIndex, int resultRow) {
        double firstPixelResult, secondPixelResult;

        if (columnPixelNeedsRecovered(currRow, subImageMaxRow, currColumn, subImageMaxColumn)){
            firstPixelResult = imagePixelArray[currColumn][resultRow][bandIndex]
                    + imagePixelArray[currColumn][(subImageMaxRow/2)+ resultRow][bandIndex];
            secondPixelResult = imagePixelArray[currColumn][resultRow][bandIndex]
                    - imagePixelArray[currColumn][(subImageMaxRow/2)+ resultRow][bandIndex];
            result3DArray[currColumn][currRow][bandIndex] = firstPixelResult;
            result3DArray[currColumn][currRow + 1][bandIndex] = secondPixelResult;
        }
        else{
            result3DArray[currColumn][currRow][bandIndex] = imagePixelArray[currColumn][currRow][bandIndex];
            if (currRow+1 < numOfRows) {
                result3DArray[currColumn][currRow+1][bandIndex] = imagePixelArray[currColumn][currRow+1][bandIndex];
            }
        }
    }

    private void performSpecialPixelColumnRecover(double [][][] result3DArray, int currRow, int currColumn,
                                                  int bandIndex){
        double pixelResult;

        pixelResult = result3DArray[currColumn][currRow-1][bandIndex]
                + (2 * imagePixelArray[currColumn][currRow][bandIndex]);
        result3DArray[currColumn][currRow][bandIndex] = pixelResult;
    }

    private boolean columnPixelNeedsRecovered(int currRow, int subImageMaxRow, int currColumn, int subImageMaxColumn){
        return currColumn < subImageMaxColumn && currRow < subImageMaxRow;

    }

    public void resetImage(){
        imagePixelArray = copyRasterTo3DArray(originalRaster);
    }

}

class TransformException extends Throwable{
    public TransformException(String message){
        super(message);
    }

    @Override
    public String getMessage(){
        String message = "ERROR in HaarTransform." + super.getMessage();
        return message;
    }
}

