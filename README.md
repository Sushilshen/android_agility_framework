# android_agility
库内包括主流UI控件,网络请求,数据缓存,位图加载,常用工具等模块,有助于应用快速研发和框架搭建<br>
<br>
<h3>网络请求</h3>
  1."HttpClientAgent":将HttpConnection,OKHttp,Volley主流请求框架统一接口调用,支持动态切换<br>
  2."DataParser":网络数据数据解析;文件下载(FileParser),接口访问(GsonParser),图片加载(BitmapParser)<br>
<br>
<h3>主流UI控件</h3>
  1."Draggable":下拉刷新控件,无缝手势越界拖动,支持之定义刷新样式<br>
  2."SweetCircularView":Banner循环轮播控件,轮播索引指示器<br>
  3."SweetProgress":仿IOS加载圈,无需额外图片资源<br>
  4."TabController":选项卡切换控制器<br>
  5."SimpleAdapter":简化Adapter代码逻辑,支持RecycleView<br>
<br>
<h3>通用工具</h3>
  1."BaseUtils":网络状态,数据类型转换,存储空间,反射资源等<br>
<br>
<h3>数据缓存</h3>
  1."DataCacheManager":支持任意数据类型本地存储,采用LRU算法二级缓存，网络接口数据缓存是应用场景之一<br>
<br>
<h3>图片加载</h3> 
  1."ImageLoader":图片异步加载框架,采用LRU算法二级缓存,支持本地,网络图片加载<br>
<br>
<h3>HTTP服务器组件</h3>
  1.整合NanoHTTPPD框架，实现Android本地搭建HTTP服务器；<br>
<br>

<br>
Agility的设计在于简化开发过程中的基础模块，做到功能借口标准，简介，使调用者能一行代码实现复杂的功能。<br>
<br>
    
