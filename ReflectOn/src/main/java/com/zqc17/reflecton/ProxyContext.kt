package com.zqc17.reflecton

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class ProxyContext(
    private val mClazz: Class<*>,
    private val mMethodFinder: (String, Array<Class<*>>) -> Method
) {
    val Fn: InterfaceFnNameWrapper = InterfaceFnNameWrapper()
    private val mMethodImplMap: MutableMap<Method, (Any, Array<Any?>) -> Any?> = mutableMapOf()

    fun newProxyInstance(): Any {
        val handler =
            InvocationHandler { proxy, method, args -> mMethodImplMap[method]?.invoke(proxy, args) }
        return Proxy.newProxyInstance(mClazz.classLoader, arrayOf(mClazz), handler)
    }

    inner class InterfaceFnImplWrapper(
        private val mName: String,
        private val mArgsClazz: Array<Class<*>>
    ) {
        operator fun invoke(impl: (Any, Array<Any?>) -> Any?) {
            val method = mMethodFinder.invoke(mName, mArgsClazz)
            mMethodImplMap[method] = impl
        }
    }

    inner class InterfaceFnArgsClazzWrapper(private val mName: String) {
        operator fun get(vararg argsClazz: Class<*>): InterfaceFnImplWrapper {
            return InterfaceFnImplWrapper(mName, argsClazz as Array<Class<*>>)
        }

        operator fun invoke(impl: (Any, Array<Any?>) -> Any?) {
            get(*arrayOf()).invoke(impl)
        }
    }

    inner class InterfaceFnNameWrapper {
        operator fun get(name: String): InterfaceFnArgsClazzWrapper {
            return InterfaceFnArgsClazzWrapper(name)
        }
    }
}