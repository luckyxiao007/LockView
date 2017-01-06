package xiao.lockview.LockView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import xiao.lockview.R;

/**
 * Created by Lucky_Xiao on 2016/12/27.
 */

public class LockView extends View {
    int ViewWidth, ViewHeight;
    int rowNum, colNum;
    int bgColor, pointColor, lineColor;
    boolean useVibrator;
    final int DefRowNum = 3, DefColNum = 3;
    final int DefBGColor = Color.BLUE, DefPointColor = Color.WHITE, DefLineColor = Color.WHITE;
    final boolean DefUseVibrator = true;
    //记录每个点是否被点击
    boolean[][] selectedPoint;
    //上一个选择点的坐标
    int lastRow = -1, lastCol = -1;
    float currentX = -1, currentY = -1;
    boolean touchable=true;
    //记录输入结果的list，保存了输入点的下标，行优先
    ArrayList<Integer> resultList=new ArrayList<>();
    onFinishListener listener;
    //记录哪些点之间有连线
    HashMap<String, ArrayList<Point>> lineMap = new HashMap<>();
    //输入完毕后更新界面，调用监听器
    Handler upHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            touchable=true;
            lineMap.clear();
            for(int i=0;i<selectedPoint.length;i++)
                for(int j=0;j<selectedPoint[0].length;j++)
                    selectedPoint[i][j]=false;
            listener.onFinish(resultList);
            Toast.makeText(getContext(), "Up", Toast.LENGTH_SHORT).show();
            invalidate();
            lastRow=-1;
            lastCol=-1;
        }
    };

    private class Point {
        int row;
        int col;

        public Point(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public LockView(Context context) {
        super(context);
    }

    public LockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.lockViewAttr);
        rowNum = array.getInteger(R.styleable.lockViewAttr_rowNum, DefRowNum);
        colNum = array.getInteger(R.styleable.lockViewAttr_colNum, DefColNum);
        bgColor = array.getColor(R.styleable.lockViewAttr_backgroundColor, DefBGColor);
        pointColor = array.getColor(R.styleable.lockViewAttr_pointColor, DefPointColor);
        lineColor = array.getColor(R.styleable.lockViewAttr_lineColor, DefLineColor);
        useVibrator = array.getBoolean(R.styleable.lockViewAttr_useVibrator, DefUseVibrator);

        selectedPoint = new boolean[rowNum + 1][colNum + 1];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取View的宽高
        ViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        ViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        //保持正方形
        if (ViewWidth > ViewHeight)
            ViewWidth = ViewHeight;
        else
            ViewHeight = ViewWidth;
        Point p1=new Point(1,2);
        Point p2=new Point(1,2);
        String str;
        if(p1==p2)
            str="true";
        else
            str="false";
        Log.e("testEqu:",str);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        canvas.drawColor(bgColor);
        paint.setColor(pointColor);
        paint.setStrokeWidth(5);
        Log.e("ViewWidth:", ViewWidth + "");
        Log.e("ViewHeight:", ViewHeight + "");
        //绘制最后一个点和当前点击位置的连线
        if (lastRow != -1) {
            paint.setColor(lineColor);
            int lastX = ViewWidth / (colNum + 1) * lastCol;
            int lastY = ViewHeight / (rowNum + 1) * lastRow;
            canvas.drawLine(lastX, lastY, currentX, currentY, paint);
            paint.setColor(pointColor);
        }
        //绘制基本点
        for (int i = 1; i <= colNum; i++)
            for (int j = 1; j <= rowNum; j++) {
                int x = ViewWidth / (colNum + 1) * j;
                int y = ViewHeight / (rowNum + 1) * i;
                //绘制该点相关的连线
                if (lineMap.containsKey(i+"#"+j)) {
                    paint.setColor(lineColor);
                    ArrayList<Point> points = lineMap.get(i+"#"+j);
                    for (int m = 0; m < points.size(); m++) {
                        Point targetPoint = points.get(m);
                        int targetX = ViewWidth / (colNum + 1) * targetPoint.col;
                        int targetY = ViewHeight / (rowNum + 1) * targetPoint.row;
                        canvas.drawLine(x, y, targetX, targetY, paint);
                    }
                    paint.setColor(pointColor);
                }
                //绘制被选中的点
                if (selectedPoint[i][j]) {
                    canvas.drawCircle(x, y, ViewHeight / 20, paint);
                }

                canvas.drawCircle(x, y, ViewHeight / 30, paint);//绘制基本点，保证在图层最上层
            }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!touchable)
            return true;
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //判断是否点击了某个点
            for (int i = 1; i <= colNum; i++)
                for (int j = 1; j <= rowNum; j++) {
                    int pointX = ViewWidth / (colNum + 1) * j;
                    int pointY = ViewHeight / (rowNum + 1) * i;
                    if (Math.abs(x - pointX) < ViewHeight / 30 && Math.abs(y - pointY) < ViewHeight / 30) {
                        resultList.add((i-1)*colNum+j);
                        selectedPoint[i][j] = true;
                        lastRow = i;
                        lastCol = j;
                        currentX=x;
                        currentY=y;
                        invalidate();
                        callVibrator();
                    }
                }
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            for (int i = 1; i <= colNum; i++)
                for (int j = 1; j <= rowNum; j++) {
                    int pointX = ViewWidth / (colNum + 1) * j;
                    int pointY = ViewHeight / (rowNum + 1) * i;
                    //点击到了某个点
                    if (Math.abs(x - pointX) < ViewHeight / 30 && Math.abs(y - pointY) < ViewHeight / 30) {
                        if (!selectedPoint[i][j]) {
                            selectedPoint[i][j] = true;
                            resultList.add((i-1)*colNum+j);
                            //不是是第一个点击的点,就保存与上一个点的连线
                            if (lastRow != -1) {
                                String oldPoint = lastRow+"#"+lastCol;
                                if (lineMap.containsKey(oldPoint))
                                    lineMap.get(oldPoint).add(new Point(i, j));
                                else {
                                    ArrayList<Point> list = new ArrayList<>();
                                    list.add(new Point(i, j));
                                    lineMap.put(oldPoint, list);
                                }
                            }
                            lastRow = i;
                            lastCol = j;
                            currentX = x;
                            currentY = y;
                            invalidate();
                            callVibrator();
                        }
                    } else {
                        currentX = x;
                        currentY = y;
                    }
                }
        }
        else if(event.getAction()==MotionEvent.ACTION_UP)
        {
            touchable=false;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    upHandler.sendEmptyMessage(0);
                }
            },500);
        }
        return true;
    }

    public void setOnFinishListener(onFinishListener listener)
    {
        this.listener=listener;
    }

    private void callVibrator() {
        if (useVibrator) {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 100}; // 停止 开启
            vibrator.vibrate(pattern, -1); //只震动一次，index设为-1
        }
    }
}