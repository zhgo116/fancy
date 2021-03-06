/**
 * 地图新建
 */
var map;
//绘制的多边形区域对象
var polygon;
$(document).ready(function() {
	// 地图加载
	map = new AMap.Map("container", {
		resizeEnable : true
	});
	//地图点击事件
	map.on('click', clearBefore);
	//开启鼠标工具
	var mousetool = new AMap.MouseTool(map);
	//鼠标绘图监听
	AMap.event.addListener(mousetool, "draw", function callback(e) {
		polygon = e.obj;
        $("#data").val(e.obj.getPath( ));
    });
	//多边形绘制开启
	mousetool.polygon();
	
	//*************输入提示点击后定位到位置******************
	//输入提示
	var autoOptions = {
		input : "tipinput"
	};
	var auto = new AMap.Autocomplete(autoOptions);
	var placeSearch = new AMap.PlaceSearch({
		map : map
	}); //构造地点查询类
	AMap.event.addListener(auto, "select", select);//注册监听，当选中某条记录时会触发
	function select(e) {
		placeSearch.setCity(e.poi.adcode);
		placeSearch.search(e.poi.name, poicallback); //关键字查询查询
	}

	function poicallback() {
		map.clearMap();
	}
	//*************输入提示点击后定位到位置******************
	
});

//只允许选择一块区域
function clearBefore(){
	if(polygon){
		polygon.setMap(null);
		$("#data").val("");
	}
}

function saveMap(){
	if (!$("input[name='districtId']").val()) {
		 jBox.tip("请选择小区后保存");
		 return;
	}
	if (!$("#name").val()) {
		jBox.tip("请输入地图名称后保存");
		return;
	}
	if (!$("#data").val()) {
		jBox.tip("请绘制地图后保存");
		return;
	}
	var bound = map.getBounds();
	$("#southwest").val(bound.southwest);
	$("#northeast").val(bound.northeast);
	$("#zoom").val(map.getZoom());
	$("#inputForm").submit();
}
