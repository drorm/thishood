package com.thishood.metric

import com.thishood.domain.CommunityMetricType
import grails.plugins.springsecurity.Secured
import java.awt.Color
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtilities
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.data.category.DefaultCategoryDataset
import com.thishood.domain.CommunityMetric
import javax.servlet.http.HttpServletResponse
import com.thishood.ThisHoodConstant

@Secured(["isAuthenticated()"])
class CommunityMetricController {
	def communityMetricService

	def popularityIndex = {
		request.setAttribute(ThisHoodConstant.ATTRIBUTE_ENABLE_CACHING, true)
		cache store: true, shared: true, validUntil: new Date() + 1, auth: false

		Long communityId = params.id as Long
		//pseudo just to have new url on every day
		String date = params.date

		def metrics = communityMetricService.findLast(communityId, CommunityMetricType.POPULARITY)


		withCacheHeaders {
			lastModified {
				Date d = new Date()
				d.clearTime()
				d
			}
			etag {
				"${communityId.encodeAsMD5()}"
			}
			generate {
				byte[] imageBytes = renderChart(metrics, response)

				response.contentType = "image/png"
				response.contentLength = imageBytes.length
				response.outputStream << imageBytes
			}
		}
	}

	def members = {
		request.setAttribute(ThisHoodConstant.ATTRIBUTE_ENABLE_CACHING, true)
		cache store: true, shared: true, validUntil: new Date() + 1, auth: false

		Long communityId = params.id as Long
		//pseudo just to have new url on every day
		String date = params.date

		def metrics = communityMetricService.findLast(communityId, CommunityMetricType.MEMBERS)


		withCacheHeaders {
			lastModified {
				Date d = new Date()
				d.clearTime()
				d
			}
			etag {
				"${communityId.encodeAsMD5()}"
			}
			generate {
				byte[] imageBytes = renderChart(metrics, response)

				response.contentType = "image/png"
				response.contentLength = imageBytes.length
				response.outputStream << imageBytes
			}
		}
	}

	private byte[] renderChart(List<CommunityMetric> metrics, HttpServletResponse response) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset()
		//reversing to draw chart in more userfriendly way - in left side more older values than in right side
		metrics.reverse().each {dataset.setValue(it.value, "", it.dateCreated)}
		JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset, PlotOrientation.VERTICAL, false, false, false)
		chart.borderVisible = false
		chart.antiAlias = true
		//chart.setBackgroundPaint(Color.yellow)
		//chart.getTitle().setPaint(Color.blue)
		CategoryPlot plot = chart.categoryPlot
		//p.setRangeGridlinePaint(Color.red)

		plot.rangeGridlinesVisible = false
		plot.backgroundPaint = Color.white
		plot.rangeGridlinePaint = Color.black
		plot.outlineVisible = false

		BarRenderer renderer = (BarRenderer) plot.renderer
		renderer.setSeriesPaint(0, Color.GRAY)
		renderer.drawBarOutline = false
		renderer.shadowVisible = false
		StandardBarPainter painter = new StandardBarPainter()
		renderer.barPainter = painter

		CategoryAxis domainAxis = plot.domainAxis
		domainAxis.visible = false
		domainAxis.categoryMargin = 0.05

		NumberAxis rangeAxis = (NumberAxis) plot.rangeAxis
		rangeAxis.visible = false

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ChartUtilities.writeChartAsPNG(baos, chart, 170, 70)

		baos.toByteArray()
	}
}
