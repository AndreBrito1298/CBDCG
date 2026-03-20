package isel.pt.cbdcg

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform