package com.android.toolbox.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.toolbox.Log;
import com.android.toolbox.ToolBox;
import com.android.toolbox.listeners.OnCustomClickListener;
import com.android.toolbox.listeners.OnCustomClickListenerInterface;

/**
 * @author abezzarg (amine.bezzarga@labgency.com)
 */
public class ImageZonePagerAdapter extends AbstractPagerAdapter<String>{
		private final static String TAG = ImageZonePagerAdapter.class.getSimpleName();
		private LayoutInflater mInflater = null;
		private Context mContext;
		private boolean mIsSoftwareRendered;
		private OnCustomClickListenerInterface mCallback;
		
		public ImageZonePagerAdapter(Context context, boolean isSoftwareRendered){
			mContext = context;
			mInflater = LayoutInflater.from(context);
			mIsSoftwareRendered = isSoftwareRendered;
		}
		
		public ImageZonePagerAdapter(Context context, boolean isSoftwareRendered, OnCustomClickListenerInterface callback){
			mContext = context;
			mInflater = LayoutInflater.from(context);
			mIsSoftwareRendered = isSoftwareRendered;
			mCallback = callback;
		}	

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView item = new ImageView(mContext);
			MarginLayoutParams params = new MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.MATCH_PARENT);
			params.setMargins(10, 0, 10, 0);
			item.setLayoutParams(params);
			
			container.addView(item);
			String imageUrl = mData.get(position);
			
			if (imageUrl == null){
				Log.e(TAG, "instantiateItem | oups mo is null");
				return item;
			}
			
			//disable hardware acceleration to render this view, because some of the image are too large to be uploaded as a opengl texture
			// avoid error | W/OpenGLRenderer: Bitmap too large to be uploaded into a texture
//			Log.e(TAG, "is imageview hardware accelerated before:" + coverImgView.isHardwareAccelerated());
			if (Build.VERSION.SDK_INT >= 11 && mIsSoftwareRendered){
				((View) item.getParent()).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
			
			
			item.setScaleType(ScaleType.FIT_CENTER);
			
			int resourceId = ToolBox.getResourceIdentifier(mContext, imageUrl, "drawable", "com.universcine.fr.vod");
			if(resourceId != 0){//its a resource
				ToolBox.pickupDefaultImage(resourceId, item);
			}else{//its an url
				ToolBox.downloadImageWithoutFade(imageUrl, item);
			}
			if ( mCallback!=null ) item.setOnClickListener(new OnCustomClickListener(mCallback, position)); 
			
			return item;
		}
}
