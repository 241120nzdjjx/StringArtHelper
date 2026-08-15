/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 */
package com.niuza.stringartreader2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.List;

@android.annotation.SuppressLint("ViewConstructor")
final class GeneratedPreviewView extends View {
    private final List<Integer> sequence;
    private final int pinCount;
    private float projectLineMm;
    private float circleDiameterMm;
    private boolean actualRatio;
    private float customLineMm;
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pin = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pinDot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pinLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float zoom = 1f, panX, panY, lastX, lastY, lastDistance;
    private boolean pinching;
    GeneratedPreviewView(Context context, List<Integer> value, int nails, float lineMm, int circleMm,
                         boolean useActualRatio, float customMm) {
        super(context); sequence=value; pinCount=nails;
        circleDiameterMm=Math.max(1f,circleMm);
        projectLineMm=lineMm;
        actualRatio=useActualRatio; customLineMm=customMm;
        pinDot.setColor(0xFF55555F);
        pinLabel.setColor(0xFF42424C);
    }
    void setThreadDisplay(boolean useActualRatio, float customMm) {
        actualRatio=useActualRatio;
        customLineMm=Math.max(.01f,Math.min(1f,customMm));
        invalidate();
    }
    void setProjectLineMm(float lineMm) {
        projectLineMm=Math.max(.01f,Math.min(1f,lineMm));
        invalidate();
    }
    void setCircleDiameterMm(float circleMm) {
        circleDiameterMm=Math.max(1f,circleMm);
        invalidate();
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); canvas.drawColor(Color.rgb(248,247,251));
        clampPan();
        float side=Math.min(getWidth(),getHeight()), c=getWidth()*.5f+panX, cy=getHeight()*.5f+panY, r=side*.45f*zoom;
        float strokeRatio = (actualRatio ? projectLineMm : customLineMm) / circleDiameterMm;
        float stroke = Math.max(.12f, 2f * r * strokeRatio);
        int alpha = Math.max(26, Math.min(82, Math.round(26f + side * strokeRatio * 90f)));
        line.setColor(Color.argb(alpha, 18, 18, 18)); line.setStyle(Paint.Style.STROKE); line.setStrokeWidth(stroke);
        for(int i=1;i<sequence.size();i++) { int a=sequence.get(i-1), b=sequence.get(i); if(a<0||a>=pinCount||b<0||b>=pinCount)continue; float ax=c+(float)Math.cos(Math.PI*2*a/pinCount)*r, ay=cy+(float)Math.sin(Math.PI*2*a/pinCount)*r, bx=c+(float)Math.cos(Math.PI*2*b/pinCount)*r, by=cy+(float)Math.sin(Math.PI*2*b/pinCount)*r; canvas.drawLine(ax,ay,bx,by,line); }
        pin.setStyle(Paint.Style.STROKE); pin.setStrokeWidth(1.5f); pin.setColor(0xFF333333); canvas.drawCircle(c,cy,r,pin);
        NailIndexRenderer.draw(canvas, pinCount, c, cy, r,
                getResources().getDisplayMetrics().density, pinDot, pinLabel);
    }

    @Override public boolean onTouchEvent(android.view.MotionEvent event) {
        if(event.getActionMasked()==android.view.MotionEvent.ACTION_DOWN && getParent()!=null)
            getParent().requestDisallowInterceptTouchEvent(true);
        if(event.getActionMasked()==android.view.MotionEvent.ACTION_POINTER_DOWN && event.getPointerCount()>=2){
            pinching=true;
            lastDistance=distance(event);
            return true;
        }
        if(event.getActionMasked()==android.view.MotionEvent.ACTION_DOWN){lastX=event.getX();lastY=event.getY();pinching=false;return true;}
        if(event.getActionMasked()==android.view.MotionEvent.ACTION_MOVE){if(pinching&&event.getPointerCount()>=2){float d=distance(event);if(lastDistance>0)zoom=Math.max(1f,Math.min(5f,zoom*d/lastDistance));lastDistance=d;}else{panX+=event.getX()-lastX;panY+=event.getY()-lastY;lastX=event.getX();lastY=event.getY();}clampPan();invalidate();return true;}
        if(event.getActionMasked()==android.view.MotionEvent.ACTION_UP||event.getActionMasked()==android.view.MotionEvent.ACTION_CANCEL){
            pinching=false;
            if(event.getActionMasked()==android.view.MotionEvent.ACTION_UP)performClick();
            if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(false);
        }else if(event.getActionMasked()==android.view.MotionEvent.ACTION_POINTER_UP)pinching=false;
        return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    private float distance(android.view.MotionEvent e){float x=e.getX(0)-e.getX(1),y=e.getY(0)-e.getY(1);return(float)Math.sqrt(x*x+y*y);}
    private void clampPan(){
        float side=Math.min(getWidth(),getHeight());
        if(side<=0f)return;
        float baseRadius=side*.45f,radius=baseRadius*zoom;
        float edgeAllowance=12f*getResources().getDisplayMetrics().density;
        float maxPan=Math.max(edgeAllowance,radius-baseRadius+edgeAllowance);
        panX=Math.max(-maxPan,Math.min(maxPan,panX));
        panY=Math.max(-maxPan,Math.min(maxPan,panY));
    }
}
