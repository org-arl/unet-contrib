# Scripts

Place Groovy scripts which you want to access from Unet here.

# Running Scripts
- Any Groovy script placed here (for eg. `myscript.groovy`) can be executed from the shell using the command `<script name>` (for eg. `myscript`).

# Special Scripts
There are a few special scripts that are run by UnetStack at various points of time. These may be edited and deleted as required.

- `setup.groovy` - This is run at boot and can be used to **override** the default Unet Agent setup.
- `startup.groovy` - This is run after `setup.groovy`, and can be used to load any custom Agents and also configure any of the default agents.
- `saved-state.groovy` - Used by the StateManager Agent to load the last saved state (using the `savestate` command) to boot.
