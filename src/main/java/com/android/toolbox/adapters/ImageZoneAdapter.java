package com.android.toolbox.adapters;

import java.lang.reflect.Field;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.toolbox.ToolBox;


public class ImageZoneAdapter extends AbstractBaseAdapter<String> {
	private final static String TAG = ImageZoneAdapter.class.getSimpleName();
	protected Context mContext;
	boolean mIsSoftwareRendered;
	private int mBackgroundResource = -1;
	private int mImageResource		= -1;
	private int mItemWidth = LayoutParams.WRAP_CONTENT;
	private int mItemHeight = LayoutParams.WRAP_CONTENT;
	private ScaleType mItemScaleType = ScaleType.FIT_CENTER;
	private int mItemPadding = 0;
	private boolean mItemAdjustViewBound = false;
	private boolean mWantThumbnail;

	public ImageZoneAdapter(Context c, boolean isSoftwareRendered) {
		mContext = c;
		mIsSoftwareRendered = isSoftwareRendered;		
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {  // if it's not recycled, initialize some attributes
			imageView = new ImageView(mContext);
			imageView.setAdjustViewBounds(mItemAdjustViewBound);
			imageView.setScaleType(mItemScaleType);
			if(mBackgroundResource!=-1)
				imageView.setBackgroundResource(mBackgroundResource);
//			imageView.setPadding(mItemPadding, mItemPadding, mItemPadding, mItemPadding);

			//Width his taken from parent gridview which is setted to 100dp
			if(parent instanceof AbsListView){
				imageView.setLayoutParams(new AbsListView.LayoutParams(mItemWidth, mItemHeight));
			}else if(parent instanceof Gallery){
				imageView.setLayoutParams(new Gallery.LayoutParams(mItemWidth, mItemHeight));
			}else if( parent instanceof ViewGroup){
				imageView.setLayoutParams(new ViewGroup.LayoutParams(mItemWidth, mItemHeight));
			}
			
			//disable hardware acceleration to render this view, because some of the image are too large to be uploaded as a opengl texture
			// avoid error | W/OpenGLRenderer: Bitmap too large to be uploaded into a texture
			//		Log.e(TAG, "is imageview hardware accelerated before:" + coverImgView.isHardwareAccelerated());
			if (Build.VERSION.SDK_INT >= 11 && mIsSoftwareRendered){
				imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}

			if(imageView.getScaleType()==ScaleType.CENTER_CROP){
				setCropToPadding(parent, imageView, true);
			}

		} else {
			imageView = (ImageView) convertView;
		}
		
		
//		if(mImageResource!=-1)
//			ToolBox.pickupDefaultImage(mImageResource, imageView);
//		String url = getItemAtPosition(position);
//		ToolBox.downloadImage(url, imageView, mWantThumbnail, true);
		
		if(mImageResource!=-1)
			ToolBox.pickupDefaultImage(mImageResource, imageView);
		String url = getItemAtPosition(position);
		int resourceId = ToolBox.getResourceIdentifier(mContext, url, "drawable", mContext.getPackageName());
		if(resourceId != 0){//its a resource
			ToolBox.pickupDefaultImage(resourceId, imageView);
		}else{//its an url
			ToolBox.downloadImage(url, imageView, mWantThumbnail, true);
		}

		return imageView;
	}

	private void setCropToPadding(ViewGroup parent, ImageView imageView, boolean b) {
		if (Build.VERSION.SDK_INT >= 16){
			imageView.setCropToPadding(b);
		}else{//We will try a hack to set attribute using reflection
			Field field;
			try {
				field = ImageView.class.getDeclaredField("mCropToPadding");
				field.setAccessible(true);
				field.set(imageView, b);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public void setItemBackgroundResource(int resource){
		mBackgroundResource = resource;
	};
	
	public void setItemImageResource(int resource){
		mImageResource = resource;
	};

	public void setItemWidth(int width){
		mItemWidth = width;
	};	

	public void setItemHeight(int height){
		mItemHeight = height;
	};

	public void setItemPadding(int padding){
		mItemPadding = padding;
	};

	public void setItemScaleType(ScaleType scaleType){
		mItemScaleType = scaleType;
	};

	public void setItemAdjustViewBounds(boolean adjustViewBounds){
		mItemAdjustViewBound = adjustViewBounds;
	};
	
	public void setWantThumbnail(boolean b){
		mWantThumbnail = b;
	}

}
