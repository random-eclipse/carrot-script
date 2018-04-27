package worker.resource

trait A

object A {
  /** publish resource folder class path */
  val path = "/" + classOf[ A ].getPackage.getName.replace( '.', '/' )
}
