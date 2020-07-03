MyAgent
=======

A minimal Unet agent project template.

To get started:
* Edit the project name in `settings.gradle`
* Add your code in `src/main/groovy/MyAgent.groovy` and `src/main/groovy/MyAgentParam.groovy` (and rename the files)
* Add your test code in `src/test/groovy/MyAgentTest.groovy` (and rename the file)
* Build using `gradle`
* You'll find the compiled jar file in `build/libs/`
* Test with `gradle test`
* Copy the jar file to the `jars` folder on a UnetStack modem or simulator
* You should now be able to load your agent in UnetStack using `container.add 'myagent', new myagent.MyAgent()`

Related information:
* [UnetStack](https://unetstack.net/)
* [Unet handbook](https://unetstack.net/handbook/unet-handbook_preface.html)
