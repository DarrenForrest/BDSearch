package com.bonc;

public class GpsUtil {
 
	private static final double x_pi= Math.PI*3000.0 / 180.0;
	
	 
	private static double[] delta(double lat,double lon) {
    	double a = 6378245.0;//  a: 卫星椭球坐标投影到平面地图坐标系的投影因子。
    	double ee= 0.00669342162296594323; //  ee: 椭球的偏心率。
    	double  dLat = GpsUtil.transformLat(lon - 105.0, lat - 35.0);
    	double dLon = GpsUtil.transformLon(lon - 105.0, lat - 35.0);
    	double radLat = lat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
 
        return new double[] {dLat,dLon};
    }
	 //WGS-84 to GCJ-02
    public static double[] gcj_encrypt  (double wgsLat,double wgsLon) {
        if (GpsUtil.outOfChina(wgsLat, wgsLon))
//            return {'lat': wgsLat, 'lon': wgsLon};
        	return new double[] {wgsLat,wgsLon};
 
        double[] d = GpsUtil.delta(wgsLat, wgsLon);
        return new double[]{wgsLat+d[0],wgsLon+d[1]};
    } 
    //GCJ-02 to WGS-84
   public static double[] gcj_decrypt (double gcjLat,double gcjLon) {
        if (GpsUtil.outOfChina(gcjLat, gcjLon))
            return new double[]{gcjLat,  gcjLon};
         
        double[] d = GpsUtil.delta(gcjLat, gcjLon);
        return new double[]{ gcjLat - d[0], gcjLon - d[1]};
    }
    //GCJ-02 to WGS-84 exactly
   public static double[] gcj_decrypt_exact(double gcjLat, double gcjLon) {
        double initDelta = 0.01;
        double threshold = 0.000000001;
        double dLat = initDelta, dLon = initDelta;
        double mLat = gcjLat - dLat, mLon = gcjLon - dLon;
        double pLat = gcjLat + dLat, pLon = gcjLon + dLon;
        double wgsLat, wgsLon, i = 0;
        while (true) {
            wgsLat = (mLat + pLat) / 2;
            wgsLon = (mLon + pLon) / 2;
            double tmp[] = GpsUtil.gcj_encrypt(wgsLat, wgsLon);
            dLat = tmp[0] - gcjLat;
            dLon = tmp[1] - gcjLon;
            if ((Math.abs(dLat) < threshold) && (Math.abs(dLon) < threshold))
                break;
 
            if (dLat > 0) pLat = wgsLat; else mLat = wgsLat;
            if (dLon > 0) pLon = wgsLon; else mLon = wgsLon;
 
            if (++i > 10000) break;
        }
        //console.log(i);
        return new double[]{ wgsLat,  wgsLon};
    } 
    //GCJ-02 to BD-09
    public static double[] bd_encrypt   (double gcjLat,double  gcjLon) {
    	double x = gcjLon, y = gcjLat;  
    	double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * GpsUtil.x_pi);  
    	double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * GpsUtil.x_pi);  
    	double bdLon = z * Math.cos(theta) + 0.0065;  
    	double bdLat = z * Math.sin(theta) + 0.006; 
        return new double[]{ bdLat, bdLon};
    } 
    //BD-09 to GCJ-02
    public static double[] bd_decrypt   (double bdLat,double bdLon) {
        double x = bdLon - 0.0065, y = bdLat - 0.006;  
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * GpsUtil.x_pi);  
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * GpsUtil.x_pi);  
        double gcjLon = z * Math.cos(theta);  
        double gcjLat = z * Math.sin(theta);
        return new double[]{ gcjLat, gcjLon};
    } 
    //WGS-84 to Web mercator
    //mercatorLat -> y mercatorLon -> x
    public static double[] mercator_encrypt  (double wgsLat, double wgsLon) {
    	double x = wgsLon * 20037508.34 / 180.;
    	double y = Math.log(Math.tan((90. + wgsLat) * Math.PI / 360.)) / (Math.PI / 180.);
        y = y * 20037508.34 / 180.;
        return new double[]{  y,   x};
        /*
        if ((Math.abs(wgsLon) > 180 || Math.abs(wgsLat) > 90))
            return null;
        var x = 6378137.0 * wgsLon * 0.017453292519943295;
        var a = wgsLat * 0.017453292519943295;
        var y = 3189068.5 * Math.log((1.0 + Math.sin(a)) / (1.0 - Math.sin(a)));
        return {'lat' : y, 'lon' : x};
        //*/
    } 
    // Web mercator to WGS-84
    // mercatorLat -> y mercatorLon -> x
    public static double[]  mercator_decrypt  (double mercatorLat, double mercatorLon) {
    	double x = mercatorLon / 20037508.34 * 180.;
    	double y = mercatorLat / 20037508.34 * 180.;
        y = 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180.)) - Math.PI / 2);
        return new double[]{ y, x};
        /*
        if (Math.abs(mercatorLon) < 180 && Math.abs(mercatorLat) < 90)
            return null;
        if ((Math.abs(mercatorLon) > 20037508.3427892) || (Math.abs(mercatorLat) > 20037508.3427892))
            return null;
        var a = mercatorLon / 6378137.0 * 57.295779513082323;
        var x = a - (Math.floor(((a + 180.0) / 360.0)) * 360.0);
        var y = (1.5707963267948966 - (2.0 * Math.atan(Math.exp((-1.0 * mercatorLat) / 6378137.0)))) * 57.295779513082323;
        return {'lat' : y, 'lon' : x};
        //*/
    } 
  
  private static boolean  outOfChina (double lat,double lon) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }
   private static double  transformLat(double x,double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    } 
   private static double  transformLon (double x,double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }
   //WGS-84  to BD
   public static double[] wg_bd_encrypt(double wgsLat,double wgsLon) {
	   double[] d =  GpsUtil.gcj_encrypt(wgsLat, wgsLon);
	   return  GpsUtil.bd_encrypt(d[0], d[1]);
   }
}
