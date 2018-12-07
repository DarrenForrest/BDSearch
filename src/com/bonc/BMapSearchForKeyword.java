package com.bonc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class BMapSearchForKeyword {
	private static String[] aks = new String[]{"mjE3I5xDa9TGWElWGg5p80yfHVuLbItu",
			"qzerP2oomVriLfyYCFgfyBZiWzIBpILv",
			"3k0KG0jADuc0GFziTgOWHamzH9KCSdLh",
			"jWK9pF6AOfNER0SbZStYjyO0",
			"tTGPQ3OuSb7Cs8Xg8u1R3YtSMTdL5XAj",
			"0zjoYrH3vUns4AcE5BYti0141lzwcKXa",
			"5AaIx2ltmq1ZdGnOmAtuWBRfxdBU6yNk","nYgIo9qqgcI24IQkqXSOOpsaDsjvPQiX"};
  	
    public static void main(String[] args) {
		BMapSearchForKeyword bsfk = new BMapSearchForKeyword();
//		int i =0;
//		while(true){
//			try {
////				if(i==20){
////					System.out.println("POI搜索达到最大结果限制 400条数据！搜索结束！");
////					break;
////				}
//				System.out.println(bsfk.searchBMapInfoByRegionKeyWord("太原市小店区","小区",i,"json",aks[1],true).toString());
//				i++;
//				Thread.sleep(1500);
//			} catch (Exception e) {
//				e.printStackTrace();
//				break;
//			}
//		}
//		try {
//			//37.751629,112.485346,38.024566,112.626776
//			List<String> res = bsfk.searchBMapInfoByBoundsKeyWord("37.573657,112.226484,38.069857,112.86119",25,25,"物流公司","5AaIx2ltmq1ZdGnOmAtuWBRfxdBU6yNk",true);
//			System.out.println("返回结果集合数 "+res.size());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		List<String[]> areaList = bsfk.areaList();
		List<String[]> poiList = bsfk.poiList();
		
		for(String[] poi:poiList){
			for(String[] area:areaList){
				DownloadBD dd = new DownloadBD(poi[1],poi[0],area[0],"");
		        try{
		        	String regioncode = dd.getCityCode(area[0]);
		            String res = dd.getTotalPoiForKeyWord();
		            if(StringUtils.isNotBlank(res)&&StringUtils.isNotBlank(regioncode)){
		            	System.out.println(area[0]+"  查询【"+poi[0]+"-"+poi[1]+"】数据返回总量："+res);
		            	if(checkPoiTotalIsExist(poi[1],regioncode)){
		            		insertBMapSearchPoiTotal(poi[0],poi[1],area,regioncode,res);
		            	}else{
		            		updateBMapSearchPoiTotal(poi[0],poi[1],area,regioncode,res);
		            	}
		            }
		        }catch (Exception e){
		            e.printStackTrace();
		        }
			}
		}
	}
    
	private static void insertBMapSearchPoiTotal(String firstpoi,String secondpoi, String[] area, String regioncode, String res) {
		Connection bizConn=null;
		PreparedStatement ps = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","map_auto","MAP_AUTO");
			ps=bizConn.prepareStatement("insert into map_poisearch_total(keyword,regionname,regioncode,total,bounds,stepnum,wd2name) values ('"+secondpoi+"','"+area[0]+"','"+regioncode+"','"+res+"','"+area[1]+"','"+area[2]+"','"+firstpoi+"')");
			ps.execute();
			ps.close();
			ps = null;
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				ps.close();
				ps = null;
				bizConn.close();
				bizConn = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void updateBMapSearchPoiTotal(String firstpoi,String secondpoi, String[] area, String regioncode, String res) {
		Connection bizConn=null;
		PreparedStatement ps = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","map_auto","MAP_AUTO");
			ps=bizConn.prepareStatement("update map_auto.map_poisearch_total set total='"+res+"',bounds='"+area[1]+"',stepnum='"+area[2]+"' where keyword='"+secondpoi+"' and regioncode='"+regioncode+"'");
			ps.execute();
			ps.close();
			ps = null;
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				ps.close();
				ps = null;
				bizConn.close();
				bizConn = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 行政区划区域检索
	 * 关键字搜索百度地图的POI数据
	 * @param city                行政区域 如：太原市或太原市小店区
	 * @param keyWord        关键字    如：小区、酒店、KTV
	 * @param pageNum      搜索分页码：分页页码，默认为0,0代表第一页，1代表第二页，以此类推。 常与page_size搭配使用。系统中默认采用page_size=20
	 * @param outputType   检索结果输出格式为json或者xml 
	 * @param ak                 百度地图授权使用  你的密钥
	 * @param no                 检索批次
	 * @return                       返回json格式字符串
	 * @throws Exception
	 */
	public String searchBMapInfoByRegionKeyWord(String city,String keyWord,int pageNum,String outputType,String ak,boolean flag) throws Exception {
		int akno = (int)(1+Math.random()*(aks.length-1));
		if(StringUtils.isBlank(ak)){
			ak = aks[akno];
		}
		JSONArray jarray = new JSONArray();
		if (city == null || "".equals(city)) {
            return "";
        }
        if(outputType == null || "".equals(outputType)){
        	outputType="json";
        }
        int pageSize = 20;//一次搜索最多返回数据条数
        boolean refreash = true;
        if(flag){
        	refreash = checkIsExistsPoiDataForRegion(city,keyWord,pageNum+"");
        }
        if(refreash){
        	refreash = checkIsExistsNoPoiData("", keyWord, "", pageNum+"", city);
        }
        if(refreash){
        	String urlStr = "http://api.map.baidu.com/place/v2/search?query="+keyWord+"&region="+city+"&page_size="+pageSize+"&page_num="+pageNum+"&output="+outputType+"&ak="+ak;
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
            JSONObject obj = JSONObject.parseObject(jsonStr);
            JSONArray content = (JSONArray) obj.get("results");
            if (content == null || content.isEmpty()) {
            	insertSearchNoPoiData("", keyWord, "", pageNum+"", city);
                //百度查询不到
                throw new RuntimeException("百度地图在第"+(pageNum+1)+"页查询不到" + city + ":" + keyWord);
            }
            for (int i = 0; i < content.size(); i++) {
                JSONObject o = (JSONObject) content.get(i);
                String name = notNull(o.getString("name"));
                JSONObject location =  o.getJSONObject("location");
                String address = notNull(o.getString("address"));
                String province = notNull(o.getString("province"));
                String cityName = notNull(o.getString("city"));
                String area = notNull(o.getString("area"));
                String street_id = notNull(o.getString("street_id"));
                String uid = o.getString("uid");
                address = address.replace("'", "\'");

                JSONObject jpoint = new JSONObject();
                jpoint.put("name", name);
                jpoint.put("province", province);
                jpoint.put("city", cityName);
                jpoint.put("area", area);
                jpoint.put("address", address);
                jpoint.put("streetId", street_id);
                jpoint.put("uid", uid);
                jpoint.put("lat", notNull(location.get("lat")));
                jpoint.put("lng", notNull(location.get("lng")));
                jpoint = downloadShape(uid, jpoint);
                jarray.add(jpoint);
                //数据存入数据库
                if(checkPOIByUid(uid,notNull(location.get("lat")),notNull(location.get("lng")))){
                	savePOI(name,province,cityName,area,address,street_id,uid,notNull(location.get("lat")),notNull(location.get("lng")),keyWord,jpoint.getString("range"),pageNum+"","","",city);
                }
            }
        }
        return jarray.toJSONString();
    }
	
	private void savePOI(String name, String province, String cityName, String area, String address, String street_id,
			String uid, String lat, String lng,String keyWord,String range,String no,String bounds,String sizeNum,String region) {
		Connection bizConn=null;
		PreparedStatement ps = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","autoams","autoams123");
			ps=bizConn.prepareStatement("insert into tb_bmap_search_poi(poiname,province,cityname,area,address,street_id,bduid,lat,lng,createtime,batchno,keyword,range,bounds,stepnum,region) values ('"+name+"','"+province+"','"+cityName+"','"+area+"','"+address+"','"+street_id+"','"+uid+"','"+lat+"','"+lng+"',sysdate,'"+no+"','"+keyWord+"','"+range+"','"+bounds+"','"+sizeNum+"','"+region+"')");
			ps.execute();
			ps.close();
			ps = null;
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				ps.close();
				ps = null;
				bizConn.close();
				bizConn = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	//根据POI名称、百度地图uid、百度坐标系经纬度查询是否已存在该POI数据
	private boolean checkPOIByUid(String uid, String lat, String lng) {
		boolean flag = true;
		Connection bizConn=null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","autoams","autoams123");
			ps=bizConn.prepareStatement("select count(*) from tb_bmap_search_poi where bduid='"+uid+"' and lat = '"+lat+"' and lng='"+lng+"'");
			rs = ps.executeQuery();
			int no = 0;
			while (rs.next()) {
				no = rs.getInt(1);
			}
			if(no > 0){
				flag = false;
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (ps != null) {
				ps.close();
				ps = null;
			}
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (ps != null) {
					ps.close();
					ps = null;
				}
				if (bizConn != null) {
					bizConn.close();
					bizConn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
	private String notNull(Object o) {
        if (o == null) {
            return "";
        } else {
            return o.toString().trim();
        }
    }
    
	public List<String> searchBMapInfoByBoundsKeyWord(String bounds,
			int WindowSizeXnum,int WindowSizeYnum,
			String keyWord,String ak,boolean flag) throws Exception{
		List<String> list = new ArrayList<String>();
		for(int i=0;i<WindowSizeXnum*WindowSizeYnum;i++){
			if(StringUtils.isBlank(ak)){
				int akno = (int)(1+Math.random()*(aks.length-1));
				ak = aks[akno];
			}
			String smallRect = getSmallRect(bounds,WindowSizeXnum,WindowSizeYnum,i);
			//System.out.println("第"+(i+1)+"子区域范围："+smallRect);
			list.addAll(requestBaiduApi(keyWord,smallRect,ak,i,(WindowSizeXnum+"*"+WindowSizeYnum),flag)); 
			Thread.sleep(1000); 
		}
		return list;
	}
	private List<String> requestBaiduApi(String keyWord, String smallRect, String ak, int recti,String sizenum,boolean flag) {
		List<String> list = new ArrayList<String>();
		for(int pageNum=0;pageNum<20;pageNum++){
			try{
				boolean refreash=true;//是否重新检索该区域的某页数据
				if(flag){//是否检查表中是否已存在该区域的某页数据:20条为判断依据
					refreash = checkIsExistsPoiDataForBounds(smallRect,keyWord,sizenum,recti+"-"+pageNum);
				}
				if(refreash){
					//检查之前是否已有过调用接口查询不到数据的记录
					refreash = checkIsExistsNoPoiData(smallRect, keyWord, sizenum, recti+"-"+pageNum, "");
				}
				if(refreash){
					String urlStr = "http://api.map.baidu.com/place/v2/search?query="+keyWord+"&bounds="+smallRect+"&page_size=20&page_num="+pageNum+"&output=json&ak="+ak;
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
			        JSONObject obj = JSONObject.parseObject(jsonStr);
			        JSONArray content = (JSONArray) obj.get("results");
			        if(content == null || content.isEmpty()){
			        	System.out.println("矩形区域第"+(recti+1)+"划分区域【"+smallRect+"】第"+(pageNum+1)+"页未检索到POI数据，程序退出");
			        	insertSearchNoPoiData(smallRect,keyWord,sizenum,recti+"-"+pageNum,"");
			        }else{
				        for (int i = 0; i < content.size(); i++) {
				            JSONObject o = (JSONObject) content.get(i);
				            String name = notNull(o.getString("name"));
				            JSONObject location =  o.getJSONObject("location");
				            String address = notNull(o.getString("address"));
				            String province = notNull(o.getString("province"));
				            String cityName = notNull(o.getString("city"));
				            String area = notNull(o.getString("area"));
				            String street_id = notNull(o.getString("street_id"));
				            String uid = o.getString("uid");
				            address = address.replace("'", "\'");
			
				            JSONObject jpoint = new JSONObject();
				            jpoint.put("name", name);
				            jpoint.put("province", province);
				            jpoint.put("city", cityName);
				            jpoint.put("area", area);
				            jpoint.put("address", address);
				            jpoint.put("streetId", street_id);
				            jpoint.put("uid", uid);
				            jpoint.put("lat", notNull(location.get("lat")));
				            jpoint.put("lng", notNull(location.get("lng")));
				            
				            if(!list.contains(jpoint.toJSONString())){
				            	jpoint = downloadShape(uid,jpoint);
				            	list.add(jpoint.toJSONString());
				            }
				            
				            //数据存入数据库
				            if(checkPOIByUid(uid,notNull(location.get("lat")),notNull(location.get("lng")))){
				            	System.out.println(jpoint.toJSONString());
				            	savePOI(name,province,cityName,area,address,street_id,uid,notNull(location.get("lat")),notNull(location.get("lng")),keyWord,jpoint.getString("range"),recti+"-"+pageNum,smallRect,sizenum,"");
				            }
				        }
			        }
			        Thread.sleep(1500);
				}
			}catch(Exception ex){
				ex.printStackTrace();
				break;
			}
		}
		return list;
	}
	
	
	
	private void insertSearchNoPoiData(String smallRect, String keyWord, String sizenum, String pagenum,
			String regionname) {
		Connection bizConn=null;
		PreparedStatement ps = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","autoams","autoams123");
			ps=bizConn.prepareStatement("insert into tb_bmap_search_nodata (id,smallbounds,regionname,stepnum,pagenum,keyword) values (sys_guid(),'"+smallRect+"','"+regionname+"','"+sizenum+"','"+pagenum+"','"+keyWord+"')");
			ps.execute();
			ps.close();
			ps = null;
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				ps.close();
				ps = null;
				bizConn.close();
				bizConn = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean checkIsExistsNoPoiData(String smallRect, String keyWord, String sizenum, String batchno,String regionname) {
		boolean flag = true;
		Connection bizConn=null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","autoams","autoams123");
			if(StringUtils.isNotBlank(regionname)){
				ps=bizConn.prepareStatement("select count(*) from tb_bmap_search_nodata where regionname='"+regionname+"' and pagenum='"+batchno+"' and keyword='"+keyWord+"'");
			}else{
				ps=bizConn.prepareStatement("select count(*) from tb_bmap_search_nodata where smallbounds='"+smallRect+"' and stepnum='"+sizenum+"' and pagenum='"+batchno+"' and keyword='"+keyWord+"'");
			}
			rs = ps.executeQuery();
			int no = 0;
			while (rs.next()) {
				no = rs.getInt(1);
			}
			if(no > 0){
				flag = false;
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (ps != null) {
				ps.close();
				ps = null;
			}
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (ps != null) {
					ps.close();
					ps = null;
				}
				if (bizConn != null) {
					bizConn.close();
					bizConn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
	
	private boolean checkIsExistsPoiDataForBounds(String smallRect, String keyWord, String sizenum, String batchno) {
		boolean flag = true;
		Connection bizConn=null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","autoams","autoams123");
			ps=bizConn.prepareStatement("select count(*) from tb_bmap_search_poi where batchno='"+batchno+"' and bounds='"+smallRect+"' and stepnum='"+sizenum+"' and keyWord='"+keyWord+"'");
			rs = ps.executeQuery();
			int no = 0;
			while (rs.next()) {
				no = rs.getInt(1);
			}
			if(no == 20){
				flag = false;
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (ps != null) {
				ps.close();
				ps = null;
			}
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (ps != null) {
					ps.close();
					ps = null;
				}
				if (bizConn != null) {
					bizConn.close();
					bizConn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
	private boolean checkIsExistsPoiDataForRegion(String region,String keyWord,String batchno) {
		boolean flag = true;
		Connection bizConn=null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","autoams","autoams123");
			ps=bizConn.prepareStatement("select count(*) from tb_bmap_search_poi where batchno='"+batchno+"' and region='"+region+"' and keyWord='"+keyWord+"'");
			rs = ps.executeQuery();
			int no = 0;
			while (rs.next()) {
				no = rs.getInt(1);
			}
			if(no == 20){
				flag = false;
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (ps != null) {
				ps.close();
				ps = null;
			}
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (ps != null) {
					ps.close();
					ps = null;
				}
				if (bizConn != null) {
					bizConn.close();
					bizConn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
	private String getSmallRect(String bounds, int windowSizeXnum, int windowSizeYnum, int i) {
//34.601479,110.211749,40.765297,114.093579
// 获取小矩形的左上角和右下角坐标字符串（百度坐标系） :
// param bigRect: 关注区域坐标信息 :
//	param windowSize: 细分窗口数量信息 :
//	param windowIndex: Z型扫描的小矩形索引号 :
//	return: lat,lng,lat,lng """ 
		float offset_x = (Float.parseFloat(bounds.split(",")[3])-Float.parseFloat(bounds.split(",")[1]))/windowSizeXnum;
		float offset_y = (Float.parseFloat(bounds.split(",")[2])-Float.parseFloat(bounds.split(",")[0]))/windowSizeYnum;
		float left_x = Float.parseFloat(bounds.split(",")[1]) + offset_x * (i % windowSizeXnum);
		float left_y = Float.parseFloat(bounds.split(",")[0]) + offset_y * (i / windowSizeYnum);
		float right_x = (left_x + offset_x);
	    float right_y = (left_y + offset_y);
		return left_y +"," + left_x + ","  + right_y + "," + right_x ;
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
                if (geo.indexOf("|1-") > 0) {
                    geo = geo.substring(geo.indexOf("|1-") + 3);
                    geo = geo.substring(0, geo.indexOf(";"));
                    String[] pointStrArr = geo.split(",");
                    if (pointStrArr.length % 2 == 0) {
                        JSONArray tempArray = new JSONArray();
                        for (int i = 0; i < pointStrArr.length; i += 2) {
                            double[] point = PointUtil.convertMC2LL(new double[]{Double.parseDouble(pointStrArr[i]), Double.parseDouble(pointStrArr[i + 1])});
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
            e.printStackTrace();
        }
        return jpoint;
    }
	
	public List<String[]> areaList(){
		List<String[]> list = new ArrayList<String[]>();
		Connection bizConn=null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","map_auto","MAP_AUTO");
			ps=bizConn.prepareStatement("select area_name,bounds,stepnum from map_auto.map_areainfo where area_level='3' order by area_code asc");
			rs = ps.executeQuery();
			while (rs.next()) {
				String[]  str = new String[3];
				str[0]=rs.getString(1);
				str[1]=rs.getString(2);
				str[2]=rs.getString(3);
				list.add(str);
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (ps != null) {
				ps.close();
				ps = null;
			}
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (ps != null) {
					ps.close();
					ps = null;
				}
				if (bizConn != null) {
					bizConn.close();
					bizConn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public List<String[]> poiList(){
		List<String[]> list = new ArrayList<String[]>();
		Connection bizConn=null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","map_auto","MAP_AUTO");
			ps=bizConn.prepareStatement("select first_type,second_type from map_auto.MAP_RESOURCE");
			rs = ps.executeQuery();
			while (rs.next()) {
				String[] str = new String[2];
				str[0]=rs.getString(1);//一级poi
				str[1]=rs.getString(2);//二级poi
				list.add(str);
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (ps != null) {
				ps.close();
				ps = null;
			}
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (ps != null) {
					ps.close();
					ps = null;
				}
				if (bizConn != null) {
					bizConn.close();
					bizConn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	private static boolean checkPoiTotalIsExist(String keyword,String regioncode){
		boolean flag = true;
		Connection bizConn=null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","map_auto","MAP_AUTO");
			ps=bizConn.prepareStatement("select count(*) from map_auto.map_poisearch_total where keyword='"+keyword+"' and regioncode='"+regioncode+"'");
			rs = ps.executeQuery();
			int n=0;
			while (rs.next()) {
				n = rs.getInt(1);
			}
			if(n>0){
				flag = false;
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (ps != null) {
				ps.close();
				ps = null;
			}
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (ps != null) {
					ps.close();
					ps = null;
				}
				if (bizConn != null) {
					bizConn.close();
					bizConn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
	
}
