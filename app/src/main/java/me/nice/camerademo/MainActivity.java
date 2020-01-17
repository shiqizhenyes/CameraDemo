package me.nice.camerademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.animation.ValueAnimator.INFINITE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ImageReader.OnImageAvailableListener {

    private String tag = MainActivity.class.getSimpleName();


    private CameraManager cameraManager;
    private ConstraintLayout mainRootLayout;

    private AppCompatButton buttonChangeScale;

    private TextureView mainTexture;

    private TextView mainOutPreviewSizeS;

    private TextView mainSurfaceSize;
    private TextView mainPreviewSize;


    private int rootLayoutHeight;

    private boolean supportFullScreen() {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            int color;
            if (Build.VERSION.SDK_INT >= 23) {
                color = Color.TRANSPARENT;
            } else {
                color = 0x2d000000;
            }
            window.setStatusBarColor(color);

            if (Build.VERSION.SDK_INT >= 23) {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                int uiVisibility = window.getDecorView().getSystemUiVisibility();
//                if (mMode == IBNaviListener.DayNightMode.DAY) {
//                    uiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//                }
                window.getDecorView().setSystemUiVisibility(uiVisibility);
            } else {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            return true;
        } else {
            return false;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportFullScreen();
        setContentView(R.layout.activity_main);
        mainRootLayout = findViewById(R.id.mainRootLayout);

        buttonChangeScale = findViewById(R.id.buttonChangeScale);

        mainTexture = findViewById(R.id.mainTexture);
        mainOutPreviewSizeS = findViewById(R.id.mainOutPreviewSizeS);

        mainSurfaceSize = findViewById(R.id.mainSurfaceSize);
        mainPreviewSize = findViewById(R.id.mainPreviewSize);


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO}, 0x11);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

    }

    private Handler cameraHandler;

    private void createCameraHandler() {
        HandlerThread cameraHandlerThread = new HandlerThread(getString(R.string.camera2));
        cameraHandlerThread.start();
        cameraHandler = new Handler(cameraHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
            }
        };
    }

    private int PREVIEW = 900;
//    private int

    private int status ;


    private void takePhoto() {


    }


    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            // TODO: 2020/1/17
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            // TODO: 2020/1/17
            Log.d(tag, " onCaptureCompleted " +  result.getPartialResults().size());

        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
        }
    };


    private CameraCaptureSession cameraCaptureSession;

    private CameraCaptureSession.StateCallback sessionStateCallBack = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            try {
                cameraCaptureSession = session;
                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, cameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }
    };

    private String usingCameraId;
    private CaptureRequest.Builder captureRequestBuilder;

    private void startCameraPreview(CameraDevice cameraDevice) throws CameraAccessException {
        // 获取texture实例
        SurfaceTexture surfaceTexture = mainTexture.getSurfaceTexture();
        //我们将默认缓冲区的大小配置为我们想要的相机预览的大小。
        usingCameraId = cameraDevice.getId();
        cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            Size[] outPutSizes = map.getOutputSizes(SurfaceTexture.class);
            mPreviewSize = getPreferredPreviewSize(outPutSizes, surfaceTextureWidth, surfaceTextureHeight);
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(surfaceTexture);
            runOnUiThread(() -> {
                StringBuilder outputSizeS = new StringBuilder();
                for (Size size : outPutSizes) {
                    outputSizeS.append(" ").append(size.getWidth()).append("*").append(size.getHeight());
                }
                mainOutPreviewSizeS.setText(String.format(getString(R.string.output_pre_sizes), outputSizeS.toString()));
                mainPreviewSize.setText(String.format(getString(R.string.default_pre),
                        mPreviewSize.getWidth(), mPreviewSize.getHeight()));
                transform();
                configureTransform(surfaceTextureWidth, surfaceTextureHeight);
            });
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(surface);
            if (cameraHandler == null) {
                createCameraHandler();
            }
            cameraDevice.createCaptureSession(Collections.singletonList(surface), sessionStateCallBack,
                    cameraHandler);
        }

    }

    private CameraDevice cameraDevice;

    private CameraDevice.StateCallback stateCallback = new StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                startCameraPreview(cameraDevice);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };


    private int margin;
    private int transformMargin;

    private void transform() {
        ViewGroup.LayoutParams layoutParams = mainTexture.getLayoutParams();
        ConstraintLayout.LayoutParams constraintLayoutParams =
                new ConstraintLayout.LayoutParams(layoutParams);
        constraintLayoutParams.startToStart = R.id.mainRootLayout;
        constraintLayoutParams.endToEnd = R.id.mainRootLayout;
        constraintLayoutParams.topToTop = R.id.mainRootLayout;
        constraintLayoutParams.bottomToBottom = R.id.mainRootLayout;
        constraintLayoutParams.height = surfaceTextureHeight;
        mainTexture.setLayoutParams(constraintLayoutParams);
//        margin = constraintLayoutParams.bottomMargin;
//        transformMargin = rootLayoutHeight - surfaceTextureHeight;
//        ValueAnimator valueAnimator = ValueAnimator.ofInt(margin, transformMargin);
//        valueAnimator.addUpdateListener(animation -> {
//            constraintLayoutParams.setMargins(0, 0, 0, (Integer) animation.getAnimatedValue());
//            mainTexture.setLayoutParams(constraintLayoutParams);
//            Log.d(tag, "预览上面坐标 " + mainTexture.getTop());
//        });
//        valueAnimator.setDuration(200);
//        valueAnimator.setInterpolator(new LinearOutSlowInInterpolator());
//        valueAnimator.setRepeatCount(0);
//        valueAnimator.start();
    }

    private Size mPreviewSize;

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mainTexture || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
        matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);

        float scale = Math.max((float) viewHeight / mPreviewSize.getWidth(),
                (float) viewWidth / mPreviewSize.getHeight());

        matrix.postScale(scale, scale, centerX, centerY);
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mainTexture.setTransform(matrix);
    }

    private String supportFlashCamera;
    private String frontCameraId;
    private String backCameraId;

    @SuppressLint("MissingPermission")
    private void getCameraS() {
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String s : cameraIdList) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(s);
                boolean supportFlash = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (supportFlash) {
                    supportFlashCamera = s;
                }
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                        == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = s;
                }
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                        == CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = s;
                }
                Log.d(tag, "cameraId " + s + " " + supportFlash);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0x11) {
            getCameraS();
        }

        for (int result : grantResults) {
            Log.d(tag, String.valueOf(result));
        }

    }

    private Size getPreferredPreviewSize(Size[] sizes, int width, int height) {
        List<Size> collectorSizes = new ArrayList<>();
        for (Size option : sizes) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    collectorSizes.add(option);
                }
            } else {
                if (option.getHeight() > width && option.getWidth() > height) {
                    collectorSizes.add(option);
                }
            }
        }
        if (collectorSizes.size() > 0) {
            return Collections.min(collectorSizes, (s1, s2) -> Long.signum(s1.getWidth() * s1.getHeight() - s2.getWidth() * s2.getHeight()));
        }
        return sizes[0];
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static int sensorToDeviceRotation(CameraCharacteristics characteristics, int deviceOrientation) {
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }


    private CameraCharacteristics cameraCharacteristics;

    private void openCamera(String cameraId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        try {
//            mSurfaceWidth = width;
//            mSurfaceHeight = height;

            cameraManager.openCamera(cameraId, stateCallback, cameraHandler);
//            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
//            getCameraId(cameraId);
//            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            // 获取设备方向
//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            int totalRotation = sensorToDeviceRotation(characteristics, rotation);
//            boolean swapRotation = totalRotation == 90 || totalRotation == 270;
//            int rotatedWidth = mSurfaceWidth;
//            int rotatedHeight = mSurfaceHeight;
//            if (swapRotation) {
//                rotatedWidth = mSurfaceHeight;
//                rotatedHeight = mSurfaceWidth;
//            }
            // 获取最佳的预览尺寸
//            mPreviewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
//            if (swapRotation) {
//                texture.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            } else {
//                texture.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
//            }
//            if (mImageReader == null) {
//                // 创建一个ImageReader对象，用于获取摄像头的图像数据,maxImages是ImageReader一次可以访问的最大图片数量
//                mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
//                        ImageFormat.JPEG, 2);
//                mImageReader.setOnImageAvailableListener(this, cameraHandler);
//            }
            //检查是否支持闪光灯
//            Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
//            mFlashSupported = available == null ? false : available;

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
        }
    }


    private boolean surfaceTextureAvailable = false;
    private int surfaceTextureWidth;
    private int surfaceTextureHeight;

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            surfaceTextureAvailable = true;
            surfaceTextureWidth = width;
            surfaceTextureHeight = height;
            mainSurfaceSize.setText(String.format(getString(R.string.surface),
                    surfaceTextureWidth, surfaceTextureHeight));
//            openCamera(width, height, backCameraId);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d(tag, " onSurfaceTextureSizeChanged width " + width + " height " + height );
//            surfaceTextureWidth = width;
//            surfaceTextureHeight = height;
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            surfaceTextureAvailable = false;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        rootLayoutHeight = mainRootLayout.getHeight();
        if (mainTexture.isAvailable()) {
            surfaceTextureAvailable = true;
            if (cameraDevice != null) {
                openCamera(usingCameraId);
            }
        } else {
            mainTexture.setSurfaceTextureListener(textureListener);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (cameraDevice != null) {
            cameraDevice.close();
        }
    }

    private int SIXTEEN_TO_NINE = 100;
    private int FOUR_TO_THREE = 101;
    private int MI_DEFAULT = 200;
    private int previewScale = SIXTEEN_TO_NINE;


    private boolean enableFlash = false;

    private void changePreviewScale() {
        if (previewScale == SIXTEEN_TO_NINE) {
            surfaceTextureHeight = surfaceTextureWidth / 4 * 3;
            Log.d("", "底边边距 " + (rootLayoutHeight - surfaceTextureHeight));
//            constraintLayoutParams.setMargins(0, 0, 0, rootLayoutHeight - surfaceTextureHeight);
//            mainTexture.setLayoutParams(constraintLayoutParams);
        } else if (previewScale == FOUR_TO_THREE){
            surfaceTextureHeight = surfaceTextureWidth / 9 * 16;
//            constraintLayoutParams.setMargins(0, 0, 0, rootLayoutHeight - surfaceTextureHeight);
//            mainTexture.setLayoutParams(constraintLayoutParams);
        } else if (previewScale == MI_DEFAULT) {
            surfaceTextureHeight = surfaceTextureWidth / 2 * 3;
//            constraintLayoutParams.setMargins(0, 0, 0, rootLayoutHeight - surfaceTextureHeight);
//            mainTexture.setLayoutParams(constraintLayoutParams);
        }
        closeCamera();
        openCamera(usingCameraId);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.buttonOpenFlash) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    enableFlash = !enableFlash;
                    if (enableFlash) {
                        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    } else {
                        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                    }
                    cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        } else if (id == R.id.buttonOpenFront) {
            if (surfaceTextureAvailable) {
                closeCamera();
                openCamera(frontCameraId);
            }
        } else if (id == R.id.buttonOpenBack) {
            if (surfaceTextureAvailable) {
                closeCamera();
                openCamera(backCameraId);
            }
        } else if (id == R.id.buttonChangeScale) {
            if (previewScale == SIXTEEN_TO_NINE) {
                previewScale = FOUR_TO_THREE;
                buttonChangeScale.setText(R.string.sixteen_to_nine);
            } else {
                previewScale = SIXTEEN_TO_NINE;
                buttonChangeScale.setText(R.string.four_to_three);
            }
            changePreviewScale();
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {

    }
}
