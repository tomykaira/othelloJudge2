import play.api._

import models._

object Global extends GlobalSettings {
  
  override def onStart(app: Application) {
    BattleWorker.start
  }
  
}
