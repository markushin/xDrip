package com.eveningoutpost.dexdrip.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.eveningoutpost.dexdrip.UtilityModels.Constants;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Created by adrian on 07/09/15.
 */
public class BgToSpeech {

    private static BgToSpeech instance;
    private final Context context;

    private TextToSpeech tts = null;

    private long timestamp = 0;

    public static BgToSpeech getSingleton(Context context){

        if(instance == null) {
            instance = new BgToSpeech(context);
        }
        return instance;
    }

    private BgToSpeech(Context context){
        this.context = context;
    }



    public void speak(final double value, long timestamp){
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (! prefs.getBoolean("bg_to_speech", false)){
            return;
        }

        if(this.timestamp == timestamp){
            return;
        }

        this.timestamp = timestamp;

        if(tts == null){
            tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {

                    if (status == TextToSpeech.SUCCESS) {


                        //try local language
                        int result = tts.setLanguage(Locale.getDefault());
                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("BgToSpeech", "Default system language is not supported");
                            result = tts.setLanguage(Locale.ENGLISH);
                        }
                        //try any english
                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("BgToSpeech", "English is not supported");
                            tts = null;
                        } else {
                            //first call will be made after initialization
                            tts.speak(calculateText(value, prefs), TextToSpeech.QUEUE_FLUSH, null);
                        }

                    } else {
                        tts= null;
                    }
                }
            });
        } else {

        if (tts == null) {
            return;
        }
        tts.speak(calculateText(value, prefs), TextToSpeech.QUEUE_FLUSH, null);
    }
    }

    private String calculateText(double value, SharedPreferences prefs) {
        boolean doMgdl = (prefs.getString("units", "mgdl").equals("mgdl"));

        String text = "";

        DecimalFormat df = new DecimalFormat("#");
        if (value >= 400) {
            text = "high";
        } else if (value >= 40) {
            if(doMgdl) {
                df.setMaximumFractionDigits(0);
                text =  df.format(value);
            } else {
                df.setMaximumFractionDigits(1);
                df.setMinimumFractionDigits(1);
                text =  df.format(value* Constants.MGDL_TO_MMOLL);
            }
        } else if (value > 12) {
            text =  "low";
        } else {
            text = "no value";
        }
        Log.d("BgToSpeech", "speaking: " + text);
        return text;
    }


    public static void installTTSData(Context ctx){
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

}
