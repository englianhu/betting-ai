package dk.bettingai.marketsimulator

import org.junit._
import Assert._
import scala.io._
import java.io._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.trader._

class SimulatorIntegrationTest {

	private val betex = new Betex()
	private val marketEventProcessor = new MarketEventProcessorImpl(betex)
	private val trader = new SimpleTrader()
	private val simulator = new Simulator(marketEventProcessor,trader,100,1000,betex)

	@Test def testNoMarketEvents {
		val marketEventsFile = Source.fromFile(new File("src/test/resources/marketDataEmpty.csv"))

		/**Run market simulation.*/
		val marketRiskReport = simulator.runSimulation(marketEventsFile,p => {})
		assertEquals(0,marketRiskReport.size)
	}

	@Test def testCreateMarketEventOnly {
		val marketEventsFile = Source.fromFile(new File("src/test/resources/marketDataCreateMarketOnly.csv"))

		/**Run market simulation.*/
		val marketRiskReport = simulator.runSimulation(marketEventsFile,p => {})
		assertEquals(1,marketRiskReport.size)

		assertEquals(10,marketRiskReport(0).marketId)
		assertEquals("Match Odds",marketRiskReport(0).marketName)
		assertEquals("Man Utd vs Arsenal",marketRiskReport(0).eventName)
		assertEquals(0,marketRiskReport(0).expectedProfit,0)
		assertEquals(0,marketRiskReport(0).matchedBetsNumber,0)
		assertEquals(0,marketRiskReport(0).unmatchedBetsNumber,0)
	}

	@Test def testOneMatchedBackBetNegativeExpectedProfit {
		val marketEventsFile = Source.fromFile(new File("src/test/resources/marketDataPlaceLayBet.csv"))

		/**Run market simulation.*/
		val marketRiskReport = simulator.runSimulation(marketEventsFile,p => {})
		assertEquals(1,marketRiskReport.size)

		assertEquals(10,marketRiskReport(0).marketId)
		assertEquals("Match Odds",marketRiskReport(0).marketName)
		assertEquals("Man Utd vs Arsenal",marketRiskReport(0).eventName)
		assertEquals(-0.625,marketRiskReport(0).expectedProfit,0.001)
		assertEquals(1,marketRiskReport(0).matchedBetsNumber,0)
		assertEquals(1,marketRiskReport(0).unmatchedBetsNumber,0)
	}

	@Test def testOneMatchedLayBetPositiveExpectedProfit {
		val marketEventsFile = Source.fromFile(new File("src/test/resources/marketDataPlaceBackBet.csv"))

		/**Run market simulation.*/
		val marketRiskReport = simulator.runSimulation(marketEventsFile,p => {})
		assertEquals(1,marketRiskReport.size)

		assertEquals(10,marketRiskReport(0).marketId)
		assertEquals("Match Odds",marketRiskReport(0).marketName)
		assertEquals("Man Utd vs Arsenal",marketRiskReport(0).eventName)
		assertEquals(0.199,marketRiskReport(0).expectedProfit,0.001)
		assertEquals(1,marketRiskReport(0).matchedBetsNumber,0)
		assertEquals(1,marketRiskReport(0).unmatchedBetsNumber,0)
	}

	@Test def testThreeMatchedBackBetsNegativeExpectedProfit {
		val marketEventsFile = Source.fromFile(new File("src/test/resources/marketDataPlaceAndCancelLayBet.csv"))

		/**Run market simulation.*/
		val marketRiskReport = simulator.runSimulation(marketEventsFile,p => {})
		assertEquals(1,marketRiskReport.size)

		assertEquals(10,marketRiskReport(0).marketId)
		assertEquals("Match Odds",marketRiskReport(0).marketName)
		assertEquals("Man Utd vs Arsenal",marketRiskReport(0).eventName)
		assertEquals(-0.607,marketRiskReport(0).expectedProfit,0.001)
		assertEquals(3,marketRiskReport(0).matchedBetsNumber,0)
		assertEquals(1,marketRiskReport(0).unmatchedBetsNumber,0)
	}

	@Test def testOneMatchedBetAFewMatchedBets {
		val marketEventsFile = Source.fromFile(new File("src/test/resources/marketDataPlaceAFewBets.csv"))

		/**Run market simulation.*/
		val marketRiskReport = simulator.runSimulation(marketEventsFile,p => {})
		assertEquals(1,marketRiskReport.size)

		assertEquals(10,marketRiskReport(0).marketId)
		assertEquals("Match Odds",marketRiskReport(0).marketName)
		assertEquals("Man Utd vs Arsenal",marketRiskReport(0).eventName)
		assertEquals(-1.497,marketRiskReport(0).expectedProfit,0.001)
		assertEquals(12,marketRiskReport(0).matchedBetsNumber,0)
		assertEquals(12,marketRiskReport(0).unmatchedBetsNumber,0)
	}

	@Test def testOneMatchedBetsOnTwoMarkets {
		val marketEventsFile = Source.fromFile(new File("src/test/resources/marketDataPlaceBackBetOnTwoMarkets.csv"))

		/**Run market simulation.*/
		val marketRiskReport = simulator.runSimulation(marketEventsFile,p => {})
		assertEquals(2,marketRiskReport.size)

		assertEquals(20,marketRiskReport(0).marketId)
		assertEquals("Match Odds",marketRiskReport(0).marketName)
		assertEquals("Fulham vs Wigan",marketRiskReport(0).eventName)
		assertEquals(1.8,marketRiskReport(0).expectedProfit,0.001)
		assertEquals(3,marketRiskReport(0).matchedBetsNumber,0)
		assertEquals(3,marketRiskReport(0).unmatchedBetsNumber,0)

		assertEquals(10,marketRiskReport(1).marketId)
		assertEquals("Match Odds",marketRiskReport(1).marketName)
		assertEquals("Man Utd vs Arsenal",marketRiskReport(1).eventName)
		assertEquals(4.183,marketRiskReport(1).expectedProfit,0.001)
		assertEquals(6,marketRiskReport(1).matchedBetsNumber,0)
		assertEquals(6,marketRiskReport(1).unmatchedBetsNumber,0)
	}
}