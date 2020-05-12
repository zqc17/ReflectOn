package com.zqc17.reflecton

/**
 * <example>
 *     ReflectOn(Test::class.java)
 *         .handleError { Log.e(it) } // 处理异常
 *         .reflectStatic {
 *             Attr["sStaticField"] = 0 // 反射修改静态属性
 *         }
 *         .bindInstance(testInstance) // 绑定此对象
 *         .doReflect {
 *             Attr["testField"] = 0 // 反射修改实例属性
 *             Fn["testMethod"](0, 1) // 反射调用实例方法
 *         }
 *         .bindInstance(anotherInstance) // 绑定另一个对象
 *         .doReflect {} // 在新对象上执行反射
 *         .newInstance(0, 1) // 创建一个新对象并且自动绑定
 *         .doReflect {} // 在上一步创建并且绑定的对象上执行反射
 */
class ReflectOn {
    private var mErrorHandler: ((Exception) -> Unit)? = null
    private lateinit var mBoundInstance: Any
    private val mReflectContextLazyInitiator: Lazy<ReflectContext>
    private val mReflectContext: ReflectContext
        get() = mReflectContextLazyInitiator.value

    constructor(instance: Any) {
        mReflectContextLazyInitiator = lazy {
            ReflectContext(instance.javaClass)
        }
        bindInstance(instance)
    }

    constructor(className: String) {
        mReflectContextLazyInitiator = lazy {
            ReflectContext(Class.forName(className))
        }
    }

    constructor(clazz: Class<*>) {
        mReflectContextLazyInitiator = lazy {
            ReflectContext(clazz)
        }
    }

    /**
     * 反射创建实例并且绑定该对象
     */
    fun newInstance(vararg args: Any): ReflectOn {
        captureError {
            val instance = mReflectContext.newInstance(*args)
            bindInstance(instance)
        }
        return this
    }

    /**
     * 绑定一个对象用于后续反射
     */
    fun bindInstance(instance: Any): ReflectOn {
        mBoundInstance = instance
        return this
    }

    /**
     * 在绑定对象上执行反射
     */
    fun doReflect(reflectTask: ReflectContext.() -> Unit): ReflectOn {
        captureError {
            mReflectContext.obj = mBoundInstance
            reflectTask.invoke(mReflectContext)
        }
        return this
    }

    /**
     * 反射类的静态属性和方法
     */
    fun reflectStatic(staticTask: ReflectContext.() -> Unit): ReflectOn {
        captureError {
            staticTask.invoke(mReflectContext)
        }
        return this
    }

    /**
     * 处理异常
     */
    fun handleError(errorHandler: ((Exception) -> Unit)?): ReflectOn {
        mErrorHandler = errorHandler
        return this
    }

    private fun captureError(reflectTask: () -> Unit) {
        try {
            reflectTask.invoke()
        } catch (e: Exception) {
            mErrorHandler?.invoke(e) ?: let {
                throw e
            }
        }
    }
}

