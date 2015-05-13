package com.md.art;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.md.art.pojo.Movie;
import com.md.art.sysmon.SystemMonitorAPI;
import com.md.art.utils.ImageUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.opencv.imgproc.Imgproc.*;

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private final String TAG="art-log";
    private static final int cameraW=640;
    private static final int cameraH=480;
    private boolean isDebug=false;
    private boolean isLoop=true;
    private boolean isRecognized=false;
    private CameraBridgeViewBase mOpenCvCameraView;
    private AssetManager am;
    private Bitmap quadbmp;
    private List<String> hashes;
    private String serverIP;

    private Point item_center;
    private int threshold;
    private float alpha;

    private Mat rgba;

    private List<Boolean> switches;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraCapture);

        //TODO: getSupportedPreviewSizes(), setPreviewSize(), setParameters()
        //mOpenCvCameraView.setMaxFrameSize(cameraW,cameraH);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        am=getAssets();

        //TODO: this is for TESTS ONLY
        List<Bitmap> bmp=new ArrayList<>();
        hashes=new ArrayList<>();
        /*try {
            for (int i=1;i<=8;i++) {
                bmp.add(BitmapFactory.decodeStream(am.open(i+".jpg")));
                hashes.add(ImageUtils.buildHash(bmp.get(i - 1)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        switches=new ArrayList<>();
        itsMeSwitches();

        quadbmp=Bitmap.createBitmap(8,8, Bitmap.Config.ARGB_8888);

        final Handler handler = new Handler();
        final SystemMonitorAPI sm = new SystemMonitorAPI(MainActivity.this);

        Timer timer=new Timer();
        TimerTask doSysMon=new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SystemMonitor sysmon = new SystemMonitor();
                            sysmon.execute(sm);
                        }catch (Exception e){Log.e(TAG,e.getMessage());}
                    }
                });
            }
        };
        //TODO: option to disable timer
        timer.schedule(doSysMon, 0, 1000);
        //TODO:PreferencesActivity
        //serverIP="http://77.122.192.15/";
        serverIP="http://31.133.81.17/";
        item_center=new Point(0,0);

        threshold=70;

        //debugging
        SeekBar sb=(SeekBar)findViewById(R.id.seekBar);
        sb.setOnSeekBarChangeListener(l);
    }

    private SeekBar.OnSeekBarChangeListener l= new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            alpha=progress/100.0f;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    private void itsMeSwitches()
    {
        switches.clear();
        for (int i=1;i<=4;i++) {
            Switch sw=(Switch)findViewById(getResources().getIdentifier("switch" + i, "id", getPackageName()));
            switches.add(sw.isChecked());
        }
    }



    //TODO: move all math into separate class
   /* private Point computeIntersect(double[] a,double[] b)
    {
        double x1 = a[0], y1 = a[1], x2 = a[2], y2 = a[3], x3 = b[0], y3 = b[1], x4 = b[2], y4 = b[3];
        double d = ((float)(x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));
        if (d!=0)
        {
            Point pt = null;
            pt.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
            pt.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
            return pt;
        }
        else
        return new Point(-1, -1);
    }
*/
    private List<Point> sortCorners(List<Point> corners, Point center)
    {
        List<Point> top=new ArrayList<>(), bot=new ArrayList<>();
        for (int i = 0; i < corners.size(); i++)
        {
            if (corners.get(i).y < center.y)
                top.add(corners.get(i));
            else
                bot.add(corners.get(i));
        }
        List<Point> nc=new ArrayList<>();

            Point tl = top.get(0).x > top.get(1).x ? top.get(1) : top.get(0);
            Point tr = top.get(0).x > top.get(1).x ? top.get(0) : top.get(1);
            Point bl = bot.get(0).x > bot.get(1).x ? bot.get(1) : bot.get(0);
            Point br = bot.get(0).x > bot.get(1).x ? bot.get(0) : bot.get(1);
            nc.add(tl);
            nc.add(tr);
            nc.add(br);
            nc.add(bl);
        return nc;
    }

    protected Mat findQuad(Mat src)
    {
        Mat bw0=new Mat();
        cvtColor(src, bw0, Imgproc.COLOR_RGB2GRAY);
       // double[] ker={-0.1,-0.1,-0.1, -0.1, 2, -0.1, -0.1,-0.1,-0.1};
        //Mat kernel=new Mat(3,3,CvType.CV_32F,new Scalar(ker));
        Mat kernel=Mat.ones(5,5,CvType.CV_8UC1);
        //filter2D(bw,bw,-1,kernel,new Point(-1,-1),0);
        Mat bw=new Mat();
        if (switches.get(0)) {
            GaussianBlur(bw0, bw, new Size(3,3),0);//blur(bw, bw, new Size(3, 3));
            //Core.addWeighted(bw, 1.5, bw, -0.5, 0, bw);
            //Laplacian(bw,bw0,CvType.CV_16S,3,1,0);
            //Core.convertScaleAbs(bw0,bw);
        }
        if (switches.get(1)) Canny(bw, bw, threshold, threshold*3,3,true);
        if (switches.get(2)) dilate(bw,bw,kernel);
        if (switches.get(3))
        {
            int morph_size=5;
            Mat element=getStructuringElement(MORPH_RECT,new Size( 2*morph_size + 1, 2*morph_size+1 ), new Point( morph_size, morph_size ));
            morphologyEx(bw,bw,MORPH_OPEN,element);
        }

        //Mat lines = new Mat();
        Mat hier=new Mat();
        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();

        findContours(bw,contours,hier,RETR_CCOMP,CHAIN_APPROX_NONE);

        //drawContours(src, contours, -1, new Scalar(255, 0, 0), -1);

        double max_area=0;
        int max_c=0;//,cmin=100,cmax=5000;
       // Rect max_r=new Rect();

        Mat dst;
        if (isDebug) dst=bw.clone();
        else dst=src.clone();


        for ( int idx=0; idx < contours.size(); idx++ )
        {
            if (hier.get(0,idx)[3]>=0)
                //drawContours(src, contours, idx, new Scalar(255, 0, 0), 1);
            {
                double area = contourArea(contours.get(idx), false);
                // if (area<cmin || area>cmax) { continue;}
                //MatOfPoint box=new MatOfPoint(new Point(0,0),new Point(src.width(),0),new Point(src.width(),src.height()),new Point(0,src.height()));

                if (area > max_area) {
                    max_area = area;
                    max_c = idx;
                    //Rect r=boundingRect(contours.get(idx));
                    //max_r=r;
                }
            }
        }
        //drawContours(src, contours, max_c, new Scalar(255, 255, 255), 1);
        MatOfPoint2f contour=new MatOfPoint2f();
        if (contours.size()==0) return src;
        contours.get(max_c).convertTo(contour, CvType.CV_32FC2);
        MatOfPoint2f poly = new MatOfPoint2f();
        approxPolyDP(contour, poly, arcLength(contour,true)*alpha, true);
        //if quad
        if (poly.total()==4)
        {
            // and if it is convex
            MatOfPoint a = new MatOfPoint();
            a.fromArray(poly.toArray());
            if (Imgproc.isContourConvex(a))
            {
                List<Point> corners=Arrays.asList(poly.toArray());
                Point center=new Point(0,0);
                for (int i = 0; i < corners.size(); i++)
                {
                    center.x += corners.get(i).x;
                    center.y += corners.get(i).y;
                }
                center.x/= corners.size();
                center.y/= corners.size();
                try {corners=sortCorners(corners, center);}catch (Exception e){return dst;}
                item_center=center.clone();
                Mat transformed = Mat.zeros(256, 256, CvType.CV_8UC3);
                List<Point> quad_pts=new ArrayList<>();
                quad_pts.add(new Point(0, 0));
                quad_pts.add(new Point(transformed.cols(), 0));
                quad_pts.add(new Point(transformed.cols(), transformed.rows()));
                quad_pts.add(new Point(0, transformed.rows()));
                Mat transmtx = getPerspectiveTransform(Converters.vector_Point2f_to_Mat(corners), Converters.vector_Point2f_to_Mat(quad_pts));
                warpPerspective(src, transformed, transmtx, transformed.size());

                circle(dst, corners.get(0), 10, new Scalar(255,0,0), 2);
                circle(dst, corners.get(1), 10, new Scalar(0,255,0), 2);
                circle(dst, corners.get(2), 10, new Scalar(0,0,255), 2);
                circle(dst, corners.get(3), 10, new Scalar(255,255,0), 2);

                line(dst, corners.get(0), corners.get(1), new Scalar(0, 0, 255), 1);
                line(dst, corners.get(1), corners.get(2), new Scalar(0, 0, 255), 1);
                line(dst, corners.get(2), corners.get(3), new Scalar(0, 0, 255), 1);
                line(dst, corners.get(3), corners.get(0), new Scalar(0, 0, 255), 1);
                rectangle(dst, corners.get(0), corners.get(2), new Scalar(0, 255, 0));

                quadbmp = Bitmap.createBitmap(transformed.width(), transformed.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(transformed, quadbmp);
                isRecognized=true;
                //ImageUtils.saveToFile("b1",bmp);
            }

        }
        else {
            isRecognized = false;
        }
        //cleaning up
        src.release();
        bw.release();
        hier.release();
        return dst;

    }

    public void grabImage(View v)
    {
        /*Mat src = new Mat();
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeStream(am.open("card.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.bitmapToMat(bmp,src);
        findQuad(src);
        Utils.matToBitmap(src,bmp);
        ImageView i=(ImageView)findViewById(R.id.imageView);
        i.setImageBitmap(bmp);*/

        if (isDebug) {((TextView)v).setText("><"); isDebug=false;}
        else
        {
            ((TextView)v).setText("<>");
            isDebug=true;
        }
    }

    public Bitmap drawMovieInfo(Movie info)
    {
        int o=getResources().getConfiguration().orientation;
        String[] strs=info.toString().split("\n");

        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(35.0f);
        Paint bg=new Paint();
        Paint circ=new Paint();
        int width=400;
        int height=(int)((strs.length+1)*p.getTextSize()+100);

        Bitmap b=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);
        bg.setColor(Color.BLACK);
        bg.setStyle(Paint.Style.STROKE);
        //c.drawRect(0,0,width,height,bg);
        circ.setColor(Color.rgb(0, 50, 200));
        circ.setStrokeWidth(3.0f);
        circ.setStyle(Paint.Style.FILL);
        float r=30;
        c.drawLine(r,height-r,r*2,(strs.length)*p.getTextSize()+5,circ);
        c.drawLine(r*2,(strs.length)*p.getTextSize()+5,width,(strs.length)*p.getTextSize()+5,circ);
        c.drawCircle(r,height-r,r,circ);
        circ.setColor(Color.WHITE);
        c.drawCircle(r,height-r,r*0.8f,circ);
        circ.setColor(Color.rgb(0,70,220));
        c.drawCircle(r,height-r,r*0.4f,circ);

        p.setColor(Color.WHITE);

        p.setShadowLayer(4.0f,8.0f,8.0f,Color.rgb(0,0,150));

        for (int i=0;i<strs.length;i++) {
            c.drawText(strs[i], r*2, (i+1)*p.getTextSize(), p);
        }

        return b;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        rgba=new Mat(width,height,CvType.CV_8UC4);

    }

    @Override
    public void onCameraViewStopped() {
        rgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        rgba=inputFrame.rgba();
        if (getResources().getConfiguration().orientation==1)
        {
            Mat rotm=getRotationMatrix2D(new Point(rgba.size().width/2.0f,rgba.size().height/2.0f),270,1);
            warpAffine(rgba, rgba, rotm, rgba.size());
            rotm.release();
        }
        try {
            //framerate=0;
            rgba=findQuad(rgba);
        }catch (Exception e){Log.e(TAG,"FindQuad exception:"+e.getMessage());}

        return rgba;
    }

    class SystemMonitor extends AsyncTask<SystemMonitorAPI,Void,Void> {
        final int DELAY=100;
        private float cpu,pcpu;
        private SystemMonitorAPI sm;
        private String hash="";
        private Movie movieCandidate;

        @Override
        protected Void doInBackground(SystemMonitorAPI... params) {
            sm=params[0];
            int pid = android.os.Process.myPid();
            String cpuStat1 = sm.readSystemStat();
            String pidStat1 = sm.readProcessStat(pid);
            try {
                Thread.sleep(DELAY);
            } catch (Exception e) {
            }

            String cpuStat2 = sm.readSystemStat();
            String pidStat2 = sm.readProcessStat(pid);

            cpu = sm.getSystemCpuUsage(cpuStat1, cpuStat2);

            String[] toks = cpuStat1.split(" ");
            long cpu1 = sm.getSystemUptime(toks);

            toks = cpuStat2.split(" ");
            long cpu2 = sm.getSystemUptime(toks);

            pcpu = sm.getProcessCpuUsage(pidStat1, pidStat2,
                    cpu2 - cpu1);


            //getting info from server
            hash=ImageUtils.buildHash(quadbmp);
            if (hash!="") {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    URL url = new URL(serverIP + "index.php?hash=" + hash);
                    movieCandidate = mapper.readValue(url, Movie.class);


                } catch (Exception e) {
                    movieCandidate = null;
                }
                finally {
                    publishProgress(new Void[]{});
                }
            }
            if (cpu>=0.0f) {
                publishProgress(new Void[]{});
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Void... values)
        {
            super.onProgressUpdate(values);
            TextView info = (TextView) findViewById(R.id.sysinfo);
            ImageView img = (ImageView) findViewById(R.id.imageView);
            ImageView hint=(ImageView)findViewById(R.id.hint);

            hash=ImageUtils.buildHash(quadbmp);
            try {
                if (cpu>=0.0f) {
                    info.setText(sm.getDeviceName() + "\n" + sm.getOsVersion() + "\n" +
                            "CPU: " + String.valueOf(cpu) + "%(" + String.valueOf(pcpu) + "%)\n" +
                            "RAM: " + (sm.getTotalMemory() - sm.getFreeMemory()) + "/" + sm.getTotalMemory() + "(" + sm.getMemoryLoad() + "%)\n" +
                            "Battery: "+sm.getBattery()+"%\n"+
                            "Hash:" + hash + "\n" + hashes.toString());
                }
                img.setImageBitmap(quadbmp);


                if (isRecognized && movieCandidate!=null) {
                    hint.setX((float)item_center.x);
                    hint.setY((float)item_center.y + hint.getHeight());
                    hint.setImageBitmap(drawMovieInfo(movieCandidate));
                    TranslateAnimation ta=new TranslateAnimation(0,0,0,0);
                    ta.setDuration(1000);
                    hint.setAnimation(ta);
                    hint.setVisibility(View.VISIBLE);
                }
                else {hint.setVisibility(View.GONE);}

                itsMeSwitches();

            } catch (Exception e) {
                Log.e(TAG, "System Monitor exception:" + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(Void res)
        {
            movieCandidate=null;
        }


    }

}
