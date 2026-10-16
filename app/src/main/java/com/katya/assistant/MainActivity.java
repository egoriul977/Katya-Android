package com.katya.assistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private final int BG=0xff0b0d17, PANEL=0xff191d30, PINK=0xffff40b6, CYAN=0xff46ddff;
    private TextView status, heard;
    private EditText input;
    private TextToSpeech tts;
    private SpeechRecognizer recognizer;
    private boolean ready=false;
    private final Locale RU=new Locale("ru","RU");

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sc=new ScrollView(this); sc.setFillViewport(true); sc.setBackgroundColor(BG);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22),dp(38),dp(22),dp(28)); sc.addView(root);
        TextView title=label("КАТЯ",36,Color.WHITE); title.setTypeface(null,Typeface.BOLD); root.addView(title);
        TextView sub=label("Твой голосовой помощник · версия 2.0",14,CYAN); root.addView(sub);
        addSpace(root,28);
        status=label("Привет! Нажми на микрофон и скажи, что нужно сделать.",19,Color.WHITE);
        status.setPadding(dp(18),dp(22),dp(18),dp(22)); status.setBackground(card()); root.addView(status);
        addSpace(root,16);
        heard=label("",14,0xffaeb5ce); root.addView(heard);
        addSpace(root,12);
        Button talk=button("🎤  ГОВОРИТЬ",PINK); root.addView(talk);
        talk.setOnClickListener(v->listen());
        addSpace(root,12);
        input=new EditText(this); input.setSingleLine(false); input.setMinLines(2);
        input.setTextColor(Color.WHITE); input.setHintTextColor(0xff9ba5c0);
        input.setHint("Или напиши команду здесь…"); input.setBackground(card()); input.setPadding(dp(16),dp(10),dp(16),dp(10)); root.addView(input);
        addSpace(root,10);
        Button send=button("ВЫПОЛНИТЬ КОМАНДУ",CYAN); send.setTextColor(BG); root.addView(send);
        send.setOnClickListener(v->{String s=input.getText().toString().trim(); if(!s.isEmpty()){heard.setText("Ты: "+s);handle(s);input.setText("");}});
        addSpace(root,24);
        TextView examples=label("ПОПРОБУЙ СКАЗАТЬ",14,PINK); examples.setTypeface(null,Typeface.BOLD); root.addView(examples);
        for(String ex:new String[]{"Открой Todoist","Поставь будильник на 8:30","Открой календарь","Открой настройки","Открой камеру","Открой ChatGPT","Построй маршрут до Каширской 9В","Какие команды ты умеешь?"}) {
            TextView row=label("›  "+ex,15,Color.WHITE); row.setPadding(dp(12),dp(12),dp(12),dp(12)); root.addView(row);
            row.setOnClickListener(v->{heard.setText("Ты: "+ex);handle(ex);});
        }
        setContentView(sc);
        tts=new TextToSpeech(this,result->{if(result==TextToSpeech.SUCCESS){int r=tts.setLanguage(RU);ready=r!=TextToSpeech.LANG_MISSING_DATA&&r!=TextToSpeech.LANG_NOT_SUPPORTED;}});
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},42);
    }
    private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private void addSpace(LinearLayout l,int h){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
    private GradientDrawable card(){GradientDrawable d=new GradientDrawable();d.setColor(PANEL);d.setCornerRadius(dp(18));return d;}
    private Button button(String s,int color){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(16);b.setTextColor(Color.WHITE);GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(16));b.setBackground(d);b.setMinHeight(dp(58));return b;}
    private void say(String s){status.setText(s);if(ready)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"katya_reply");}
    private void listen(){
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},42);say("Разреши доступ к микрофону.");return;}
        if(!SpeechRecognizer.isRecognitionAvailable(this)){say("На телефоне не найден сервис распознавания речи. Можно писать команды текстом.");return;}
        if(recognizer!=null){recognizer.destroy();recognizer=null;}
        recognizer=SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener(){
            public void onReadyForSpeech(Bundle b){status.setText("Слушаю тебя…");}
            public void onBeginningOfSpeech(){} public void onRmsChanged(float r){} public void onBufferReceived(byte[] b){}
            public void onEndOfSpeech(){status.setText("Распознаю…");} public void onPartialResults(Bundle b){} public void onEvent(int t,Bundle b){}
            public void onError(int e){say("Не удалось распознать речь. Попробуй ещё раз или напиши команду.");}
            public void onResults(Bundle b){ArrayList<String> a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(a!=null&&!a.isEmpty()){heard.setText("Ты: "+a.get(0));handle(a.get(0));}else say("Ничего не расслышала.");}
        });
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ru-RU");i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        try{recognizer.startListening(i);}catch(Exception e){say("Не удалось запустить микрофон.");}
    }
    private void open(Intent i,String response){try{startActivity(i);say(response);}catch(Exception e){say("Не удалось открыть приложение на этом телефоне.");}}
    private void openApp(String pkg,String fallback,String name){
        Intent i=getPackageManager().getLaunchIntentForPackage(pkg);
        if(i!=null){open(i,"Открываю "+name);return;}
        try{Intent web=new Intent(Intent.ACTION_VIEW,Uri.parse(fallback));open(web,"Открываю "+name+" в браузере");}
        catch(Exception e){say(name+" не найден. Установи приложение.");}
    }
    private void handle(String raw){
        String s=raw.toLowerCase(RU).replace("ё","е");
        if(s.contains("какие команды")||s.contains("что умеешь")||s.contains("помощь")){
            say("Могу открывать Todoist, ChatGPT, камеру, календарь и настройки, ставить будильник и открывать маршруты. Для задач в Todoist пока открываю приложение — прямой синхронизации ещё нет.");
        } else if(s.contains("todoist")||s.contains("тудуист")||s.contains("тудоист")){
            openApp("com.todoist","https://todoist.com/app","Todoist");
        } else if(s.contains("chatgpt")||s.contains("чат джипити")||s.contains("чат гпт")){
            openApp("com.openai.chatgpt","https://chatgpt.com","ChatGPT");
        } else if(s.contains("настройк")){
            open(new Intent(Settings.ACTION_SETTINGS),"Открываю настройки");
        } else if(s.contains("камер")){
            open(new Intent("android.media.action.STILL_IMAGE_CAMERA"),"Открываю камеру");
        } else if(s.contains("календар")){
            Intent i=new Intent(Intent.ACTION_VIEW,CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build());
            open(i,"Открываю календарь");
        } else if(s.contains("будильник")){
            Matcher m=Pattern.compile("(\\d{1,2})[:.](\\d{2})").matcher(s);
            Intent i=new Intent(AlarmClock.ACTION_SET_ALARM);
            if(m.find()){int h=Integer.parseInt(m.group(1)),min=Integer.parseInt(m.group(2));
                if(h>23||min>59){say("Неверное время будильника.");return;}
                i.putExtra(AlarmClock.EXTRA_HOUR,h);i.putExtra(AlarmClock.EXTRA_MINUTES,min);
            }
            i.putExtra(AlarmClock.EXTRA_MESSAGE,"Будильник от Кати");
            open(i,"Открываю установку будильника");
        } else if(s.contains("маршрут")||s.contains("навигатор")||s.contains("поехали")){
            String dest=raw.replaceFirst("(?iu)^.*?(?:маршрут до|маршрут в|навигатор до|поехали в)\\s*","").trim();
            if(dest.equals(raw)||dest.isEmpty()){say("Скажи: построй маршрут до, и назови адрес.");return;}
            String url="geo:0,0?q="+Uri.encode(dest);
            open(new Intent(Intent.ACTION_VIEW,Uri.parse(url)),"Показываю маршрут до "+dest);
        } else if(s.contains("привет")||s.contains("катя")){
            say("Привет! Я здесь. Чем помочь?");
        } else {
            say("Я услышала: "+raw+". Пока не умею выполнять эту команду, но мы добавим её в следующих версиях.");
        }
    }
    @Override protected void onDestroy(){if(recognizer!=null)recognizer.destroy();if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
