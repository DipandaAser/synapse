# Synapse

Synapse forwards the SMS an Android phone receives to webhooks. Triggers set rules on the sender and the message text, and Synapse calls a trigger's URL when a text matches.

Nothing runs in the background and there is no permanent notification: Android starts Synapse for each incoming SMS through a receiver declared in the manifest. Webhook calls run in WorkManager, which waits for a network and tries again when the server is down or busy.
