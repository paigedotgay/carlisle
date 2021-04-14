# Carlisle

This is something I've been trying to get working for a while, and am finally willing to take another stab at.

A while back I tried making a version of my Discord Bot, [Hallita](https://github.com/qanazoga/hallita) in Kotlin, and writing commands was extremely easy, and fun! But I had issues making a working Eval command (it eventually halfway worked, but required some weird hacky things and was generally a pain to use), so the project was scrapped and done in JavaScript instead.

I'm learning Clojure right now, and got a bot to work with Clojure and [JDA](https://github.com/DV8FromTheWorld/JDA) some time ago, however, that was just a test to see if I could Eval Clojure inside JDA. I want a full bot.

I want to keep the easy to write commands of Kotlin, but do the base of the bot in Clojure so I can still do nice and clean Eval commands. Thanks JVM!

---

### Planned Features
Ideally, Carlisle should be able to do everything Hallita could, and hopefully some of the things only [Soturi](https://github.com/qanazoga/soturi) could (and also some of Hallita's planned things I never finished).
I'm adding all of these things to the [project issues](https://github.com/qanazoga/carlisle-bot/issues) so I have an actual sense of progress as I work through them.

#### Additional Things (Future Projects)
- Hallita had a planned feature that would allow per-guild setup thru Discord's OAuth, that's still a cool idea that I really wanna do.
- Bring back Soturi's [role signup](https://github.com/qanazoga/soturi/blob/master/cogs/role_signup_listener.py) feature, but with an easy way to set up that doesn't involve me manually adding entries to an unreadable JSON file.
- Bring back Soturi's [message filtering](https://github.com/qanazoga/soturi/blob/master/cogs/free_game_news_moderator.py) because it was great, and make it better by adding more filtering options. Again it should be made easy to set up via OAuth.
