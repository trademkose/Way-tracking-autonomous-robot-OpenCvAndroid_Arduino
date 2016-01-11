package org.opencv.samples.colorblobdetect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.util.Log;
import android.widget.TextView;

public class ColorBlobDetector {
    private static final String  TAG              = "OCVSample::Activity";
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.1;
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(25,50,50,0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    List<Point> userPath = new ArrayList<Point>();
    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
    public double x_object=0.0;
    public double y_object=0.0;
    public double radius=0.0;

    public String name="";
    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }
    public double getX() {
        return x_object;
    }
    public double getY() {
        return y_object;
    }
    public double getRadius() {
        return radius;
    }
    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }

    public void process(Mat rgbaImage) {
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        int i_max_contour=0;
        // Find max contour area
        double maxArea = 0;
        int i=0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);

            if (area > maxArea){
                maxArea = area;
                i_max_contour=i;
            }
            i++;
        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        userPath.clear();
        each = contours.iterator();
        i=0;
        while (each.hasNext()) {

            MatOfPoint contour = each.next();
            // Size a=new Size();
            //Point p=new Point();
            // contour.locateROI(a,p);

            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);

                if(i==i_max_contour && name!="maze")
                {
                    int x=0;
                    int y=0;
                    Rect rect = Imgproc.boundingRect(contour);
                    x=rect.x;
                    y=rect.y;
                    Point nokta=new Point();
                    //Point nokta2=new Point();
                    // nokta2.x=200;
                    // nokta2.y=100;
                    nokta.x=x;
                    nokta.y=y;
                    nokta.x=nokta.x+rect.width/2;
                    nokta.y=nokta.y+rect.height/2;
                    radius=rect.height/2;
                    if(name=="car_back"){
                        Core.circle(rgbaImage,nokta,rect.height/2, new Scalar(255,0,0,0),4);
                    }
                    if(name=="car_front"){
                        Core.circle(rgbaImage,nokta,rect.height/2, new Scalar(255,0,0,0),4);
                    }
                    if(name=="goal"){
                        Core.circle(rgbaImage,nokta,rect.height/2, new Scalar(255,255,255,0),4);

                    }

                    // Core.circle(rgbaImage,nokta2,50, new Scalar(0,255,0,0),4);
                    x_object=nokta.x;
                    y_object=nokta.y;
                    mContours.add(contour);
                }

                if(name=="maze"){
                    int x=0;
                    int y=0;
                    Rect rect = Imgproc.boundingRect(contour);
                    x=rect.x;
                    y=rect.y;
                    Point nokta=new Point();
                    nokta.x=x;
                    nokta.y=y;
                    nokta.x=nokta.x+rect.width/2;
                    nokta.y=nokta.y+rect.height/2;
                    radius=rect.height/2;
                    // Imgproc.circle(rgbaImage,nokta,5, new Scalar(255,255,255,255),4);
                    userPath.add(nokta);//merkezler ekleniyor yolun
                    // Core.circle(rgbaImage,nokta2,50, new Scalar(0,255,0,0),4);
                    Log.i("deger", "Ihope" +nokta.x+":"+nokta.y);
                    x_object=nokta.x;
                    y_object=nokta.y;
                    mContours.add(contour);

                }
            }
            i++;
        }
    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }
}
