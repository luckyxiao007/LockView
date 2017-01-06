package xiao.lockview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import xiao.lockview.LockView.LockView;
import xiao.lockview.LockView.onFinishListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LockView lockView= (LockView) findViewById(R.id.lockView);
        lockView.setOnFinishListener(new onFinishListener() {
            @Override
            public void onFinish(ArrayList<Integer> list) {
                for(int i=0;i<list.size();i++)
                    Log.e("No:",list.get(i)+"");
            }
        });
    }
}
