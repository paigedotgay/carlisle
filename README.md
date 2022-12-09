# Carlisle

[![support-server]](https://server.carlisle-bot.com)
[![open-issues]](https://repo.carlisle-bot.com/issues)
[![closed-issues]](https://gitlab.com/qanazoga/carlisle/-/issues?scope=all&state=closed)
[![last-update]](https://repo.carlisle-bot.com/commits)

This is something I've been trying to get working for a while, and am finally willing to take another stab at.

A while back I tried making a version of my Discord Bot, [Hallita](https://gitlab.com/soturi/hallita-variants/hallita.js) in Kotlin, and writing commands was extremely easy, and fun! But I had issues making a working Eval command (it eventually halfway worked, but required some weird hacky things and was generally a pain to use), so the project was scrapped and done in JavaScript instead.

I'm learning Clojure right now, and got a bot to work with Clojure and [JDA](https://github.com/DV8FromTheWorld/JDA) some time ago, however, that was just a test to see if I could Eval Clojure inside JDA. I want a full bot.

---

### Planned Features
Ideally, Carlisle should be able to do everything Hallita could, and hopefully some of the things only [Soturi](https://gitlab.com/soturi/soturi-variants/soturi) could (and also some of Hallita's planned things I never finished).
I'm adding all of these things to the [project issues](https://gitlab.com/qanazoga/carlisle-bot/issues) so I have an actual sense of progress as I work through them.

#### Additional Things (Future Projects)
- Hallita had a planned feature that would allow per-guild setup thru Discord's OAuth, that's still a cool idea that I really wanna do.
- ~~Bring back Soturi's [role signup](https://gitlab.com/soturi/soturi-variants/soturi.py/blob/master/cogs/role_signup_listener.py) feature, but with an easy way to set up that doesn't involve me manually adding entries to an unreadable JSON file.~~  
This feature is now fully functional
- Bring back Soturi's [message filtering](https://gitlab.com/soturi/soturi-variants/soturi.py/blob/master/cogs/free_game_news_moderator.py) because it was great, and make it better by adding more filtering options. Again it should be made easy to set up via OAuth.

[support-server]: https://img.shields.io/discord/279319437377536002?color=%235663F7%20&label=Support&logo=discord&style=flat-square
[open-issues]: https://img.shields.io/badge/dynamic/json?style=flat-square&color=yellow&label=open%20issues&query=statistics.counts.opened&url=https%3A%2F%2Fgitlab.com%2Fapi%2Fv4%2Fprojects%2F27978882%2Fissues_statistics
[closed-issues]: https://img.shields.io/badge/dynamic/json?color=success&label=closed%20issues&query=statistics.counts.closed&url=https%3A%2F%2Fgitlab.com%2Fapi%2Fv4%2Fprojects%2F27978882%2Fissues_statistics&style=flat-square
[last-update]: https://img.shields.io/badge/dynamic/json?style=flat-square&color=blue&label=Last%20Update&query=last_activity_at&url=https%3A%2F%2Fgitlab.com%2Fapi%2Fv4%2Fprojects%2F27978882
