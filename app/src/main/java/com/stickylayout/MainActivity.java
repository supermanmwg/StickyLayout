package com.stickylayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.stickylayout.listener.IGiveUpTouchListener;
import com.stickylayout.ui.StickyLayout;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private String data[] =
                {
                    "banana0", "apple0", "oranges0", "pear0", "grape0", "cherry0",
                    "banana1", "apple1", "oranges1", "pear1", "grape1", "cherry1",
                    "banana2", "apple2", "oranges2", "pear2", "grape2", "cherry2",
                    "banana3", "apple3", "oranges3", "pear3", "grape3", "cherry3",
                    "banana4", "apple4", "oranges4", "pear4", "grape4", "cherry4",
                    "banana5", "apple5", "oranges5", "pear5", "grape5", "cherry5"};

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        mListView = (ListView) findViewById(R.id.sticky_content);
        mListView.setAdapter(adapter);

        StickyLayout stickLayout = (StickyLayout) findViewById(R.id.sticky_layout);
        stickLayout.setGiveUpTouchListener(mGiveUpTouchListener);
    }

    private IGiveUpTouchListener mGiveUpTouchListener = new IGiveUpTouchListener() {
        @Override
        public boolean giveUpTouchEvent(MotionEvent event) {
            if (mListView != null && 0 == mListView.getFirstVisiblePosition()) {
                View firstChild = mListView.getChildAt(0);
                Log.d(TAG, "first child top is " + firstChild.getTop());
                if (firstChild.getTop() >= 0) {
                    return true;
                }
            }

            return false;
        }
    };
}
