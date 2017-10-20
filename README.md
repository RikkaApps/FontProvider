# Font Provider

在绝大部分的 Android 系统中 CJK 字体只提供了一个字重，而默认的英语字体 (Roboto) 却提供了多达 7 个字重，
这就是为什么我们会发现在英语环境下，如对话框的按钮、AppBar 标题等要相对粗一些，而 CJK 语言下都是细细的。

因此我们提供了这个应用，为其他应用提供常用字重的 Noto CJK 字体，未来我们还会提供更多使用无限制的字体。

## 对普通用户

在安装我们提供的应用后并下载需要的字体，若使用的应用适配了 Font Provider，就能看到界面“原本的样子”。

## 对应用开发者

### 如何适配 Font Provider

适配 Font Provider 非常简单，最少只需要几行代码即可使用。

#### 加入依赖

[![Download](https://api.bintray.com/packages/rikkaw/FontProvider/api/images/download.svg)](https://bintray.com/rikkaw/FontProvider/api/_latestVersion)

```
implementation 'moe.shizuku.fontprovider:api:<替换为上面的版本号>' // 如果使用 3.0.0 之前的 gradle 插件需要将 implementation 替换为 compile
```
   
#### 申请权限

由于 API 限制，在 Android 7.0 之前的系统使用 Font Provider **需要申请存储权限**。
    
> 由于在 API 24 之前，只能通过 path 创建，而中文字体体积较大，不适合使用像 Google 的 downloadable font 那样通过保存字体至应用的私有空间的方法。因此不得不出此下策，申请存储权限。
    
   
#### 使用

##### （可选）检查可用性

```java
int code = FontProviderClient.checkAvailability(context);
if (code != FontProviderAvailability.OK) {
	// 根据具体的 code 提示用户
}
```
    	

##### 获取 FontProviderClient

我们提供了三种取得 `FontProviderClient` 的方式。

* `void FontProviderClient.create(context, callback)`
	
    通过绑定服务创建可用的 `FontProviderClient`，由于绑定服务是异步的，第一个 Activity 替换的 Typeface 不会生效（因为替换之前就已经创建好了）。
    
* `void FontProviderClient.create(activity, callback, names...)`
	
    通过绑定服务创建可用的 `FontProviderClient`，不同于第一种，在 Callback 执行 `client.replace` 时会遍历该 Activity 种的 TextView 并自动替换其 Typeface。`names` 参数为不存在于 `fonts.xml` 的字体（如 `serif-medium`）。
    
* `FontProviderClient FontProviderClient.createSync(context)`
	
    通过 `Content Provider` 创建可用的 `FontProviderClient`，会由于`Content Provider` 本身的原因消耗更多时间。
    
##### 基本使用方式

一个完整的使用第二种方法的例子：

```java
public class BaseActivity extends FragmentActivity {

    private static boolean sFontProviderInitialized = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!sFontProviderInitialized) {
            // 替换默认的 sans-serif 字体的方法，添加了 Noto Color Emoji
            FontRequests.DEFAULT_SERIF_FONTS = new FontRequest[]{FontRequest.DEFAULT, FontRequest.NOTO_COLOR_EMOJI};
            
            // 创建 FontProviderClient
            FontProviderClient.create(this, new FontProviderClient.Callback() {
                @Override
                public boolean onServiceConnected(FontProviderClient client, ServiceConnection serviceConnection) {
                    // 替换的简单例子
                    
                    // 将 "sans-serif" 替换为 "Noto Sans CJK"，具体字重及默认英语字体将会根据 "sans-serif" 获得
                    client.replace("sans-serif", "Noto Sans CJK");
                    client.replace("sans-serif-medium", "Noto Sans CJK");
                    
                    // 将 "serif" 替换为 "Noto Serif CJK"，并指定替换的字重为 500
                    client.replace("serif", "Noto Serif CJK", 500);
                    return true;
                }
            });

            sFontProviderInitialized = true;
        }
    }
}
```

### FAQ

##### 用户如果没有安装会发生什么？

什么也不会发生。

##### 和直接使用公开的 API 有什么区别呢？

可以决定回退顺序，包含语言字重等信息。
