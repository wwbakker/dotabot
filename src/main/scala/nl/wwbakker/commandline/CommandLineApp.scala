package nl.wwbakker.commandline

import nl.wwbakker.services.dota.{DotaApiRepo, VisualizationService}
import nl.wwbakker.services.dota.DotaApiRepo.DotaApiRepo
import nl.wwbakker.services.dota.VisualizationService.NamedWinLoss
import sttp.client3.asynchttpclient.zio.{AsyncHttpClientZioBackend, SttpClient}
import zio.console.{Console, putStrLn}
import zio.{ZIO, ZLayer}

object CommandLineApp extends zio.App {

  val wesselId = 21842016
  val numberOfDaysInThePast = 14

  lazy val dependencies: ZLayer[Any, Any, SttpClient with DotaApiRepo with Console] =
    AsyncHttpClientZioBackend.layer() >+> DotaApiRepo.live ++ Console.live


  def printWinLoss: ZIO[DotaApiRepo with SttpClient with Console, Throwable, Unit] = for {
    wessel <- DotaApiRepo.winLoss(wesselId).map(NamedWinLoss.from(_, "Wessel"))
    peers <- DotaApiRepo.peers(wesselId).map(_.map(NamedWinLoss.from))
    text <- VisualizationService.listWinLoss(peers.prepended(wessel))
    _ <- putStrLn("Results last 14 days:")
    _ <- putStrLn(text)
  } yield ()

  override def run(args: List[String]) = {
    printWinLoss
      .provideLayer(dependencies)
      .exitCode
  }
}
