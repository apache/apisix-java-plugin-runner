package plugins_test

import (
	"github.com/gavv/httpexpect/v2"
	"github.com/onsi/ginkgo"
	"github.com/onsi/ginkgo/extensions/table"
	"net/http"

	"github.com/apache/apisix-java-plugin-runner/tests/e2e/tools"
)

var _ = ginkgo.Describe("Stop", func() {
	table.DescribeTable("test route create and update",
		func(tc tools.HttpTestCase) {
			tools.RunTestCase(tc)
		},
		table.Entry("create java runner stop plugin route success", tools.HttpTestCase{
			Object: tools.GetA6Expect(),
			Method: http.MethodPut,
			Path:   "/apisix/admin/routes/1",
			Body: `{
				"uri":"/test/java/runner/stop",
				"plugins":{
					"ext-plugin-pre-req":{
						"conf":[
							{
								"name":"StopRequestDemoFilter",
								"value":"{\"stop_response_code\":201,\"stop_response_header_name\":\"X-Resp-A6-Runner\",\"stop_response_header_value\":\"Java\",\"stop_response_body\":\"Hello, APISIX Java Plugin Runner\"}"
							}
						]
					}
				},
				"upstream":{
					"nodes":{
						"web:8888":1
					},
					"type":"roundrobin"
				}
			}`,
			Headers:      map[string]string{"X-API-KEY": tools.GetAdminToken()},
			ExpectStatusRange: httpexpect.Status2xx,
		}),
		table.Entry("test java runner stop plugin route success", tools.HttpTestCase{
			Object:       tools.GetA6Expect(),
			Method:       http.MethodGet,
			Path:         "/test/java/runner/stop",
			ExpectStatus: http.StatusCreated,
			ExpectHeaders: map[string]string{
				"X-Resp-A6-Runner": "Java",
			},
			ExpectBody: "Hello, APISIX Java Plugin Runner",
		}),
	)
})
