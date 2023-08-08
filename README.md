[![Build](https://github.com/DarkAtra/v-rising-discord-bot/actions/workflows/build.yml/badge.svg)](https://github.com/DarkAtra/v-rising-discord-bot/actions/workflows/build.yml)

# V Rising Discord Bot

This bot allows you to display some information about your v rising server on discord.

<img alt="Preview" src="./docs/preview.png" width="400"/>

## Support

If you have questions or need support, feel free to join [this discord server](https://discord.gg/KcMcYKa6Nt).

## Discord Commands

Please note that all commands are [guild](https://discord.com/developers/docs/resources/guild) specific.

### `/list-servers`

Lists all server status monitors.

### `/add-server`

Adds a server to the status monitor.

| Parameter                         | Description                                                                                                                                                                                                                                                           | Required | Default value |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| `server-hostname`                 | The hostname of the server to add a status monitor for.                                                                                                                                                                                                               | `true`   | `null`        |
| `server-query-port`               | The query port of the server to add a status monitor for.                                                                                                                                                                                                             | `true`   | `null`        |
| `server-api-hostname`             | The hostname to use when querying the server's api. Only required if you're planing to use the [v-rising-discord-bot-companion integration](#v-rising-discord-bot-companion-integration).                                                                             | `false`  | `null`        |
| `server-api-port`                 | The api port of the server. Only required if you're planing to use the [v-rising-discord-bot-companion integration](#v-rising-discord-bot-companion-integration).                                                                                                     | `false`  | `null`        |
| `display-server-description`      | Whether or not to display the v rising server description on discord.                                                                                                                                                                                                 | `false`  | `true`        |
| `display-player-gear-level`       | Whether or not to display each player's gear level. Only has an effect if `server-api-hostname` and `server-api-port` are set.                                                                                                                                        | `false`  | `true`        |
| `player-activity-feed-channel-id` | The id of the channel to post the player activity feed in. Only has an effect if `server-api-hostname` and `server-api-port` are set.                                                                                                                                 | `false`  | `null`        |
| `pvp-kill-feed-channel-id`        | The id of the channel to post the pvp kill feed in. Only has an effect if `server-api-hostname` and `server-api-port` are set. Requires at least version `0.4.0` of the [v-rising-discord-bot-companion](https://github.com/DarkAtra/v-rising-discord-bot-companion). | `false`  | `null`        |

### `/update-server`

Updates the given server status monitor. Only parameters specified are updated, all other parameters remain unchanged.

| Parameter                         | Description                                                                                                                                                                                                                                                           | Required | Default value |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| `server-status-monitor-id`        | The id of the server status monitor to update.                                                                                                                                                                                                                        | `true`   | `null`        |
| `server-hostname`                 | The hostname of the server to add a status monitor for.                                                                                                                                                                                                               | `false`  | `null`        |
| `server-query-port`               | The query port of the server to add a status monitor for.                                                                                                                                                                                                             | `false`  | `null`        |
| `server-api-hostname`             | The hostname to use when querying the server's api. Only required if you're planing to use the [v-rising-discord-bot-companion integration](#v-rising-discord-bot-companion-integration).                                                                             | `false`  | `null`        |
| `server-api-port`                 | The api port of the server. Only required if you're planing to use the [v-rising-discord-bot-companion integration](#v-rising-discord-bot-companion-integration).                                                                                                     | `false`  | `null`        |
| `status`                          | Determines if a server status monitor should be updated or not. Either `ACTIVE` or `INACTIVE`.                                                                                                                                                                        | `false`  | `null`        |
| `display-server-description`      | Whether or not to display the v rising server description on discord.                                                                                                                                                                                                 | `false`  | `true`        |
| `display-player-gear-level`       | Whether or not to display each player's gear level. Only has an effect if `server-api-hostname` and `server-api-port` are set.                                                                                                                                        | `false`  | `true`        |
| `player-activity-feed-channel-id` | The id of the channel to post the player activity feed in. Only has an effect if `server-api-hostname` and `server-api-port` are set.                                                                                                                                 | `false`  | `null`        |
| `pvp-kill-feed-channel-id`        | The id of the channel to post the pvp kill feed in. Only has an effect if `server-api-hostname` and `server-api-port` are set. Requires at least version `0.4.0` of the [v-rising-discord-bot-companion](https://github.com/DarkAtra/v-rising-discord-bot-companion). | `false`  | `null`        |

### `/remove-server`

Removes a server from the status monitor.

| Parameter                  | Description                                    | Required | Default value |
|----------------------------|------------------------------------------------|----------|---------------|
| `server-status-monitor-id` | The id of the server status monitor to remove. | `true`   | `null`        |

### `/get-server-details`

Gets all the configuration details for the specified server.

| Parameter                  | Description                                             | Required | Default value |
|----------------------------|---------------------------------------------------------|----------|---------------|
| `server-status-monitor-id` | The id of the server status monitor to get details for. | `true`   | `null`        |

## Configuration Properties

| Property                       | Type     | Description                                                                                                                      | Default value          |
|--------------------------------|----------|----------------------------------------------------------------------------------------------------------------------------------|------------------------|
| `bot.discord-bot-token`        | String   | The token for the discord bot. You can find this in the [discord developer portal](https://discord.com/developers/applications). | `null`                 |
| `bot.database-path`            | Path     | The path to the database file. Should be overwritten when running inside a docker container.                                     | `./bot.db`             |
| `bot.database-username`        | String   | The username for the database.                                                                                                   | `v-rising-discord-bot` |
| `bot.database-password`        | String   | The password for the database.                                                                                                   | `null`                 |
| `bot.update-delay`             | Duration | The delay between status monitor updates. At least 30 seconds.                                                                   | `1m`                   |
| `bot.max-failed-attempts`      | Int      | The maximum number of attempts to be made until a server is disabled. Use `0` if you don't want to use this feature.             | `0`                    |
| `bot.max-recent-errors`        | Int      | The maximum number of errors to keep for debugging via `/get-server-details`. Use `0` if you don't want to use this feature.     | `5`                    |
| `bot.max-characters-per-error` | Int      | The maximum number of errors to keep for debugging via `/get-server-details`. Use `0` if you don't want to use this feature.     | `200`                  |

## [v-rising-discord-bot-companion](https://github.com/DarkAtra/v-rising-discord-bot-companion) Integration

<img alt="Companion Preview" src="./docs/companion-preview.png" width="400"/>

The v-rising-discord-bot is able to fetch additional data about players, such as the gear level, if
the [v-rising-discord-bot-companion](https://github.com/DarkAtra/v-rising-discord-bot-companion) is installed on the v rising server and the api port of that
server is accessible from where the bot is running. **I highly recommend to not expose the api port to the internet.**

### Enabling the v-rising-discord-bot-companion integration

1. Update your v-rising-discord-bot to [the latest version](https://github.com/DarkAtra/v-rising-discord-bot/releases).
2. [Install the v-rising-discord-bot-companion](https://github.com/DarkAtra/v-rising-discord-bot-companion#installing-this-bepinex-plugin-on-your-v-rising-server)
   on your V Rising Server.
3. Update the configuration of your v rising server using the /update-server discord command. You should be able to set both the `server-api-hostname` and
   the `server-api-port`. **It is not recommended to expose the api port to the internet.** Ensure that the api port is accessible from where the bot is running
   though.
4. You should now see the additional data the next time the discord embed is updated (once every minute by default).

## How to run it yourself using docker-compose

Find the latest docker image [here](https://github.com/DarkAtra/v-rising-discord-bot/pkgs/container/v-rising-discord-bot). If you prefer to use the JVM based
version of this bot, remove the `-native` suffix from the `image` name in the example below.

```yaml
services:
  v-rising-discord-bot:
    image: ghcr.io/darkatra/v-rising-discord-bot:2.2.0-native
    command: -Dagql.nativeTransport=false
    mem_reservation: 128M
    mem_limit: 256M
    volumes:
      - /opt/v-rising-discord-bot:/data/v-rising-discord-bot
    environment:
      - BOT_DISCORD_BOT_TOKEN=<your-discord-bot-token>
      - BOT_DATABASE_PATH=/data/v-rising-discord-bot/bot.db
      - BOT_DATABASE_PASSWORD=<the-database-password>
    restart: unless-stopped
```

> **Note**
> The container uses user `1000:1000`. Make sure that this user has read and write permissions on the volume, in this
> case `/opt/v-rising-discord-bot`. Also, if you're on windows, please replace `/opt/v-rising-discord-bot` in the example above with any valid window path,
> e.g. `/C/Users/<username>/Desktop/v-rising-discord-bot`.

## How to run it yourself without docker

1. Find the latest jar [here](https://github.com/DarkAtra/v-rising-discord-bot/releases) or build the application yourself using `mvn clean verify`
2. Copy the jar file to any directory of you choice and create a file `application.yml` with the following content in the same directory:
   ```yaml
   bot:
     discord-bot-token: <your-discord-bot-token>
     database-password: <the-database-password>
   ```
3. Run the application using `java -jar v-rising-discord-bot-<version>.jar`
4. Profit

If you run the application in a Linux environment, make sure that you use a separate user. Also, ensure that you're using at least Java 17.
This user only needs read and write permissions for the `bot.db` database file and read permissions for the `application.yml`, both of which are located in the
applications working directory by default.

You can change the location of the database file by modifying the `application.yml` slightly:

```yaml
bot:
  discord-bot-token: <your-discord-bot-token>
  database-password: <the-database-password>
  database-path: /data/bot.db
```

## Native Docker Image (Beta)

The v-rising-discord-bot is also available as a native executable built with GraalVM Native Image. The native executable should behave exactly the same as
the JVM version but requires less memory and is overall faster. It is also possible to use the same database with bot the native executable and the jar version.
Please [file a new issue](https://github.com/DarkAtra/v-rising-discord-bot/issues/new/choose) if you notice any issues.

Find the latest native docker image [here](https://github.com/DarkAtra/v-rising-discord-bot/pkgs/container/v-rising-discord-bot).

## Required Permissions

![Required Discord Permissions](./docs/discord-permissions.png)
