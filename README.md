# Font Provider

在绝大部分的 Android 系统中 CJK 字体只提供了一个字重，而默认的英语字体 (Roboto) 却提供了多达 7 个字重，
这就是为什么我们会发现在英语环境下，如对话框的按钮、AppBar 标题等要相对粗一些，而 CJK 语言下都是细细的。

因此我们提供了这个应用，为其他应用提供常用字重的 Noto CJK 字体。

## 对普通用户
在安装我们提供的应用后，若使用的应用适配了 Font Provider，就能看到界面“原本的样子”。

## 对应用开发者

### 如何适配 Font Provider

1. 加入依赖，如果使用 3.0.0 之前的 gradle 插件需要将 `implementation` 替换为 `compile`
   
   `implementation 'moe.shizuku.fontprovider:api:1.0.8' // 在 release 可以看到最新版本`
   
2. 在合适的地方（如 `Application.onCreate`）调用 `TypefaceReplacer.init(Context context, )` 。

3. 在需要的地方只需按原本的方式使用即可，比如在 layout xml 中 `android:fontFamily="sans-serif-medium"` 
或是直接创建 `Typeface` 实例 `Typeface.create("sans-serif-medium", )`。

### 技术细节

调用 `TypefaceReplacer.init` 后将通过绑定服务向 Font Provider 应用的 `FontProviderService` 索要字体文件，
之后将通过反射使用私有 API 创建对应的 `Typeface`，并替换 `Typeface` 中的对应缓存，因此对应用开发者几乎透明。

由于绑定服务需要时间，因此在完成前已经创建的 `Typeface` 将不会被替换。

### FAQ

##### 用户如果没有安装会发生什么？

什么也不会发生。

##### 和直接使用公开的 API 有什么区别呢？

可以决定回退顺序，包含语言字重等信息。
