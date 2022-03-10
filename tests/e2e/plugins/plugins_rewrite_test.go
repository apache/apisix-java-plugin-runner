package plugins_test

import (
	"github.com/apache/apisix-java-plugin-runner/tests/e2e/tools"
	"github.com/gavv/httpexpect/v2"
	"github.com/onsi/ginkgo"
	"github.com/onsi/ginkgo/extensions/table"
	"net/http"
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
				"uri":"/test/java/runner/rewrite",
				"plugins":{
					"ext-plugin-pre-req":{
						"conf":[
							{
								"name":"RewriteRequestDemoFilter",
								"value":"{\"rewrite_path\":\"/hello/java/runner\",\"conf_header_name\":\"X-APISIX-Plugin-Runner\",\"conf_header_value\":\"Java\",\"conf_arg_name\":\"runner\",\"conf_arg_value\":\"java\"}"
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
			Headers:           map[string]string{"X-API-KEY": tools.GetAdminToken()},
			ExpectStatusRange: httpexpect.Status2xx,
		}),
		table.Entry("test java runner stop plugin route success", tools.HttpTestCase{
			Object:       tools.GetA6Expect(),
			Method:       http.MethodGet,
			Path:         "/test/java/runner/rewrite",
			ExpectBody:   []string{"/hello/java/runner", "Java"},
			ExpectStatus: http.StatusOK,
		}),
	)
})
