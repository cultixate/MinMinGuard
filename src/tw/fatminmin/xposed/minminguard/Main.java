package tw.fatminmin.xposed.minminguard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tw.fatminmin.xposed.minminguard.adnetwork.AdMarvel;
import tw.fatminmin.xposed.minminguard.adnetwork.Adfurikun;
import tw.fatminmin.xposed.minminguard.adnetwork.Admob;
import tw.fatminmin.xposed.minminguard.adnetwork.AdmobGms;
import tw.fatminmin.xposed.minminguard.adnetwork.Amazon;
import tw.fatminmin.xposed.minminguard.adnetwork.Amobee;
import tw.fatminmin.xposed.minminguard.adnetwork.Bonzai;
import tw.fatminmin.xposed.minminguard.adnetwork.Chartboost;
import tw.fatminmin.xposed.minminguard.adnetwork.Domob;
import tw.fatminmin.xposed.minminguard.adnetwork.Flurry;
import tw.fatminmin.xposed.minminguard.adnetwork.Hodo;
import tw.fatminmin.xposed.minminguard.adnetwork.Inmobi;
import tw.fatminmin.xposed.minminguard.adnetwork.KuAd;
import tw.fatminmin.xposed.minminguard.adnetwork.Madvertise;
import tw.fatminmin.xposed.minminguard.adnetwork.MdotM;
import tw.fatminmin.xposed.minminguard.adnetwork.Millennial;
import tw.fatminmin.xposed.minminguard.adnetwork.MoPub;
import tw.fatminmin.xposed.minminguard.adnetwork.Nend;
import tw.fatminmin.xposed.minminguard.adnetwork.Og;
import tw.fatminmin.xposed.minminguard.adnetwork.Onelouder;
import tw.fatminmin.xposed.minminguard.adnetwork.OpenX;
import tw.fatminmin.xposed.minminguard.adnetwork.SmartAdserver;
import tw.fatminmin.xposed.minminguard.adnetwork.Startapp;
import tw.fatminmin.xposed.minminguard.adnetwork.TWMads;
import tw.fatminmin.xposed.minminguard.adnetwork.Tapfortap;
import tw.fatminmin.xposed.minminguard.adnetwork.Vpadn;
import tw.fatminmin.xposed.minminguard.adnetwork.Vpon;
import tw.fatminmin.xposed.minminguard.adnetwork.mAdserve;
import tw.fatminmin.xposed.minminguard.custom_mod.Backgrounds;
import tw.fatminmin.xposed.minminguard.custom_mod.OneWeather;
import tw.fatminmin.xposed.minminguard.custom_mod.Train;
import tw.fatminmin.xposed.minminguard.custom_mod._2chMate;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookZygoteInit,
                             IXposedHookLoadPackage,
                             IXposedHookInitPackageResources {


    public static final String MY_PACKAGE_NAME = Main.class.getPackage().getName();
    public static String MODULE_PATH = null;
    public static XSharedPreferences pref;
    public static Set<String> urls;
    public static Resources res;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        
        pref = new XSharedPreferences(MY_PACKAGE_NAME);
        Util.pref = pref;
        
        MODULE_PATH = startupParam.modulePath;

        res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "host/output_file");
        String decoded = new String(array);
        String[] sUrls = decoded.split("\n");

        urls = new HashSet<String>();
        for(String url : sUrls) {
            urls.add(url);
        }
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        pref.reload();

        final String packageName = lpparam.packageName;
        
        Class<?> activity = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);
        XposedBridge.hookAllMethods(activity, "onCreate", new XC_MethodHook() {
           @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context) param.thisObject;
                
                if(pref.getBoolean(packageName, false)) {
                    adNetwork(packageName, lpparam, false, context);
                    appSpecific(packageName, lpparam);
                    UrlFiltering.removeWebViewAds(packageName, lpparam, false);
                    
                    nameBasedBlocking(packageName, lpparam);
                    
                }
                else {
                    adNetwork(packageName, lpparam, true, context);
                }
            }  
        });    
        if(pref.getBoolean(packageName, false)) {
            XposedBridge.hookAllMethods(activity, "setContentView", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity ac = (Activity)(param.thisObject);
                    ViewGroup root = (ViewGroup) ac.getWindow().getDecorView().findViewById(android.R.id.content);
                    clearAdViewInLayout(packageName, root);
                }
            });
        }
    }
    
    static final ArrayList<String> banners = new ArrayList<String>(Arrays.asList(
        Adfurikun.banner, AdMarvel.banner, Admob.banner, AdmobGms.banner, Amazon.banner, Amobee.banner,  
        Bonzai.banner, Chartboost.banner, Domob.banner, Flurry.banner, Hodo.banner, Inmobi.banner, KuAd.banner, mAdserve.banner,
        Madvertise.banner, MdotM.banner, Millennial.banner, MoPub.banner, Nend.banner, Og.banner, Onelouder.banner, 
        OpenX.banner, SmartAdserver.banner, Startapp.banner, Tapfortap.banner, TWMads.banner, Vpadn.banner, 
        Vpon.banner));
    static {
        banners.add("mong.moptt.ad.AdContainer");
    }
    
    private static void clearAdViewInLayout(final String packageName, final View view) {
        
        if(banners.contains(view.getClass().getName())) {
            removeAdView(view, packageName, true);
            Util.log(packageName, "clearAdViewInLayout: " + view.getClass().getName());
        }
        
        if(view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for(int i = 0; i < vg.getChildCount(); i++) {
                clearAdViewInLayout(packageName, vg.getChildAt(i));
            }
        }
    }
    
    private static void nameBasedBlocking(final String packageName, final LoadPackageParam lpparam) {
        
        Class<?> viewGroup = XposedHelpers.findClass("android.view.ViewGroup", lpparam.classLoader);
        XposedBridge.hookAllMethods(viewGroup, "addView", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View v = (View) param.args[0];
                if(banners.contains(v.getClass().getName())) {
                    removeAdView((View) v, packageName, true);
                    Util.log(packageName, "Name based blocking: " + v.getClass().getName());
                }
            }
        });
    }
    
    private static void adNetwork(String packageName, LoadPackageParam lpparam, boolean test, Context context) {
        
        List<String> networks = new ArrayList<String>();
        if(Adfurikun.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Adfurikun");
        }
        if(AdMarvel.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("AdMarvel");
        }
        if(Admob.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("AdMob");
        }
        if(AdmobGms.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("AdMobGms");
        }
        if(Amazon.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Amazon");
        }
        if(Amobee.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Amobee");
        }
        if(Bonzai.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Bonzai");
        }
        if(Chartboost.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Chartboost");
        }
        if(Domob.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Domob");
        }
        if(Flurry.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Flurry");
        }
        if(Hodo.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("HODo");
        }
        if(Inmobi.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Inmobi");
        }
        if(KuAd.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("KuAd");
        }
        if(mAdserve.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("mAdserve");
        }
        if(Madvertise.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Madvertise");
        }
        if(MdotM.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("MdotM");
        }
        if(Millennial.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Millennial");
        }
        if(MoPub.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("MoPub");
        }
        if(Nend.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Nend");
        }
        if(Og.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Og");
        }
        if(Onelouder.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Onelouder");
        }
        if(OpenX.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("OpenX");
        }
        if(SmartAdserver.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("SmartAdserver");
        }
        if(Startapp.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Startapp");
        }
        if(Tapfortap.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Tapfortap");
        }
        if(TWMads.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("TWMads");
        }
        if(Vpadn.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Vpadn");
        }
        if(Vpon.handleLoadPackage(packageName, lpparam, test)) {
            networks.add("Vpon");
        }
        
        if(networks.size() > 0) {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.parse("content://tw.fatminmin.xposed.minminguard/" + packageName);
            StringBuilder sb = new StringBuilder();
            for(String network : networks) {
                if(sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(network);
            }
            ContentValues values = new ContentValues();
            values.put("networks", sb.toString());
            resolver.update(uri, values, null, null);
        }
    }

    private static void appSpecific(String packageName, LoadPackageParam lpparam) {
        _2chMate.handleLoadPackage(packageName, lpparam, false);
        OneWeather.handleLoadPackage(packageName, lpparam, false);
    }

    public static void removeAdView(final View view, final String packageName, final boolean apiBased) {
        
        if(convertPixelsToDp(view.getHeight()) > 0 && convertPixelsToDp(view.getHeight()) <= 55) {
            view.setVisibility(View.GONE);
        }
        ViewTreeObserver observer= view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float heightDp = convertPixelsToDp(view.getHeight());
                if(heightDp <= 55) {
                    view.setVisibility(View.GONE);
                }
            }
        });
        
        
        final ViewParent parent = view.getParent();
        if(parent instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) parent;
            final boolean recursive = pref.getBoolean(packageName + "_recursive", false);
            final boolean relative = vg.getLayoutParams() instanceof RelativeLayout.LayoutParams;
            
            
            if(recursive || !relative) {
                removeAdView(vg, packageName, apiBased);
            }
            else {
                for(int i = 0; i < vg.getChildCount(); i++) {
                    vg.getChildAt(i).setVisibility(View.GONE);
                }
            }
        }
        
    }

    private static float convertPixelsToDp(float px){
        DisplayMetrics metrics = res.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        Backgrounds.handleInitPackageResources(resparam);
        Train.handleInitPackageResources(resparam);
    }
}
