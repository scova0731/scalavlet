package org.scalavlet.examples.simple

import scala.xml.{Node, Text}
import org.scalavlet.{NotFound, Request, Scalavlet}

object Template {

  def page(title:String,
           content:Seq[Node],
           url: String => String = identity _) = {
    <html lang="en">
      <head>
        <title>{ title }</title>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <meta name="description" content="" />
        <meta name="author" content="" />

        <link href={url("/css/bootstrap.css")} rel="stylesheet" />

      </head>

      <body>
        <div class="container">
          <div class="content">
            <div class="page-header">
              <h1>{ title }</h1>
            </div>
            {content}
            <hr/>
            <a href={url("/ping")}>Ping Pong</a><br/>
            <a href={url("/date/2009/12/26")}>date example</a><br/>
            <a href={url("/form")}>form example</a><br/>
            <!--<a href={url("/upload")}>upload</a><br/>-->
            <a href={url("/")}>hello world</a><br/>
            <!--<a href={url("/flash-map/form")}>flash scope</a><br/>-->
            <!--<a href={url("/login")}>login</a><br/>-->
            <!--<a href={url("/logout")}>logout</a><br/>-->
            <!--<a href={url("/basic-auth")}>basic auth</a><br/>-->
            <a href={url("/filter-example")}>filter example</a><br/>
            <!--<a href={url("/cookies-example")}>cookies example</a><br/>-->
            <!--<a href={url("/atmosphere")}>atmosphere chat demo</a><br/>-->
          </div> <!-- /content -->
        </div> <!-- /container -->

      </body>

    </html>
  }
}

object PageServlet extends Scalavlet {

  private def displayPage(request:Request, title:String, content:Seq[Node]) =
    Template.page(title, content, url(request, _, includeServletPath = false))

  get("/") { q =>
    displayPage(q, "Scalavlet: Hello World",
      <h2>Hello world!</h2>
        <p>Referer: { (q referrer) map { Text(_) } getOrElse { <i>none</i> }}</p>
        <pre>Route: /</pre>)
  }


  get("/ping") { request =>
    "pong"
  }

  get("/not-found") { request =>
    NotFound("Oops")
  }

  get("/date/:year/:month/:day") { request =>
    displayPage(request, "Scalavlet: Date Example",
      <ul>
        <li>Year: {request.params("year")}</li>
        <li>Month: {request.params("month")}</li>
        <li>Day: {request.params("day")}</li>
      </ul>
        <pre>Route: /date/:year/:month/:day</pre>
    )
  }

  get("/form") { request =>
    displayPage(request, "Scalavlet: Form Post Example",
      <form action={url(request, "/form-post")} method='POST'>
        Post something: <input name="submission" type='text'/>
        <input type='submit'/>
      </form>
        <pre>Route: /form</pre>
    )
  }

  post("/form-post") { request =>
    displayPage(request, "Scalavlet: Form Post Result",
      <p>You posted: {request.params("submission")}</p>
        <pre>Route: /post</pre>
    )
  }

//  get("/login") {
//    (session.get("first"), session.get("last")) match {
//      case (Some(first:String), Some(last:String)) =>
//        displayPage("Scalavlet: Session Example",
//          <pre>You have logged in as: {first + "-" + last}</pre>
//            <pre>Route: /login</pre>)
//      case x =>
//        displayPage("Scalavlet: Session Example" + x.toString,
//          <form action={url("/login")} method='POST'>
//            First Name: <input name="first" type='text'/>
//            Last Name: <input name="last" type='text'/>
//            <input type='submit'/>
//          </form>
//            <pre>Route: /login</pre>)
//    }
//  }

//  post("/login") {
//    (params("first"), params("last")) match {
//      case (first:String, last:String) => {
//        session("first") = first
//        session("last") = last
//        displayPage("Scalavlet: Session Example",
//          <pre>You have just logged in as: {first + " " + last}</pre>
//            <pre>Route: /login</pre>)
//      }
//    }
//  }

//  get("/logout") {
//    session.invalidate
//    displayPage("Scalavlet: Session Example",
//      <pre>You have logged out</pre>
//        <pre>Route: /logout</pre>)
//  }


  get("/flash-map/form") { request =>
    displayPage(request, "Scalavlet: Flash Map Example",
      <span>Supports the post-then-redirect pattern</span><br />
        <form method="post">
          <label>Message: <input type="text" name="message" /></label><br />
          <input type="submit" />
        </form>)
  }

//  post("/flash-map/form") {
//    flash("message") = params.getOrElse("message", "")
//    redirect("/flash-map/result")
//  }

//  get("/flash-map/result") {
//    displayPage(
//      title = "Scalavlet: Flash  Example",
//      content = <span>Message = {flash.getOrElse("message", "")}</span>
//    )
//  }

//  post("/echo") {
//    params("echo").urlDecode
//  }

}
