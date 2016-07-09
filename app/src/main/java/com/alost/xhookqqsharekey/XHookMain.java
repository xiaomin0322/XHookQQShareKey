package com.alost.xhookqqsharekey;

import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by Alost on 6-27.
 */

public class XHookMain implements IXposedHookLoadPackage {
    int n = 0;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //XposedBridge.log("Loaded App: " + loadPackageParam.packageName);

        if (!loadPackageParam.packageName.equals("com.tencent.mobileqq"))
            return;
        XposedBridge.log("Loaded App: " + loadPackageParam.packageName);

        XposedHelpers.findAndHookMethod("oicq.wlogin_sdk.tools.EcdhCrypt", loadPackageParam.classLoader, "get_c_pub_key", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                byte[] pubKey = (byte[])param.getResult();
                XposedBridge.log("pubKey: " + bytesToHexString(pubKey));
            }
        });

        XposedHelpers.findAndHookMethod("oicq.wlogin_sdk.tools.EcdhCrypt", loadPackageParam.classLoader, "get_g_share_key", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                byte[] shareKey = (byte[])param.getResult();
                XposedBridge.log("shareKey: " + bytesToHexString(shareKey));
            }
        });

        XposedHelpers.findAndHookMethod("oicq.wlogin_sdk.tools.EcdhCrypt", loadPackageParam.classLoader, "GenereateKey", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                n = new Random().nextInt(Keys.pubKeys.size()-1);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            }
        });

        XposedHelpers.findAndHookMethod("oicq.wlogin_sdk.tools.EcdhCrypt", loadPackageParam.classLoader, "set_c_pub_key", byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (((byte[])(param.args[0])) != null){
                    byte[] pubKey = hexStringToBytes(Keys.pubKeys.get(n));
                    param.args[0] = pubKey;
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            }
        });
        XposedHelpers.findAndHookMethod("oicq.wlogin_sdk.tools.EcdhCrypt", loadPackageParam.classLoader, "set_g_share_key", byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (((byte[])(param.args[0])) != null){
                    byte[] shareKey = hexStringToBytes(Keys.shareKeys.get(n));
                    param.args[0] = shareKey;
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            }
        });
    }


    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
    public static byte[] hexStringToBytes(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }
    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }
}
