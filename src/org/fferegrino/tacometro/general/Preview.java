package org.fferegrino.tacometro.general;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;
	CameraDraw mDrawOnTop;
	boolean mFinished;

	public Preview(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public Preview(Context context) {
		this(context,null,0);
	}
	
	
	public void setCameraDrawCompanion(CameraDraw c){
		this.mDrawOnTop = c;
	}
	
	public Preview(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs, defStyle);

		mDrawOnTop = null;
		mFinished = false;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);

			// Preview callback used whenever new viewfinder frame is available
			mCamera.setPreviewCallback(new PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera camera) {
					if ((mDrawOnTop == null) || mFinished)
						return;

					if (mDrawOnTop.mBitmap == null) {
						// Initialize the draw-on-top companion
						Camera.Parameters params = camera.getParameters();
						mDrawOnTop.mImageWidth = params.getPreviewSize().width;
						mDrawOnTop.mImageHeight = params.getPreviewSize().height;
						mDrawOnTop.mBitmap = Bitmap.createBitmap(
								mDrawOnTop.mImageWidth,
								mDrawOnTop.mImageHeight, Bitmap.Config.RGB_565);
						mDrawOnTop.mRGBData = new int[mDrawOnTop.mImageWidth
								* mDrawOnTop.mImageHeight];
						mDrawOnTop.mYUVData = new byte[data.length];
					}

					// Pass YUV data to draw-on-top companion
					System.arraycopy(data, 0, mDrawOnTop.mYUVData, 0,
							data.length);
					mDrawOnTop.invalidate();
				}
			});
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mFinished = true;
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.set("orientation", "portrait");
		parameters.setRotation(90);
		
		//parameters.setPreviewSize(240, 320);
		parameters.setPreviewFrameRate(15);
		parameters.setSceneMode(Camera.Parameters.SCENE_MODE_SUNSET);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		mCamera.setParameters(parameters);
		mCamera.setDisplayOrientation(90);
		mCamera.startPreview();
	}

}