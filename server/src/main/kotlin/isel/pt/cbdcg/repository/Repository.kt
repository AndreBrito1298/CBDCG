package isel.pt.cbdcg.repository

/**
 * Generic Interface for a Repository.
 */
interface Repository<T> {

    /**
     *  Function that finds a specific element, given its id.
     *  When found, returns the element, otherwise returns null.
     */
    fun findById(id: Int): T?

    /**
     * Function that updates an existing element.
     */
    fun save(element: T)

    /**
     * Function that deletes an element, given its id.
     */
    fun deleteById(id: Int)

    /**
     * Function that deletes every element.
     */
    fun clear()
}