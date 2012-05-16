package com.github.jrwest.scalamachine.core

import java.util.Date

/**
 * Resources represent HTTP resources in an application's API.
 *
 * Applications should subclass this trait and implement the functions necessary for
 * its resources' logic.
 */
trait Resource {
  import Resource._

  //def init: C

  /**
   * @note default - `true`
   * @return true if the service(s) necessary to service this resource are available, false otherwise
   */
  def serviceAvailable(data: ReqRespData): (Res[Boolean],ReqRespData) = (default(true), data)

  /**
   * @note default - `List(OPTIONS, TRACE, CONNECT, HEAD, GET, POST, PUT, DELETE)`
   * @return list of [[com.github.jrwest.scalamachine.core.HTTPMethod]]s known by this resource
   */
  def knownMethods(data: ReqRespData): (Res[List[HTTPMethod]],ReqRespData)= (
    default(List(OPTIONS, TRACE, CONNECT, HEAD, GET, POST, PUT, DELETE)),
    data
  )

  /**
   * @note default - `false`
   * @return true if the request URI is too long, otherwise false
   */
  def uriTooLong(data: ReqRespData): (Res[Boolean],ReqRespData) = (default(false), data)

  /**
   * @note default - `List(GET)`
   * @return list of [[com.github.jrwest.scalamachine.core.HTTPMethod]]s allowed by this resource
   */
  def allowedMethods(data: ReqRespData): (Res[List[HTTPMethod]],ReqRespData) = (default(GET :: Nil), data)

  /**
   * @note default - `false`
   * @return true if the request is malformed, false otherwise
   */
  def isMalformed(data: ReqRespData): (Res[Boolean],ReqRespData) = (default(false), data)

  /**
   * @note default - `AuthSuccess`
   * @return [[com.github.jrwest.scalamachine.core.AuthSuccess]] if request is authorized
   *        [[com.github.jrwest.scalamachine.core.AuthFailure]] otherwise
   */
  def isAuthorized(data: ReqRespData): (Res[AuthResult],ReqRespData) = (default(AuthSuccess), data)

  /**
   * @note default - `false`
   * @return true if request is forbidden, false otherwise
   */
  def isForbidden(data: ReqRespData): (Res[Boolean],ReqRespData) = (default(false), data)

  /**
   * @note default - `true`
   * @return true if `Content-*` headers are valid, false otherwise
   */
  def contentHeadersValid(data: ReqRespData): (Res[Boolean],ReqRespData) = (default(true), data)

  /**
   * @note default - `true`
   * @return true if content type is known, false otherwise
   */
  def isKnownContentType(data: ReqRespData): (Res[Boolean],ReqRespData) = (default(true), data)

  /**
   * @note default - `true`
   * @return true if request body length is valid, false otherwise
   */
  def isValidEntityLength(data: ReqRespData): (Res[Boolean],ReqRespData) = (default(true), data)

  /**
   * @note default - no additional headers
   * @return additional headers to include in the reponse to an `OPTIONS` request
   */
  def options(data: ReqRespData): (Res[Map[HTTPHeader, String]],ReqRespData) = (default(Map()), data)

  /**
   * This function determines the body of GET/HEAD requests. If your resource responds to them, make sure to implement it
   * because the default most likely will not do. It should return a list of 2-tuples containing the content type and its
   * associated body rendering function. The body rendering function has signature
   * `ReqRespData => (Res[Array[Byte],ReqRespData)`
   *
   * @note default - by default resources provice the `text/html` content type, rendering a simple HTML response
   * @return list of 2-tuples from the [[com.github.jrwest.scalamachine.core.ContentType]] to the rendering function.
   * @see [[com.github.jrwest.scalamachine.core.Resource.ContentTypesProvided]]
   */
  def contentTypesProvided(data: ReqRespData): (Res[ContentTypesProvided],ReqRespData) = {
    (default((ContentType("text/html") -> defaultResponse) :: Nil), data)
  }

  /**
   * @note default - `true`
   * @return true if the accepted language is available
   * @todo change to real content negotiation like ruby port
   */
  def isLanguageAvailable(data: ReqRespData): (Res[Boolean],ReqRespData) = (default(true), data)

  /**
   * this functions determines the charset of the response body and influences whether or not the request
   * can be serviced by this resource based on the `Accept-Charset`. If the charset is acceptable,
   * the charsetting function will be run. See information about the return value for me.
   *
   * The charsetting function is of type `Array[Byte] => Array[Byte]`
   *
   * @note default - `None`
   * @return An optional list of provided charsets. If `None``, charset short-circuiting is used and no charsetting is performed.
   *         However, if `Some` containing a list of 2-tuples of charsets and charsetting functions, the chosen charsetting function
   *         will be applied the response body.
   */
  def charsetsProvided(data: ReqRespData): (Res[CharsetsProvided],ReqRespData) = (default(None), data)

  /**
   * @note default - supports the "identity" encoding
   * @return similar to [[com.github.jrwest.scalamachine.core.Resource.charsetsProvided]] except for response encoding
   */
  def encodingsProvided(data: ReqRespData): (Res[CharsetsProvided],ReqRespData) = (default(Some(("identity", identity[Array[Byte]](_)) :: Nil)), data)

  /**
   * for most resources that access a datasource this is where that access should most likely be performed and
   * placed into a variable in the resource (or the resource's context -- coming soon)
   * @note default - `true`
   * @return true if the resource exists, false otherwise
   */
  def resourceExists(data: ReqRespData): (Res[Boolean],ReqRespData) = (default(true), data)

  /**
   * @note default - `Nil`
   * @return headers to be included in the `Vary` response header.
   *         `Accept`, `Accept-Encoding`, `Accept-Charset` will always be included and do not need ot be in the returned list
   */
  def variances(data: ReqRespData): (Res[Seq[String]], ReqRespData) = (default(Nil), data)

  /**
   * @note default - `None`
   * @return the _etag_, if any, for the resource to be included in the response or to determine if the cached response is fresh
   */
  def generateEtag(data: ReqRespData): (Res[Option[String]], ReqRespData) = (default(None), data)

  /**
   * @note default - `None`
   * @return the last modified date of the resource being requested, if any, included in response or to determine if
   *         cached response is fresh
   */
  def lastModified(data: ReqRespData): (Res[Option[Date]], ReqRespData) = (default(None), data)

  /**
   * @note default - `None`
   * @return None if the resource has not been moved, Some, contain the URI of its new location otherwise
   */
  def movedPermanently(data: ReqRespData): (Res[Option[String]], ReqRespData) = (default(None), data)

  /**
   * @note default - `false`
   * @return true if the resource existed before, although it does not now, false otherwise
   */
  def previouslyExisted(data: ReqRespData): (Res[Boolean], ReqRespData) = (default(false), data)

  /**
   * @note default - `None`
   * @return None if the resource is not moved temporarily, Some containing its temporary URI otherwise
   */
  def movedTemporarily(data: ReqRespData): (Res[Option[String]], ReqRespData) = (default(None), data)

  /**
   * @note default - `false`
   * @return true if this resource allows `POST` requests to be serviced when the resource does not exist,
   *         false otherwise
   */
  def allowMissingPost(data: ReqRespData): (Res[Boolean], ReqRespData) = (default(false), data)

  /**
   * Perform the deletion of the resource during a `DELETE` request
   * @note default - `false`
   * @return true if the deletion succeed (does not necessarily have to complete), false otherwise
   */
  def deleteResource(data: ReqRespData): (Res[Boolean], ReqRespData) = (default(false), data)

  /**
   * @note default - `true`
   * @return true if the deletion is complete at the time this function is called, false otherwise
   */
  def deleteCompleted(data: ReqRespData): (Res[Boolean], ReqRespData) = (default(true), data)

  /**
   * Determines whether or not `createPath` or `processPost` is called during a `POST` request
   * @note default - `false`
   * @return true will cause the resource to follow a path that calls `createPath`, false will
   *         result in the flow calling `processPost`
   */
  def postIsCreate(data: ReqRespData): (Res[Boolean], ReqRespData) = (default(false), data)

  /**
   * Used for `POST` requests that represent generic processing on a resource, instead of creation
   * @note default - `false`
   * @return true of the processing succeeded, false otherwise
   */
  def processPost(data: ReqRespData): (Res[Boolean], ReqRespData) = (default(false), data)

  /**
   * Use for `POST` requests that represent creation of data. Makes handling somewhat similar to
   * a `PUT` request ultimately
   * @note default - `None`
   * @return if this function is called it must return `Some` containing the created path,
   *         returning None is considered an error
   * @todo need to elaborate more on what happens with the returned path and what
   *       exactly the string should be
   */
  def createPath(data: ReqRespData): (Res[Option[String]], ReqRespData) = (default(None), data)

  /**
   * Used during `PUT` requests and `POST` requests if `postIsCreate` returns true. Returns a list
   * of accepted `Content-Type`s and their associated handler functions. The handler functions
   * have the signature `ReqRespData` => `(Res[Boolean],ReqRespData)`. If the handler returns true
   * it is considered successful, otherwise it is considered to have failed and to be an error.
   *
   * If the `Content-Type` of the request is not acceptable a response with code 415 is returned
   *
   * @note default - `Nil`
   * @return a list of 2-tuples containing the accepted content types and their associated handlers
   * @see [[com.github.jrwest.scalamachine.core.Resource.ContentTypesAccepted]]
   */
  def contentTypesAccepted(data: ReqRespData): (Res[ContentTypesAccepted], ReqRespData) = (default(Nil), data)

  /**
   * @note default - `false`
   * @return true if the `PUT` request cannot be handled due to conflict, false otherwise
   */
  def isConflict(data: ReqRespData): (Res[Boolean], ReqRespData) = (default(false), data)

  /**
   * @note default - `None`
   * @return if `Some` the date will be set as the value of the `Expires` header in the response
   */
  def expires(data: ReqRespData): (Res[Option[Date]], ReqRespData) = (default(None), data)

  def multipleChoices(data: ReqRespData): (Res[Boolean], ReqRespData) = (default(false), data)

  private def default[A](value: A): Res[A] = ValueRes(value)

  private val defaultResponse: ReqRespData => (Res[Array[Byte]], ReqRespData) = (default("hello, scalamachine".getBytes), _)

}

object Resource {
  type ContentTypesProvided = List[(ContentType, ReqRespData => (Res[Array[Byte]],ReqRespData))]
  type ContentTypesAccepted = List[(ContentType, ReqRespData => (Res[Boolean], ReqRespData))]
  type CharsetsProvided = Option[List[(String,Array[Byte] => Array[Byte])]] // None value specifies charset negotiation short-circuiting
  type EncodingsProvided = Option[List[(String,Array[Byte] => Array[Byte])]] // None values specifies encoding negotiation short-circuiting
}

