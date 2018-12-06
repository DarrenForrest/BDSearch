package com.bonc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class DownloadBD {
    private String inputFile;
    private String outFile;
    private String outShapeFile;
    private String outImagePath;
    private Map<String, String> cityCodes = new HashMap<>();
    private String wdName;
    private String cityName;
    private String cityCode;
    private String wd2Name;

    public DownloadBD(String wdName,String wd2Name,String cityName,String cityCode) {
        this.wdName = wdName;
        this.wd2Name = wd2Name;
        this.cityName = cityName;
        this.cityCode = cityCode;
    }
    
    public List<String> download() throws Exception {
    	List<String> list = new ArrayList<String>();
//        String result = "";
        if (this.wdName.indexOf("厕所") < 0) {
        	if(StringUtils.isNotBlank(cityName)&&StringUtils.isBlank(cityCode)){
        		list.addAll(downloadKeyWord(this.cityName,this.wdName,this.wd2Name)) ;
        	}else{
        		list.addAll(downloadKeyWordForCitycode(this.cityCode,this.wdName,this.wd2Name));
        	}
        }
        return list;
    }

    private List<String> downloadKeyWord(String city, String keyWord,String wd2) throws Exception {
    	List<String> list = new ArrayList<String>();
    	//http://api.map.baidu.com/?qt=s&c=131&wd=餐饮&rn=1
        for(int ii=0;ii<16;ii++){
        	String cityCode = this.getCityCode(city);
            if (cityCode == null || cityCode.equals("")) {
                return list;
            }
            int rn = 50;//一次搜索最多返回数据条数
            String urlStr = "http://api.map.baidu.com/?qt=s&c=" + cityCode + "&wd=" + keyWord + "&rn=" + rn+"&pn="+ii;
            if(StringUtils.isNotBlank(wd2)){
            	urlStr+="&wd2="+wd2;
            }
            URL url = new URL(urlStr);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setConnectTimeout(10000);
            huc.setReadTimeout(10000);
            huc.setRequestMethod("GET");
            InputStream in = url.openStream();
            InputStreamReader inr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(inr);
            StringBuffer sb = new StringBuffer();
            String tmpline = "";
            while ((tmpline = br.readLine()) != null) {
                sb.append(tmpline);
            }
            inr.close();
            in.close();
            String jsonStr = sb.toString();
            int n = 0;
            while (jsonStr.contains("\"ext\"")) {
            	String tmpImage ="";
            	if(jsonStr.contains("\"image\"")){
            		tmpImage = jsonStr.substring(jsonStr.indexOf("\"image\""));
                    tmpImage = tmpImage.substring(0, tmpImage.indexOf("\",") + 2);
            	}
                int ext = jsonStr.indexOf("\"ext\"");
                int ext_type = jsonStr.indexOf("\"ext_type\"", ext) + 10;
                if (ext < ext_type && n < rn) {
                    n++;
                    jsonStr = jsonStr.substring(0, ext) + tmpImage + "\"tmpmytype\"" + jsonStr.substring(ext_type);
                } else {
                    break;
                }
            }

            JSONObject obj = JSONObject.parseObject(jsonStr);
            JSONArray content = (JSONArray) obj.get("content");
            if (content == null) {
                //百度查询不到
                System.out.println("百度查询不到POI数据【"+city+"】【"+keyWord+"】【第"+(ii+1)+"页查询】");
                break;
            }
            JSONArray jarray = new JSONArray();
            for (int i = 0; i < content.size(); i++) {
                JSONObject o = (JSONObject) content.get(i);
                String uid = o.getString("uid");
                String image = notNull(o.getString("image"));
                String x = notNull(o.getString("x"));
                String y = notNull(o.getString("y"));
                String name = notNull(o.getString("name"));
                String addr = notNull(o.getString("addr"));
                String stdTag = notNull(o.getString("std_tag"));
                String diTag = notNull(o.getString("di_tag"));
                String area_name = notNull(o.getString("area_name"));
                addr = addr.replace("'", "\'");
                if (isNull(uid) || isNull(x) || isNull(y) || isNull(name)) {
                    continue;
                }
                double[] point = PointUtil.convertMC2LL(new double[]{Double.parseDouble(x) / 100, Double.parseDouble(y) / 100});
                JSONObject jpoint = new JSONObject();
                jpoint.put("lng", point[0]);
                jpoint.put("lat", point[1]);
                jpoint.put("name", name);
                jpoint.put("city", area_name);
                jpoint.put("addr", addr);
                jpoint.put("uid", uid);
                jpoint.put("diTag", diTag);
                jpoint.put("stdTag", stdTag);
                //String imageUrl = downloadImage(uid, image);
                //String imageUrl = "";
                //点经纬度生成
//                String outStr =
//                        "'" + name + "','" + city + "','" + addr + "','{\"lng\":" + point[0] + ",\"lat\":" + point[1] + "}',"
//                                + "'" + uid + "','" + imageUrl + "','" + point[0] + "','" + point[1] + "'";   //输出格式随意改，根据需要想改啥都行
//                sqlWriter.write(outStr + "\n");
//                sqlWriter.flush();
                //区域经纬度生成
                jpoint = downloadShape(uid, jpoint);
//                jarray.add(jpoint);
                list.add(jpoint.toJSONString());
            }
        }
        return list;
    }

    private List<String> downloadKeyWordForCitycode(String cityCode, String keyWord,String wd2) throws Exception {
        //http://api.map.baidu.com/?qt=s&c=131&wd=餐饮&rn=1
    	List<String> list = new ArrayList<String>();
    	for(int ii=0;ii<16;ii++){
	    	if (cityCode == null || cityCode.equals("")) {
	            return list;
	        }
	        int rn = 50;//一次搜索最多返回数据条数
	        String urlStr = "http://api.map.baidu.com/?qt=s&c=" + cityCode + "&wd=" + keyWord + "&rn=" + rn+"&pn="+ii;
	        if(StringUtils.isNotBlank(wd2)){
            	urlStr+="&wd2="+wd2;
            }
	        URL url = new URL(urlStr);
	        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
	        huc.setConnectTimeout(10000);
	        huc.setReadTimeout(10000);
	        huc.setRequestMethod("GET");
	        InputStream in = url.openStream();
	        InputStreamReader inr = new InputStreamReader(in);
	        BufferedReader br = new BufferedReader(inr);
	        StringBuffer sb = new StringBuffer();
	        String tmpline = "";
	        while ((tmpline = br.readLine()) != null) {
	            sb.append(tmpline);
	        }
	        inr.close();
	        in.close();
	        String jsonStr = sb.toString();
	        int n = 0;
	        while (jsonStr.contains("\"ext\"")) {
	        	String tmpImage ="";
            	if(jsonStr.contains("\"image\"")){
            		tmpImage = jsonStr.substring(jsonStr.indexOf("\"image\""));
                    tmpImage = tmpImage.substring(0, tmpImage.indexOf("\",") + 2);
            	}
	            int ext = jsonStr.indexOf("\"ext\"");
	            int ext_type = jsonStr.indexOf("\"ext_type\"", ext) + 10;
	            if (ext < ext_type && n < rn) {
	                n++;
	                jsonStr = jsonStr.substring(0, ext) + tmpImage + "\"tmpmytype\"" + jsonStr.substring(ext_type);
	            } else {
	                break;
	            }
	        }
	
	        JSONObject obj = JSONObject.parseObject(jsonStr);
	        JSONArray content = (JSONArray) obj.get("content");
	        if (content == null) {
	            //百度查询不到
	           System.out.println("百度查询不到POI数据【"+cityCode+"】【"+keyWord+"】【第"+(ii+1)+"页查询】");
	           break;
	        }
//	        JSONArray jarray = new JSONArray();
	        for (int i = 0; i < content.size(); i++) {
	            JSONObject o = (JSONObject) content.get(i);
	            String uid = o.getString("uid");
	            String image = notNull(o.getString("image"));
	            String x = notNull(o.getString("x"));
	            String y = notNull(o.getString("y"));
	            String name = notNull(o.getString("name"));
	            String addr = notNull(o.getString("addr"));
	            String stdTag = notNull(o.getString("std_tag"));
	            String diTag = notNull(o.getString("di_tag"));
	            addr = addr.replace("'", "\'");
	            String area_name = notNull(o.getString("area_name"));
	            
	            if (isNull(uid) || isNull(x) || isNull(y) || isNull(name)) {
	                continue;
	            }
	            double[] point = PointUtil.convertMC2LL(new double[]{Double.parseDouble(x) / 100, Double.parseDouble(y) / 100});
	            JSONObject jpoint = new JSONObject();
	            jpoint.put("lng", point[0]);
	            jpoint.put("lat", point[1]);
	            jpoint.put("name", name);
	            jpoint.put("city", area_name);
	            jpoint.put("addr", addr);
	            jpoint.put("uid", uid);
	            jpoint.put("diTag", diTag);
	            jpoint.put("stdTag", stdTag);
	            //String imageUrl = downloadImage(uid, image);
	            //String imageUrl = "";
	            //点经纬度生成
	//            String outStr =
	//                    "'" + name + "','" + city + "','" + addr + "','{\"lng\":" + point[0] + ",\"lat\":" + point[1] + "}',"
	//                            + "'" + uid + "','" + imageUrl + "','" + point[0] + "','" + point[1] + "'";   //输出格式随意改，根据需要想改啥都行
	//            sqlWriter.write(outStr + "\n");
	//            sqlWriter.flush();
	            //区域经纬度生成
	            jpoint = downloadShape(uid, jpoint);
//	            jarray.add(jpoint);
	            list.add(jpoint.toJSONString());
	        }
    	}
        return list;
    }
    
    private String notNull(Object o) {
        if (o == null) {
            return "";
        } else {
            return o.toString().trim();
        }
    }

    private boolean isNull(Object o) {
        if (o == null) {
            return true;
        } else {
            if (o.toString().trim().equals("")) {
                return true;
            } else {
                return false;
            }
        }
    }

    public String getCityCode(String cityName) {
        String cityCode = "";
        try {
            String urlStr = "http://api.map.baidu.com/?qt=s&c=1&wd=" + cityName;
            URL url = new URL(urlStr);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setConnectTimeout(10000);
            huc.setReadTimeout(10000);
            huc.setRequestMethod("GET");
            InputStream in = url.openStream();
            InputStreamReader inr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(inr);
            StringBuffer sb = new StringBuffer();
            String tmpline = "";
            while ((tmpline = br.readLine()) != null) {

                sb.append(tmpline);
            }

            inr.close();
            in.close();

            //JSONObject obj = JSONObject.parseObject(sb.toString());
            //JSONObject content = obj.getJSONObject("content");
            //cityCode = content.getString("code");

            Pattern pattern = Pattern.compile("\"code\":([0-9]+),");
            Matcher matcher = pattern.matcher(sb.toString());
            if (matcher.find()) {
                cityCode = matcher.group(1).trim();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return cityCode;
    }

    private JSONObject downloadShape(String uid, JSONObject jpoint) {
        try {
            String urlStr = "http://map.baidu.com/?reqflag=pcmap&from=webmap&qt=ext&uid=" + uid + "&ext_ver=new&l=18";
            URL url = new URL(urlStr);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setConnectTimeout(10000);
            huc.setReadTimeout(10000);
            huc.setRequestMethod("GET");
            InputStream in = url.openStream();
            InputStreamReader inr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(inr);
            StringBuffer sb = new StringBuffer();
            String tmpline = "";
            while ((tmpline = br.readLine()) != null) {
                sb.append(tmpline);
            }

            inr.close();
            in.close();

            JSONObject obj = JSONObject.parseObject(sb.toString());
            JSONObject content = obj.getJSONObject("content");
            String geo = content.getString("geo");

            if (geo != null) {
                //System.out.println(geo);
                if (geo.indexOf("|1-") > 0) {
                    geo = geo.substring(geo.indexOf("|1-") + 3);
                    geo = geo.substring(0, geo.indexOf(";"));
                    String[] pointStrArr = geo.split(",");
                    if (pointStrArr.length % 2 == 0) {
                        JSONArray tempArray = new JSONArray();
                        for (int i = 0; i < pointStrArr.length; i += 2) {
                            double[] point = PointUtil.convertMC2LL(new double[]{Double.parseDouble(pointStrArr[i]), Double.parseDouble(pointStrArr[i + 1])});
                            //shpStr.append("{\"lng\":" + point[0] + ",\"lat\":" + point[1] + "}");
                            JSONObject jo = new JSONObject();
                            jo.put("lng", point[0]);
                            jo.put("lat", point[1]);
                            tempArray.add(jo);
                        }
                        jpoint.put("range", tempArray);
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return jpoint;
    }

    private String downloadImage(String uid, String imageUrl) {
        if (isNull(imageUrl)) {
            return "default.jpg";
        }
        try {
            File f = new File(this.outImagePath + "/" + uid + ".jpg");
            if (!f.exists()) {
                URL url = new URL(imageUrl);
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setConnectTimeout(10000);
                huc.setReadTimeout(10000);
                huc.setRequestMethod("GET");
                InputStream in = url.openStream();

                byte[] buffer = new byte[4096];
                int bytes_read;

                f.createNewFile();
                FileOutputStream fos = new FileOutputStream(f);
                while ((bytes_read = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytes_read);
                }
                fos.flush();
                fos.close();
                in.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
//			e.printStackTrace();
            return "default.jpg";
        }
        return uid + ".jpg";
    }
    
    //获取结果
    public String getTotalPoiForKeyWord(){
    	 String result = "";
         if (this.wdName.indexOf("厕所") < 0) {
        	 try{
        		 result = getTotalKeyWord(this.cityName, this.wdName,this.wd2Name);
        	 }catch(Exception ex){
        		 ex.printStackTrace();
        	 }
         }
         return result;
    }
    
    private String getTotalKeyWord(String city, String keyWord,String wd2) throws Exception {
        String cityCode = getCityCode(city);
        if (cityCode == null || cityCode.equals("")) {
            return "";
        }
        int rn = 5;//一次搜索最多返回数据条数
        String urlStr = "http://api.map.baidu.com/?qt=s&c=" + cityCode + "&wd=" + keyWord + "&rn="+rn;
        if(StringUtils.isNotBlank(wd2)){
        	urlStr+="&wd2="+wd2;
        }
        URL url = new URL(urlStr);
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setConnectTimeout(10000);
        huc.setReadTimeout(10000);
        huc.setRequestMethod("GET");
        InputStream in = url.openStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(inr);
        StringBuffer sb = new StringBuffer();
        String tmpline = "";
        while ((tmpline = br.readLine()) != null) {
            sb.append(tmpline);
        }
        inr.close();
        in.close();
        String jsonStr = sb.toString();
        int n = 0;
        while (jsonStr.contains("\"ext\"")) {
        	String tmpImage ="";
        	if(jsonStr.contains("\"image\"")){
        		tmpImage = jsonStr.substring(jsonStr.indexOf("\"image\""));
                tmpImage = tmpImage.substring(0, tmpImage.indexOf("\",") + 2);
        	}
            int ext = jsonStr.indexOf("\"ext\"");
            int ext_type = jsonStr.indexOf("\"ext_type\"", ext) + 10;
            if (ext < ext_type && n < rn) {
                n++;
                jsonStr = jsonStr.substring(0, ext) + tmpImage + "\"tmpmytype\"" + jsonStr.substring(ext_type);
            } else {
                break;
            }
        }
        JSONObject obj = JSONObject.parseObject(jsonStr);
        JSONObject result = (JSONObject)obj.get("result");
        String total = result.getString("total");
        return total;
    }
    
    public static void main(String[] args) {
        DownloadBD dd = new DownloadBD("商铺","购物","太原小店","8272");
        try{
           List<String> resList = dd.download();
           for(String s:resList){
        	   System.out.println(s);
           }
           System.out.println(resList.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
}
