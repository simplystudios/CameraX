package com.my.newproject8;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.*;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.appbar.AppBarLayout;
import android.app.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.webkit.*;
import android.animation.*;
import android.view.animation.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;
import org.json.*;
import android.widget.LinearLayout;
import android.widget.ImageView;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.LifecycleOwner;
import android.util.Size;
import androidx.camera.camera2.Camera2Config;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Build;
import androidx.core.content.FileProvider;
import java.io.File;
import android.content.ClipData;
import android.os.Bundle;
import java.io.InputStream;
import androidx.concurrent.futures.*;
import com.google.common.util.concurrent.*;
import androidx.camera.camera2.*;
import androidx.camera.core.*;
import androidx.exifinterface.*;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;


public class MainActivity extends  AppCompatActivity  { 
	
	public final int REQ_CD_CAMERA = 101;
	public final int REQ_CD_FILE = 102;
	
	private Toolbar _toolbar;
	private AppBarLayout _app_bar;
	private CoordinatorLayout _coordinator;
	private String msg = "";
	
	private LinearLayout linear1;
	private TextureView view_finder;
	private LinearLayout linear3;
	private ImageView capture_button;
	
	private CameraX Camera;
	private Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	private File _file_camera;
	private Intent file = new Intent(Intent.ACTION_GET_CONTENT);
	private Intent ite = new Intent();
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
		}
		else {
			initializeLogic();
		}
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		
		_app_bar = (AppBarLayout) findViewById(R.id._app_bar);
		_coordinator = (CoordinatorLayout) findViewById(R.id._coordinator);
		_toolbar = (Toolbar) findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		linear1 = (LinearLayout) findViewById(R.id.linear1);
		view_finder = (TextureView) findViewById(R.id.view_finder);
		linear3 = (LinearLayout) findViewById(R.id.linear3);
		capture_button = (ImageView) findViewById(R.id.capture_button);
		_file_camera = FileUtil.createNewPictureFile(getApplicationContext());
		Uri _uri_camera = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			_uri_camera= FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", _file_camera);
		}
		else {
			_uri_camera = Uri.fromFile(_file_camera);
		}
		camera.putExtra(MediaStore.EXTRA_OUTPUT, _uri_camera);
		camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		file.setType("image/*");
		file.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
	}
	
	private void initializeLogic() {
		startCamera();
		
	}
	    private void startCamera() {
		        //make sure there isn't another camera instance running before starting
		        CameraX.unbindAll();
		
		        /* start preview */
		        int aspRatioW = view_finder.getWidth(); //get width of screen
		        int aspRatioH = view_finder.getHeight(); //get height
		        Rational asp = new Rational (aspRatioW, aspRatioH); //aspect ratio
		        Size screen = new Size(aspRatioW, aspRatioH); //size of the screen
		
		        //config obj for preview/viewfinder thingy.
		        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(asp).setTargetResolution(screen).build();
		        Preview preview = new Preview(pConfig); //lets build it
		
		        preview.setOnPreviewOutputUpdateListener(
		                new Preview.OnPreviewOutputUpdateListener() {
					                    //to update the surface texture we have to destroy it first, then re-add it
					                    @Override
					                    public void onUpdated(Preview.PreviewOutput output){
								                        ViewGroup parent = (ViewGroup) view_finder.getParent();
								                        parent.removeView(view_finder);
								                        parent.addView(view_finder, 0);
								
								                        view_finder.setSurfaceTexture(output.getSurfaceTexture());
								                        updateTransform();
								                    }
					                });
		
		        /* image capture */
		
		        //config obj, selected capture mode
		        ImageCaptureConfig imgCapConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
		                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
		        final ImageCapture imgCap = new ImageCapture(imgCapConfig);
		
		        findViewById(R.id.capture_button).setOnClickListener(new View.OnClickListener() {
					            @Override
					            public void onClick(View v) {
								                File file = new File(FileUtil.getPublicDir(Environment.DIRECTORY_DCIM) + "/" + System.currentTimeMillis() + ".jpg");
								                imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
											                    @Override
											                    public void onImageSaved(@NonNull File file) {
														                        String msg = file.getAbsolutePath();
						ite.setClass(getApplicationContext(), CameraPreviewActivity.class);
						ite.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						ite.putExtra("image", msg);
						ite.putExtra("type", "post");
						startActivity(ite);
														                    }
											
											                    @Override
											                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
														                        String msg = "Photo capture failed: " + message;
														                        Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
														                        if(cause != null){
																	                            cause.printStackTrace();
																	                        }
														                    }
											                });
								            }
					        });
		
		        /* image analyser */
		
		        ImageAnalysisConfig imgAConfig = new ImageAnalysisConfig.Builder().setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE).build();
		        ImageAnalysis analysis = new ImageAnalysis(imgAConfig);
		
		        analysis.setAnalyzer(
		            new ImageAnalysis.Analyzer(){
					                @Override
					                public void analyze(ImageProxy image, int rotationDegrees){
								                    //y'all can add code to analyse stuff here idek go wild.
								                }
					            });
		
		        //bind to lifecycle:
		        CameraX.bindToLifecycle((LifecycleOwner)this, analysis, imgCap, preview);
		    }
	
	    private void updateTransform(){
		        /*
        * compensates the changes in orientation for the viewfinder, bc the rest of the layout stays in portrait mode.
        * methinks :thonk:
        * imgCap does this already, this class can be commented out or be used to optimise the preview
        */
		        Matrix mx = new Matrix();
		        float w = view_finder.getMeasuredWidth();
		        float h = view_finder.getMeasuredHeight();
		
		        float centreX = w / 2f; //calc centre of the viewfinder
		        float centreY = h / 2f;
		
		        int rotationDgr;
		        int rotation = (int)view_finder.getRotation(); //cast to int bc switches don't like floats
		
		        switch(rotation){ //correct output to account for display rotation
					            case Surface.ROTATION_0:
					                rotationDgr = 0;
					                break;
					            case Surface.ROTATION_90:
					                rotationDgr = 90;
					                break;
					            case Surface.ROTATION_180:
					                rotationDgr = 180;
					                break;
					            case Surface.ROTATION_270:
					                rotationDgr = 270;
					                break;
					            default:
					                return;
					        }
		
		        mx.postRotate((float)rotationDgr, centreX, centreY);
		        view_finder.setTransform(mx); //apply transformations to textureview
		    }
	private void foo() {
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		
		super.onActivityResult(_requestCode, _resultCode, _data);
		
		switch (_requestCode) {
			
			default:
			break;
		}
	}
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input){
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels(){
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels(){
		return getResources().getDisplayMetrics().heightPixels;
	}
	
}
