package eu.fjetland.loomosocketserver;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Languages;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.audiodata.RawDataListener;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;
import com.segway.robot.sdk.voice.tts.TtsListener;
import org.jetbrains.annotations.NotNull;

import static eu.fjetland.loomosocketserver.ConstantsKt.LOG_TAG;

public class LoomoJv {
    private ServiceBinder.BindStateListener mSpeakerBindStateListener;
    private Speaker mSpeaker;
    private TtsListener mTtsListener;
    private Context context;

    public void setupSpeaker(Context mainContext) {
        context = mainContext;
        mSpeaker = Speaker.getInstance();
        initListeners();
        mSpeaker.bindService(context,mSpeakerBindStateListener);
        try {
            mSpeaker.speak("Hello world", mTtsListener);
        } catch (VoiceException e) {
            Log.w(LOG_TAG,"Exeprion: ", e);
        }
    }

    private void initListeners() {
        mSpeakerBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(LOG_TAG, "speaker service onBind");
            }

            @Override
            public void onUnbind(String s) {
                Log.d(LOG_TAG, "speaker service onUnbind");
            }
        };

        mTtsListener = new TtsListener() {
            @Override
            public void onSpeechStarted(String s) {
                //s is speech content, callback this method when speech is starting.
                Log.d(LOG_TAG, "onSpeechStarted() called with: s = [" + s + "]");
            }

            @Override
            public void onSpeechFinished(String s) {
                //s is speech content, callback this method when speech is finish.
                Log.d(LOG_TAG, "onSpeechFinished() called with: s = [" + s + "]");

            }

            @Override
            public void onSpeechError(String s, String s1) {
                //s is speech content, callback this method when speech occurs error.
                Log.d(LOG_TAG, "onSpeechError() called with: s = [" + s + "], s1 = [" + s1 + "]");

            }
        };
    }
}
