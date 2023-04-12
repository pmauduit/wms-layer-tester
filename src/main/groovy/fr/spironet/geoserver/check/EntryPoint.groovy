package fr.spironet.geoserver.check

import org.apache.commons.io.IOUtils
import org.geotools.http.SimpleHttpClient
import org.geotools.ows.wms.Layer
import org.geotools.ows.wms.WMSUtils
import org.geotools.ows.wms.WebMapServer
import org.geotools.ows.wms.response.GetMapResponse

class EntryPoint {

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
        if (response.getContentType().startsWith("text/xml"))
            throw new RuntimeException("content-type: png expected, application/xml received")
        return IOUtils.toByteArray(response.getInputStream())
    }

    static void main(String[] args) {
        /*
        def httpClient = new MultithreadedHttpClient()
        httpClient.setReadTimeout(10)
        httpClient.setConnectTimeout(10)
        */
        def httpClient = new SimpleHttpClient()
        if (args.size() == 0) {
            println "Usage: java -jar ... <wms url to test>"
            System.exit(1)
        }

        def wms = new WebMapServer(
                new URL(args[0])
                , httpClient
        )
        def getCap = wms.getCapabilities()

        def layers = WMSUtils.getNamedLayers(getCap)
        layers.each {
            try {
                println "${it.getName()}: GetMap()"
                byte[] img = getMap(wms, it)

                new File(it.getName() + ".png").bytes = img

            } catch (Exception e) {
                println "Error with ${it.getName()}"
            }
        }

        return
    }
}
