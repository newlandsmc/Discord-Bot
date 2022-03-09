# SVDiscord Bot

## Setup
copy the contents of `example.env` to `.env` and fill in the values

Regex config can be found in `regex.json`, roles config
can be found in `roles.json`

Once the bot is up, to have it send the choose role message, type in `>roles` in any channel
It will send the message to the channel that is configured in `roles.json`, and automatically
configure the `rolesmessage` file.

## Commands

| Command        | Descrption                    | Permission       | Usage                                             |
|----------------|-------------------------------|------------------|---------------------------------------------------|
| /ban           | Ban a member permanently      | BAN_MEMBERS      | /ban <user> <reason>                              |
| /tempban       | Temporarily ban a member      | BAN_MEMBERS      | /tempban <user> <reason> [days] [hours] [minutes] |
| /mute /timeout | Mute a user                   | MODERATE_MEMBERS | /mute <user> <reason> [days] [hours] [minutes]    |
| /kick          | Kick a user                   | KICK_MEMBERS     | /kick <user> <reason>                             |
| /purge /prune  | Remove previous X messages    | MANAGE_SERVER    | /purge <number of messages>                       |
| /reload        | Reload all modules of the bot | ADMINISTRATOR    | /reload                                           |
