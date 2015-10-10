# Snapshot

## v1.0

使用Toolbar+DrawerLayout组合来进行相册与图片列表的显示

使用ViewHolder模式的ListView进行图片的显示

使用HandlerThread进行图片的异步加载

使用两级缓存，内存缓存及硬盘缓存

### BUG

应用进入后台再回到前台时出现ListView项不跟随手势滑动的情况，暂时是蜜汁BUG