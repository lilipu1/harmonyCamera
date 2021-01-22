package com.frank.cameraapplication;

import com.frank.cameraapplication.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.agp.window.dialog.ToastDialog;
import ohos.bundle.IBundleManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        initReqPermissions();
    }

    private void initReqPermissions() {
        //动态权限
        if (verifySelfPermission(PermissionUtils.CAMERA) != IBundleManager.PERMISSION_GRANTED
                || verifySelfPermission(PermissionUtils.MICROPHONE) != IBundleManager.PERMISSION_GRANTED) {
            // 应用未被授予权限
            if (canRequestPermission(PermissionUtils.CAMERA) || canRequestPermission(PermissionUtils.MICROPHONE)) {
                // 是否可以申请弹框授权(首次申请或者用户未选择禁止且不再提示)
                requestPermissionsFromUser(
                        PermissionUtils.permission, PermissionUtils.PERMISSION_CODE);
            }
        }else {
            super.setMainRoute(MainAbilitySlice.class.getName());
        }

    }

    @Override
    public void onRequestPermissionsFromUserResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.PERMISSION_CODE:
                // 匹配requestPermissions的requestCode
                if (grantResults.length > 0
                        && grantResults[0] == IBundleManager.PERMISSION_GRANTED) {
                    // 权限被授予
                    // 注意：因时间差导致接口权限检查时有无权限，所以对那些因无权限而抛异常的接口进行异常捕获处理
                    HiLog.error(new HiLogLabel(3, 3, "权限回调"), "线程id:" + Thread.currentThread().getName());
                    getUITaskDispatcher().delayDispatch(() -> {
                        new ToastDialog(MainAbility.this).setText("权限获取成功").show();
                        MainAbility.this.setMainRoute(MainAbilitySlice.class.getName());
                    },0);

                } else {
                    // 权限被拒绝
                    new ToastDialog(this).setText("权限被拒绝").show();
                }
                break;
        }
    }
}
