### 浮游商城应用（Plankton Mall App）

#### Emoji 标识（win10 快捷键：win + .）

- ✅ = 完成 > 中文输入法：无 > 在 emoji 分类（底部）：“符号”（心形icon）
- ⏳ = 处理中（正在进行，请稍候） > 中文输入法：沙漏
- 📌 = 待办（有待处理事项，需关注） > 中文输入法：图钉
- ❎/⛔/🚫 = 已取消或不需要处理 > 中文输入法：错误 > 在 emoji 分类（底部）：“符号”（心形icon）
- 🛠 = 需要修改（需手动修改）> 中文输入法：无 > 在 emoji 分类（底部）：“庆祝和物品”（气球icon），找到“锤子与扳手”
- 💤 = 暂缓/搁置（睡着了，以后再说） > 中文输入法：睡觉
- 🔥 = 紧急待办（火烧眉毛了） > 中文输入法：火焰
- ❓ = 疑问 > 中文输入法：问号 > 在 emoji 分类（底部）：“符号”（心形icon）
- ✨/🌟 = 重要 > 中文输入法：无
- ⚠ = 注意 > 中文输入法：警告
- 🐞/🐛 = bug > 中文输入法：瓢虫/毛毛虫

#### App 页面和完成情况

- 主页页面：
    - 页面轮播、商品列表：下拉刷新、上划加载更多 ✅
    - 数据：Room💤 + Network✅
- 搜索列表页面（展示搜索结果）：
    - 页面下拉刷新、上滑加载更多 ✅
    - 数据： Room💤 + Network✅
- 购物车页面：
    - 页面 Flow 监听所有购物车数据，不分页，没下拉刷新；✅
    - 数据：Room✅ + Network✅
- 立即购买 > 订单不用缓存，直接网络提交、读取。若成功，则跳转到订单页 ✅
- 底部导航栏，购物车设置角标 ✅
- 个人中心页面: ✅
    - 头部：头像和昵称 ✅
    - 订单：
        - 页面：下拉刷新、上滑加载更多 ✅
        - 数据：Room✅ + Network✅
    - 足迹（历史）
        - 页面：一次性加载所有本地库数据✅，删除足迹💤
        - 数据：仅本地数据，没下拉刷新、上划加载更多 ✅
    - 收藏：
        - 页面：左滑删除、下拉刷新、上滑加载更多✅
        - 数据：Room✅ + Network✅
    - 个人信息页面：展示✅，编辑💤
- app 启动背景 🚫
- 每次导航到主页（home）都发送请求服务器？ > 优化数据展示：RemoteMediator 💤
- 响应式布局 💤

#### Room

- 对于`Room`的所有读写操作，不应该放在`ViewModel`中，而应该全部封装在`Repository`，然后在`RepositoryImpl`中实现；
    - 单一职责（SRP）：`ViewModel`持有并处理 UI 状态、响应事件，`Repository`管理数据源（网络API、本地数据库、内存缓存）；
    - 数据源解耦：未来业务变化，只需修改`Repository`，不用改`ViewModel`；
    - 唯一数据源：`ViewModel`观察`Repository`暴露的`Flow`，遵循单向数据流；
- 最佳实践：
    - `ViewModel`收到用户操作 > 调用`Repository`的方法 > `Repository`写入`Room`；
    - `Repository`监听`Room`作为`Flow`（如：`fun observeCartLocalData(): Flow<List<CartProduct>>`）；
    - `ViewModel`中订阅收集`Repository`的`Flow`，收到自动更新 UI。
- 查看`databases`文件：
    - View > Tool Windows > Device Explorer，默认情况下，这个文件位于当前应用的私有目录：
    - `/data/data/<包名>/databases/<初始化 Database 时指定的名字>`是一种二进制格式；
- 初始化 Database 时指定的名字，在`~/di/DatabaseModule.kt` 中；

#### DataStore

- 查看`DataStore`文件：
    - View > Tool Windows > Device Explorer，默认情况下，这个文件位于当前应用的私有目录：
    - `/data/data/<包名>/files/datastore/<初始化 DataStore 时指定的名字>.preferences_pb`是一种二进制格式；
- 初始化 DataStore 时指定的名字，在`~/di/DataStoreModule.kt` 中；
- 在`DataStore(Preferences DataStore)`中，不管往`MutablePreferences`对象（行为类似于Map）里塞了多少个键值对，也不管调用了几次`edit { }`，最终在磁盘上都只会对应一个物理文件；

#### 其他

- “页面初始化”和“页面下拉刷新”不一样，页面初始化相当于 page reload，下拉刷新是一个手势逻辑；
- `*RepositoryImpl`中，最好不要依赖其他`Repository`，而是操作底层`DataStore`或`Dao`，否则容易引起循环依赖；

#### ISSUES

- 待优化：下拉刷新页面、上滑加载更多的功能，没完全测试，主页的列表这两个功能完善的比较好，其他页面可参考主页；

#### 效果

- 竖屏
- ![screenshot_portrait](./app/src/main/res/drawable/pixpin_2026_11_09_22_39_16.png)
