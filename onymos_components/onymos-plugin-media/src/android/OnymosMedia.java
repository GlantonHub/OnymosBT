/*
 * Copyright 2015-2016 Onymos Inc
 * 
 */

package com.onymos.components.media;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.io.ByteArrayOutputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import android.graphics.BitmapFactory;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.util.Base64;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.media.ExifInterface;
import android.os.Build;

import android.Manifest;
import android.Manifest.permission_group;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.media.MediaMetadataRetriever;

import org.apache.cordova.CordovaInterface;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.Clip;
import org.ffmpeg.android.ShellUtils.ShellCallback;

public class OnymosMedia extends CordovaPlugin implements MediaScannerConnectionClient  {

		private static final int DURI = 0; 
		private static final int FURI = 1;              
		private static final int NURI = 2;
		
		private static final int PIC = 0;              
		private static final int VD = 1;
		private static final int AM = 2;              
		private static final String GA = "Get All";
		private static final String GV = "Get Video";
		
		private static final String LTAG = "OnymosMedia";

		private static final int HQ = 0;
		private static final int MQ = 1;
		private static final int LQ = 2;
		
		private static final int AB = 0;
		private static final int CD = 1;
		private static final int EF = 2;
		private static final int GH = 3;
		private static final int PL = 0;          
		private static final int CAM = 1;                
		private static final int SPA = 2;  

		private int mq;                   
		private int tw;                
		private int th;                              
		private int et;               
		private int mt; 
		private Uri iuri;
		private int st ;
		private	int dt ;                   
		private boolean sa;       
		private boolean co;     
		private boolean oc;   
		private boolean ae;
		private JSONArray executeArgs;              

		public CallbackContext callbackContext;
		private MediaScannerConnection conn; 
		private int npics;

											
		private Uri cUri;
		private Uri scanMe; 

		protected final static String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE }; 

    //protected final static String[] permissions = { Manifest.permission_group.STORAGE };

		public static final int PERMISSION_DENIED_ERROR = 20;
    public static final int TAKE_PIC_SEC = 0;
    public static final int SAVE_TO_ALBUM_SEC = 1;
    public static final int CREATE_IMAGE_SEC = 2;
    public static final int CREATE_THUMBNAIL_SEC = 3;
    public static final int CREATE_VIDEO_SEC = 4;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
      super.initialize(cordova, webView);
      //checkReadStoragePermission();  
    }

		/**
		 * Executes the request and returns PluginResult object with a status and message.
		 */

		public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
				this.callbackContext = callbackContext;
				this.executeArgs = args;

				if (action.equals("takePicture")) {
						
						this.st = CAM;
						this.dt = FURI;
						this.sa = false;
						this.th = 0;
						this.tw = 0;
						this.et = 0;
						this.mt = 0;
						this.mq = 80;

						this.mq = args.getInt(0);
						this.dt = args.getInt(1);
						this.st = args.getInt(2);
						this.tw = args.getInt(3);
						this.th = args.getInt(4);
						this.et = args.getInt(5);
						this.mt = args.getInt(6);
						this.ae = args.getBoolean(7);
						this.co = args.getBoolean(8);
						this.sa = args.getBoolean(9);

            if(this.st == 100) {
            	this.st = 0;
            } else if(this.st == 101) {
            	this.st = 1;
            } else if(this.st == 102) {
            	this.st = 2;
            }

            if(this.mt == 200) {
            	this.mt = 0;
            } else if(this.mt == 201) {
            	this.mt = 1;
            } else if(this.mt == 202) {
            	this.mt = 2;
            }

						if (this.tw < 1) {
								this.tw = -1;
						}
						if (this.th < 1) {
								this.th = -1;
						}

						 try {
								if (st == CAM) {
									this.co = true;
									if (Build.VERSION.SDK_INT >= 23) {
	    							// Marshmallow+
										checkReadStoragePermission(TAKE_PIC_SEC);				
									} else {
	    							// Pre-Marshmallow
	    							this.TPFC(dt, et);
									}
								}
								if ((st == PL) || (st == SPA)) {
									if (Build.VERSION.SDK_INT >= 23) {
	    							// Marshmallow+
										checkReadStoragePermission(CREATE_IMAGE_SEC);				
									} else {
	    							// Pre-Marshmallow
	    							this.FIGHJ(st, dt, et);
									}
							  }
						}
						catch (IllegalArgumentException e)
						{
								callbackContext.error("Illegal Argument Exception");
								PluginResult r = new PluginResult(PluginResult.Status.ERROR);
								callbackContext.sendPluginResult(r);
								return true;
						}
						 
						PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
						r.setKeepCallback(true);
						callbackContext.sendPluginResult(r);
						
						return true;
				} else if (action.equals("transcodeVideo")) {
						try {
								if (Build.VERSION.SDK_INT >= 23) {
    							// Marshmallow+
									checkReadStoragePermission(CREATE_VIDEO_SEC);				
								} else {
    							// Pre-Marshmallow
    							this.HQMPMOV(this.executeArgs, callbackContext);
								}
						} catch (IOException e) {
								callbackContext.error("Illegal Exception");
								PluginResult r = new PluginResult(PluginResult.Status.ERROR);
								callbackContext.sendPluginResult(r);  
						}
						return true;
				} else if (action.equals("createThumbnail")) {
						
						try {

								//System.out.println("In createThumbnail : Build.VERSION.SDK_INT" + Build.VERSION.SDK_INT);			
								if (Build.VERSION.SDK_INT >= 23) {
    							// Marshmallow+
									checkReadStoragePermission(CREATE_THUMBNAIL_SEC);				
								} else {
    							// Pre-Marshmallow
    							this.CTCR(this.executeArgs);
								}
								//this.CTCR();				
						} catch (Exception e) {
								callbackContext.error("Illegal Exception");
								PluginResult r = new PluginResult(PluginResult.Status.ERROR);
								callbackContext.sendPluginResult(r);  
						}
						return true;
				} else if (action.equals("getApplicationName")) {

					String appName = this.cordova.getActivity().getApplicationContext().getPackageName();

					if (appName != null) {
						PluginResult result = new PluginResult(PluginResult.Status.OK, appName);
						result.setKeepCallback(true);
						callbackContext.sendPluginResult(result);
					}
					else {
						PluginResult r = new PluginResult(PluginResult.Status.ERROR);
						r.setKeepCallback(true);
						callbackContext.sendPluginResult(r); 
					}
					return true;
				}	
				return false;
		}

		public void TPFC(int returnType, int encodingType) {
				
				this.npics = OnymosMediaUtil.qIDB(OnymosMediaUtil.wCS(),this.cordova).getCount();

				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				File photo = OnymosMediaUtil.cCF(encodingType,this.cordova);
				intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
				this.iuri = Uri.fromFile(photo);

				if (this.cordova != null) {

						PackageManager mPm = this.cordova.getActivity().getPackageManager();
						if(intent.resolveActivity(mPm) != null)
						{

								this.cordova.startActivityForResult((CordovaPlugin) this, intent, (CAM + 1) * 16 + returnType + 1);
						}
						else
						{
								LOG.d(LTAG, "Error: You don't have a default camera.  Your device may not be CTS complaint.");
						}
				}
		}

		public void FIGHJ(int srcType, int returnType, int encodingType) {
				Intent intent = new Intent();
				String title = GA;
				cUri = null;

				if (this.mt == PIC) {
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);    
				} else if (this.mt == VD) {
					intent.setType("video/*");
					title = GV;
					intent.setAction(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
				} else if (this.mt == AM) {
					intent.setType("*/*");
					title = GA;
					intent.setAction(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
				}

				if (this.cordova != null) {
						this.cordova.startActivityForResult((CordovaPlugin) this, Intent.createChooser(intent,
										new String(title)), (srcType + 1) * 16 + returnType + 1);
				}
		}

		public void onActivityResult(int requestCode, int resultCode, Intent intent) {
			
			//System.out.println("DEBUG : JAVA : onActivityResult - requestCode[" + requestCode + "]");
			//System.out.println("DEBUG : JAVA : onActivityResult - requestCode[" + resultCode + "]");
			
			int st = (requestCode / 16) - 1;
			int dt = (requestCode % 16) - 1;

			if (requestCode >= 100) {
					if (resultCode == Activity.RESULT_OK) {

							dt = requestCode - 100;
							try {
									prfc(dt, intent);
							} catch (IOException e) {
									e.printStackTrace();
									Log.e(LTAG, "Unable to write to file");
							}

					}// If cancelled
					else if (resultCode == Activity.RESULT_CANCELED) {
							this.failMedia("Camera cancelled.");
					}
					else {
							this.failMedia("Did not complete!");
					}

			}         
			else if (st == CAM) {
						if (resultCode == Activity.RESULT_OK) {
								try {
										if(this.ae)
										{
												Uri tmpFile = Uri.fromFile(OnymosMediaUtil.cCF(this.et, this.cordova));
												percp(tmpFile, dt, intent);
										}
										else {
												this.prfc(dt, intent);
										}
								} catch (IOException e) {
										e.printStackTrace();
										this.failMedia("Error capturing image.");
								}
						}

						// If cancelled
						else if (resultCode == Activity.RESULT_CANCELED) {
								this.failMedia("Camera cancelled.");
						}

						// If something else
						else {
								this.failMedia("Did not complete!");
						}
				}
			// If retrieving photo from library
			else if ((st == 0) || (st == 2)) {
					if (resultCode == Activity.RESULT_OK && intent != null) {
							this.LMNOP(dt, intent);
					}
					else if (resultCode == Activity.RESULT_CANCELED) {
							this.failMedia("Selection cancelled.");
					}
					else {
							this.failMedia("Selection did not complete!");
					}
			}
		}

	private void percp(Uri picUri, int destType, Intent cameraIntent) {
		try {
			Intent cropIntent = new Intent("com.android.camera.action.CROP");

			// indicate image type and Uri
			cropIntent.setDataAndType(picUri, "image/*");
			// set crop properties
			cropIntent.putExtra("crop", "true");

			// indicate output X and Y
			if (tw > 0) {
					cropIntent.putExtra("outputX", tw);
			}
			if (th > 0) {
					cropIntent.putExtra("outputY", th);
			}
			if (th > 0 && tw > 0 && tw == th) {
					cropIntent.putExtra("aspectX", 1);
					cropIntent.putExtra("aspectY", 1);
			}
			// create new file handle to get full resolution crop
			cUri = Uri.fromFile(OnymosMediaUtil.cCF(this.et, System.currentTimeMillis() + "",this.cordova));
			cropIntent.putExtra("output", cUri);

			// start the activity - we handle returning in onActivityResult

			if (this.cordova != null) {
				this.cordova.startActivityForResult((CordovaPlugin) this,
						cropIntent, 100 + destType);
			}
		} catch (ActivityNotFoundException anfe) {
			Log.e(LTAG, "Crop operation not supported on this device");
			try {
					prfc(destType, cameraIntent);
			}
			catch (IOException e)
			{
					e.printStackTrace();
					Log.e(LTAG, "Unable to write to file");
			}
		}
	}


		private void LMNOP(int destType, Intent intent) {
				
				Uri uri = intent.getData();
				
				if (uri == null) {
						if (cUri != null) {
								uri = cUri;
						} else {
								this.failMedia("null data from photo library");
								return;
						}
				}
				
				int rotate = 0;
				String uriString = uri.toString();
				String mimeType = OnymosMediaUtil.getMT(uriString, this.cordova);

				if (mimeType != null && (mimeType.toString()).indexOf("video") != -1) {
						if (uriString != null && uriString.indexOf("picasa") != -1) {
								this.callbackContext.success(OnymosMediaUtil.getMRP(uri,this.cordova));
						}
						else {
								 if (uri != null && (uri.toString()).indexOf("ACTUAL")  != -1) {
									String videoUri = OnymosMediaUtil.getRVPWAU(uri);
									this.callbackContext.success(videoUri);
								 } else {
									this.callbackContext.success(uri.toString());
								 	//this.callbackContext.success(fileLocation);
								 }  
						}
				} else if (mimeType != null && (mimeType.toString()).indexOf("image") != -1) {
						
						if (!("image/jpeg".equalsIgnoreCase(mimeType) || "image/png".equalsIgnoreCase(mimeType))) {
								this.failMedia("Unable to retrieve path to picture!");
								return;
						}  

							if (destType == FURI || destType == NURI) {

								String newFilePath = OnymosMediaUtil.getSPU(uri, this.cordova);
								Uri newUri = null ;

								if (newFilePath != null) {
									newUri = Uri.parse(newFilePath);
								}

								if (newUri == null || newFilePath == null) {
									this.failMedia("null data from photo library");
									return;
								} else {
									newFilePath = newFilePath + "?jpg";
									this.callbackContext.success(newFilePath);
								}
								System.gc();
						}
				} else { /* Neither Video nor Image */
						this.failMedia("Media selected is neither a Video nor Photo");
						return;
				}
		}  

private void prfc(int dt, Intent intent) throws IOException {
				int rotate = 0;        
				ExifInterface inFile = null;

				String sourcePath = (this.ae && this.cUri != null) ?
						OnymosMediaUtil.sFP(this.cUri.toString()) :
						OnymosMediaUtil.sFP(this.iuri.toString());
				Uri myURI = Uri.parse(sourcePath);
				if (this.et == 0) {
						try {
								inFile = new ExifInterface(sourcePath);
								rotate = OnymosMediaUtil.getOtation(inFile.getAttribute(ExifInterface.TAG_ORIENTATION));  
								if(rotate == 0) {
									rotate = OnymosMediaUtil.getIO(myURI, cordova);
								}
						} catch (IOException e) {
								e.printStackTrace();
						}
				}

				Bitmap bitmap = null;
				Uri uri = null;

				// If sending base64 image back
				if (dt == DURI) {
						bitmap = gsb(sourcePath);

						if (bitmap == null) {
								// Try to get the bitmap from intent.
								bitmap = (Bitmap)intent.getExtras().get("data");
						}
						
						// Double-check the bitmap.
						if (bitmap == null) {
								Log.d(LTAG, "I either have a null image path or bitmap");
								this.failMedia("Unable to create bitmap!");
								return;
						}

						if (rotate != 0 && this.co) {
								bitmap = OnymosMediaUtil.grb(rotate, bitmap);
								if (inFile != null) {
									inFile.setAttribute(ExifInterface.TAG_ORIENTATION, "" + ExifInterface.ORIENTATION_NORMAL);
								}
						}

						this.pp(bitmap, this.et);
						cdi(DURI);
				}

				// If sending filename back
				else if (dt == FURI || dt == NURI) {
					
						if (this.sa) {
								//Create a URI on the filesystem so that we can write the file.
								uri = Uri.fromFile(new File(gpp()));
						} else {
								uri = Uri.fromFile(OnymosMediaUtil.cCF(this.et, System.currentTimeMillis() + "", cordova));
						}

						// If all this is true we shouldn't compress the image.
						if (this.th == -1 && this.tw == -1 && this.mq == 100 && 
										!this.co) {
								OnymosMediaUtil.wunci(uri, this.cordova, this.iuri);

								this.callbackContext.success(uri.toString());
						} else {
								bitmap = gsb(sourcePath);

								// Double-check the bitmap.
								if (bitmap == null) {
										Log.d(LTAG, "I either have a null image path or bitmap");
										this.failMedia("Unable to create bitmap!");
										return;
								}

								if (rotate != 0 && this.co) {
										bitmap = OnymosMediaUtil.grb(rotate, bitmap);
										if (inFile != null) {
											inFile.setAttribute(ExifInterface.TAG_ORIENTATION, "" + ExifInterface.ORIENTATION_NORMAL);
										}
								}

								// Add compressed version of captured image to returned media store Uri
								OutputStream os = this.cordova.getActivity().getContentResolver().openOutputStream(uri);
								CompressFormat compressFormat = et == 0 ?
												CompressFormat.JPEG :
												CompressFormat.PNG;

								bitmap.compress(compressFormat, this.mq, os);
								os.close();

								// Restore exif data to file
								if (this.et == 0) {
										String exifPath;
										exifPath = uri.getPath();
										ExifInterface outFile = new ExifInterface(exifPath);
										wED(outFile, inFile);
								}

								//Broadcast change to File System on MediaStore
								if(this.sa) {
										rgall(uri);
								}


								// Send Uri back to JavaScript for viewing image
								this.callbackContext.success(uri.toString());

						}
				} else {
						throw new IllegalStateException();
				}

				this.cleanup(FURI, this.iuri, uri, bitmap);
				bitmap = null;
		}

		public void wED(ExifInterface outFile, ExifInterface inFile) throws IOException {

				if (outFile == null || inFile == null) {
						return;
				}

				String aperture = inFile.getAttribute(ExifInterface.TAG_APERTURE);
				String datetime = inFile.getAttribute(ExifInterface.TAG_DATETIME);
				String exposureTime = inFile.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
				String flash = inFile.getAttribute(ExifInterface.TAG_FLASH);
				String focalLength = inFile.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
				String gpsAltitude = inFile.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
				String gpsAltitudeRef = inFile.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
				String gpsDateStamp = inFile.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
				String gpsLatitude = inFile.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
				String gpsLatitudeRef = inFile.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
				String gpsLongitude = inFile.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
				String gpsLongitudeRef = inFile.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
				String gpsProcessingMethod = inFile.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
				String gpsTimestamp = inFile.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
				String iso = inFile.getAttribute(ExifInterface.TAG_ISO);
				String make = inFile.getAttribute(ExifInterface.TAG_MAKE);
				String model = inFile.getAttribute(ExifInterface.TAG_MODEL);
				String orientation = inFile.getAttribute(ExifInterface.TAG_ORIENTATION);
				String whiteBalance = inFile.getAttribute(ExifInterface.TAG_WHITE_BALANCE);

				if (aperture != null) {
						outFile.setAttribute(ExifInterface.TAG_APERTURE, aperture);
				}
				if (datetime != null) {
						outFile.setAttribute(ExifInterface.TAG_DATETIME, datetime);
				}
				if (exposureTime != null) {
						outFile.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, exposureTime);
				}
				if (flash != null) {
						outFile.setAttribute(ExifInterface.TAG_FLASH, flash);
				}
				if (focalLength != null) {
						outFile.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, focalLength);
				}
				if (gpsAltitude != null) {
						outFile.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, gpsAltitude);
				}
				if (gpsAltitudeRef != null) {
						outFile.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, gpsAltitudeRef);
				}
				if (gpsDateStamp != null) {
						outFile.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, gpsDateStamp);
				}
				if (gpsLatitude != null) {
						outFile.setAttribute(ExifInterface.TAG_GPS_LATITUDE, gpsLatitude);
				}
				if (gpsLatitudeRef != null) {
						outFile.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, gpsLatitudeRef);
				}
				if (gpsLongitude != null) {
						outFile.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, gpsLongitude);
				}
				if (gpsLongitudeRef != null) {
						outFile.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, gpsLongitudeRef);
				}
				if (gpsProcessingMethod != null) {
						outFile.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, gpsProcessingMethod);
				}
				if (gpsTimestamp != null) {
						outFile.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, gpsTimestamp);
				}
				if (iso != null) {
						outFile.setAttribute(ExifInterface.TAG_ISO, iso);
				}
				if (make != null) {
						outFile.setAttribute(ExifInterface.TAG_MAKE, make);
				}
				if (model != null) {
						outFile.setAttribute(ExifInterface.TAG_MODEL, model);
				}
				if (orientation != null) {
						outFile.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
				}
				if (whiteBalance != null) {
						outFile.setAttribute(ExifInterface.TAG_WHITE_BALANCE, whiteBalance);
				}

				outFile.saveAttributes();
		}


		private void cdi(int type) {
			int diff = 1;
			Uri contentStore = OnymosMediaUtil.wCS();
			Cursor cursor = OnymosMediaUtil.qIDB(contentStore,cordova);
			int currentNumOfImages = cursor.getCount();

			if (type == FURI && this.sa) {
					diff = 2;
			}

			// delete the duplicate file if the difference is 2 for file URI or 1 for Data URL
			if ((currentNumOfImages - npics) == diff) {
					cursor.moveToLast();
					int id = Integer.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
					if (diff == 2) {
							id--;
					}
					Uri uri = Uri.parse(contentStore + "/" + id);
					this.cordova.getActivity().getContentResolver().delete(uri, null, null);
					cursor.close();
			}
		}

		private void cleanup(int imageType, Uri oldImage, Uri newImage, Bitmap bitmap) {
			if (bitmap != null) {
					bitmap.recycle();
			}

			// Clean up initial camera-written image file.
			(new File(OnymosMediaUtil.sFP(oldImage.toString()))).delete();

			cdi(imageType);
			// Scan for the gallery to update pic refs in gallery
			if (this.sa && newImage != null) {
					this.sfg(newImage);
			}
			System.gc();
		}
 
		private void rgall(Uri contentUri) {
				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				mediaScanIntent.setData(contentUri);
				this.cordova.getActivity().sendBroadcast(mediaScanIntent);
		}

		private void sfg(Uri newImage) {
				this.scanMe = newImage;
				if(this.conn != null) {
						this.conn.disconnect();
				}
				this.conn = new MediaScannerConnection(this.cordova.getActivity().getApplicationContext(), this);
				conn.connect();
		}

		public void onMediaScannerConnected() {
				try{
						this.conn.scanFile(this.scanMe.toString(), "image/*");
				} catch (java.lang.IllegalStateException e){
						LOG.e(LTAG, "Can't scan file in MediaScanner after taking picture");
				}

		}

		public void onScanCompleted(String path, Uri uri) {
				this.conn.disconnect();
		}

		private String gpp() {
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String imageFileName = "IMG_" + timeStamp + (this.et == 0 ? ".jpg" : ".png");
			File storageDir = Environment.getExternalStoragePublicDirectory(
							Environment.DIRECTORY_PICTURES);
			String galleryPath = storageDir.getAbsolutePath() + "/" + imageFileName;
			return galleryPath;
		}

		public void pp(Bitmap bitmap, int encodingType) {
			ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
			CompressFormat compressFormat = encodingType == 0 ?
							CompressFormat.JPEG :
							CompressFormat.PNG;
			try {
					if (bitmap.compress(compressFormat, mq, jpeg_data)) {
							byte[] code = jpeg_data.toByteArray();
							byte[] output = Base64.encode(code, Base64.NO_WRAP);
							String js_out = new String(output);
							this.callbackContext.success(js_out);
							js_out = null;
							output = null;
							code = null;
							bitmap.recycle(); /*added by Bhavani */
					}
			} catch (Exception e) {
					this.failMedia("Error compressing image.");
			}
			jpeg_data = null;
		}

		private Bitmap gsb(String imageUrl) throws IOException {
				// If no new width or height were specified return the original bitmap
				if (this.tw <= 0 && this.th <= 0) {
						InputStream fileStream = null;
						Bitmap image = null;
						try {
								fileStream = OnymosMediaUtil.getISTFromUStr(imageUrl, cordova);
								image = BitmapFactory.decodeStream(fileStream);
						} finally {
								if (fileStream != null) {
										try {
												fileStream.close();
										} catch (IOException e) {
												LOG.d(LTAG,"Exception while closing file input stream.");
										}
								}
						}
						return image;
				}

				// figure out the original width and height of the image
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				InputStream fileStream = null;
				try {
						fileStream = OnymosMediaUtil.getISTFromUStr(imageUrl, cordova);
						BitmapFactory.decodeStream(fileStream, null, options);
				} finally {
						if (fileStream != null) {
								try {
										fileStream.close();
								} catch (IOException e) {
										LOG.d(LTAG,"Exception while closing file input stream.");
								}
						}
				}
				
				if(options.outWidth == 0 || options.outHeight == 0)
				{
						return null;
				}
				
				int[] widthHeight = OnymosMediaUtil.car(options.outWidth, options.outHeight, this.tw, this.th);

				// Load in the smallest bitmap possible that is closest to the size we want
				options.inJustDecodeBounds = false;
				options.inSampleSize = OnymosMediaUtil.css(options.outWidth, options.outHeight, this.tw, this.th);
				Bitmap unscaledBitmap = null;
				try {
						fileStream = OnymosMediaUtil.getISTFromUStr(imageUrl, cordova);
						unscaledBitmap = BitmapFactory.decodeStream(fileStream, null, options);
				} finally {
						if (fileStream != null) {
								try {
										fileStream.close();
								} catch (IOException e) {
										LOG.d(LTAG,"Exception while closing file input stream.");
								}
						}
				}
				if (unscaledBitmap == null) {
						return null;
				}

				return Bitmap.createScaledBitmap(unscaledBitmap, widthHeight[0], widthHeight[1], true);
		}

		/**
		 * Give back the error message to Javascript
		 */
		public void failMedia(String error) {
				this.callbackContext.error(error);
		}

		@SuppressWarnings("deprecation")
		private void HQMPMOV(JSONArray args, final CallbackContext callbackContext) throws JSONException, IOException {
				
				JSONObject options = args.optJSONObject(0);

				final File ipf = OnymosMediaUtil.resLFSU(options.getString("fileUri"), this.cordova);
				if (!ipf.exists()) {
						Log.d(LTAG, "input file does not exist");
						callbackContext.error("input video does not exist.");
						return;
				}
												
				final String vsp = ipf.getAbsolutePath();
				final String opf = options.optString(
						"outputFileName", 
						new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date())
				);
				final int vq = options.optInt("quality", 100);
				final boolean o6611583 = options.optBoolean("optimizeBySourceSize", true);
				final int optype = options.optInt("outputFileType", CD);
				
												
String opext; switch(optype) { case GH: opext = ".mov"; break; case EF: opext = ".m4a"; break; case AB: opext = ".m4v"; break; case CD: default: opext = ".mp4"; break; }

int mw = 0; int mh = 0; int mb = 0; int mf = 0;

try {
MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever(); mediaMetadataRetriever.setDataSource(new File(vsp).getCanonicalPath());

mw = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)); mh = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

if (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE) != null) { mb = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)); }

if (mf <= 0) { mf = 24; } if (mb <= 0) { mb = 960000; } }
catch (Exception e) { e.printStackTrace(); }
float t6710082 = mb; float tc87 = mw; float t67104 = mh; final float cf82 = mf; if (vq <= 100) { float o86113 = vq; if (o6611583 == true) { if (mw > mh) { o86113 = Math.min(100, (o86113 * 3840/mw)); } else { o86113 = Math.min(100, (o86113 * 3840/mh)); } }
t6710082 = mb * o86113/100; if (vq <= 25) { tc87 = tc87/4; t67104 = t67104/4; } else if (vq <= 50) { tc87 = tc87/2; t67104 = t67104/2; } else if (vq <= 75) { tc87 = tc87*3/4; t67104 = t67104*3/4; } }
else { float a82 = tc87/t67104; float n70115 = 640; if (vq == 200) { t6710082 = Math.min(1200000, mb); n70115 = 640; } else if (vq == 300) { t6710082 = Math.min(2000000, mb); n70115 = 960; } else if (vq == 400) { t6710082 = Math.min(3000000, mb); n70115 = 1280; }
if (tc87 > t67104) { tc87 = Math.min(n70115, tc87); t67104 = tc87/a82; } else { t67104 = Math.min(n70115, t67104); tc87 = t67104*a82; } }

final float c68114 = t6710082; final float c119 = tc87; final float c104 = t67104;

				
				final Context appContext = cordova.getActivity().getApplicationContext();
				final PackageManager pm = appContext.getPackageManager();
				
				ApplicationInfo ai;
				try {
						ai = pm.getApplicationInfo(cordova.getActivity().getPackageName(), 0);
				} catch (final NameNotFoundException e) {
						ai = null;
				}
				final String appName = (String) (ai != null ? pm.getApplicationLabel(ai) : "Unknown");
				
				final boolean stb = options.optBoolean("saveToLibrary", true);
				File msd;
				
				if (stb) {
						msd = new File(
								Environment.getExternalStorageDirectory() + "/Movies",
								appName
						);  
				} else {
						msd = new File(appContext.getExternalCacheDir().getPath());
				}
				
				if (!msd.exists()) {
						if (!msd.mkdir()) {
								callbackContext.error("Can't access or make Movies directory");
								return;
						}
				}
				
				final String outputFilePath =  new File(
						msd.getPath(),
						"VID_" + opf + opext
				).getAbsolutePath();
				
				final double videoDuration = options.optDouble("duration", 0);
			 
				cordova.getThreadPool().execute(new Runnable() {
						public void run() {             
														
								 try {
										File tempFile = File.createTempFile("ffmpeg", null, appContext.getCacheDir());
										FfmpegController ffmpegController = new FfmpegController(appContext, tempFile);
										
										TCB tcCallback = new TCB();
										
										Clip inVar = new Clip(vsp);
										
										Clip outVar = new Clip(outputFilePath);
										outVar.videoCodec = "libx264";
										outVar.videoFps = "" + Math.round(cf82);
										outVar.videoBitrate = Math.round(c68114/1024);
										outVar.audioChannels = 1;
										outVar.width = Math.round(c119);
										outVar.height = Math.round(c104);
										outVar.duration = videoDuration;
										
										ffmpegController.processVideo(inVar, outVar, true, tcCallback);
										
										File outFile = new File(outputFilePath);
										if (!outFile.exists()) {
												Log.d(LTAG, "outputFile doesn't exist!");
												callbackContext.error("an error ocurred during transcoding");
												return;
										}
																				
										// make the gallery display the new file if saving to library
										if (stb) {
												// remove the original input file when saving to gallery
												// comment out or remove the delete based on your needs
												if (!ipf.delete()) {
														Log.d(LTAG, "unable to delete in file");
												}
												
												Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
												scanIntent.setData(Uri.fromFile(ipf));
												scanIntent.setData(Uri.fromFile(outFile));
												appContext.sendBroadcast(scanIntent);
										}
										
										callbackContext.success(outputFilePath);
								} catch (Throwable e) {
										Log.d(LTAG, "transcode exception ", e);
										callbackContext.error(e.toString());
								}
						}
				});
		}
		
		private class TCB implements ShellCallback {

				@Override
				public void shellOut(String shellLine) {
						Log.v(LTAG, "shellOut: " + shellLine);
				}

				@Override
				public void processComplete(int exitValue) {
						Log.v(LTAG, "processComplete: " + exitValue);
				}
				
		}

  
	public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
  
	  //System.out.println("DEBUG : JAVA : onRequestPermissionResult");

    //super.onRequestPermissionResult(requestCode, permissions, grantResults);

	  for(int r:grantResults) {

	      if(r == PackageManager.PERMISSION_DENIED) {
	          this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
	          return;
	      }
	  }
	  
	  //System.out.println("DEBUG : JAVA : onRequestPermissionResult : requestCode : " + requestCode);

    switch(requestCode) {
	  	case CREATE_THUMBNAIL_SEC:
	  	  if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	  	  	try {
	        	CTCR(this.executeArgs);
	        } catch (Exception e) {
	        	callbackContext.error("Error converting to thumbnail with error [" + e.getMessage() +"]");
	        }
	        break;
	      }
	    case CREATE_VIDEO_SEC:
	  	  if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	  	  	try {
	        	this.HQMPMOV(this.executeArgs, this.callbackContext);
	        } catch (Exception e) {
	        	callbackContext.error("Error transcoding video with error [" + e.getMessage() +"]");
	        }
	        break;
	      }  
	    case CREATE_IMAGE_SEC:
	  	  if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	  	  	try {
	        	this.FIGHJ(this.st, this.dt, this.et);
	        } catch (Exception e) {
	        	callbackContext.error("Error selecting media with error [" + e.getMessage() +"]");
	        }
	        break;
	      }
		    case TAKE_PIC_SEC:
		  	  if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
		  	  	try {
		        	this.TPFC(this.dt, this.et);
		        } catch (Exception e) {
		        	callbackContext.error("Error taking picture with error [" + e.getMessage() +"]");
		        }
		        break;
		      }	  
	  }
  }

  private void checkReadStoragePermission(int permissionCode) {

  	//System.out.println("DEBUG : JAVA : checkReadStoragePermission : 1");

  		if (OnymosPermissionHelper.hasPermission(this, permissions[0])) {
  			  //System.out.println("DEBUG : JAVA : checkReadStoragePermission : has permission : 2 :" + permissions[0]);
	          switch(permissionCode) {
					  	case CREATE_THUMBNAIL_SEC:
					  	  	try {
					        	CTCR(this.executeArgs);
					        } catch (Exception e) {
					        	callbackContext.error("Error converting to thumbnail with error [" + e.getMessage() +"]");
					        }
					        break;
					    case CREATE_VIDEO_SEC:
					  	  	try {
					        	this.HQMPMOV(this.executeArgs, this.callbackContext);
					        } catch (Exception e) {
					        	callbackContext.error("Error transcoding video with error [" + e.getMessage() +"]");
					        }
					        break;
					    case CREATE_IMAGE_SEC:
					  	  	try {
					        	this.FIGHJ(this.st, this.dt, this.et);
					        } catch (Exception e) {
					        	callbackContext.error("Error selecting media with error [" + e.getMessage() +"]");
					        }
					        break;
					    case TAKE_PIC_SEC:
					  	  	try {
					        	this.TPFC(this.dt, this.et);
					        } catch (Exception e) {
					        	callbackContext.error("Error taking picture with error [" + e.getMessage() +"]");
					        }
					        break;    	    							
					  }
      } else {
      	  //System.out.println("DEBUG : JAVA : checkReadStoragePermission : no permission : 3");
          OnymosPermissionHelper.requestPermission(this, permissionCode, permissions[0]);
      }
  } 

		@SuppressWarnings("deprecation")
		private void CTCR(JSONArray args)  throws Exception {
				
				
			JSONObject options = args.optJSONObject(0);

		  File inFile = null;
			inFile = OnymosMediaUtil.resLFSU(options.getString("fileUri"), this.cordova);
		

      //System.out.println("DEBUG - JAVA - inFile [" + inFile.getAbsolutePath() + "]");
			
			if (!inFile.exists()) {
					Log.d(LTAG, "input file does not exist");
					callbackContext.error("input video does not exist.");
					return;
			}
			String svd = inFile.getAbsolutePath();
			
			String opf = options.optString(
					"outputFileName", 
					new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date())
			); 
		
			Context appContext = cordova.getActivity().getApplicationContext();
			PackageManager pm = appContext.getPackageManager();
			
			ApplicationInfo ai;
			try {
					ai = pm.getApplicationInfo(cordova.getActivity().getPackageName(), 0);
			} catch (final NameNotFoundException e) {
					ai = null;
			}
			final String appName = (String) (ai != null ? pm.getApplicationLabel(ai) : "Unknown");
							
			File tsp = appContext.getExternalCacheDir();
			
			File of =  new File(
					tsp.getPath(),
					"PIC_" + opf + ".jpg"
			);
			
			Bitmap tn = ThumbnailUtils.createVideoThumbnail(svd, MediaStore.Images.Thumbnails.MINI_KIND);
			
			FileOutputStream theOutputStream;
			try {
					if (!of.exists()) {
							if (!of.createNewFile()) {
									callbackContext.error("Thumbnail could not be saved.");
							}
					}
					if (of.canWrite()) {
							theOutputStream = new FileOutputStream(of);
							if (theOutputStream != null) {
									tn.compress(CompressFormat.JPEG, 75, theOutputStream);
							} else {
									callbackContext.error("Thumbnail could not be saved; target not writeable");
							}
					}
			} catch (IOException e) {
					callbackContext.error(e.toString());
			}
							
			callbackContext.success(of.getAbsolutePath());   
		}  

}
