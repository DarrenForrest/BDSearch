# BDSearch
百度地图搜索POI服务
1、首先采用非ak认证的方式缩小区域搜索poi;
如：http://api.map.baidu.com/?qt=s&c=" + 百度地图中地市区县编码 + "&wd=" + 搜索关键字 + "&rn=50&pn="+查询页码;
返回结果中有个total字段，如果total值大于750，则需要ak认证方式再次搜索；
注意：百度地图非ak认证方式搜索一个区域最多返回760条记录；
2、ak认证方式搜索需要获取区域左下角坐标和右下角坐标值，划分子区域格式x*y;
如：http://api.map.baidu.com/place/v2/search?query="+搜索关键字+"&bounds="+矩形区域范围(左下角坐标,右下角坐标)+"&page_size=20&page_num="+查询页码+"&scope=2&output=json&ak="+ak;
注意：百度地图ak认证方式搜索一个区域最多返回400条记录；
      非个人认证的ak最多一天可以请求2000次，并发量2次/秒；
      个人认证的ak最多一天可以请求30000次，并发量50次/秒；
      
应用表说明：
co_base_station_info：基站基础信息表
map_areainfo：各地市区县信息表
map_poisearch_ak：各地市对应百度地图检索POI对应的AK
map_poisearch_aknum：每天调用百度地图检索POI使用AK请求次数记录表
map_poisearch_info：百度地图POI检索数据记录表
map_poisearch_nodata：记录区域检索POI无数据的信息表
map_poisearch_record：记录区域检索POI已有数据的信息表
map_poisearch_total：各地市区县对应百度地图POI检索总量记录表
map_resource

Functions 自定义函数：
f_get_searchno   查询当前时间AK调用次数

Procedures 存储过程：
dealpoisearchaknum 插入或更新当前时间AK调用信息
