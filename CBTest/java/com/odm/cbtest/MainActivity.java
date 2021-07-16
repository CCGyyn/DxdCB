package com.odm.cbtest;

import android.annotation.NonNull;
import android.annotation.Nullable;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.odm.cbtest.CbGeoUtils.Circle;
import com.odm.cbtest.CbGeoUtils.Geometry;
import com.odm.cbtest.CbGeoUtils.LatLng;
import com.odm.cbtest.CbGeoUtils.Polygon;
import android.location.LocationRequest;
import android.os.HandlerExecutor;
import android.os.Process;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.odm.cbtest.CbSendMessageCalculator.SEND_MESSAGE_ACTION_AMBIGUOUS;
import static com.odm.cbtest.CbSendMessageCalculator.SEND_MESSAGE_ACTION_NO_COORDINATES;
import static com.odm.cbtest.CbSendMessageCalculator.SEND_MESSAGE_ACTION_SEND;


public class MainActivity extends Activity {

    private static final boolean DBG = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        testBroadcastArea();
//        testEasy();
        testLocationA();
    }

    LocationRequester mLocationRequester;
    /**
     * 测试位置精确度
     */
    private void testLocationA() {
        mLocationRequester = new LocationRequester(
                this,
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
                new Handler());
        int maximumWaitingTime = 60;
        mLocationRequester.requestLocationUpdate((location, accuracy) -> {
            if (location != null) {
                Log.d("location=" + location.toString());
            } else {
                Log.d("location=" + (location == null));
            }
        }, maximumWaitingTime);
    }

    protected void requestLocationUpdate(LocationUpdateCallback callback, int maximumWaitTimeSec) {
        mLocationRequester.requestLocationUpdate(callback, maximumWaitTimeSec);
    }

    List<CbGeoUtils.Geometry> geometries = new ArrayList<>();
    /**
     * 测试坐标是否在范围内
     */
    private void testEasy() {
        String geoString = "polygon|40.65018653869629,-74.75475311279297|40.64310550689697,-74.51528549194336|40.90282917022705,-74.16535377502441|40.90282917022705,-74.16535377502441|41.15358352661133,-74.57674026489258|40.941925048828125,-74.83792304992676|40.65018653869629,-74.75475311279297;polygon|41.30807876586914,-72.93763160705566|41.17422580718994,-71.57730102539062|41.82087421417236,-71.42031669616699|42.25908279418945,-71.81977272033691|42.14582920074463,-72.62134552001953|41.84387683868408,-73.34704399108887|41.30807876586914,-72.93763160705566;circle|40.341668128967285,-74.4320297241211|37515.625;circle|39.74312782287598,-75.55057525634766|80000.0";
        List<Geometry> parsedGeometries = CbGeoUtils.parseGeometriesFromString(geoString);
        CbSendMessageCalculator calc =
                new CbSendMessageCalculator(this, parsedGeometries);
        LatLng location = new LatLng(40.7984829, -77.8654437);
        double accuracy = 3.7899999618530273;
        calc.addCoordinate(location, accuracy);
        if (calc.getAction() == SEND_MESSAGE_ACTION_SEND
                || calc.getAction() == SEND_MESSAGE_ACTION_AMBIGUOUS
                || calc.getAction() == SEND_MESSAGE_ACTION_NO_COORDINATES) {
            Log.d("performGeoFencing: SENT.");
        } else {
            Log.d("Device location is outside the broadcast area");
        }
    }

    private void testBroadcastArea() {
        /*
         * demo:
         * polygon|40.65018653869629,-74.75475311279297|40.64310550689697,-74.51528549194336|40.90282917022705,-74.16535377502441|40.90282917022705,-74.16535377502441|41.15358352661133,-74.57674026489258|40.941925048828125,-74.83792304992676|40.65018653869629,-74.75475311279297;polygon|41.30807876586914,-72.93763160705566|41.17422580718994,-71.57730102539062|41.82087421417236,-71.42031669616699|42.25908279418945,-71.81977272033691|42.14582920074463,-72.62134552001953|41.84387683868408,-73.34704399108887|41.30807876586914,-72.93763160705566;
         * circle|40.341668128967285,-74.4320297241211|37515.625;
         * circle|39.74312782287598,-75.55057525634766|80000.0
         * */
        String broadcastArea = "polygon|43.16202163696289,-77.61368751525879|42.88736343383789,-78.87531280517578|42.12389945983887,-80.10406494140625|40.79678535461426,-77.8630256652832|41.40515327453613,-75.65717697143555|43.048553466796875,-76.15413665771484|43.16202163696289,-77.61368751525879;polygon|41.30807876586914,-72.93763160705566|41.17422580718994,-71.57730102539062|41.82087421417236,-71.42031669616699|42.25908279418945,-71.81977272033691|42.14582920074463,-72.62134552001953|41.84387683868408,-73.34704399108887|41.30807876586914,-72.93763160705566;circle|38.90657901763916,-77.03664779663086|100000.0;circle|39.74312782287598,-75.55057525634766|80000.0";
        //分出每个图形的坐标
        String[] graphs = broadcastArea.split(";");
        for (String grash : graphs
        ) {
            Log.d(grash);
            String[] loc = grash.split("\\|");
            String s = loc[0];
            Log.d(loc[0] + loc[1]);
            if (s.equals("polygon")) {
                polugonFun(loc);
            } else if (s.equals("circle")) {
                circleFun(loc);
            }

        }
        String geoString = CbGeoUtils.encodeGeometriesToString(geometries);
        Log.d("geoString=" + geoString);
        List<Geometry> parsedGeometries = CbGeoUtils.parseGeometriesFromString(geoString);
        CbSendMessageCalculator calc =
                new CbSendMessageCalculator(this, parsedGeometries);
        LatLng location = new LatLng(40.7984848, -77.8654054);
        double accuracy = 7.991000175476074;
        calc.addCoordinate(location, accuracy);
    }

    private void polugonFun(String[] loc) {
        List<CbGeoUtils.LatLng> vertices = new ArrayList<>();
        for (int i = 1; i < loc.length; i++) {
            Log.d("polugonFun" + loc[i]);
            String[] strings = loc[i].split(",");

            double[] coordinates = Arrays.asList(strings).stream().mapToDouble(Double::parseDouble).toArray();
            Log.d("polugonFun" + coordinates);
            vertices.add(new LatLng(coordinates[0], coordinates[1]));
        }
        geometries.add(new Polygon(vertices));
    }

    private void circleFun(String[] loc) {
        Log.d("circleFun" + loc[1]);
        String[] strings = loc[1].split(",");

        double[] coordinates = Arrays.asList(strings).stream().mapToDouble(Double::parseDouble).toArray();
        Log.d("circleFun" + coordinates + " r=" + Double.valueOf(loc[2]));
        geometries.add(new Circle(new LatLng(coordinates[0], coordinates[1]), Double.valueOf(loc[2])));
    }

    /** The callback interface of a location request. */
    public interface LocationUpdateCallback {
        /**
         * Call when the location update is available.
         * @param location a location in (latitude, longitude) format, or {@code null} if the
         * location service is not available.
         */
        void onLocationUpdate(@Nullable LatLng location, double accuracy);
    }

    private static final class LocationRequester {
        private static final String TAG = LocationRequester.class.getSimpleName();

        /**
         * Fused location provider, which means GPS plus network based providers (cell, wifi, etc..)
         */
        //TODO: Should make LocationManager.FUSED_PROVIDER system API in S.
        private static final String FUSED_PROVIDER = "fused";

        private final LocationManager mLocationManager;
        private final List<LocationUpdateCallback> mCallbacks;
        private final Context mContext;
        private final Handler mLocationHandler;

        private boolean mLocationUpdateInProgress;
        private final Runnable mTimeoutCallback;
        private CancellationSignal mCancellationSignal;

        LocationRequester(Context context, LocationManager locationManager, Handler handler) {
            mLocationManager = locationManager;
            mCallbacks = new ArrayList<>();
            mContext = context;
            mLocationHandler = handler;
            mLocationUpdateInProgress = false;
            mTimeoutCallback = this::onLocationTimeout;
        }

        /**
         * Request a single location update. If the location is not available, a callback with
         * {@code null} location will be called immediately.
         *
         * @param callback a callback to the response when the location is available
         * @param maximumWaitTimeS the maximum wait time of this request. If location is not
         * updated within the maximum wait time, {@code callback#onLocationUpadte(null)} will be
         * called.
         */
        void requestLocationUpdate(@NonNull LocationUpdateCallback callback,
                                   int maximumWaitTimeS) {
            mLocationHandler.post(() -> requestLocationUpdateInternal(callback, maximumWaitTimeS));
        }

        private void onLocationTimeout() {
            Log.e(TAG, "Location request timeout");
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
            onLocationUpdate(null);
        }

        private void onLocationUpdate(@Nullable Location location) {
            mLocationUpdateInProgress = false;
            mLocationHandler.removeCallbacks(mTimeoutCallback);
            LatLng latLng = null;
            float accuracy = 0;
            if (location != null) {
                Log.d(TAG, "Got location update");
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                accuracy = location.getAccuracy();
            } else {
                Log.e(TAG, "Location is not available.");
            }

            for (LocationUpdateCallback callback : mCallbacks) {
                callback.onLocationUpdate(latLng, accuracy);
            }
            mCallbacks.clear();
        }

        private void requestLocationUpdateInternal(@NonNull LocationUpdateCallback callback,
                                                   int maximumWaitTimeS) {
            if (DBG) Log.d(TAG, "requestLocationUpdate");
            if (!hasPermission(ACCESS_FINE_LOCATION) && !hasPermission(ACCESS_COARSE_LOCATION)) {
                if (DBG) {
                    Log.e(TAG, "Can't request location update because of no location permission");
                }
                callback.onLocationUpdate(null, Float.NaN);
                return;
            }

            if (!mLocationUpdateInProgress) {
                LocationRequest request = LocationRequest.create()
                        .setProvider(FUSED_PROVIDER)
                        .setQuality(LocationRequest.ACCURACY_FINE)
                        .setInterval(0)
                        .setFastestInterval(0)
                        .setSmallestDisplacement(0)
                        .setNumUpdates(1)
                        .setExpireIn(TimeUnit.SECONDS.toMillis(maximumWaitTimeS));
                if (DBG) {
                    Log.d(TAG, "Location request=" + request);
                }
                try {
                    mCancellationSignal = new CancellationSignal();

                    mLocationManager.getCurrentLocation(request, mCancellationSignal,
                            new HandlerExecutor(mLocationHandler), this::onLocationUpdate);
                    // TODO: Remove the following workaround in S. We need to enforce the timeout
                    // before location manager adds the support for timeout value which is less
                    // than 30 seconds. After that we can rely on location manager's timeout
                    // mechanism.
                    mLocationHandler.postDelayed(mTimeoutCallback,
                            TimeUnit.SECONDS.toMillis(maximumWaitTimeS));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Cannot get current location. e=" + e);
                    callback.onLocationUpdate(null, 0.0);
                    return;
                }
                mLocationUpdateInProgress = true;
            }
            mCallbacks.add(callback);
        }

        private boolean hasPermission(String permission) {
            // TODO: remove the check. This will always return true because cell broadcast service
            // is running under the UID Process.NETWORK_STACK_UID, which is below 10000. It will be
            // automatically granted with all runtime permissions.
            return mContext.checkPermission(permission, Process.myPid(), Process.myUid())
                    == PackageManager.PERMISSION_GRANTED;
        }
    }
}
