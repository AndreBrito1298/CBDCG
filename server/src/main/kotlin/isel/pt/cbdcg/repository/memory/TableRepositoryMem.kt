package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.repository.Repository
import isel.pt.cbdcg.repository.TableError


object TableRepositoryMem: Repository<Table> {

    /**
     * List of Game Tables registered.
     */
    val tables = mutableListOf<Table>()

    /**
     * Function to create a new Table.
     * @param name The name of the table.
     * @param owner Unique identifier of the user creating the table.
     * @throws TableError.DuplicateName Table names must be unique.
     * @return The Table created.
     */
    fun createTable(name: Name, owner: Int): Table {

        if(tables.any{it.name.string == name.string})
            throw TableError.DuplicateName(name.string)

        val table = Table(tables.size, name, owner,1)
        tables.add(table)

        return table
    }

    /**
     * Function to find a Table given its name.
     * @param name The name of the table.
     * @return The table.
     */
    fun findByName(name: Name): Table {
        return tables.find{ it.name.string == name.string }
            ?: throw TableError.TableDoesNotExist(name.string)
    }

    // Generic Operations

    override fun findById(id: Int): Table? {
        return tables.find{ it.id == id}
    }

    override fun save(element: Table) {
        tables.add(element)
    }

    override fun deleteById(id: Int) {
        tables.removeIf{ it.id == id}
    }

    override fun clear() {
        tables.clear()
    }


}