package hram.sudoku.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import hram.sudoku.R;

@SuppressLint("Registered")
public class MainActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_main);
        /*
	    findViewById(R.id.btResume).setOnClickListener(new OnClickListener() 
	    {			
			@Override
			public void onClick(View v) 
			{
				
			}
		});
	    
	    findViewById(R.id.btCapture).setOnClickListener(new OnClickListener() 
	    {			
			@Override
			public void onClick(View v) 
			{
				startActivity(new Intent(MainActivity.this, CaptureActivity.class));
			}
		});
	    
	    findViewById(R.id.btGallery).setOnClickListener(new OnClickListener() 
	    {			
			@Override
			public void onClick(View v) 
			{
				startActivity(new Intent(MainActivity.this, GalleryActivity.class));
			}
		});
	    
	    findViewById(R.id.btOpenGame).setOnClickListener(new OnClickListener() 
	    {			
			@Override
			public void onClick(View v) 
			{
				startActivity(new Intent(MainActivity.this, PlayActivity.class));
			}
		});
	    
	    findViewById(R.id.btTessTest).setOnClickListener(new OnClickListener() 
	    {			
			@Override
			public void onClick(View v) 
			{
				startActivity(new Intent(MainActivity.this, TestTessAPI.class));
			}
		});
		*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        //findViewById(R.id.btGallery).setEnabled(!CapturedImagesAdapter.IsEmpty());
        //findViewById(R.id.btResume).setEnabled(DataController.Instance().GetLastGame() != null);
    }
}
