package fr.spironet.geoserver.check

import groovy.time.TimeCategory
import groovy.xml.XmlSlurper
import org.apache.commons.io.IOUtils
import org.geotools.http.SimpleHttpClient
import org.geotools.http.commons.MultithreadedHttpClient
import org.geotools.ows.wms.Layer
import org.geotools.ows.wms.WMSUtils
import org.geotools.ows.wms.WebMapServer
import org.geotools.ows.wms.response.GetMapResponse

import javax.imageio.ImageIO
import java.nio.charset.Charset

class EntryPoint {

    static def getXmlError(def str) {
        try {
            def slurped = new XmlSlurper(false, false).parseText(str)
            println slurped.ServiceException.text().strip()
        } catch (Exception e) {
            return "Unknown error"
        }
    }

    static def getMap(WebMapServer wms, Layer layer) throws Exception {
        def request = wms.createGetMapRequest()
        request.setFormat("image/png")
        request.setDimensions("320", "200")
        request.setTransparent(true)
        def srs = layer.getSrs().first()
        request.setSRS(srs)
        request.setBBox(layer.getBoundingBoxes()[srs])
        request.addLayer(layer)

        GetMapResponse response = (GetMapResponse) wms.issueRequest(request)
        if (response.getContentType().startsWith("text/xml")) {
            def xmlMesg = IOUtils.toString(response.getInputStream(), Charset.defaultCharset())
            def errMesg = getXmlError(xmlMesg)
            throw new RuntimeException("content-type: png expected, application/xml received: ${errMesg}")
        }
        return IOUtils.toByteArray(response.getInputStream())
    }

    static void main(String[] args) {
        if (args.size() == 0) {
            println "Usage: java -jar ... <wms url to test>"
            System.exit(1)
        }

        // Weirdly, when using this http client, I got very bad performances ... ?!
        //def httpClient = new MultithreadedHttpClient()
        //httpClient.setReadTimeout(10)
        //httpClient.setConnectTimeout(10)

        // This one is much faster
        def httpClient = new SimpleHttpClient()

        def tsStart = new Date()

        def wms = new WebMapServer(
                new URL(args[0])
                , httpClient
        )

        def getCap = wms.getCapabilities()

        def layers = WMSUtils.getNamedLayers(getCap)
        def tcs = []
        def nbtests = 0
        def nberrors = 0
        layers.each {
            def name = it.getName()
            def error = null
            def tc = [
                    classname: name,
                    name: name
            ]
            def tcStart = new Date()
            nbtests++
            try {
                println "${it.getName()}: GetMap()"
                byte[] img = getMap(wms, it)
                new File(it.getName() + ".png").bytes = img
                def parsed = ImageIO.read(new ByteArrayInputStream(img))
                if (parsed == null) {
                    throw new RuntimeException("Unable to read received PNG file")
                }
            } catch (Exception e) {
                println "Error with ${name}"
                tc.error = [type: "Error", message: e.getMessage()]
                nberrors++
            } finally {
                tc.time = TimeCategory.minus(new Date(), tcStart).toMilliseconds() / 1000
                tcs << tc
            }
        }

        def totalDuration = TimeCategory.minus(new Date(), tsStart).toMilliseconds() / 1000

        def junitXmlReport = JunitXmlReportGenerator.generate(
                [
                        id: "GetMap",
                       name: "WMS GetMap testsuite",
                       tests: nbtests,
                       errors: nberrors,
                       time: totalDuration
                ],
                tcs
        )
        println junitXmlReport
        new File("junit.xml").write(junitXmlReport)
        System.exit(0)
    }
}
