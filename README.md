Scalavlet
=========

a tiny Scala based web application framework on top of servlet container


```Scala
class SampleServlet extends Scalavlet {
  get("/ping") { request =>
    "pong"
  }
}

class Bootstrap extends ScalavletBootable {
  def onStart (context: Context): Unit = {
    context.mount(new SampleServlet, "/")
  }
}
```

Scalavlet is aiming to be a simple and easy-to-understand web framework on top of
Servlet specification. Already a lot of elegant and sophisticated web frameworks
written by Scala, but sometimes they are too elegant for Scala new comers and daily
usages. That's the motivation why I'm trying to re-invent the wheel.

Scalavlet will stick to a web ONLY framework. You can integrate with any kind of
view frameworks and persistence frameworks. We may provide several integration samples.

This project is highly affected by [Scalatra](http://scalatra.org/) and [Finatra](http://finatra.info/).
I would like to say to thank them so much. `m(_ _)m`



Environment Matrix
=========

| JVM   | Scala         | Servlet     | Status        |
| ----- | -----         | -----       | -----         |
| 1.6   | *             | *           | Not Planned   |
| *     | 2.9.x         | *           | Not Planned   |
| *     | *             | 2.5         | Not Planned   |
| 1.7   | 2.10 (2.10.3) | 3.0 (3.0.1) | Working       |
| 1.7   | 2.11 (2.11.0) | 3.0 (3.0.1) | In the future |
| 1.8   | 2.11 (2.11.0) | 3.0 (3.0.1) | In the future |
| *     | *             | 3.1         | In the future |

Numbers inside parenthesis in Scala and Servlet mean the version used in tests.



Releases
=========

Nothing yet.



Roadmap
=========

## 0.1 Beta

- Basic Sinatra like routing : DONE
- No xml configuration : DONE
- Integrated JSON input and output
- Integrated Typesafe/Config configuration
- Simple sample application
- Refactoring and testing

## Future plans

- Authentication support
- Servlet 3.1 Web Socket/Async. support
- Session and Cookie management
- File uploading
- XSS protection
- I18n/L10n
- Swagger support
- Sample applications (JNDI, Slick, view libraries, Jolokia/hawt.io ...)
- Multi version builds and CI with Cloudbees
- SBT/Gradle builds in addition to Maven
- CSRF/XSRF
- SSL
- CORS



License
=========

Apache License Version 2.0
