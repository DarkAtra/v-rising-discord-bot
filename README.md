# V Rising Discord Bot

This bot allows you to display some information about your v rising server on discord.

![Preview](./docs/preview.png)

## Commands

| Command                                                                                                       | Description                               |
|---------------------------------------------------------------------------------------------------------------|-------------------------------------------|
| `/list-servers`                                                                                               | Lists all server status monitors.         |
| `/add-server <server-hostname> <server-query-port> <display-player-gear-level>`                               | Adds a server to the status monitor.      |
| `/update-server <server-status-monitor-id> <server-hostname> <server-query-port> <display-player-gear-level>` | Updates the given server status monitor.  |
| `/remove-server <server-status-monitor-id>`                                                                   | Removes a server from the status monitor. |

Please note that all commands are [guild](https://discord.com/developers/docs/resources/guild) specific.

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
    image: ghcr.io/darkatra/v-rising-discord-bot:1.4.0
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

1. Build the application using `mvn clean verify`
2. Check the `target` folder and find a file called `v-rising-discord-bot:<version>.jar`
3. Copy that file to any directory of you choice and create a file `application.yml` with the following content in the same directory:
   ```yaml
   bot:
     discord-bot-token: <your-discord-bot-token>
     database-password: <the-database-password>
   ```
4. Run the application using `java -jar v-rising-discord-bot:<version>.jar`
5. Profit

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
