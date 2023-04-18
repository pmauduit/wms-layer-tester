package fr.spironet.geoserver.check

import groovy.xml.MarkupBuilder


class JunitXmlReportGenerator {

    /*
         <testsuite id="wmslayers" name="Checking WMS layers" tests="45" errors="17" time="1.00">
           <testcase classname="WmsLayer" name="layer:name" time="0.98">
             <error type="Error" message="errmsg">errmsg</error>
           </testcase>
           <testcase classname="WmsLayer" name="layer:name" time="0.02" />
         </testsuite>
     */
    /**
     *
     * @param ts a Map describing the testsuite having the following form:
     * ```
     * [ id: "id", name: "name", tests: 12, errors:1, time: 1.00 ]
     * ```
     * @param tc a arrany describing the testcases, having the following form:
     * ```
     * [
     *   [ classname: "Class.A", name: "aaa", time: 0.1, error: <...> ],
     * ]
     * ```
     * the error can either be null if the testcase succeeded, or containing a map having the following form
     * if the test errored:
     * ```
     * [ type: "Error", message: "error message explaining what went wrong" ]
     * ```
     */
    public static def generate(def ts, def tc) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.testsuite(ts) {
            tc.each {
                def tcWithoutError = it.findAll{ k,v -> k != "error" }
                def err = it.error
                if (it.error != null)
                    testcase(tcWithoutError) {
                        error(err, err.message)
                    }
                else
                    testcase(tcWithoutError)
            }
        }
        return writer.toString()
    }

    public static void main(String[] args) {
        def tc = [
                [
                        classname: "Class.A",
                        name: "fluptop",
                        time: 0.0,
                ],
                [
                        classname: "Class.B",
                        name: "flibidi",
                        time: 0.5,
                        error: [type: "error", message: "errormsg"]
                ]
        ]

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.testsuite(id: "wmslayer", name: "Checking WMS layers",
                tests: 45, errors: 17, time: 1.00) {
            tc.each {
                    def tcWithoutError = it.findAll{ k,v -> k != "error" }
                    def err = it.error
                    if (it.error != null)
                         testcase(tcWithoutError) {
                             error(err, err.message)
                         }
                    else
                        testcase(tcWithoutError)
            }
        }

    }

}
