package kottage.core

/**
 * @author Michael Vaughan
 */
class Body(val data: ByteArray) {

    fun asString(): String {
        return String(data);
    }

}