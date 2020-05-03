package com.zqc17.reflecton

import kotlin.reflect.KClass

fun Null(kClazz: KClass<*>): Any = Null(kClazz.javaObjectType)

/**
 * 如果反射调用方法时传 null，需要用下面这个方法给出类型：
 * <example>
 *     ReflectOn(Test::class).newInstance().doReflect {
 *         Fn["setRunnable"](Null(Runnable::class.java))
 *     }
 */
fun Null(clazz: Class<*>): Any = NullType(clazz)

/**
 * 如果反射调用的方法签名使用的是包装类型，需要用下面这个方法包装一下：
 * <example>
 *     class Test {
 *         fun showNumber(number: Int?) { println(number) }
 *     }
 *
 *     ReflectOn(Test::class).newInstance().doReflect {
 *         Fn["showNumber"](NonPrimitive(0))
 *     }
 */
fun NonPrimitive(value: Boolean): Any = NonPrimitiveType(value)

fun NonPrimitive(value: Char): Any = NonPrimitiveType(value)

fun NonPrimitive(value: Byte): Any = NonPrimitiveType(value)

fun NonPrimitive(value: Short): Any = NonPrimitiveType(value)

fun NonPrimitive(value: Int): Any = NonPrimitiveType(value)

fun NonPrimitive(value: Float): Any = NonPrimitiveType(value)

fun NonPrimitive(value: Long): Any = NonPrimitiveType(value)

fun NonPrimitive(value: Double): Any = NonPrimitiveType(value)

private class NullType(val clazz: Class<*>)

private class NonPrimitiveType {
    val value: Any
    val clazz: Class<*>

    constructor(value: Boolean) {
        this.value = value
        clazz = Boolean::class.javaObjectType
    }

    constructor(value: Char) {
        this.value = value
        clazz = Char::class.javaObjectType
    }

    constructor(value: Byte) {
        this.value = value
        clazz = Byte::class.javaObjectType
    }

    constructor(value: Short) {
        this.value = value
        clazz = Short::class.javaObjectType
    }

    constructor(value: Int) {
        this.value = value
        clazz = Int::class.javaObjectType
    }

    constructor(value: Float) {
        this.value = value
        clazz = Float::class.javaObjectType
    }

    constructor(value: Long) {
        this.value = value
        clazz = Long::class.javaObjectType
    }

    constructor(value: Double) {
        this.value = value
        clazz = Double::class.javaObjectType
    }
}

internal fun getArgsClazz(vararg args: Any): Array<Class<*>> {
    return Array(args.size) {
        when (val arg = args[it]) {
            is NullType -> {
                arg.clazz
            }
            is NonPrimitiveType -> {
                arg.clazz
            }
            else -> {
                arg::class.javaPrimitiveType ?: arg::class.javaObjectType
            }
        }
    }
}

internal fun getArgs(vararg args: Any): Array<Any?> {
    return Array(args.size) {
        when (val arg = args[it]) {
            is NullType -> {
                null
            }
            is NonPrimitiveType -> {
                arg.value
            }
            else -> {
                arg
            }
        }
    }
}
