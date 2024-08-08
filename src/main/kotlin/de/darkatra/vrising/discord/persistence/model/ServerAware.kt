package de.darkatra.vrising.discord.persistence.model

interface ServerAware {
    fun getServer(): Server
    fun setServer(server: Server)
}
