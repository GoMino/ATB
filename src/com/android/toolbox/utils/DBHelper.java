package com.android.toolbox.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;

import com.android.toolbox.Log;

public class DBHelper {
	//HELPER FUNCTION
	
	public static boolean checkDataBase(String fileName) {
	    java.io.File dbFile = new java.io.File(fileName);
	    return dbFile.exists();
	}
	
	public static void backupDBonSDcard(Context context, String tableName){
//		String DB_PATH = "/data/data/"+context.getPackageName()+"/databases/";
//		String DB_PATH = context.getFilesDir().getPath()+"/databases/";
		String DB_PATH = context.getDatabasePath(tableName).getPath();
		Log.d("DB_PATH:" + DB_PATH);
		if(checkDataBase(DB_PATH)){
			InputStream myInput;
			try {
				Log.e("[backupDBonSDcard] saving file to SDCARD");
				myInput = new FileInputStream(DB_PATH);

				// Path to the just created empty db
				String outFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + java.io.File.separator + tableName;

				//Open the empty db as the output stream
				OutputStream myOutput;
				try {
					myOutput = new FileOutputStream(outFileName);
					//transfer bytes from the inputfile to the outputfile
					byte[] buffer = new byte[1024];
					int length;
					while ((length = myInput.read(buffer))>0){
						myOutput.write(buffer, 0, length);
					}
					//Close the streams
					myOutput.flush();
					myOutput.close();
					myInput.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}else{
			Log.d("DB "+tableName+" not found");
		}
	}
	
	public static void deleteDBBackup(String tableName){
		String DB_BACKUP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + java.io.File.separator + tableName;
		java.io.File dbFile = new java.io.File(DB_BACKUP_PATH);
		if (dbFile.exists()){
			dbFile.delete();
		}
	}
}
