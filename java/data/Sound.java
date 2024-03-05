package com.coresaken.multiplication.data;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.util.HashMap;

public class Sound {
    private final SoundPool soundPool;
    HashMap<Integer, Integer> soundsId;
    AudioManager audioManager;

    public Sound(Context context, int[] sounds){

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        soundsId = new HashMap<>();
        for(int i=0;i<sounds.length;i++){
            int soundId = soundPool.load(context, sounds[i], 1);
            soundsId.put(sounds[i], soundId);
        }

    }

    public void addSound(Context context, int sound){
        int soundId = soundPool.load(context, sound, 1);
        soundsId.put(sound, soundId);
    }

    public void play(int sound){
       if(soundsId.containsKey(sound)){
           float volume = getVolume();
           soundPool.play(soundsId.get(sound), volume, volume, 1, 0, 1f);
       }
       else{
           Log.d("XD", "NO SOUND ID: ");
       }
    }

    private float getVolume() {
        // Pobranie aktualnego poziomu głośności z AudioManager
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // Obliczenie poziomu głośności jako wartość z zakresu 0.0 do 1.0
        return (float) currentVolume / maxVolume;
    }
}
