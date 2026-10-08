package com.liason.scalper;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends Activity {
    EditText price, dl, dh, sl, sh, atr, rr, risk, balance;
    CheckBox trend, bos, rejection;
    TextView signal, levels, score;
    int black=Color.rgb(20,20,20), gray=Color.rgb(95,95,95), green=Color.rgb(0,125,70), red=Color.rgb(190,25,25);

    TextView label(String s){ TextView t=new TextView(this); t.setText(s); t.setTextColor(gray); t.setTextSize(13); t.setPadding(2,12,2,4); return t; }
    EditText field(String value){ EditText e=new EditText(this); e.setText(value); e.setTextSize(16); e.setSingleLine(true); e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED); e.setPadding(12,2,12,2); return e; }
    LinearLayout row(){ LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); r.setWeightSum(2); return r; }
    void addPair(LinearLayout root,String title,EditText a,EditText b){ root.addView(label(title)); LinearLayout r=row(); r.addView(a,new LinearLayout.LayoutParams(0,58,1)); r.addView(b,new LinearLayout.LayoutParams(0,58,1)); root.addView(r); }
    double v(EditText e){ try{return Double.parseDouble(e.getText().toString());}catch(Exception x){return Double.NaN;} }
    String f(double x){return String.format(Locale.US,"%.2f",x);}

    @Override public void onCreate(Bundle b){ super.onCreate(b);
        ScrollView scroll=new ScrollView(this); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(20,16,20,24); root.setBackgroundColor(Color.WHITE); scroll.addView(root);
        TextView title=new TextView(this); title.setText("LIASON SCALPER 2.0"); title.setTextSize(27); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD); title.setTextColor(black); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView sub=new TextView(this); sub.setText("XAUUSD • M15 • Supply & Demand • DEMO"); sub.setGravity(Gravity.CENTER); sub.setTextColor(gray); sub.setPadding(0,2,0,16); root.addView(sub);
        root.addView(label("Current XAUUSD price")); price=field("4107.33"); root.addView(price);
        addPair(root,"Demand zone (low → high)",dl=field("4090"),dh=field("4100"));
        addPair(root,"Supply zone (low → high)",sl=field("4110"),sh=field("4122"));
        root.addView(label("ATR buffer / volatility")); atr=field("10"); root.addView(atr);
        root.addView(label("Risk / Reward")); rr=field("2.0"); root.addView(rr);
        root.addView(label("Account balance (demo)")); balance=field("500"); root.addView(balance);
        root.addView(label("Risk per trade (%)")); risk=field("1"); root.addView(risk);
        trend=new CheckBox(this); trend.setText("H4 trend agrees with setup"); trend.setChecked(true); root.addView(trend);
        bos=new CheckBox(this); bos.setText("M5 break of structure confirmed"); root.addView(bos);
        rejection=new CheckBox(this); rejection.setText("M15 rejection candle confirmed"); root.addView(rejection);
        Button scan=new Button(this); scan.setText("SCAN FOR SETUP"); root.addView(scan);
        signal=new TextView(this); signal.setTextSize(23); signal.setTypeface(Typeface.DEFAULT,Typeface.BOLD); signal.setGravity(Gravity.CENTER); signal.setPadding(6,20,6,8); root.addView(signal);
        score=new TextView(this); score.setTextSize(15); score.setGravity(Gravity.CENTER); score.setTextColor(gray); root.addView(score);
        levels=new TextView(this); levels.setTextSize(16); levels.setPadding(8,12,8,20); root.addView(levels);
        TextView note=new TextView(this); note.setText("DEMO SIGNAL ENGINE. No broker orders are sent. Signals are rule-based and can lose money; always backtest and confirm on your broker's price feed."); note.setTextColor(gray); root.addView(note);
        scan.setOnClickListener(v->scan()); setContentView(scroll);
    }
    void scan(){
        double p=v(price), demandL=v(dl), demandH=v(dh), supplyL=v(sl), supplyH=v(sh), a=v(atr), r=v(rr), bal=v(balance), rp=v(risk);
        if(Double.isNaN(p)||Double.isNaN(demandL)||Double.isNaN(demandH)||Double.isNaN(supplyL)||Double.isNaN(supplyH)||Double.isNaN(a)||Double.isNaN(r)||Double.isNaN(bal)||Double.isNaN(rp)){ signal.setText("CHECK INPUTS"); levels.setText(""); return; }
        if(r<1){ signal.setText("RR MUST BE ≥ 1:1"); levels.setText(""); return; }
        boolean inD=p>=demandL&&p<=demandH, inS=p>=supplyL&&p<=supplyH;
        int s=0; if(trend.isChecked())s+=30; if(bos.isChecked())s+=30; if(rejection.isChecked())s+=25; if(inD||inS)s+=15;
        if(inD && trend.isChecked() && bos.isChecked() && rejection.isChecked()){
            double entry=p, stop=demandL-a, tp=entry+(entry-stop)*r, riskCash=bal*rp/100.0, dist=Math.abs(entry-stop), units=dist>0?riskCash/dist:0;
            signal.setText("BUY SETUP"); signal.setTextColor(green); score.setText("Confidence score: "+s+"/100"); levels.setText("Entry: "+f(entry)+"\nSL: "+f(stop)+"\nTP: "+f(tp)+"\nRisk: $"+f(riskCash)+"\nDistance to SL: "+f(dist)+"\nDemo position-size units: "+f(units));
        } else if(inS && trend.isChecked() && bos.isChecked() && rejection.isChecked()){
            double entry=p, stop=supplyH+a, tp=entry-(stop-entry)*r, riskCash=bal*rp/100.0, dist=Math.abs(stop-entry), units=dist>0?riskCash/dist:0;
            signal.setText("SELL SETUP"); signal.setTextColor(red); score.setText("Confidence score: "+s+"/100"); levels.setText("Entry: "+f(entry)+"\nSL: "+f(stop)+"\nTP: "+f(tp)+"\nRisk: $"+f(riskCash)+"\nDistance to SL: "+f(dist)+"\nDemo position-size units: "+f(units));
        } else {
            signal.setText("WAIT — NO CONFIRMED TRADE"); signal.setTextColor(black); score.setText("Setup score: "+s+"/100"); String why=inD?"Price is in demand: need bullish H4/M5/M15 confirmation.":inS?"Price is in supply: need bearish H4/M5/M15 confirmation.":"Wait for price to retest supply or demand."; levels.setText(why);
        }
    }
}
