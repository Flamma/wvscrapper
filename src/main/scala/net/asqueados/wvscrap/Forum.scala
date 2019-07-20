package net.asqueados.wvscrap

import io.circe.Encoder
import io.circe.generic.semiauto._


/**
 * Forum post
 *
 * @param username: the user who created the post
 * @param content: what was posted
 */
case class Post(username: String, content: String)

object Post {
    implicit val encoder: Encoder[Post] = deriveEncoder[Post]
}

/**
  * Forum Thread
  *
  * @param title title of the thread
  * @param posts all posts of the thread
  */
case class Thread(title: String, posts: List[Post])

object Thread {
    implicit val encoder: Encoder[Thread] = deriveEncoder[Thread]
}

/**
  * Subforum
  *
  * @param title title of the subforum
  * @param threads all threads of the subforum
  */
case class Subforum(title: String, threads: List[Thread])

object Subforum {
    implicit val encoder: Encoder[Subforum] = deriveEncoder[Subforum]
}