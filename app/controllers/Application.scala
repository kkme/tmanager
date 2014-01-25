package controllers

import play.api._
import play.api.mvc._
import models.dbmanager._
import scala.slick.driver.MySQLDriver.simple._
import play.api.data.Forms._
import play.api.data._
import play.api.cache.Cache
import play.api.Play.current


object Application extends Controller with Secured {

  val memberLoginForm = Form(
    mapping(
      "id" -> nonEmptyText,
      "password" -> nonEmptyText
    )(MemberLogin.apply)(MemberLogin.unapply)
      verifying("ID나 Password가 일치하지 않습니다", f => Members.validateUserInformation(f.id, f.password))
  )
  val signUpForm = Form(
    mapping(
      "id" -> nonEmptyText,
      "password" -> nonEmptyText(minLength = 4),
      "password_confirmation" -> nonEmptyText,
      "name" -> nonEmptyText
    )(MemberSignUp.apply)(MemberSignUp.unapply)
      verifying("패스워드가 일치하지 않습니다", f => f.password == f.passwordConfirmation)
      verifying("이미 존재하는 ID입니다", f => !Members.exists(f.id))
  )

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def login = Action {
    implicit request =>
      Ok(views.html.login(memberLoginForm))
  }

  def signUp = Action {
    implicit request =>
      Ok(views.html.signup(signUpForm))
  }

  def signUpProcess = Action {
    implicit request =>
      signUpForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.signup(formWithErrors))
        , userData => Members.add(userData) match {
          case 1 => Redirect(routes.Application.login)
          case _ => InternalServerError
        }
      )
  }

  def authenticate = Action {
    implicit request =>
      memberLoginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.login(formWithErrors)),
        userData =>
          Members.findById(userData.id) match {
            case Some(user) =>
              val hashedId = Members.getHashedPassword(user.id)
              Cache.set(hashedId, user)
              Redirect(routes.Application.appMain("")).withSession("user" -> hashedId)
            case None => Redirect(routes.Application.login).withNewSession
          }
      )
  }

  def appMain(url: String) = IsAuthenticated { (user,member) =>
    Ok("")
  }

}

/**
 * Provide security features
 */
trait Secured {

  /**
   * Retrieve the connected user email.
   */
  private def userKey(request: RequestHeader) = request.session.get("user")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)

  // --

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => (Member, Request[AnyContent]) => Result) = Security.Authenticated(userKey, onUnauthorized) {
    userKey =>
      Cache.getAs[Member](userKey) match {
        case Some(user) => Action(request => f(user,request))
        case None =>
          Action {
            request =>
              if (Logger.isDebugEnabled) {
                Logger.debug("Anonymous user trying to access : '%s'".format(request.uri))
              }
              onUnauthorized(request).withNewSession
          }
      }
  }

  //  /**
  //   * Check if the connected user is a owner of this task.
  //   */
  //  def IsOwnerOf(task: Long)(f: => String => Request[AnyContent] => Result) = IsAuthenticated { user => request =>
  //    if(Task.isOwner(task, user)) {
  //      f(user)(request)
  //    } else {
  //      Results.Forbidden
  //    }
  //  }

}