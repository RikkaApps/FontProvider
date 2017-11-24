# Font Provider

在绝大部分的 Android 系统中 CJK 字体只提供了一个字重，而默认的英语字体 (Roboto) 却提供了多达 7 个字重，
这就是为什么我们会发现在英语环境下，如对话框的按钮、AppBar 标题等要相对粗一些，而 CJK 语言下都是细细的。

因此我们提供了这个应用，为其他应用提供常用字重的 Noto CJK 字体，未来我们还会提供更多使用无限制的字体。

## 对普通用户

在安装我们提供的应用后并下载需要的字体，若使用的应用适配了 Font Provider，就能看到界面“原本的样子”。

### 已支持的应用
|图片|名称和下载|介绍|
|--|--|--|
|<img src="https://lh3.googleusercontent.com/lL4apRXXEY0c-zuiulfv7HwNqifLSGoqwTSsgkeLmse-SJG2ocI_glRfBCz3cvt9noiH=w300-rw" alt="AppOps" title="AppOps" width="50" height="50" />|[AppOps](https://play.google.com/store/apps/details?id=rikka.appops)|长得最好看的 appops 的 UI，还支持免 root 使用|

如果你的应用已支持 Font Provider，可以通过 pull request 来加入上面的列表。

## 对应用开发者

### 如何适配 Font Provider

适配 Font Provider 非常简单，最少只需要几行代码即可使用。

#### 加入依赖

[![Download](https://api.bintray.com/packages/rikkaw/FontProvider/api/images/download.svg)](https://bintray.com/rikkaw/FontProvider/api/_latestVersion)

```
implementation 'moe.shizuku.fontprovider:api:<替换为上面的版本号>' // 如果使用 3.0.0 之前的 gradle 插件需要将 implementation 替换为 compile
```
   
#### 申请权限

由于 API 限制，在 Android 7.0 之前的系统使用 Font Provider **需要申请存储权限**。（也可以选择只为 7.0 以上用户开启）
    
> 由于在 API 24 之前，只能通过 path 创建，而中文字体体积较大（如全字重的 Noto Sans CJK 有 100 MB 以上），不适合使用例如 Google 的 Downloadable Fonts 的保存字体文件至应用的私有空间的方法。
    
   
#### 使用

##### （可选）检查可用性

```java
int code = FontProviderClient.checkAvailability(context);
if (code != FontProviderAvailability.OK) {
	// 根据具体的 code 提示用户
}
```
    	
##### 基本使用方式

```java
public abstract class BaseActivity extends Activity {

    /**
    * 同一进程只需要替换一次，所以是 static
    */
    private static boolean sFontInitialized = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        /**
        * 在 Activity 进行替换，可以保证在需要显示界面时才替换字体。
        * 若在 Application 进行，可能会出现仅有 Service / BroadcastReceiver / 
        * ContentProvider 时也进行替换，而浪费宝贵时间。
        */
        if (!sFontInitialized) {
            FontProviderClient client = FontProviderClient.create(this);
            /**
            * 不可用时会返回 null
            */
            if (client != null) {
                /**
                * 设置下次请求会替换默认的回退列表
                * 这样在使用自己提供字体时也能同时使用 Font Provider 的字体
                */
                client.setNextRequestReplaceFallbackFonts(true);
                
                /**
                * 将 "sans-serif" 和 "sans-serif-medium" 替换为 "Noto Sans CJK" 的对应字体
                * 字重将根据名称自动解析。
                * 在 sample 项目中还可以看到如何设定默认字体（如替换 emoji 字体）。
                * 
                * 会返回对应个数的 Typeface，本别是包含全部字体的 Typeface 及由此 Typeface 
                * 创建的有对应字重别名的 Typeface。
                */
                client.replace("Noto Sans CJK",
                        "sans-serif", "sans-serif-medium");
            }
            
            sFontInitialized = true;
        }

        super.onCreate(savedInstanceState);
        
        /**
        * 创建一个来自 asset 的字体 OpenSans-Light.ttf，且通过隐藏 API 指定字重是 100，
        * 这样在需要显示其他语言的字体时也会保证有正确的字重，否则其他语言（在 Android Oreo 之前）
        * 将一律是 400 字重。
        */
        Typeface myTypeface = TypefaceCompat.createWeightAlias(
            Typeface.createFromAsset(assets, "OpenSans-Light.ttf"), 100)；
    }
}
```
