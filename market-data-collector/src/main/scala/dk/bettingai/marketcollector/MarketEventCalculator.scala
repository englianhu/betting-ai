package dk.bettingai.marketcollector

import dk.bettingai.marketsimulator.betex.api.IMarket._
import dk.bettingai.marketsimulator.betex.Market._

/**This trait represents a function that calculates market events for the delta between previous and current state of the market runner.
 * 
 * @author KorzekwaD
 *
 */
object MarketEventCalculator  extends IMarketEventCalculator{

	/**Calculates market events for the delta between previous and current state of the market runner.
	 * 
	 * @param userId The user Id that the bet placement events are calculated for.
	 * @param marketId The market id that the bet placement events are calculated for. 
	 * @param runnerId The market runner id that the bet placement events are calculated for. 
	 * @param newMarketData Element 1 - marketPrices for the market runner, Element 2 - total traded volume for the market runner
	 * @param previousMarketData Element 1 - marketPrices for the market runner, Element 2 - total traded volume for the market runner
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for the market runner
	 */
	def calculate(userId:Long,marketId:Long,runnerId:Long)(newMarketData:Tuple2[List[IRunnerPrice],List[IPriceTradedVolume]],previousMarketData:Tuple2[List[IRunnerPrice],List[IPriceTradedVolume]]): List[String] = {

		/**Get delta between new and previous market prices.*/
		val deltaForUpdatedAndNewPrices = for {
			newRunnerPrice <- newMarketData._1
			val previousRunnerPrice = previousMarketData._1.find(_.price==newRunnerPrice.price).getOrElse(new RunnerPrice(newRunnerPrice.price,0,0))
			val runnerPriceDelta = new RunnerPrice(newRunnerPrice.price,newRunnerPrice.totalToBack-previousRunnerPrice.totalToBack,newRunnerPrice.totalToLay-previousRunnerPrice.totalToLay)
			if(runnerPriceDelta.totalToBack != 0 || runnerPriceDelta.totalToLay !=0)
		} yield runnerPriceDelta

		/**Get all runner prices that are in a previous market data and are not in a new market data.*/
		val deltaForRemovedPrices = previousMarketData._1.filter(prevPrice => !newMarketData._1.exists(newPrice => newPrice.price==prevPrice.price))

		val deltaForAllMarketPrices = deltaForUpdatedAndNewPrices ::: deltaForRemovedPrices.map(runnerPrice => new RunnerPrice(runnerPrice.price,-runnerPrice.totalToBack,-runnerPrice.totalToLay))

		/**Create bet placement events for delta between new and previous market prices.*/
		val placeLayBetEvents:List[String] = for {
			deltaMarketPrice <- deltaForAllMarketPrices 
			
			if(deltaMarketPrice.totalToBack > 0) 
					val placeLayBetEvent = """{"eventType":"PLACE_BET","userId":%s,"betId":%s,"betSize":%s,"betPrice":%s,"betType":"%s","marketId":%s,"runnerId":%s}""".format(123,100,deltaMarketPrice.totalToBack,deltaMarketPrice.price,"LAY",marketId,runnerId)
			
		} yield placeLayBetEvent
		val placeBackBetEvents:List[String] = for {
			deltaMarketPrice <- deltaForAllMarketPrices 
			
			if(deltaMarketPrice.totalToLay > 0) 
						val placeBackBetEvent = """{"eventType":"PLACE_BET","userId":%s,"betId":%s,"betSize":%s,"betPrice":%s,"betType":"%s","marketId":%s,"runnerId":%s}""".format(123,100,deltaMarketPrice.totalToLay,deltaMarketPrice.price,"BACK",marketId,runnerId)
			
			
		} yield placeBackBetEvent
		
		placeLayBetEvents ::: placeBackBetEvents
	}
}