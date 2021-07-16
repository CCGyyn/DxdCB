package com.odm.cb;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.provider.Telephony;
import android.provider.Telephony.CellBroadcasts;
import android.telephony.SmsCbMessage;
import android.net.Uri;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import java.util.*;
import android.widget.Toast;
import android.text.format.DateUtils;
import android.annotation.NonNull;
import com.odm.cb.MainActivity.GeoFencingTriggerMessage.CellBroadcastIdentity;
import android.content.ContentUris;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainActivity extends Activity {

    private static final String TAG = "ccgD";

    private static final String MESSAGE_NOT_DISPLAYED = "0";

    public static final String[] QUERY_COLUMNS = {
        CellBroadcasts._ID,
        CellBroadcasts.SLOT_INDEX,
        CellBroadcasts.SUBSCRIPTION_ID,
        CellBroadcasts.GEOGRAPHICAL_SCOPE,
        CellBroadcasts.PLMN,
        CellBroadcasts.LAC,
        CellBroadcasts.CID,
        CellBroadcasts.SERIAL_NUMBER,
        CellBroadcasts.SERVICE_CATEGORY,
        CellBroadcasts.LANGUAGE_CODE,
        CellBroadcasts.DATA_CODING_SCHEME,
        CellBroadcasts.MESSAGE_BODY,
        CellBroadcasts.MESSAGE_FORMAT,
        CellBroadcasts.MESSAGE_PRIORITY,
        CellBroadcasts.ETWS_WARNING_TYPE,
        // TODO: Remove the hardcode and make this system API in S.
        // CellBroadcasts.ETWS_IS_PRIMARY,
        "etws_is_primary",
        CellBroadcasts.CMAS_MESSAGE_CLASS,
        CellBroadcasts.CMAS_CATEGORY,
        CellBroadcasts.CMAS_RESPONSE_TYPE,
        CellBroadcasts.CMAS_SEVERITY,
        CellBroadcasts.CMAS_URGENCY,
        CellBroadcasts.CMAS_CERTAINTY,
        CellBroadcasts.RECEIVED_TIME,
        CellBroadcasts.LOCATION_CHECK_TIME,
        CellBroadcasts.MESSAGE_BROADCASTED,
        CellBroadcasts.MESSAGE_DISPLAYED,
        CellBroadcasts.GEOMETRIES,
        CellBroadcasts.MAXIMUM_WAIT_TIME
    };

    private TelephonyManager mTelephonyManager;

    private Button querycb;
    private Button addcb;
    private Button removecb;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        initView();
        initAction();
    }

    private void initView() {
        removecb = (Button) findViewById(R.id.btn_nt_runin);
        addcb = (Button) findViewById(R.id.btn_fail_runin);
        querycb = (Button) findViewById(R.id.btn_write_runin);
    }

    private void initAction() {
        removecb.setOnClickListener(v -> {
            int status = mContext.getContentResolver().delete(CellBroadcasts.CONTENT_URI, null, null);
            if (status > 0) {
                Toast.makeText(getApplicationContext(), "Delete success", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "status=" + status);
        });
        addcb.setOnClickListener(v -> {
            ContentValues cv = getContentValuesCB(993, 4371);
            ContentValues cv1 = getContentValuesCB(994, 4371);
            ContentValues cv2 = getContentValuesCB(997, 4373);
            Uri uri = mContext.getContentResolver().insert(CellBroadcasts.CONTENT_URI, cv);
            Uri uri1 = mContext.getContentResolver().insert(CellBroadcasts.CONTENT_URI, cv1);
            Uri uri2 = mContext.getContentResolver().insert(CellBroadcasts.CONTENT_URI, cv2);
        });
        querycb.setOnClickListener(v -> {
            List<SmsCbMessage> cbMessages = new ArrayList<>();
            final List<Uri> cbMessageUris = new ArrayList<>();
            long lastReceivedTime = System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS;
            String where;
            where = CellBroadcasts.SERVICE_CATEGORY + "=? AND "
                + CellBroadcasts.SERIAL_NUMBER + "=? AND "
                + CellBroadcasts.MESSAGE_DISPLAYED + "=? AND "
                + CellBroadcasts.RECEIVED_TIME + ">?";
            List<CellBroadcastIdentity> cbIdentifiers = new ArrayList<>();
            cbIdentifiers.add(new CellBroadcastIdentity(4371, 993));
            cbIdentifiers.add(new CellBroadcastIdentity(4384, 994));
            cbIdentifiers.add(new CellBroadcastIdentity(4373, 997));
            cbIdentifiers.add(new CellBroadcastIdentity(4386, 998));
            int open = 1;
            GeoFencingTriggerMessage geoFencingTriggerMessage = new GeoFencingTriggerMessage(2, cbIdentifiers);
            for (CellBroadcastIdentity identity : geoFencingTriggerMessage.cbIdentifiers) {
                try (Cursor cursor = mContext.getContentResolver().query(CellBroadcasts.CONTENT_URI,
                            QUERY_COLUMNS,
                            where,open==1?
                            new String[] { Integer.toString(identity.messageIdentifier),
                                Integer.toString(identity.serialNumber), MESSAGE_NOT_DISPLAYED,
                                Long.toString(lastReceivedTime) }
                                :new String[] { Integer.toString(identity.messageIdentifier),
                                    Integer.toString(identity.serialNumber), MESSAGE_NOT_DISPLAYED,"en",

                                    Long.toString(lastReceivedTime) },
                                    null /* sortOrder */)) {
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            System.out.println("rwei----mLanguage>"+cursor.getString(
                                        cursor.getColumnIndexOrThrow(CellBroadcasts.LANGUAGE_CODE)));
                            cbMessages.add(SmsCbMessage.createFromCursor(cursor));
                            cbMessageUris.add(ContentUris.withAppendedId(CellBroadcasts.CONTENT_URI,
                                        cursor.getInt(cursor.getColumnIndex(CellBroadcasts._ID))));
                        }
                    }
                } 
            }
            Log.d(TAG, "Found " + cbMessages.size() + " not broadcasted messages since ");


            /*try (Cursor cursor = mContext.getContentResolver().query(CellBroadcasts.CONTENT_URI,
                        QUERY_COLUMNS,
                        null,
                        null,
                        null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        cbMessages.add(SmsCbMessage.createFromCursor(cursor));
                    }
                }
            }*/
            for (SmsCbMessage cb : cbMessages) {
                Log.d(TAG, cb.toString());
            }

        });
    }

    public ContentValues getContentValuesCB(int serialNumber, int serviceCategory) {
        ContentValues cv = new ContentValues(16);
        cv.put(CellBroadcasts.SLOT_INDEX, 0);
        cv.put(CellBroadcasts.SUBSCRIPTION_ID, 1);
        cv.put(CellBroadcasts.GEOGRAPHICAL_SCOPE, 0);
        /*if (mLocation.getPlmn() != null) {
            cv.put(CellBroadcasts.PLMN, mLocation.getPlmn());
        }
        if (mLocation.getLac() != -1) {
            cv.put(CellBroadcasts.LAC, mLocation.getLac());
        }
        if (mLocation.getCid() != -1) {
            cv.put(CellBroadcasts.CID, mLocation.getCid());
        }*/
        cv.put(CellBroadcasts.SERIAL_NUMBER, serialNumber);
        cv.put(CellBroadcasts.SERVICE_CATEGORY, serviceCategory);
        cv.put(CellBroadcasts.LANGUAGE_CODE, "en");
        cv.put(CellBroadcasts.DATA_CODING_SCHEME, 0x45);
        cv.put(CellBroadcasts.MESSAGE_BODY, "EXTREME ALERT First CMAS Message ID");
        cv.put(CellBroadcasts.MESSAGE_FORMAT, 2);
        cv.put(CellBroadcasts.MESSAGE_PRIORITY, SmsCbMessage.MESSAGE_PRIORITY_EMERGENCY);

        /*SmsCbEtwsInfo etwsInfo = getEtwsWarningInfo();
        if (etwsInfo != null) {
            cv.put(CellBroadcasts.ETWS_WARNING_TYPE, etwsInfo.getWarningType());
            cv.put(CellBroadcasts.ETWS_IS_PRIMARY, etwsInfo.isPrimary());
        }

        SmsCbCmasInfo cmasInfo = getCmasWarningInfo();
        if (cmasInfo != null) {
            cv.put(CellBroadcasts.CMAS_MESSAGE_CLASS, cmasInfo.getMessageClass());
            cv.put(CellBroadcasts.CMAS_CATEGORY, cmasInfo.getCategory());
            cv.put(CellBroadcasts.CMAS_RESPONSE_TYPE, cmasInfo.getResponseType());
            cv.put(CellBroadcasts.CMAS_SEVERITY, cmasInfo.getSeverity());
            cv.put(CellBroadcasts.CMAS_URGENCY, cmasInfo.getUrgency());
            cv.put(CellBroadcasts.CMAS_CERTAINTY, cmasInfo.getCertainty());
        }*/

        cv.put(CellBroadcasts.RECEIVED_TIME, System.currentTimeMillis());

        cv.put(CellBroadcasts.GEOMETRIES, (String) null);
        /*if (mGeometries != null) {
            cv.put(CellBroadcasts.GEOMETRIES, CbGeoUtils.encodeGeometriesToString(mGeometries));
        } else {
            cv.put(CellBroadcasts.GEOMETRIES, (String) null);
        }*/

        cv.put(CellBroadcasts.MAXIMUM_WAIT_TIME, 0);

        return cv;
    }

    public static final class GeoFencingTriggerMessage {
        /**
         * Indicate the list of active alerts share their warning area coordinates which means the
         * broadcast area is the union of the broadcast areas of the active alerts in this list.
         */
        public static final int TYPE_ACTIVE_ALERT_SHARE_WAC = 2;

        public final int type;
        public final List<CellBroadcastIdentity> cbIdentifiers;

        GeoFencingTriggerMessage(int type, @NonNull List<CellBroadcastIdentity> cbIdentifiers) {
            this.type = type;
            this.cbIdentifiers = cbIdentifiers;
        }

        /**
         * Whether the trigger message indicates that the broadcast areas are shared between all
         * active alerts.
         * @return true if broadcast areas are to be shared
         */
        boolean shouldShareBroadcastArea() {
            return type == TYPE_ACTIVE_ALERT_SHARE_WAC;
        }

        /**
         * The GSM cell broadcast identity
         */
        public static final class CellBroadcastIdentity {
            public final int messageIdentifier;
            public final int serialNumber;
            CellBroadcastIdentity(int messageIdentifier, int serialNumber) {
                this.messageIdentifier = messageIdentifier;
                this.serialNumber = serialNumber;
            }
        }

        @Override
        public String toString() {
            String identifiers = cbIdentifiers.stream()
                .map(cbIdentifier ->String.format("(msgId = %d, serial = %d)",
                            cbIdentifier.messageIdentifier, cbIdentifier.serialNumber))
                .collect(Collectors.joining(","));
            return "triggerType=" + type + " identifiers=" + identifiers;
        }
    }

}
