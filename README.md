# V Rising Discord Bot

This bot allows you to display some information about your v rising server on discord.

![Preview](./docs/preview.png)

## Commands

| Command                                             | Description                               |
|-----------------------------------------------------|-------------------------------------------|
| `/add-server <server-hostname> <server-query-port>` | Adds a server to the status monitor.      |
| `/list-servers`                                     | Lists all server status monitors.         |
| `/remove-server <server-status-monitor-id>`         | Removes a server from the status monitor. |

Please note that all commands are [guild](https://discord.com/developers/docs/resources/guild) specific.

## Configuration Properties

| Property                | Type   | Description                                                                                                                      | Default value          |
|-------------------------|--------|----------------------------------------------------------------------------------------------------------------------------------|------------------------|
| `bot.discord-bot-token` | String | The token for the discord bot. You can find this in the [discord developer portal](https://discord.com/developers/applications). | `null`                 |
| `bot.database-path`     | Path   | The path to the database file. Should be overwritten when running inside a docker container.                                     | `./bot.db`             |
| `bot.database-username` | String | The username for the database.                                                                                                   | `v-rising-discord-bot` |
| `bot.database-password` | String | The password for the database.                                                                                                   | `null`                 |

## How to run it yourself using docker-compose

Find the latest docker image [here](https://github.com/DarkAtra/v-rising-discord-bot/pkgs/container/v-rising-discord-bot).

```yaml
services:
  v-rising-discord-bot:
    image: ghcr.io/darkatra/v-rising-discord-bot:1.1.0
    volumes:
      - /opt/v-rising-discord-bot:/data/v-rising-discord-bot
    environment:
      - BOT_DISCORD_BOT_TOKEN=<your-discord-bot-token>
      - BOT_DATABASE_PATH=/data/v-rising-discord-bot/bot.db
      - BOT_DATABASE_PASSWORD=<the-database-password>
    restart: unless-stopped
```

Please note that the container uses the `1000:1000` user. So make sure that this user has read and write permissions on the volume, in this
case `/opt/v-rising-discord-bot`.
