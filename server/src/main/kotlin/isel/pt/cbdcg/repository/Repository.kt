package isel.pt.cbdcg.repository

/**
 * Generic Interface for a Repository.
 */
interface Repository<T> {

    /**
     *  Function that finds a specific element, given its id.
     *  When found, returns the element, otherwise returns null.
     */
    suspend fun findById(id: UInt): T?

    /**
     * Function that updates an existing element.
     */
    suspend fun save(element: T)

    /**
     * Function that deletes an element, given its id.
     */
    suspend fun deleteById(id: UInt)

    /**
     * Function that deletes every element.
     */
    suspend fun clear()
}