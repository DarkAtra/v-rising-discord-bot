# Using the bot as a service

I decided to provide the bot as a free service going forward. Feel free
to [invite the bot to your discord server](https://discord.com/oauth2/authorize?client_id=982682186207592470) and use it as you please.
Please [reach out](https://discord.gg/KcMcYKa6Nt) if something doesn't work for you.

> [!NOTE]
> This service is provided to the best of my ability. I can not guarantee that the bot is available 24/7.
> I also reserve the right to prohibit you from using the bot at any time if there is any suspicion of misuse.

## Setup Guide

1. [Invite the bot to your discord server](https://discord.com/oauth2/authorize?client_id=982682186207592470)
2. Create a new channel that is dedicated for the status embed.
3. Restrict the permissions for the new channel so that members of your server can read messages but can not post anything in there.
   This includes the following permissions:
    * `View Channel`
    * `Read Message History`
4. Ensure that `Jarvis` is allowed to read and write to the channel. This includes the following permissions:
    * `View Channel`
    * `Send Messages`
    * `Embed Links`
    * `Manage Messages`
    * `Read Message History`
5. Find the `IP Address` and `Query Port` of your V Rising server. [Battlemetrics](https://www.battlemetrics.com/servers/vrising) might come in handy here.
6. Navigate to the channel that you created in step 2 and use the `/add-server` command. Discord will ask you for a `server-hostname` and `server-query-port`.
   This is where you enter the `IP Address` and `Query Port` that you determined in the previous step.
7. `Jarvis` will respond with a message telling you that you've successfully added your first status monitor for your V Rising server.
8. Now you only need to wait a minute for the status display to appear.

> [!NOTE]
> If no status embed appears after 2 minutes, use the `/get-server-details` command with the id that `Jarvis` gave you in step 7.
> This will give you a detailed error message of what went wrong. In most cases, it simply means that the `IP Address` or `Query Port` are incorrect.
> You can always change them using the `/update-server` command. Feel free to check [the commands documentation](./commands.md) for details.

## Support

If you have questions or need support, feel free to join [this discord server](https://discord.gg/KcMcYKa6Nt).
