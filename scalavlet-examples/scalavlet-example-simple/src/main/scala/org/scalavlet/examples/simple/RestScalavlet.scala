package org.scalavlet.examples.simple

import org.scalavlet.{Request, Ok, Scalavlet}
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.collection.mutable

case class User(id:Int, name:String, email:String)
case class MicroPost(id:Int, content:String, userId:Int)
case class MicroComment(id:Int, comment:String, postId:Int)

case class NewUser(name:String, email:String) {
  def toUser(newId:Int) = User(newId, name, email)
}

case class RichPost(id:Int, content:String, comments:List[MicroComment])


object RestScalavlet extends Scalavlet with LazyLogging {

  val users = new mutable.HashMap[Int, User]
    with mutable.SynchronizedMap[Int, User]

  val posts = new mutable.HashMap[Int, MicroPost]
    with mutable.SynchronizedMap[Int, MicroPost]

  users +=
    (1 -> User(1, "Scalavlet Taro", "taro@scalavlet.org")) +=
    (2 -> User(2, "John Scalavlet", "john@scalavlet.org"))


  /**
   * Return the user list
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
   * Update the user
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
}
