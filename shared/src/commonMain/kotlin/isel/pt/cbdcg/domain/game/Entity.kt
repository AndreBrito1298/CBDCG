package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.dto.EntityDTO

interface Entity {
    fun Entity.toEntityDTO(): EntityDTO
    fun <T: Entity>toEntity(): Entity
}
