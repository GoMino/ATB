package com.android.toolbox.managers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.android.toolbox.Log;
import com.android.toolbox.R;


/**
 * @author gomino (amine.bezzarga@labgency.com)
 */
public class CustomFragmentManager {

	private static final String TAG = CustomFragmentManager.class.getSimpleName();
	private FragmentManager mFragmentManager;
	
	public CustomFragmentManager(Context context, FragmentManager fm) {
		mFragmentManager = fm;
	}
	
	//*****************************//
	//* Custom Fragment Managment *//
	//*****************************//
	
	public FragmentManager getFragmentManager(){
		return mFragmentManager;
	}
	
	/**
	 * add a fragment to the activity
	 * @param id, the id of the framelayout where to put the fragment
	 * @param f, the fragment to insert
	 * @param addToBackstack, is added to backstack ?
	 * @param stackName, the name of the stack record
	 * @return true if the fragment transaction has been comitted, false otherwise
	 */
	public boolean insertFragmentToActivity(int id, Fragment f, boolean addToBackstack, String stackName, boolean animated){
		if (f != null){
			FragmentTransaction ft = mFragmentManager.beginTransaction();

			if(TextUtils.isEmpty(stackName)) stackName = f.getClass().getSimpleName();
			
			if (addToBackstack) {
				addToBackStack(f, ft, stackName);
				if(animated){
					ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
				}
			}
			
			ft.replace(id, f, stackName);
			
			ft.commit();
			return true;
		}else{
			return false;
		}
	}
	
	public boolean insertFragmentToActivity(int id, Fragment f, boolean addToBackstack, String stackName){
		return insertFragmentToActivity(id, f, addToBackstack, stackName, false);
	}

	/**
	 * completely clear the backstack
	 */
	public void clearBackstack(){
		mFragmentManager.popBackStackImmediate(mFragmentManager.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		Log.v(TAG,"[clearBackstack] stack cleared  - stack entry count :" + mFragmentManager.getBackStackEntryCount() + " list size : " + mFragmentManager.getBackStackEntryCount());
	}

	
	public boolean isFragmentAlreadyInStack(Fragment fragment){
		for(int i=0; i<mFragmentManager.getBackStackEntryCount(); i++){
			BackStackEntry stackedEntry = mFragmentManager.getBackStackEntryAt(i);
			Fragment stackedFragment = mFragmentManager.findFragmentByTag(stackedEntry.getName());
			if(stackedFragment!=null){
				Log.v(TAG, "[isFragmentAlreadyInStack] is fragment from backstack: " + stackedFragment.getClass().getSimpleName() + "(" +stackedFragment.getId()+") the same type of " + fragment.getClass().getSimpleName() + "("+fragment.getId()+")");
				if (stackedFragment.getClass().isAssignableFrom(fragment.getClass())){
					Log.v(TAG, "[isFragmentAlreadyInStack] "+fragment.getClass().getSimpleName() + " already stacked");
					return true;
				}
			}
		}
		return false;
	}

	
	public int getFragmentPositionInStack(Fragment fragment, final boolean fromEnd){
		int i;
		int n;
		if(fromEnd){
			i = mFragmentManager.getBackStackEntryCount()-1;
			n = 0;
		}else{
			i = 0;
			n =  mFragmentManager.getBackStackEntryCount();
		}
		for(;(fromEnd)?i>=n:i<n;){

			BackStackEntry stackedEntry = mFragmentManager.getBackStackEntryAt(i);
			Fragment stackedFragment = mFragmentManager.findFragmentByTag(stackedEntry.getName());
			
			if(stackedFragment.getClass().isAssignableFrom(fragment.getClass())){
				Log.v(TAG, "[getFragmentPositionInStack] Fragment"+ i+"/"+mFragmentManager.getBackStackEntryCount() +": " + stackedFragment.getClass().getSimpleName());

				return (fromEnd)?mFragmentManager.getBackStackEntryCount()-i:i;
			}
			
			if(fromEnd){
				i--;
			}else{
				i++;
			}

		}
		return -1;
	}
	
	public void addToBackStack(Fragment fragmentToAdd, FragmentTransaction ft, String stackName){
		if (fragmentToAdd!=null){
			ft.addToBackStack(stackName);
			Log.v(TAG, "[addToBackStack] add fragment to backstack "+ (stackName!=null ? "("+stackName+")":"") +": " + fragmentToAdd.getClass().getSimpleName() + " id :" +fragmentToAdd.getId() + " stack entry count :" + mFragmentManager.getBackStackEntryCount());
		}
	}

	public void popBackStack(){
		Fragment lastStackedFragment = getLastStackedFragment();
		if (lastStackedFragment!=null){			
			mFragmentManager.popBackStackImmediate();
			Log.v(TAG, "[popBackStack] popping fragment from backstack: " + lastStackedFragment.getClass().getSimpleName() + " id :" +lastStackedFragment.getId() + " stack entry count :" + mFragmentManager.getBackStackEntryCount());

		}
	}
	
	public boolean popBackStack(String tillName, boolean inclusive){
		boolean hasPoppedSomething = mFragmentManager.popBackStackImmediate(tillName, (inclusive)?FragmentManager.POP_BACK_STACK_INCLUSIVE:0 );
		return hasPoppedSomething;
	}

	public Fragment getLastStackedFragment(){

		if (mFragmentManager.getBackStackEntryCount()>0){
			BackStackEntry stackedEntry = mFragmentManager.getBackStackEntryAt(mFragmentManager.getBackStackEntryCount()-1);
			Fragment lastStackedFragment = mFragmentManager.findFragmentByTag(stackedEntry.getName());
			Log.v(TAG, "[getLastStackedFragment] lastStackedFragment is : " + lastStackedFragment.getClass().getSimpleName() + " id :" +lastStackedFragment.getId());
			return lastStackedFragment;
		}
		
		return null;
	};
	
	public int getBackStackEntryCount(){
		return mFragmentManager.getBackStackEntryCount();
	}
	

}
