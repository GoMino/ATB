package com.android.toolbox.network.ssdp;

/**
 * Created by gomino on 6/13/13.
 */
public class SSDPDevice {

    private String mId;
    private String mLocation;
    private String mSearchTarget;
    private String mIp;
    private String mPort;
    private String mFriendlyName;

    public SSDPDevice(){
    }

    public String getId(){
        return  mId;
    }

    public SSDPDevice setId(String id){
        mId  = id;
        return this;
    }

    public String getLocation(){
        return  mLocation;
    }

    public SSDPDevice setLocation(String location){
        mLocation  = location;
        return this;
    }

    public String getST(){
        return  mSearchTarget;
    }

    public SSDPDevice setST(String searchTarget){
        mSearchTarget  = searchTarget;
        return this;
    }

	public SSDPDevice setIP(String deviceIP) {
		mIp = deviceIP;
		return this;
	}
	
	public String getIP(){
		return mIp;
	}

	public SSDPDevice setPort(String devicePort) {
		mPort = devicePort;
		return this;
	}
	
	public String getPort(){
		return mPort;
	}

	public SSDPDevice setFriendlyName(String friendlyName) {
		mFriendlyName = friendlyName;
		return this;
	}
	
	public String getFriendlyName(){
		return mFriendlyName;
	}

    @Override public int hashCode() {
        // Start with a non-zero constant.
        int result = 17;

        // Include a hash for each field.
        result = 31 * result +(mLocation == null ? 0 : mLocation.hashCode());
        //result = 31 * result +(mId == null ? 0 : mId.hashCode());

        return result;
    }


    @Override
    public boolean equals(Object o) {
        // Return true if the objects are identical.
        // (This is just an optimization, not required for correctness.)
        if (this == o) {
            return true;
        }

        // Return false if the other object has the wrong type.
        // This type may be an interface depending on the interface's specification.
        if (!(o instanceof SSDPDevice)) {
            return false;
        }

        // Cast to the appropriate type.
        // This will succeed because of the instanceof, and lets us access private fields.
        SSDPDevice d = (SSDPDevice) o;

        // Check each field. Primitive fields, reference fields, and nullable reference fields are all treated differently.
        if (mLocation!= null && !mLocation.equals(d.mLocation)) return false;
        //else if (mId!= null && !mId.equals(d.mId)) return false;

        return true;
    }

}
