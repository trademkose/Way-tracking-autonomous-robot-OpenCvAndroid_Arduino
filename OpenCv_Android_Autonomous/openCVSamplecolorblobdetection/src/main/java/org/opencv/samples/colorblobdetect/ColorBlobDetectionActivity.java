package org.opencv.samples.colorblobdetect;
import java.util.Collections;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.*;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private boolean              IsMazeSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba,mBlobColorRgba_car_front,mBlobColorRgba_car_back,mBlobColorRgba_maze;
    private Scalar               mBlobColorHsv,mBlobColorHsv_car_front,mBlobColorHsv_car_back,mBlobColorHsv_maze;
    private ColorBlobDetector    mDetector,mDetector_car_front,mDetector_car_back,mDetector_maze;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

    private ImageView btnLeft = null;
    private ImageView btnRight = null;
    private ImageView btnTop = null;
    private ImageView btnDown = null;
    private ImageView connect =null;
    private ImageView light =null;
    private CameraBridgeViewBase mOpenCvCameraView;
    int countt=0,step_count=0;;
    private Socket client;
    private PrintWriter printwriter;

    List<Point> userPath = new ArrayList<Point>();

    TextView textViewPoint ;
    String ip="",port="",sendServerMessage="";
    private String lightCase="close";
    private int count=0,transfer=0;
    float gradientCar =0;
    double gradientMaze =0;
    double average=0;
    int touched_counter=0;

    String buffer=new String();
    String FILE_NAME = "InternalString";
    FileOutputStream fos;
    FileInputStream fis;
    private static final String kullanici = "kullanici";

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
         Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {


        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        Toast.makeText(getBaseContext(), "Please connect to server .", Toast.LENGTH_LONG).show();


        connect = (ImageView)findViewById(R.id.connect);
        btnLeft = (ImageView)findViewById(R.id.btnLeft);
        btnTop = (ImageView)findViewById(R.id.btnTop);
        btnDown = (ImageView)findViewById(R.id.btnDown);
        btnRight = (ImageView)findViewById(R.id.btnRight);

        connect.setOnClickListener(new View.OnClickListener() {

             @Override
            public void onClick(View v) {
                // Toast.makeText(getBaseContext(), "----", Toast.LENGTH_LONG).show();
               Intent intent = new Intent(ColorBlobDetectionActivity.this, serverConnect.class);
               startActivity(intent);
            }
        });

        light = (ImageView) findViewById(R.id.light);
        light.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                ImageView iv = (ImageView) v;
                if (count % 2 == 0) {
                    lightCase = "open";
                } else {
                    lightCase = "close";
                }
                if (lightCase == "open") {
                    iv.setImageResource(R.drawable.ledopen);
                    Toast.makeText(getBaseContext(),"Light case : On", Toast.LENGTH_LONG).show();

                    sendServerMessage="c";
                    SendMessage sendMessageTask = new SendMessage();
                    sendMessageTask.execute();
                }
                if (lightCase == "close") {
                    iv.setImageResource(R.drawable.ledclose);
                    Toast.makeText(getBaseContext(),"Light case : Off", Toast.LENGTH_LONG).show();
                }
                count++;
            }
        });

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            ip = extras.getString("ipName");
            port=extras.getString("portName");
            connect.setImageResource(R.drawable.yes);

            transfer=1;
        }

        if(ip.equals("setting")) {
            try {
                fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                Log.i(TAG, "tikla dosya olusturuldu");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if(transfer==1) {
            btnLeft.setImageResource(R.drawable.left);
            btnTop.setImageResource(R.drawable.top);
            btnRight.setImageResource(R.drawable.right);
            btnDown.setImageResource(R.drawable.down);
            light.setImageResource(R.drawable.ledclose);
        }

        OnTouchListener left=new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    iv.setImageResource(R.drawable.leftred);
                    sendServerMessage="e";                       //LEFT
                    SendMessage sendMessageTask = new SendMessage();
                    sendMessageTask.execute();
                    //Toast.makeText(getBaseContext(), "Push to Left Button touch", Toast.LENGTH_LONG).show();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    iv.setImageResource(R.drawable.left);
                    return true;
                }
                return false;
            }
        };
        btnLeft.setOnTouchListener(left);


        OnTouchListener right=new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    iv.setImageResource(R.drawable.rightred);

                    sendServerMessage="q";                          //RIGHT
                    SendMessage sendMessageTask = new SendMessage();
                    sendMessageTask.execute();

                    //Toast.makeText(getBaseContext(), "Push to Right Button", Toast.LENGTH_LONG).show();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    iv.setImageResource(R.drawable.right);
                    return true;
                }

                return false;
            }
        };
        btnRight.setOnTouchListener(right);

        OnTouchListener top=new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    iv.setImageResource(R.drawable.topred);
                    //Toast.makeText(getBaseContext(), "Push to Top Button", Toast.LENGTH_LONG).show();

                    sendServerMessage="w";//Top
                    SendMessage sendMessageTask = new SendMessage();
                    sendMessageTask.execute();

                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    iv.setImageResource(R.drawable.top);
                    return true;
                }
                return false;
            }
        };
        btnTop.setOnTouchListener(top);


        OnTouchListener down=new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    iv.setImageResource(R.drawable.downred);
                                                            //DOWN
                    sendServerMessage="s";
                    SendMessage sendMessageTask = new SendMessage();
                    sendMessageTask.execute();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    iv.setImageResource(R.drawable.down);
                    return true;
                }
                return false;
            }
        };
        btnDown.setOnTouchListener(down);

        textViewPoint= (TextView) findViewById(R.id.textViewPoint);
    }

    @Override
    public void onPause () {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mDetector_car_front = new ColorBlobDetector();
        mDetector_car_back = new ColorBlobDetector();
        mDetector_maze = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorRgba_car_front = new Scalar(255);
        mBlobColorRgba_car_back = new Scalar(255);
        mBlobColorRgba_maze = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        mBlobColorHsv_car_back = new Scalar(255);
        mBlobColorHsv_car_front = new Scalar(255);
        mBlobColorHsv_maze = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        Log.i(TAG, "Touched hsv color: (" + mBlobColorHsv.val[0] + ", " + mBlobColorHsv.val[1] +
                ", " + mBlobColorHsv.val[2] + ", " + mBlobColorHsv.val[3] + ")");
        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        touched_counter++;
        //***maze yesil-green -yesil Áizecek**
        Log.i(TAG, "tikla ipmiz "+ip);

        if(!ip.equals("setting")) {
            Log.i(TAG, "tikla setting degil");
            // TODO read to file and go back mainpage
         String data=fileRead();
            Log.i(TAG, "tikla ham veri :"+data);
         char[] charArray = data.toCharArray();
         double []doubleArray=new double[16];
         int tail =0,head=0;
         int indexCount=0;

         for(int i=0;i<data.length();i++){

             if(charArray[i] == '*' && i!=data.length()-1)
             {
                 tail=i;
                 doubleArray[indexCount]=Double.parseDouble(data.substring(head,tail));
                 head=i+1;
                 indexCount++;
             }
         }
            for(int j=0;j<16;j++){
                Log.i(TAG, "tikla double array "+j+" "+doubleArray[j]);
            }
            mBlobColorHsv_maze.val[0] = doubleArray[0];
            mBlobColorHsv_maze.val[1] = doubleArray[1];
            mBlobColorHsv_maze.val[2] = doubleArray[2];
            mBlobColorHsv_maze.val[3] = 0;

            mBlobColorHsv_car_front.val[0] = doubleArray[4];
            mBlobColorHsv_car_front.val[1] = doubleArray[5];
            mBlobColorHsv_car_front.val[2] = doubleArray[6];
            mBlobColorHsv_car_front.val[3] = 0;

            mBlobColorHsv_car_back.val[0] = doubleArray[8];
            mBlobColorHsv_car_back.val[1] = doubleArray[9];
            mBlobColorHsv_car_back.val[2] = doubleArray[10];
            mBlobColorHsv_car_back.val[3] = 0;

            mBlobColorHsv.val[0]=doubleArray[12];
            mBlobColorHsv.val[1]=doubleArray[13];
            mBlobColorHsv.val[2]=doubleArray[14];
            mBlobColorHsv.val[3]=0;

            touched_counter=0;
            mIsColorSelected = true;
            IsMazeSelected=false;
            countt=1;
            step_count=0;
        }

        else {
            Log.i(TAG, "tikla settingdir");

            if (touched_counter == 1) {
                mBlobColorHsv_maze.val[0] = mBlobColorHsv.val[0];
                mBlobColorHsv_maze.val[1] = mBlobColorHsv.val[1];
                mBlobColorHsv_maze.val[2] = mBlobColorHsv.val[2];
                mBlobColorHsv_maze.val[3] = 0;
                Log.i(TAG, "tikla 1");

                Toast.makeText(getBaseContext(), "way color", Toast.LENGTH_SHORT).show();

            }
            //***maze **

            //***car front yellow-beyaz Áizecek   **
            if (touched_counter == 2) {
                mBlobColorHsv_car_front.val[0] = mBlobColorHsv.val[0];
                mBlobColorHsv_car_front.val[1] = mBlobColorHsv.val[1];
                mBlobColorHsv_car_front.val[2] = mBlobColorHsv.val[2];
                mBlobColorHsv_car_front.val[3] = 0;
                Log.i(TAG, "tikla 2");

                Toast.makeText(getBaseContext(), "car front color", Toast.LENGTH_SHORT).show();
            }
            //***car front **

            //***car back blue-yesil Áizecek**
            if (touched_counter == 3) {
                mBlobColorHsv_car_back.val[0] = mBlobColorHsv.val[0];
                mBlobColorHsv_car_back.val[1] = mBlobColorHsv.val[1];
                mBlobColorHsv_car_back.val[2] = mBlobColorHsv.val[2];
                mBlobColorHsv_car_back.val[3] = 0;
                Log.i(TAG, "tikla 3");

                Toast.makeText(getBaseContext(), "car back color", Toast.LENGTH_SHORT).show();
            }
            //***car back **

            //***object red-mavi Áizecek**
            if (touched_counter == 4) {
                //hedef renk zaten secilen renk olmus oluyor
                Log.i(TAG, "tikla 4");

                Toast.makeText(getBaseContext(), "object color", Toast.LENGTH_SHORT).show();
                if (ip.equals("setting")) {
                    // TODO write to file and go back mainpage

                    fileWrite(String.valueOf(mBlobColorHsv_maze.val[0]));
                    fileWrite(String.valueOf(mBlobColorHsv_maze.val[1]));
                    fileWrite(String.valueOf(mBlobColorHsv_maze.val[2]));
                    fileWrite(String.valueOf(mBlobColorHsv_maze.val[3]));

                    fileWrite(String.valueOf(mBlobColorHsv_car_front.val[0]));
                    fileWrite(String.valueOf(mBlobColorHsv_car_front.val[1]));
                    fileWrite(String.valueOf(mBlobColorHsv_car_front.val[2]));
                    fileWrite(String.valueOf(mBlobColorHsv_car_front.val[3]));


                    fileWrite(String.valueOf(mBlobColorHsv_car_back.val[0]));
                    fileWrite(String.valueOf(mBlobColorHsv_car_back.val[1]));
                    fileWrite(String.valueOf(mBlobColorHsv_car_back.val[2]));
                    fileWrite(String.valueOf(mBlobColorHsv_car_back.val[3]));


                    fileWrite(String.valueOf(mBlobColorHsv.val[0]));
                    fileWrite(String.valueOf(mBlobColorHsv.val[1]));
                    fileWrite(String.valueOf(mBlobColorHsv.val[2]));
                    fileWrite(String.valueOf(mBlobColorHsv.val[3]));
                    try {
                        fos.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                    Log.i(TAG, "tikla geri git");
                    Intent intent = new Intent(ColorBlobDetectionActivity.this, serverConnect.class);
                    startActivity(intent);
                }

            }
            //***object red-mavi Áizecek**
        }
        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
        mBlobColorRgba_car_front = converScalarHsv2Rgba(mBlobColorHsv_car_front);
        mBlobColorRgba_car_back = converScalarHsv2Rgba(mBlobColorHsv_car_back);
        mBlobColorRgba_maze = converScalarHsv2Rgba(mBlobColorHsv_maze);

        mDetector.setHsvColor(mBlobColorHsv);
        mDetector_car_front.setHsvColor(mBlobColorHsv_car_front);
        mDetector_car_back.setHsvColor(mBlobColorHsv_car_back);
        mDetector_maze.setHsvColor(mBlobColorHsv_maze);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
        Imgproc.resize(mDetector_car_front.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
        Imgproc.resize(mDetector_car_back.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
        Imgproc.resize(mDetector_maze.getSpectrum(), mSpectrum, SPECTRUM_SIZE);


        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            mDetector_car_back.name="car_back";
            mDetector_car_front.name="car_front";
            mDetector.name="goal";
            mDetector_maze.name="maze";

            mDetector_car_back.process(mRgba);
            mDetector_car_front.process(mRgba);

            if(mDetector_car_front.getContours().size()>0){
                contours.add(mDetector_car_front.getContours().get(0));
            }

            if(mDetector_car_back.getContours().size()>0){
                contours.add(mDetector_car_back.getContours().get(0));
            }
            for(int i=0;i<mDetector_maze.getContours().size();i++){
                contours.add(mDetector_maze.getContours().get(i));
            }


            int X_ball=(int)mDetector.getX();
            int Y_ball=(int)mDetector.getY();
            int X_car_front=(int)mDetector_car_front.getX();
            int Y_car_front=(int)mDetector_car_front.getY();

            int X_car_back=(int)mDetector_car_back.getX();
            int Y_car_back=(int)mDetector_car_back.getY();


            if(IsMazeSelected==false){
                mDetector_maze.process(mRgba);

                if(mDetector_maze.getContours().size()>0){
                    //userPath=mDetector_maze.getContours().get(0).toList();
                    userPath=mDetector_maze.userPath;
                    //Collections.reverse(userPath);
                    List<Point> temp_userPath = new ArrayList<Point>();

                    for(int k=0;k<userPath.size();k++)
                    {
                        temp_userPath.add(userPath.get(k));
                    }
                    userPath.clear();
                    //liste olusturuldu
                    //TODO listeyi s˝rala
                    int min_distance=999999;
                    int i_min_index=0;
                    double goal_y=Y_car_front;
                    double goal_x=X_car_front;
                    double t=0;
                    double z=0;
                    double value=0;
                    int dist=0;

                    while(true) {
                        Log.e(TAG, "liste basla");

                        for (int i = 0; i < temp_userPath.size(); i++) {
                            t = (temp_userPath.get(i).y - goal_y);
                            z = (temp_userPath.get(i).x - goal_x);
                            value = t * t + z * z;
                            dist = (int) (Math.sqrt(value));
                            if (dist < min_distance) {
                                min_distance = dist;
                                i_min_index = i;
                            }
                        }

                        //arabaya en yak˝n nokta indexi : i_min_index
                        userPath.add(temp_userPath.get(i_min_index));

                        goal_y = temp_userPath.get(i_min_index).y;
                        goal_x = temp_userPath.get(i_min_index).x;

                        temp_userPath.remove(i_min_index);
                        i_min_index = 0;
                        min_distance = 999999;
                        Log.e(TAG, "liste sildi");
                        if (temp_userPath.size() == 0) {
                            break;
                        }
                    }
                }
            }
            IsMazeSelected=true;


            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);
            Mat colorLabel2 = mRgba.submat(68, 136, 4, 68);
            colorLabel2.setTo(mBlobColorRgba_car_back);
            Mat colorLabel3 = mRgba.submat(136, 204, 4, 68);
            colorLabel3.setTo(mBlobColorRgba_car_front);
            Mat colorLabel4 = mRgba.submat(204, 272, 4, 68);
            colorLabel4.setTo(mBlobColorRgba_maze);

            Mat colorLabel5 ;


            if(countt<userPath.size()){
                for(int i=0;i<countt;i++){
                    colorLabel5 = mRgba.submat((int)userPath.get(i).y, (int)userPath.get(i).y+4+i*2, (int)userPath.get(i).x, (int)userPath.get(i).x+4+i*2);
                    colorLabel5.setTo(new Scalar(255,255,255,255));

                }
                countt++;
            }else if (countt==userPath.size()){
                for(int i=0;i<countt;i++){
                    colorLabel5 = mRgba.submat((int)userPath.get(i).y, (int)userPath.get(i).y+4+i*2, (int)userPath.get(i).x, (int)userPath.get(i).x+4+i*2);
                    colorLabel5.setTo(new Scalar(255,255,255,255));

                }
            }

            if(step_count==userPath.size())
            {
            }

            if(step_count<userPath.size()){
                int x = (int)userPath.get(step_count).x;
                int y =  (int)userPath.get(step_count).y;
	            int line=(Y_car_back - Y_car_front)*(x - X_car_back)- (y - Y_car_back)*(X_car_back - X_car_front);
                //int line=(Y_car_front - Y_car_back)*(x - X_car_front)- (y - Y_car_front)*(X_car_front - X_car_back);

                int a=(Y_car_back - Y_car_front);
                int b=(X_car_back - X_car_front);
                double sqrt_value=a*a+b*b;
                int distance=Math.abs(line)/(int)(Math.sqrt(sqrt_value));

                Log.i(TAG, " birim line: "+line+"    distance: "+distance+" : step : "+step_count);
                int threshold_value=(int)(mDetector_car_front.getRadius()*0.7);  //on line ? threshold value

                if(line>0 && distance>threshold_value){
                    //1 birim sola  dˆn
                    Log.i(TAG, "1 birim saga  dˆn : Size : "+userPath.size());
                    sendServerMessage="q";                       //RIGHT
                    SendMessage sendMessageTask = new SendMessage();
                    sendMessageTask.execute();


                }else if(line<0 && distance>threshold_value){
                    //1 birim saga  dˆn
                    Log.i(TAG, "1 birim sola  dˆn : Size : "+userPath.size());
                    sendServerMessage="e";                       //LEFT
                    SendMessage sendMessageTask = new SendMessage();
                    sendMessageTask.execute();

                }else{
                    //1 birim ileri git
                    Log.i(TAG, "1 birim ileri : Size : "+userPath.size());
                    sendServerMessage="w";                       //ILERI
                    SendMessage sendMessageTask = new SendMessage();
                    sendMessageTask.execute();
                }
                Log.i(TAG, " birim silecek"+userPath.size());
                //remove the first element
                //step_count++;
                //step artmaas˝ hedef ile arana˝n ˆn¸n¸n merkezinin uzakl˝g˝ belirli bir degerden
                // az oldugu zaman ki
                int t=(y - Y_car_front);
                int z=(x - X_car_front);
                double value=t*t+z*z;
                int dist=(int)(Math.sqrt(value));
                if(dist<(int)mDetector_car_front.getRadius()){
                    step_count++;
                }
                //hedef icin mavi gösterici
                if(step_count<userPath.size()){
                    Mat colorLabel7 = mRgba.submat((int)userPath.get(step_count).y, (int)userPath.get(step_count).y+10, (int)userPath.get(step_count).x, (int)userPath.get(step_count).x+10);
                    colorLabel7.setTo(new Scalar(0,0,255,255));
                }
                if(Y_ball!=0 && X_ball!=0){ // hedef kadraj iÁinde mi?
                    //hedef ile araban˝n en noktas˝ndaki uzakl˝k olculuyor

                    t=(Y_car_front - Y_ball);
                    z=(X_car_front - X_ball);
                    value=t*t+z*z;
                    dist=(int)(Math.sqrt(value));
                    if(dist<(int)(mDetector_car_front.getRadius()*1.5)){
                        step_count=userPath.size();
                        //hedefler dola˛˝lm˝˛ gibi,haraket etmeyi bitir
                        sendServerMessage="b";
                        SendMessage sendMessageTask = new SendMessage();
                        sendMessageTask.execute();
                        //send a singal to machine, it founded an object now
                        return mRgba;
                    }

                }


            }
        }
        return mRgba;
    }
    public void fileWrite(String text) {

        Log.i(TAG, "tikla dosya yazma bası");
        text = (text+ "*" ).toString();

        try {
            fos.write(text.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String fileRead() {

        String readData = null;
        try {
            fis = openFileInput(FILE_NAME);
            byte[] okunanbaytdizisi = new byte[fis.available()];

            while (fis.read(okunanbaytdizisi) != -1) {
                readData = new String(okunanbaytdizisi);
            }

            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readData;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    private class SendMessage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                client = new Socket(ip,Integer.parseInt(port)); // connect to the server
                printwriter = new PrintWriter(client.getOutputStream(), true);
                printwriter.write(sendServerMessage); // write the message to output stream

                printwriter.flush();
                printwriter.close();
                client.close(); // closing the connection

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
