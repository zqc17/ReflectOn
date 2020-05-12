package com.zqc17.reflecton

import java.lang.reflect.*

class ReflectContext(private val mClazz: Class<*>) {
    private val mConstructorCache: MutableMap<Array<Class<*>>, Constructor<*>> = mutableMapOf()
    private val mMethodCache: MutableMap<Pair<String, Array<Class<*>>>, Method> = mutableMapOf()
    private val mFieldCache: MutableMap<String, Field> = mutableMapOf()
    var obj: Any? = null
    val Attr: AttrGetSetWrapper = AttrGetSetWrapper()
    val Fn: FnGetWrapper = FnGetWrapper()

    /**
     * 反射创建实例
     */
    fun newInstance(vararg args: Any): Any {
        val argsClazz = getArgsClazz(*args)
        val constructor = getReflectConstructor(argsClazz)
        return constructor.newInstance(*getArgs(*args))
    }

    /**
     * 反射调用方法
     */
    private fun callMethod(name: String, vararg args: Any): Any? {
        val argsClazz = getArgsClazz(*args)
        val method = getReflectMethod(name, argsClazz)
        return method.invoke(obj, *getArgs(*args))
    }

    /**
     * 反射获取属性
     */
    private fun getField(name: String): Any? {
        val field = getReflectField(name)
        return field.get(obj)
    }

    /**
     * 反射设置属性
     */
    private fun setField(name: String, value: Any?) {
        val field = getReflectField(name)
        field.set(obj, value)
    }

    /**
     * 反射创建代理实例的语法设计
     *     interface ITest {
     *         fun test(name: String): Int
     *         fun getSelf(): ITest
     *     }
     *     ReflectOn("com.private.interface.ITest")
     *         .newProxyInstance {
     *             Fn["test"][String::class.java] { proxy, args -> 0 }
     *             Fn["getSelf"] { proxy, args -> proxy }
     *         }
     */
    fun newProxyInstance(builder: ProxyContext.() -> Unit): Any {
        val proxyContext = ProxyContext(mClazz, this::getReflectMethod)
        builder.invoke(proxyContext)
        return proxyContext.newProxyInstance()
    }

    // 反射获取方法并缓存
    private fun getReflectMethod(name: String, argsClazz: Array<Class<*>>): Method {
        return mMethodCache[name to argsClazz] ?: let {
            val method = mClazz.getDeclaredMethod(name, *argsClazz)
            method.isAccessible = true
            mMethodCache[name to argsClazz] = method
            method
        }
    }

    // 反射获取属性并缓存
    private fun getReflectField(name: String): Field {
        return mFieldCache[name] ?: let {
            val field = mClazz.getDeclaredField(name)
            field.isAccessible = true
            mFieldCache[name] = field // cache field
            field
        }
    }

    // 反射获取构造函数并缓存
    private fun getReflectConstructor(argsClazz: Array<Class<*>>): Constructor<*> {
        return mConstructorCache[argsClazz] ?: let {
            val constructor = mClazz.getDeclaredConstructor(*argsClazz)
            constructor.isAccessible = true
            mConstructorCache[argsClazz] = constructor
            constructor
        }
    }

    /**
     * 通过下面的语法获取一个"方法"
     * Fn["substring"]
     */
    inner class FnGetWrapper {
        operator fun get(name: String): FnInvokeWrapper {
            return FnInvokeWrapper(name)
        }
    }

    /**
     * 通过下面的语法反射调用方法
     * Fn["substring"](0, 1)
     */
    inner class FnInvokeWrapper(private val mName: String) {
        operator fun invoke(vararg args: Any): Any? {
            return callMethod(mName, *args)
        }
    }

    /**
     * 通过下面的语法反射修改和获取属性
     * Attr["mPrivateField"] = 1
     * val mPrivateField = Attr["mPrivateField"]
     */
    inner class AttrGetSetWrapper {
        operator fun get(name: String): Any? {
            return getField(name)
        }

        operator fun set(name: String, value: Any?) {
            setField(name, value)
        }
    }
}
