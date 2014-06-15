package org.scalavlet.examples.simple

import org.scalavlet.{Request, Ok, Scalavlet}
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.collection.mutable

case class User(id:Int, name:String, email:String)
case class Post(id:Int, content:String, userId:Int, comments:List[Comment])
case class Comment(id:Int, comment:String, postId:Int)

case class NewUser(name:String, email:String) {
  def toUser(newId:Int) = User(newId, name, email)
}

case class RichPost(id:Int, content:String, comments:List[Comment])


object RestScalavlet extends Scalavlet with LazyLogging {

  val users = new mutable.HashMap[Int, User]
    with mutable.SynchronizedMap[Int, User]

  val posts = new mutable.HashMap[Int, Post]
    with mutable.SynchronizedMap[Int, Post]

  users +=
    (1 -> User(1, "Scalavlet Taro", "taro@scalavlet.org")) +=
    (2 -> User(2, "John Scalavlet", "john@scalavlet.org"))

  posts +=
    (101 -> Post(101, "This is my first port.", 1, List(
      Comment(1001, "Congratulations !", 101),
      Comment(1002, "Awesome !", 101)
    ))) +=
    (102 -> Post(102, "This is my second port.", 1, List(
      Comment(1003, "This is what I have waited !", 102),
      Comment(1004, "Good job !", 102)
    ))) +=
    (103 -> Post(103, "This is my third port.", 1, List(
      Comment(1005, "What's the matter with you ?", 103),
      Comment(1006, "Thank you so much", 103)
    )))


  /**
   * Just raise an exception
   */
  get("/raise-exception"){ request =>
    throw new RuntimeException("Oooooops !")
  }


  /**
   * Return the user list
   */
  get("/users"){ request =>
    logger.debug("GET all users is called")
    respond.json(users.values.toSeq.sortBy(_.id), pretty = true)
  }

  
  /**
   * Return the user of userId
   */
  get("/users/:userId"){ request =>
    logger.debug("GET one user is called")
    val user = users.get(request.params.getAsInt("userId").get).orNull
    respond.json(user, pretty = true)
  }


  /**
   * Add a user
   */
  post("/users/new"){ request =>
    logger.debug("POST is called")
    val newUser = request.jsonBody[NewUser]

    val newId:Int = synchronized {
      val id = users.keys.max + 1
      users += (id -> newUser.toUser(id))
      id
    }
    Ok(newId)
  }


  /**
   * Update an user
   */
  put("/users/:userId"){ request =>
    logger.debug("PUT is called")
    val user = request.jsonBody[User]
    users += (user.id -> user)
    Unit
  }

  
  /**
   * Update the user. This is same as "put"
   */
  patch("/users/:userId"){ request =>
    logger.debug("PATCH is called")
    val user = request.jsonBody[User]
    users += (user.id -> user)
    Unit
  }


  /**
   * Delete the user
   */
  delete("/users/:userId"){ request =>
    logger.debug("DELETE is called")
    users -= request.params.getAsInt("userId").get
    Ok
  }


  /**
   * Return the post list
   */
  get("/posts"){ request =>
    logger.debug("GET all posts is called")
    respond.json(posts.values.toSeq.sortBy(_.id * -1), pretty = true)
  }

  /**
   * Update a post
   */
  put("/posts/:postId"){ request =>
    logger.debug("PUT is called")
    try {
      val post = request.jsonBody[Post]
    }

    Unit
  }

}
