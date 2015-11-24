package HaarTransform;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by Jason on 5/29/2015.
 */
public class HaarTransformTest extends TestCase {

    HaarTransform transformer;
    WritableRaster tempRaster;

    /*public void setUp() throws TransformException {
        super.setUp();
        File tempImageFile = new File("TestImg.bmp");
        BufferedImage tempImage = new BufferedImage(2,2, BufferedImage.TYPE_BYTE_GRAY);
        tempRaster = tempImage.getData().createCompatibleWritableRaster();
        tempRaster.setSample(0,0,0,5);
        tempRaster.setSample(0,1,0,10);
        tempRaster.setSample(1,0,0,10);
        tempRaster.setSample(1,1,0,5);
        tempImage.setData(tempRaster);
        try {
            ImageIO.write(tempImage, "bmp", tempImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        transformer = new HaarTransform();
        transformer.loadImage(tempImageFile);
    }*/

    public void tearDown() throws Exception {

    }

    public void testSetImageFromFile() throws Exception {

    }

    /*public void testTransform() throws Exception {

        //ArrayList<BufferedImage> results = transformer.generateTransformImages(false);

        BufferedImage resultImage = results.get(0);
        Raster resultRaster = resultImage.getData();
        assertEquals(5, resultRaster.getSample(0,0,0), 0.00);
        assertEquals(10, resultRaster.getSample(0,1,0), 0.00);
        assertEquals(10, resultRaster.getSample(1,0,0), 0.00);
        assertEquals(5, resultRaster.getSample(1,1,0), 0.00);

        resultImage = results.get(2);
        resultRaster = resultImage.getData();
        assertEquals(7.5, resultRaster.getSample(0,0,0), 0.00);
        assertEquals(0, resultRaster.getSample(0,1,0), 0.00);
        assertEquals(0, resultRaster.getSample(1,0,0), 0.00);
        assertEquals(-2.5, resultRaster.getSample(1,1,0), 0.00);
    }*/

    public void testCreateImageFromRaster() throws Exception {

    }

    public void testCreatePixelExpansionImageFromRaster() throws Exception {

    }

    public void testCreateImageFilesForAllStages() throws Exception {

    }

    public void testGenerateImageFile() throws Exception {

    }

    public void testRowTransform() throws Exception {

    }

    public void testPerformPixelRowTransformation() throws Exception {

    }

    public void testColumnTransform() throws Exception {

    }

    public void testPerformPixelColumnTransformation() throws Exception {

    }

    public void testRecover() throws Exception {

    }

    public void testRowRecover() throws Exception {

    }

    public void testColumnRecover() throws Exception {

    }
}