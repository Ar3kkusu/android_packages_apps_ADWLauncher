package org.adw.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;

public class DesktopIndicator extends ViewGroup implements AnimationListener {
	private View mIndicator;
	public static final int INDICATOR_TYPE_PAGER=1;
	public static final int INDICATOR_TYPE_SLIDER_TOP=2;
	public static final int INDICATOR_TYPE_SLIDER_BOTTOM=3;
	private int mIndicatorType=1;
	private int mItems=5;
	private int mCurrent=0;
	private int mIndicatorColor=0x99FFFFFF;
	private int mVisibleTime=300;
	private Animation mAnimation;
	private Handler mHandler=new Handler();
	public DesktopIndicator(Context context) {
		super(context);
		initIndicator(context);
	}

	public DesktopIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		initIndicator(context);
	}

	public DesktopIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initIndicator(context);
	}
	private void initIndicator(Context context){
		switch(mIndicatorType){
		case INDICATOR_TYPE_PAGER:
			mIndicator=new PreviewPager(context);
			((PreviewPager) mIndicator).setTotalItems(mItems);
			((PreviewPager) mIndicator).setCurrentItem(mCurrent);
			break;
		case INDICATOR_TYPE_SLIDER_TOP:
		case INDICATOR_TYPE_SLIDER_BOTTOM:
			mIndicator=new SliderIndicator(context);
			//int offset=((int) (getWidth()*(mCurrent/mItems)))-mIndicator.getLeft();
			//mIndicator.offsetLeftAndRight(offset);
			break;
		}
		addView(mIndicator);
	}
	public void setItems(int items){
		mItems=items;
		switch(mIndicatorType){
		case INDICATOR_TYPE_PAGER:
			((PreviewPager) mIndicator).setTotalItems(mItems);
			((PreviewPager) mIndicator).setCurrentItem(mCurrent);
		break;
		case INDICATOR_TYPE_SLIDER_TOP:
		case INDICATOR_TYPE_SLIDER_BOTTOM:
			((SliderIndicator)mIndicator).setTotalItems(mItems);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		LinearLayout.LayoutParams params;
		switch(mIndicatorType){
		case INDICATOR_TYPE_PAGER:
	        params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	        mIndicator.measure(getWidth(), getHeight());
	        mIndicator.setLayoutParams(params);
	        mIndicator.layout(0, 0, getWidth(), 20);
			break;
		case INDICATOR_TYPE_SLIDER_BOTTOM:
	        params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	        mIndicator.measure(getWidth(), getHeight());
	        mIndicator.setLayoutParams(params);
	        mIndicator.layout(0, getHeight()-SliderIndicator.INDICATOR_HEIGHT, getWidth(), getHeight());
	        break;
		case INDICATOR_TYPE_SLIDER_TOP:
	        params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	        mIndicator.measure(getWidth(), getHeight());
	        mIndicator.setLayoutParams(params);
	        mIndicator.layout(0, 0, getWidth(), SliderIndicator.INDICATOR_HEIGHT);
	        break;
		}
	}
	public void indicate(float percent){
		setVisibility(View.VISIBLE);
		int position=Math.round(mItems*percent);
		switch(mIndicatorType){
		case INDICATOR_TYPE_PAGER:
			((PreviewPager) mIndicator).setCurrentItem(position);
			break;
		case INDICATOR_TYPE_SLIDER_BOTTOM:
		case INDICATOR_TYPE_SLIDER_TOP:
			int offset=((int) (getWidth()*percent))-mIndicator.getLeft();
			//mIndicator.offsetLeftAndRight(offset);
			((SliderIndicator)mIndicator).setOffset(offset);
			mIndicator.invalidate();
		}
        mHandler.removeCallbacks(mAutoHide);
        if(mVisibleTime>0)
        	mHandler.postDelayed(mAutoHide, mVisibleTime);
		mCurrent=position;
	}
	public void fullIndicate(int position){
		setVisibility(View.VISIBLE);
		switch(mIndicatorType){
		case INDICATOR_TYPE_PAGER:
			((PreviewPager) mIndicator).setCurrentItem(position);
			break;
		case INDICATOR_TYPE_SLIDER_BOTTOM:
		case INDICATOR_TYPE_SLIDER_TOP:
		}
        mHandler.removeCallbacks(mAutoHide);
        if(mVisibleTime>0)
        	mHandler.postDelayed(mAutoHide, mVisibleTime);
        mCurrent=position;
	}
	public void setType(int type){
		if(type!=mIndicatorType){
			mIndicatorType=type;
			removeView(mIndicator);
			initIndicator(getContext());
		}
	}
	public void setAutoHide(boolean autohide){
		if(autohide){
			mVisibleTime=300;
			setVisibility(INVISIBLE);
		}else{
			mVisibleTime=-1;
			setVisibility(VISIBLE);
		}
	}
	private Runnable mAutoHide = new Runnable() {
		   public void run() {
			   if(mAnimation==null){
				   mAnimation=AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_fast);
				   mAnimation.setAnimationListener(DesktopIndicator.this);
			   }else{
				   if(!mAnimation.hasEnded())mAnimation.cancel();
			   }
			   startAnimation(mAnimation);				   
		   }
		};	
	
	
	/**
	 * Simple line Indicator for desktop scrolling
	 * @author adw
	 *
	 */
	private class SliderIndicator extends View{
		public static final int INDICATOR_HEIGHT=4;
		private RectF mRect;
		private Paint mPaint;
		private int mTotalItems=5;
		public SliderIndicator(Context context) {
			super(context);
			mPaint=new Paint();
			mPaint.setColor(mIndicatorColor);
			mRect=new RectF(0, 0, 5, INDICATOR_HEIGHT);
		}
		@Override
		public void draw(Canvas canvas) {
			canvas.drawRoundRect(mRect, 2, 2, mPaint);
		}
		public void setTotalItems(int items){
			mTotalItems=items;
		}
		public void setOffset(int offset){
			int width=getWidth()/mTotalItems;
			mRect.left=offset;
			mRect.right=offset+width;
		}
	}


	@Override
	public void onAnimationEnd(Animation animation) {
		setVisibility(View.INVISIBLE);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}
}
