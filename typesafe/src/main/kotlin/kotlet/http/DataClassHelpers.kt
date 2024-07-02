package kotlet.http

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

object DataClassHelpers {

    /**
     * If you want to convert a map to a data class, you can use this method. It will create a list of typed arguments
     * from a list of ["Argument name", "String value"] and then create an instance of class T.
     *
     * @param dataKClass The class you want to create
     * @param values A list of values you want to pass to the class constructor
     * @return An instance of class T
     * @throws IllegalArgumentException If the class is not a data class, if the constructor is not found, or if the
     * parameter type is not supported
     *
     *
     * Example of usage:
     *
     * ```
     * data class Person(val name: String, val age: Int)
     *
     * val values = mapOf("name" to "John", "age" to "25")
     * val person = ConvertHelpers.convertMapToDataClass(Person::class, values)
     * println(person) // Person(name=John, age=25)
     * ```
     */
    fun <T : Any> convertMapToDataClass(dataKClass: KClass<T>, values: Map<String, String>): T {
        require(dataKClass.isData) {
            "Only Kotlin data classes are expected. Class $dataKClass isn't Kotlin data class"
        }

        val primaryConstructor = requireNotNull(dataKClass.primaryConstructor) {
            "Couldn't find primary constructor for $dataKClass class"
        }

        // collect all arguments and their values in a map
        // it will be a list of ["Argument name", "String value"]
        val arguments = hashMapOf<KParameter, Any?>()
        primaryConstructor.parameters.forEach { parameter ->
            val value = convertStringToTypedValue(parameter, values)

            if (value == null && parameter.isOptional) {
                // This is a special empty branch for code readability
                // If the parameter is not in the list of values AND this argument has a default value in the data class description
                // then you don't need to put a null value in the constructor arguments, you just need to skip
                // this argument, then Kotlin will substitute the default value specified in the class description
                // NO OPERATION
            } else {
                arguments[parameter] = value
            }
        }

        // invoke the constructor with these arguments
        return primaryConstructor.callBy(arguments)
    }

    private fun convertStringToTypedValue(parameter: KParameter, values: Map<String, String>): Any? {
        val value = values[parameter.name] ?: return null

        val paramKClass = parameter.type.classifier as KClass<*>

        // Now we support only the most basic types, but there is no reason why in the future we might not want
        // something strange, for example, lists or maps (but it's better not to complicate things)
        return when {
            // strings
            isString(paramKClass) -> value
            // numbers
            isInt(paramKClass) -> value.toInt()
            isLong(paramKClass) -> value.toLong()
            isByte(paramKClass) -> value.toByte()
            isShort(paramKClass) -> value.toShort()
            // booleans
            isBoolean(paramKClass) -> value.toBooleanStrict()
            // float numbers
            isDouble(paramKClass) -> value.toDouble()
            isFloat(paramKClass) -> value.toFloat()
            // chars
            isChar(paramKClass) -> {
                require(value.length == 1) {
                    "Can't convert string $value to char – it has more than 1 character"
                }
                value[0]
            }
            // enums
            isEnum(paramKClass) -> {
                @Suppress("UNCHECKED_CAST")
                java.lang.Enum.valueOf(paramKClass.javaObjectType as Class<out Enum<*>>, value)
            }
            // default branch
            else -> {
                throw IllegalArgumentException("Unsupported type ${parameter.type} of $parameter parameter")
            }
        }
    }

    private fun isString(type: KClass<*>) = (type == String::class)
    private fun isInt(type: KClass<*>) = (type == Int::class)
    private fun isLong(type: KClass<*>) = (type == Long::class)
    private fun isShort(type: KClass<*>) = (type == Short::class)
    private fun isByte(type: KClass<*>) = (type == Byte::class)
    private fun isBoolean(type: KClass<*>) = (type == Boolean::class)
    private fun isDouble(type: KClass<*>) = (type == Double::class)
    private fun isFloat(type: KClass<*>) = (type == Float::class)
    private fun isChar(type: KClass<*>) = (type == Char::class)
    private fun isEnum(type: KClass<*>) = (type.isSubclassOf(Enum::class))

}
