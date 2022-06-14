# V Rising Discord Bot

This bot allows you to display some information about your v rising server on discord.

![Preview](./docs/preview.png)

## Discord Commands

Please note that all commands are [guild](https://discord.com/developers/docs/resources/guild) specific.

### `/list-servers`

Lists all server status monitors.

### `/add-server`

Adds a server to the status monitor.

| Parameter                    | Description                                                                                                              | Required |
|------------------------------|--------------------------------------------------------------------------------------------------------------------------|----------|
| `server-hostname`            | The hostname of the server to add a status monitor for.                                                                  | `true`   |
| `server-query-port`          | The query port of the server to add a status monitor for.                                                                | `true`   |
| `display-player-gear-level`  | Whether or not to display the gear level in the player list. Defaults to not displaying the player gear level.           | `false`  |
| `display-server-description` | Whether or not to display the v rising server description on discord. Defaults to not displaying the server description. | `false`  |

### `/update-server`

Updates the given server status monitor. Only the parameters that were specified when the command was executed are updated. All other parameters remain
untouched.

| Parameter                    | Description                                                           | Required |
|------------------------------|-----------------------------------------------------------------------|----------|
| `server-status-monitor-id`   | The id of the server status monitor.                                  | `true`   |
| `server-hostname`            | The hostname of the server to add a status monitor for.               | `false`  |
| `server-query-port`          | The query port of the server to add a status monitor for.             | `false`  |
| `status`                     | The status of the server status monitor. Either ACTIVE or INACTIVE.   | `false`  |
| `display-player-gear-level`  | Whether or not to display the gear level in the player list.          | `false`  |
| `display-server-description` | Whether or not to display the v rising server description on discord. | `false`  |

### `/remove-server`

Removes a server from the status monitor.

| Parameter                    | Description                                                           | Required |
|------------------------------|-----------------------------------------------------------------------|----------|
| `server-status-monitor-id`   | The id of the server status monitor.                                  | `true`   |

## Configuration Properties

| Property                        | Type    | Description                                                                                                                      | Default value          |
|---------------------------------|---------|----------------------------------------------------------------------------------------------------------------------------------|------------------------|
| `bot.discord-bot-token`         | String  | The token for the discord bot. You can find this in the [discord developer portal](https://discord.com/developers/applications). | `null`                 |
| `bot.database-path`             | Path    | The path to the database file. Should be overwritten when running inside a docker container.                                     | `./bot.db`             |
| `bot.database-username`         | String  | The username for the database.                                                                                                   | `v-rising-discord-bot` |
| `bot.database-password`         | String  | The password for the database.                                                                                                   | `null`                 |

## How to run it yourself using docker-compose

Find the latest docker image [here](https://github.com/DarkAtra/v-rising-discord-bot/pkgs/container/v-rising-discord-bot).

```yaml
services:
  v-rising-discord-bot:
    image: ghcr.io/darkatra/v-rising-discord-bot:1.5.0
    volumes:
      - /opt/v-rising-discord-bot:/data/v-rising-discord-bot
    environment:
      - BOT_DISCORD_BOT_TOKEN=<your-discord-bot-token>
      - BOT_DATABASE_PATH=/data/v-rising-discord-bot/bot.db
      - BOT_DATABASE_PASSWORD=<the-database-password>
    restart: unless-stopped
```

Please note that the container uses user `1000:1000`. Make sure that this user has read and write permissions on the volume, in this
case `/opt/v-rising-discord-bot`.

## How to run it yourself without docker

1. Find the latest jar [here](https://github.com/DarkAtra/v-rising-discord-bot/releases) or build the application yourself using `mvn clean verify`
2. Copy the jar file to any directory of you choice and create a file `application.yml` with the following content in the same directory:
   ```yaml
   bot:
     discord-bot-token: <your-discord-bot-token>
     database-password: <the-database-password>
   ```
3. Run the application using `java -jar v-rising-discord-bot:<version>.jar`
4. Profit

If you run the application in a Linux environment, make sure that you use a separate user.
This user only needs read and write permissions for the `bot.db` database file and read permissions for the `application.yml`, both of which are located in the
applications working directory by default.

You can change the location of the database file by modifying the `application.yml` slightly:

```yaml
bot:
  discord-bot-token: <your-discord-bot-token>
  database-password: <the-database-password>
  database-path: /data/bot.db
```
