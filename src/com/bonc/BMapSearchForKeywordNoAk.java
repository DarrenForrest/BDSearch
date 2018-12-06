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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class BMapSearchForKeywordNoAk {
    private static String[] aks = new String[]{"3k0KG0jADuc0GFziTgOWHamzH9KCSdLh","jWK9pF6AOfNER0SbZStYjyO0","1XjLLEhZhQNUzd93EjU5nOGQ"};
	
    public static void main(String[] args) {
		BMapSearchForKeywordNoAk bsfk = new BMapSearchForKeywordNoAk();
		try {
			List<String> res = bsfk.searchBMapInfoByBoundsKeyWord("太原市","37.55535,112.209811,38.120745,113.065285",10,10,"小区",aks[1],true);
			System.out.println("返回结果集合数 "+res.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	private void savePOI(String address,String city,String diTag,String lat,String lng,String poiname,String range,String stdTag,
			String bduid,String keyword,String pn,String nn,String area_name) {
		Connection bizConn=null;
		PreparedStatement ps = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			bizConn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.100.123:1521:sxmbi","autoams","autoams123");
			ps=bizConn.prepareStatement("insert into tb_bmap_search_poi_noak(address,city,diTag,lat,lng,poiname,range,stdTag,bduid,keyword,pn,nn,area_name) values ('"+address+"','"+city+"','"+diTag+"','"+lat+"','"+lng+"','"
			+poiname+"','"+range+"','"+stdTag+"','"+bduid+"','"+keyword+"','"+pn+"','"+nn+"','"+area_name+"')");
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
			ps=bizConn.prepareStatement("select count(*) from tb_bmap_search_poi_noak where bduid='"+uid+"' and lat = '"+lat+"' and lng='"+lng+"'");
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
    
	public List<String> searchBMapInfoByBoundsKeyWord(String cityname,String bounds,
			int WindowSizeXnum,int WindowSizeYnum,
			String keyWord,String ak,boolean flag) throws Exception{
		List<String> list = new ArrayList<String>();
		String cityCode = this.getCityCode(cityname);
        if (cityCode == null || cityCode.equals("")) {
            return list;
        }
		for(int i=0;i<WindowSizeXnum*WindowSizeYnum;i++){
			String smallRect = getSmallRect(bounds,WindowSizeXnum,WindowSizeYnum,i);
			System.out.println("查询区域范围百度墨卡托坐标："+smallRect);
//			int pn=0;
//			while(true){
				try{
					list.addAll(requestBaiduApi(cityCode,keyWord,smallRect,i,(WindowSizeXnum+"*"+WindowSizeYnum),flag,i)); 
//					pn++;
					Thread.sleep(1500);
				}catch(Exception ex){
					ex.printStackTrace();
					break;
				}
//			}
		}
		return list;
	}
	private List<String> requestBaiduApi(String cityCode,String keyWord, String smallRect, int recti,String sizenum,boolean flag,int pn) {
		List<String> list = new ArrayList<String>();
			try{
				int rn=50;
				int nn=0;
				if(pn == 0){
					nn = 0;
				}else if(pn == 1){
					nn = 10;
				}else if(pn == 2){
					pn = 1;
					nn = 10;
				}
				String urlStr = "http://api.map.baidu.com/?qt=s&l=15&rn=50&wd=" + keyWord +"&b=("+smallRect+")";
				System.out.println(urlStr);
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
		            String tmpImage = jsonStr.substring(jsonStr.indexOf("\"image\""));
		            tmpImage = tmpImage.substring(0, tmpImage.indexOf("\",") + 2);
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
		            throw new RuntimeException("百度查询不到区域【"+smallRect+"】POI数据:" + keyWord);
		        }
		        for (int i = 0; i < content.size(); i++) {
		            JSONObject o = (JSONObject) content.get(i);
		            String uid = o.getString("uid");
		            //String image = notNull(o.getString("image"));
		            String x = notNull(o.getString("x"));
		            String y = notNull(o.getString("y"));
		            String name = notNull(o.getString("name"));
		            String area_name = notNull(o.getString("area_name"));
		            String addr = notNull(o.getString("addr"));
		            String stdTag = notNull(o.getString("std_tag"));
		            String diTag = notNull(o.getString("di_tag"));
		            addr = addr.replace("'", "\'");
		            //String primary_uid = notNull(o.getString("primary_uid"));
                    
		            if (isNull(uid) || isNull(x) || isNull(y) || isNull(name)) {
		                continue;
		            }
		            Map<String, Double> point = Baidu.convertMC2LL(Double.parseDouble(x) / 100, Double.parseDouble(y) / 100);
		            JSONObject jpoint = new JSONObject();
		            jpoint.put("lng", point.get("lng"));
		            jpoint.put("lat", point.get("lat"));
		            jpoint.put("name", name);
		            jpoint.put("city", area_name);
		            jpoint.put("addr", addr);
		            jpoint.put("uid", uid);
		            jpoint.put("diTag", diTag);
		            jpoint.put("stdTag", stdTag);
		            //点经纬度生成
		            //区域经纬度生成
		            jpoint = downloadShape(uid, jpoint);
		            if(checkPOIByUid(uid, point.get("lat")+"", point.get("lng")+"")){
		            	savePOI(addr,cityCode, diTag, point.get("lat")+"", point.get("lng")+"", name,jpoint.getString("range"), stdTag, uid, keyWord, pn+"", ((pn-1)*10)+"", area_name);
		            }
		            System.out.println(jpoint.toJSONString());
		            list.add(jpoint.toJSONString());
		        }
			}catch(Exception ex){
				ex.printStackTrace();
			}
		return list;
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
	    Baidu bd = new Baidu();
	    Map<String, Double> left_location = bd.convertLL2MC(Double.valueOf(left_x+""),Double.valueOf(left_y+"")); 
	    Map<String, Double> right_location = bd.convertLL2MC(Double.valueOf(right_x+""),Double.valueOf(right_y+"")); 
	    DecimalFormat decimalFormat = new DecimalFormat("0.00");//格式化设置
	    return decimalFormat.format(left_location.get("x"))+","+decimalFormat.format(left_location.get("y"))
	    +","+decimalFormat.format(right_location.get("x"))+","+decimalFormat.format(right_location.get("y"));
		//return left_y +"," + left_x + ","  + right_y + "," + right_x ;
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
	
	private String getCityCode(String cityName) {
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
}
