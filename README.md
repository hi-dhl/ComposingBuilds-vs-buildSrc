![](http://cdn.51git.cn/2020-05-30-15908387076245.jpg)

## 前言

长期以来困扰我们的一个问题就是构建速度，AndroidStudio 的构建速度严重影响 Android 开发者的工作效率，尤其是更新一个版本号，导致整个项目重新构建，在网络慢的情况下，这是无法忍受的。

buildSrc 这种方式，在最近几年是非常流行的，因为它有以下优点：

* 共享 buildSrc 库工件的引用，全局只有一个地方可以修改它
* 支持 AndroidStudio 自动补全，如果图片无法查看，请点击[查看自动补全](http://cdn.51git.cn/2020-05-30-自动补全.gif)

![自动补全](http://cdn.51git.cn/2020-05-30-自动补全.gif)

* 支持 AndroidStudio 单击跳转，如果图片无法查看，请点击[查看单击跳转](http://cdn.51git.cn/2020-05-30-单击跳转.gif)

![单击跳转](http://cdn.51git.cn/2020-05-30-单击跳转.gif)

又优点的同时也有缺点，来看一下 [Gradle 文档](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#sec:build_sources)

> A change in buildSrc causes the whole project to become out-of-date. Thus, when making small incremental changes, the --no-rebuild command-line option is often helpful to get faster feedback. Remember to run a full build regularly or at least when you’re done, though.
> 
> buildSrc的更改会导致整个项目过时，因此，在进行小的增量更改时，-- --no-rebuild命令行选项通常有助于获得更快的反馈。不过，请记住要定期或至少在完成后运行完整版本。

汇总一句话就是说，buildSrc 依赖更新将重新构建整个项目，那么有没有一种方法支持自动补全和单击跳转，有不用重新构建整个项目，Composing builds 就可以实现，接下来我们来演示一下 buildSrc 和 Composing builds 它们的 build 的时间，相关代码我已经上传到 GitHub 了

[地址：https://github.com/hi-dhl/ComposingBuilds-vs-buildSrc](https://github.com/hi-dhl/ComposingBuilds-vs-buildSrc)

**通过这篇文章你将学习到以下内容，将在文末总结部分会给出相应的答案**

* 什么是 buildSrc？
* 什么是 Composing builds？
* 如何使用 Composing builds 和 buildSrc
* buildSrc 和 Composing builds 优势劣势对比？
* Composing builds 编译速度怎么样？
* buildSrc 如何迁移到 Composing builds？
* 管理 Gradle 依赖都有那几种方式？以及效率怎么样？

这篇文章涉及很多重要的知识点，请耐心读下去，我相信应该会给大家带来很多不一样的东西。

## Composing builds 和 buildSrc 对比

接下来我们来演示一下 buildSrc 和 Composing builds 它们的优势劣势对比，在分析之前，先来了解一下基本概念

### 什么是 buildSrc

摘自 [Gradle 文档](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#sec:build_sources)：当运行 Gradle 时会检查项目中是否存在一个名为 buildSrc 的目录。然后 Gradle 会自动编译并测试这段代码，并将其放入构建脚本的类路径中, 对于多项目构建，只能有一个 buildSrc 目录，该目录必须位于根项目目录中, buildSrc 是 Gradle 项目根目录下的一个目录，它可以包含我们的构建逻辑，与脚本插件相比，buildSrc 应该是首选，因为它更易于维护、重构和测试代码

### 什么是 Composing builds

摘自 [Gradle 文档](https://docs.gradle.org/current/userguide/composite_builds.html) 复合构建只是包含其他构建的构建. 在许多方面，复合构建类似于 Gradle 多项目构建，不同之处在于，它包括完整的 builds ，而不是包含单个 projects

* 组合通常独立开发的构建，例如，在应用程序使用的库中尝试错误修复时
* 将大型的多项目构建分解为更小，更孤立的块，可以根据需要独立或一起工作

### buildSrc vs Composing builds 

为了正确对比这两种方式，新建了两个空的项目分别是 Project-buildSrc 和 Project-ComposingBuild，这两个项目引用的依赖都是一样的，Project-buildSrc 包含 buildSrc，Project-ComposingBuild 包含 Composing builds。

![Composing-builds-vs-buildSr](http://cdn.51git.cn/2020-05-30-Composing-builds-vs-buildSrc2.png)

Project-buildSrc 和 Project-ComposingBuild 它们的结构都差不多的，接下来我们来看一下，编译速度 和 使用有什么不同。

#### 编译速度

Project-buildSrc 和 Project-ComposingBuild 这两个项目，它们的 androidx.appcompat:appcompat 的版本是 1.0.2，现在我们从 1.0.2 升级到 1.1.0 来看一下它们 Build 的时间。

* **Project-buildSrc**：修改了版本号 1.0.2 -> 1.1.0 重新 Build 用时 37s

![Project-buildSrc](http://cdn.51git.cn/2020-05-30-WX20200529-021920@2x.png)

* **Project-ComposingBuild**：修改了版本号 1.0.2 -> 1.1.0 重新 Build 用时 8s

![Project-ComposingBuild](http://cdn.51git.cn/2020-05-30-WX20200529-022301@2x.png)

当修改了版本号，Project-buildSrc 项目 Build 的时间几乎是 Project-ComposingBuild  项目的 4.6 倍（ PS: 每个人的环境不同，时间上会有差异，但是 Project-buildSrc 的时间总是大于 Project-ComposingBuild ）

在更大的项目中，网络慢的情况下，这种差异会更加明显，几分钟的构建都是常事，在 buildSrc 中做微小的更改，可能需要花很长时间构建，等待团队其他成员在他们提取更改之后，都将导致项目重新构建，这个代价是非常昂贵的。

#### 它们在使用上有什么不同呢

**Project-buildSrc**

* 在项目根目录下新建一个名为 buildSrc 的文件夹( 名字必须是 buildSrc，因为运行 Gradle 时会检查项目中是否存在一个名为 buildSrc 的目录 )
* 在 buildSrc 文件夹里创建名为 build.gradle.kts 的文件，添加以下内容

```
plugins {
    `kotlin-dsl`
}
repositories{
    jcenter()
}
```

* 在 `buildSrc/src/main/java/包名/` 目录下新建 Deps.kt 文件，添加以下内容

```
object Versions {
    ......
     
    val appcompat = "1.1.0"

    ......
}

object Deps {
    ......
   
    val appcompat =  "androidx.appcompat:appcompat:${Versions.appcompat}"
    
    ......
}
```
 
* 重启你的 Android Studio，项目里就会多出一个名为 buildSrc 的 module，实现上面演示的效果

**Project-ComposingBuild**

* 新建的 module 名称 versionPlugin
* 在 versionPlugin 文件夹下的 build.gradle 文件内，添加以下内容

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        // 因为使用的 Kotlin 需要需要添加 Kotlin 插件
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72"
    }
}

apply plugin: 'kotlin'
apply plugin: 'java-gradle-plugin'
repositories {
    // 需要添加 jcenter 否则会提示找不到 gradlePlugin
    jcenter()
}

gradlePlugin {
    plugins {
        version {
            // 在 app 模块需要通过 id 引用这个插件
            id = 'com.hi.dhl.plugin'
            // 实现这个插件的类的路径
            implementationClass = 'com.hi.dhl.plugin.Deps'
        }
    }
}
```

* 在 `versionPlugin/src/main/java/包名/` 目录下新建 Deps.kt 文件，添加以下内容

```
class Deps : Plugin<Project> {
    override fun apply(project: Project) {
    }

    companion object {
        val appcompat = "androidx.appcompat:appcompat:1.1.0"
    }
}
```

* 在 settings.gradle 文件内添加 `includeBuild 'versionPlugin'` 重启你的 Android Studio
* 在 app 模块 build.gradle 文件内添加以下内容，就可以实现上面演示的效果

```
plugins{
    // 这个 id 就是在 versionPlugin 文件夹下 build.gradle 文件内定义的 id
    id "com.hi.dhl.plugin"
}
```

Project-ComposingBuild 比 Project-buildSrc 多了两步操作需要在 settings.gradle 和 build.gradle 引入插件，但是和编译速度比起来，可以忽略不计

## 总结

总共从以下几个方面对比了 Composing builds 和 buildSrc

* 目录结构：它们的基本目录结构是相同的，可以根据自己的项目进行不同的扩展
* 编译速度：当修改了版本号，Project-buildSrc 项目 Build 的时间几乎是 Project-ComposingBuild  项目的 4.6 倍（ PS: 每个人的环境不同，时间上会有差异，但是 Project-buildSrc 的时间总是大于 Project-ComposingBuild ）
* 使用的区别：Composing builds 比 buildSrc 多了两步操作需要在 settings.gradle 和 build.gradle 引入插件

Project-buildSrc 和 Project-ComposingBuild 相关代码已经上传到 GitHub 了

[地址：https://github.com/hi-dhl/ComposingBuilds-vs-buildSrc](https://github.com/hi-dhl/ComposingBuilds-vs-buildSrc)

**到目前为止大概管理 Gradle 依赖提供了 4 种不同方法：**

* 手动管理 ：在每个 module 中定义插件依赖库，每次升级依赖库时都需要手动更改（不建议使用）
* 使用 ext 的方式管理插件依赖库 ：这是 Google 推荐管理依赖的方法 [Android官方文档](https://developer.android.com/studio/build/gradle-tips#configure-project-wide-properties)
* Kotlin + buildSrc：自动补全和单击跳转，依赖更新时 **将重新** 构建整个项目
* Composing builds：自动补全和单击跳转，依赖更新时 **不会重新** 构建整个项目

**buildSrc 如何迁移到 Composing builds？**

如果当前项目使用的是 buildSrc 方式，迁移到 Composing builds 很简单，需要将 buildSrc 内容拷贝的 Composing builds 中，然后删掉 buildSrc 文件夹就可以即可

## 参考文献

* [Organizing Gradle Projects](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#sec:build_sources)
* [Composing builds](https://docs.gradle.org/current/userguide/composite_builds.html)
* [Android官方文档，使用 ext 的方式管理插件依赖库](https://developer.android.com/studio/build/gradle-tips#configure-project-wide-properties)

## 结语

致力于分享一系列 Android 系统源码、逆向分析、算法、翻译、Jetpack  源码相关的文章，可以关注我，如果你喜欢这篇文章欢迎 star 一起来学习，期待与你一起成长

### 文章列表

#### Android 10 源码系列

* [0xA01 Android 10 源码分析：APK 是如何生成的](https://juejin.im/post/5e4366c3f265da57397e1189)
* [0xA02 Android 10 源码分析：APK 的安装流程](https://juejin.im/post/5e5a1e6a6fb9a07cb427d8cd)
* [0xA03 Android 10 源码分析：APK 加载流程之资源加载](https://juejin.im/post/5e6c8c14f265da574b792a1a)
* [0xA04 Android 10 源码分析：APK 加载流程之资源加载（二）](https://juejin.im/post/5e7f0f2c51882573c4676bc7)
* [0xA05 Android 10 源码分析：Dialog 加载绘制流程以及在 Kotlin、DataBinding 中的使用](https://juejin.im/post/5e9199db6fb9a03c7916f635)
* [0xA06 Android 10 源码分析：WindowManager 视图绑定以及体系结构](https://juejin.im/post/5ead0b865188256d545fd2f8)

#### Android 应用系列

* [如何高效获取视频截图](https://juejin.im/post/5d11d8835188251c10631ffd)
* [如何在项目中封装 Kotlin + Android Databinding](https://juejin.im/post/5e9c434a51882573663f6cc6)
* [[译][Google工程师] 刚刚发布了 Fragment 的新特性 “Fragment 间传递数据的新方式” 以及源码分析](https://juejin.im/post/5eb58da05188256d6d6bb248) 
* [[译][2.4K Start] 放弃 Dagger 拥抱 Koin](https://juejin.im/post/5ebc1eb8e51d454dcf45744e?utm_source=gold_browser_extension)
* [[译][5k+] Kotlin 的性能优化那些事](https://juejin.im/post/5ec0f3afe51d454db11f8a94#heading-7)
* [[译][Google工程师] 详解 FragmentFactory 如何优雅使用 Koin 以及源码分析](https://juejin.im/post/5ecc10626fb9a047e25d5aac)
* [[译] 解密 RxJava 的异常处理机制](https://juejin.im/post/5ecc10626fb9a047e25d5aac)

#### 工具系列

* [为数不多的人知道的 AndroidStudio 快捷键(一)](https://juejin.im/post/5df4933e518825126e639d62)
* [为数不多的人知道的 AndroidStudio 快捷键(二)](https://juejin.im/post/5df986d66fb9a016613903da)
* [关于 adb 命令你所需要知道的](https://juejin.im/post/5d57cfff51882505a87a8526)
* [10分钟入门 Shell 脚本编程](https://juejin.im/post/5a6378055188253dc332130a)

#### 逆向系列

* [基于 Smali 文件 Android Studio 动态调试 APP](https://juejin.im/post/5c8ce8b76fb9a049e30900bf)
* [解决在 Android Studio 3.2 找不到 Android Device Monitor 工具](https://juejin.im/post/5c556ff7f265da2dbe02ba3c)
