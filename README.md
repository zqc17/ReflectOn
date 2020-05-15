# 关于

一个用于温习Java反射和Kotlin语法的玩具工程。

# 用法

```kotlin
ReflectOn(String::class.java)
    .newInstance("abc") // 反射创建实例
    .doReflect {
        val substring = Fn["substring"](0, 1) // 反射调用方法
        println(substring) // "a"
    }
```