# IconChanger
Automatic Discord Server icon changer for when a Twitch Stream goes live and offline

[Add IconChanger to your discord server!](https://discord.com/api/oauth2/authorize?client_id=1072033783500517437&permissions=2080&scope=bot%20applications.commands)

# Setup
use `/setup <channel> <live-icon>` to set up icon changing for when that twitch channel goes live

you can also use `/setup <channel> <live-icon> <offline-icon>` if you want to add an offline icon. Without the `<offline-icon>` argument, 
your current discord server icon will be treated as the offline icon.

# Status
use `/status` to check what twitch channel your discord server is linked to.

# Reset
use `/reset` to reset your server. this will unlink the twitch channel associated with your discord server.
