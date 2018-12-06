package com.bonc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class BMapSearch {
    public static void main(String[] args) {
		BMapSearch bsfk = new BMapSearch();
		try {
			//37.751629,112.485346,38.024566,112.626776
			List<String> res = bsfk.searchBMapInfoByBoundsKeyWord("37.573657,112.226484,38.069857,112.86119",1,1,"小区","5AaIx2ltmq1ZdGnOmAtuWBRfxdBU6yNk","房地产",null,2000,null);
			System.out.println("返回结果集合数 "+res.size());
		} catch (Exception e) {
			e.printStackTrace();
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
	public List<String> searchBMapInfoByRegionKeyWord(String city,String keyWord,String outputType,String ak,String wd2Name) throws Exception {
		List<String> list = new ArrayList<String>();
		if (city == null || "".equals(city)) {
            return list;
        }
        if(outputType == null || "".equals(outputType)){
        	outputType="json";
        }
        int pageSize = 20;//一次搜索最多返回数据条数
        for(int ii=0;ii<20;ii++){
        	String urlStr = "http://api.map.baidu.com/place/v2/search?query="+keyWord+"&region="+city+"&page_size="+pageSize+"&scope=2&page_num="+ii+"&output="+outputType+"&ak="+ak;
            if(StringUtils.isNotBlank(wd2Name)){
            	urlStr+="&wd2="+wd2Name;
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
            JSONObject obj = JSONObject.parseObject(jsonStr);
            JSONArray content = (JSONArray) obj.get("results");
            if (content == null || content.isEmpty()) {
                //百度查询不到
            	System.out.println("百度地图在第"+(ii+1)+"页查询不到" + city + ":" + keyWord+"的POI数据");
            	break;
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
                
                String tag = "";
                JSONObject detail = (JSONObject)o.getJSONObject("detail_info");
                if(detail != null){
                	tag = detail.getString("tag");
                }
                
                JSONObject jpoint = new JSONObject();
                jpoint.put("name", name);
                jpoint.put("province", province);
                jpoint.put("city", cityName);
                jpoint.put("area", area);
                jpoint.put("address", address);
                jpoint.put("streetId", street_id);
                jpoint.put("uid",uid);
                jpoint.put("tag",tag);
                jpoint.put("lat", notNull(location.get("lat")));
                jpoint.put("lng", notNull(location.get("lng")));
                
            	jpoint = downloadShape(uid,jpoint);
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
    
	public List<String> searchBMapInfoByBoundsKeyWord(String bounds,
			int WindowSizeXnum,int WindowSizeYnum,
			String keyWord,String ak,String tag,List<String[]> listSearchNoData,int quota
			,List<String[]> listRecord) throws Exception{
		List<String> list = new ArrayList<String>();
		for(int i=0;i<WindowSizeXnum*WindowSizeYnum;i++){
			String smallRect = getSmallRect(bounds,WindowSizeXnum,WindowSizeYnum,i);
			list.addAll(requestBaiduApi(keyWord,smallRect,ak,i,(WindowSizeXnum+"*"+WindowSizeYnum),tag,listSearchNoData,quota,listRecord)); 
		}
		return list;
	}
	
	private List<String> requestBaiduApi(String keyWord, String smallRect, String ak, int recti,String sizenum,String tag,List<String[]> listSearchNoData,int quota,List<String[]> listRecord) {
		List<String> list = new ArrayList<String>();
		Set<Integer> listNoInt = new HashSet<Integer>();
		Set<Integer> listInt = new HashSet<Integer>();
		for(int pageNum=0;pageNum<20;pageNum++){
			listInt.add(pageNum);
			if(listSearchNoData.size()>0){
				for(String[] poinodata:listSearchNoData){
					if(smallRect.equals(poinodata[0]) && pageNum == Integer.valueOf(poinodata[1])
							&& keyWord.equals(poinodata[2]) && sizenum.equals(poinodata[3])){
						listNoInt.add(pageNum);
					}
				}
			}
			if(listRecord.size()>0){
				for(String[] record:listRecord){
					if(smallRect.equals(record[7]) && keyWord.equals(record[1]) && sizenum.equals(record[6])){
						if(StringUtils.isNotBlank(record[8]) &&  pageNum == Integer.valueOf(record[8])){
							listNoInt.add(pageNum);
						}
					}
				}
			}
		}
		
		if(listNoInt.size()>0){
			for(int i:listNoInt){
				listInt.remove(i);
			}
		}
		
		for(int pageNum=0;pageNum<20;pageNum++){
			try{
				boolean flag = false;
				List<String> resList = new ArrayList<String>();
				if(listInt.size()>0){
					for(Integer poinodata:listInt){
						if(pageNum == poinodata){
							int searchnos = getSearchAkNos(ak);
							if(searchnos>quota){
								flag = true;
								break;
							}else{
								insertSearchAkNos(ak);
							}
							String urlStr = "http://api.map.baidu.com/place/v2/search?query="+keyWord+"&bounds="+smallRect+"&page_size=20&scope=2&page_num="+pageNum+"&output=json&ak="+ak;
					        if(StringUtils.isNotBlank(tag)){
					        	urlStr+="&tag="+tag;
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
					        JSONObject obj = JSONObject.parseObject(jsonStr);
					        JSONArray content = (JSONArray) obj.get("results");
					        if(content == null || content.isEmpty()){
					        	System.out.println("矩形区域第"+(recti+1)+"划分区域【"+smallRect+"】第"+(pageNum+1)+"页未检索到POI数据，程序退出");
					        	list.add("矩形区域子区域未检索到POI数据:"+(recti)+"||"+smallRect+"||"+(pageNum));
					        	flag = true;
					        	break;
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
					
						            tag = "";
					                JSONObject detail = (JSONObject)o.getJSONObject("detail_info");
					                if(detail != null){
					                	tag = detail.getString("tag");
					                }
						            
						            JSONObject jpoint = new JSONObject();
						            jpoint.put("name", name);
						            jpoint.put("province", province);
						            jpoint.put("city", cityName);
						            jpoint.put("area", area);
						            jpoint.put("addr", address);
						            jpoint.put("streetId", street_id);
						            jpoint.put("uid", uid);
						            jpoint.put("tag", tag);
						            jpoint.put("lat", notNull(location.get("lat")));
						            jpoint.put("lng", notNull(location.get("lng")));
						            
					            	jpoint = downloadShape(uid,jpoint);
					            	list.add(jpoint.toJSONString());
					            	resList.add(jpoint.toJSONString());
					            	System.out.println(jpoint.toJSONString());
						        }
					        }
					        Thread.sleep(1000);
						}
					}
				}else{
					int searchnos = getSearchAkNos(ak);
					if(searchnos>quota){
						flag = true;
						break;
					}else{
						insertSearchAkNos(ak);
					}
					String urlStr = "http://api.map.baidu.com/place/v2/search?query="+keyWord+"&bounds="+smallRect+"&page_size=20&scope=2&page_num="+pageNum+"&output=json&ak="+ak;
			        if(StringUtils.isNotBlank(tag)){
			        	urlStr+="&tag="+tag;
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
			        JSONObject obj = JSONObject.parseObject(jsonStr);
			        JSONArray content = (JSONArray) obj.get("results");
			        if(content == null || content.isEmpty()){
			        	System.out.println("矩形区域第"+(recti+1)+"划分区域【"+smallRect+"】第"+(pageNum+1)+"页未检索到POI数据，程序退出");
			        	list.add("矩形区域子区域未检索到POI数据:"+(recti)+"||"+smallRect+"||"+(pageNum));
			        	flag = true;
			        	break;
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
			
				            tag = "";
			                JSONObject detail = (JSONObject)o.getJSONObject("detail_info");
			                if(detail != null){
			                	tag = detail.getString("tag");
			                }
				            
				            JSONObject jpoint = new JSONObject();
				            jpoint.put("name", name);
				            jpoint.put("province", province);
				            jpoint.put("city", cityName);
				            jpoint.put("area", area);
				            jpoint.put("addr", address);
				            jpoint.put("streetId", street_id);
				            jpoint.put("uid", uid);
				            jpoint.put("tag", tag);
				            jpoint.put("lat", notNull(location.get("lat")));
				            jpoint.put("lng", notNull(location.get("lng")));
				            
			            	jpoint = downloadShape(uid,jpoint);
			            	list.add(jpoint.toJSONString());
			            	resList.add(jpoint.toJSONString());
			            	System.out.println(jpoint.toJSONString());
				        }
			        }
			        Thread.sleep(1000);
				}
				if(resList.size()>0){
					try{
						tnSavePoiSearchRecord(listRecord,smallRect,pageNum,resList.size(),"AK");
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
				if(flag){
					break;
				}
			}catch(Exception ex){
				ex.printStackTrace();
				break;
			}
		}
		return list;
	}
	
	private void tnSavePoiSearchRecord(List<String[]> listRecord, String smallRect, int pageNum, int size,
			String searchType) {
		Connection bizConn=null;
		PreparedStatement ps=null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","map_auto","MAP_AUTO");
			String str="insert into map_poisearch_record(FIRSTPOI,SECONDPOI,REGIONNAME,REGIONCODE,AREACODE,BOUNDS,STEPNUM,SMALLBOUNDS,PAGENUM,SEARCHRESULT,SEARCHTYPE,CREATETIME) values "
					+ "('"+listRecord.get(0)[0]+"','"+listRecord.get(0)[1]+"',"
							+ "'"+listRecord.get(0)[2]+"','"+listRecord.get(0)[3]+"','"+listRecord.get(0)[4]+"',"
									+ "'"+listRecord.get(0)[5]+"','"+listRecord.get(0)[6]+"',"
											+ "'"+smallRect+"','"+pageNum+"','"+size+"','"+searchType+"',sysdate)";
			System.out.println(str);
			ps=bizConn.prepareStatement(str);
			ps.execute();
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
	}
	
	private void insertSearchAkNos(String ak) {
		Connection bizConn=null;
		CallableStatement cs=null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","map_auto","MAP_AUTO");
			String str="{call dealpoisearchaknum(?)}";
			cs=bizConn.prepareCall(str);
			cs.setString(1, ak);
			cs.execute();
			if (cs != null) {
				cs.close();
				cs = null;
			}
			if (bizConn != null) {
				bizConn.close();
				bizConn = null;
			}
		} catch (Exception se) {
			se.printStackTrace();
			try {
				if (cs != null) {
					cs.close();
					cs = null;
				}
				if (bizConn != null) {
					bizConn.close();
					bizConn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	private int getSearchAkNos(String ak) {
		Connection bizConn=null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int n=0;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","map_auto","MAP_AUTO");
			ps=bizConn.prepareStatement("select searchno from map_poisearch_aknum where ak='"+ak+"' and createdate=to_char(sysdate,'yyyy-MM-dd')");
			rs = ps.executeQuery();
			while (rs.next()) {
				n = rs.getInt(1);
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
		return n;
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
}
