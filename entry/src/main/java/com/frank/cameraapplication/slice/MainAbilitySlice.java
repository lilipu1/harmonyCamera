package com.frank.cameraapplication.slice;

import com.frank.cameraapplication.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.surfaceprovider.SurfaceProvider;
import ohos.agp.graphics.SurfaceOps;
import ohos.agp.window.dialog.ToastDialog;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.CameraKit;
import ohos.media.camera.device.*;
import ohos.media.image.ImageReceiver;
import ohos.media.image.common.Size;

import java.io.File;
import java.util.UUID;

import static ohos.media.camera.device.Camera.FrameConfigType.FRAME_CONFIG_PREVIEW;

public class MainAbilitySlice extends AbilitySlice {

    HiLogLabel hiLogLabel = new HiLogLabel(3, 3, "相机日志");
    CameraConfig.Builder cameraConfigBuilder;
    SurfaceProvider previewProvider;
    private SurfaceProvider imageProvider;
    private boolean isPreviewProviderCreated;
    private boolean isImageProviderCreated;
    private ImageReceiver imageReceiver;
    private Size pictureSize;

    private final ImageReceiver.IImageArrivalListener imageArrivalListener = new ImageReceiver.IImageArrivalListener() {
        @Override
        public void onImageArrival(ImageReceiver imageReceiver) {
            StringBuffer fileName = new StringBuffer("picture_");
            fileName.append(UUID.randomUUID()).append(".jpg"); // 定义生成图片文件名
            File myFile = new File(getContext().getCacheDir(), fileName.toString()); // 创建图片文件
            //imageSaver = new ImageSaver(imageReceiver.readNextImage(), myFile); // 创建一个读写线程任务用于保存图片
            //cameraEventHandler.postTask(imageSaver); // 执行读写线程任务生成图片
        }
    };

    private void debug(String errorMsg) {
        HiLog.error(hiLogLabel, errorMsg);
    }

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        previewProvider = (SurfaceProvider) findComponentById(ResourceTable.Id_surfaceProvider);
        imageProvider = (SurfaceProvider) findComponentById(ResourceTable.Id_imageProvider);
        SurfaceOps surfaceOps = previewProvider.getSurfaceOps().get();
        imageProvider.getSurfaceOps().get().addCallback(new SurfaceOps.Callback() {
            @Override
            public void surfaceCreated(SurfaceOps surfaceOps) {
                isImageProviderCreated = true;
                if (isPreviewProviderCreated)
                    openCamera();
            }

            @Override
            public void surfaceChanged(SurfaceOps surfaceOps, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceOps surfaceOps) {

            }
        });
        surfaceOps.addCallback(new SurfaceOps.Callback() {
            @Override
            public void surfaceCreated(SurfaceOps surfaceOps) {
                isPreviewProviderCreated = true;
                if (isImageProviderCreated)
                    openCamera();
            }

            @Override
            public void surfaceChanged(SurfaceOps surfaceOps, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceOps surfaceOps) {

            }
        });
    }

    FrameStateCallback frameStateCallback = new FrameStateCallback() {
        @Override
        public void onFrameStarted(Camera camera, FrameConfig frameConfig, long frameNumber, long timestamp) {
            super.onFrameStarted(camera, frameConfig, frameNumber, timestamp);
        }
    };


    private CameraStateCallback cameraStateCallback = new CameraStateCallback() {
        @Override
        public void onCreated(Camera camera) {
            super.onCreated(camera);
            debug("相机创建成功");
            cameraConfigBuilder = camera.getCameraConfigBuilder();
            cameraConfigBuilder.addSurface(previewProvider.getSurfaceOps().get().getSurface());
            cameraConfigBuilder.addSurface(imageProvider.getSurfaceOps().get().getSurface());
            cameraConfigBuilder.setFrameStateCallback(frameStateCallback, cameraEventHandler);
            try {
                camera.configure(cameraConfigBuilder.build());
            } catch (Exception e) {
                debug("配置失败:" + e.getMessage());
            }

        }

        @Override
        public void onCreateFailed(String cameraId, int errorCode) {
            super.onCreateFailed(cameraId, errorCode);
            debug("相机创建失败");
        }

        @Override
        public void onConfigured(Camera camera) {
            super.onConfigured(camera);
            debug("相机配置成功");
            FrameConfig.Builder frameConfigBuilder = camera.getFrameConfigBuilder(FRAME_CONFIG_PREVIEW);
            frameConfigBuilder.addSurface(previewProvider.getSurfaceOps().get().getSurface());
            FrameConfig frameConfig = frameConfigBuilder.build();
            try {
                int triggerId = camera.triggerLoopingCapture(frameConfig);
            } catch (Exception e) {
                debug("预览失败:" + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onPartialConfigured(Camera camera) {
            super.onPartialConfigured(camera);
            debug("相机部分配置成功");
        }

        @Override
        public void onConfigureFailed(Camera camera, int errorCode) {
            super.onConfigureFailed(camera, errorCode);
            debug("相机配置失败");
        }

        @Override
        public void onReleased(Camera camera) {
            super.onReleased(camera);
        }

        @Override
        public void onFatalError(Camera camera, int errorCode) {
            super.onFatalError(camera, errorCode);
        }

        @Override
        public void onCaptureRun(Camera camera) {
            super.onCaptureRun(camera);
        }

        @Override
        public void onCaptureIdle(Camera camera) {
            super.onCaptureIdle(camera);
        }
    };

    private EventHandler cameraEventHandler = new EventHandler(EventRunner.create("CameraCb")) {
        @Override
        protected void processEvent(InnerEvent event) {
            super.processEvent(event);
        }
    };

    private void openCamera() {
        CameraKit cameraKit = CameraKit.getInstance(this);
        if (cameraKit == null) {
            new ToastDialog(this).setText("获取相机失败").show();
            return;
        }
        String[] ids = cameraKit.getCameraIds();
        if (ids.length == 0) {
            new ToastDialog(this).setText("设备无可用相机").show();
        }
        cameraKit.createCamera(ids[0], cameraStateCallback, cameraEventHandler);
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
