package me.nice.camerademo;

import android.content.Context;
import android.graphics.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;
//    private Camera camera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
//        this.camera = camera;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
//        surfaceHolder.setType();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        this.camera.setd
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
